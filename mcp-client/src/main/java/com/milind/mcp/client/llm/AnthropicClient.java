package com.milind.mcp.client.llm;

import java.util.List;
import java.util.Map;

import com.milind.mcp.client.config.ClientConfig;
import com.milind.mcp.client.http.HttpTransport;
import com.milind.mcp.common.model.McpTool;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The LLM side of this client: calls Claude's Messages API directly over HTTPS (no
 * Anthropic SDK dependency - one endpoint, one request shape, not worth the extra
 * dependency for a sample this size) to resolve a natural-language prompt against the
 * tool catalog discovered from the router, and to turn a tool's result into a final
 * answer.
 *
 * <p>Every request in a tool-use conversation carries the same {@code tools} array -
 * the Messages API is stateless between calls, so the full message history and the
 * tool catalog are resent each turn.
 */
public class AnthropicClient {

    private static final String MESSAGES_PATH = "/v1/messages";
    private static final int MAX_TOKENS = 1024;
    private static final String SYSTEM_PROMPT =
            "You are the natural-language front end for a small set of tools hosted on AWS "
                    + "Lambda behind an MCP router. When a user's request matches one of the "
                    + "available tools, call that tool with arguments matching its input schema "
                    + "exactly. If no tool applies, answer directly without calling a tool.";

    private final ClientConfig config;
    private final HttpTransport httpTransport;
    private final ObjectMapper objectMapper;

    public AnthropicClient(ClientConfig config, HttpTransport httpTransport, ObjectMapper objectMapper) {
        this.config = config;
        this.httpTransport = httpTransport;
        this.objectMapper = objectMapper;
    }

    /**
     * Sends the full conversation so far (a JSON array of {@code {role, content}} messages,
     * as built by {@link AnthropicMessages}) plus the tool catalog, and returns Claude's
     * next turn.
     */
    public AnthropicTurn sendMessages(ArrayNode messages, List<McpTool> tools) {
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", config.getModel());
        requestBody.put("max_tokens", MAX_TOKENS);
        requestBody.put("system", SYSTEM_PROMPT);
        requestBody.set("messages", messages);
        requestBody.set("tools", AnthropicMessages.toolsArray(tools, objectMapper));

        Map<String, String> headers = Map.of(
                "x-api-key", config.getAnthropicApiKey(),
                "anthropic-version", ClientConfig.ANTHROPIC_VERSION
        );

        String responseBody = httpTransport.postJson(
                config.getAnthropicBaseUrl() + MESSAGES_PATH,
                requestBody.toString(),
                headers);

        return parseTurn(responseBody);
    }

    private AnthropicTurn parseTurn(String responseBody) {
        JsonNode root;
        try {
            root = objectMapper.readTree(responseBody);
        } catch (Exception e) {
            throw new AnthropicClientException("Failed to parse Claude response: " + e.getMessage(), e);
        }

        if (root.has("error")) {
            JsonNode error = root.get("error");
            String message = error.has("message") ? error.get("message").asText() : root.toString();
            throw new AnthropicClientException("Claude API error: " + message);
        }

        JsonNode contentNode = root.get("content");
        ArrayNode content = (contentNode instanceof ArrayNode arrayNode) ? arrayNode : objectMapper.createArrayNode();
        String stopReason = root.has("stop_reason") && !root.get("stop_reason").isNull()
                ? root.get("stop_reason").asText()
                : null;

        return new AnthropicTurn(content, stopReason);
    }
}
