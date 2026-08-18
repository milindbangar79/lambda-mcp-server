package com.milind.mcp.client.llm;

import java.util.List;

import com.milind.mcp.common.model.McpTool;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Builds the small set of JSON shapes the Anthropic Messages API needs, kept as pure,
 * independently testable functions rather than inlined into {@link AnthropicClient}.
 *
 * <p>The tool mapping is a near-direct copy: MCP's {@code McpTool} already carries
 * {@code name}/{@code description}/{@code inputSchema} as a JSON Schema object, and the
 * Messages API's {@code tools[].input_schema} expects exactly that shape - this is the
 * same JSON Schema either way, just under a different field name.
 */
public final class AnthropicMessages {

    private AnthropicMessages() {
    }

    public static ArrayNode toolsArray(List<McpTool> tools, ObjectMapper objectMapper) {
        ArrayNode array = objectMapper.createArrayNode();
        for (McpTool tool : tools) {
            ObjectNode toolNode = objectMapper.createObjectNode();
            toolNode.put("name", tool.getName());
            toolNode.put("description", tool.getDescription());
            toolNode.set("input_schema", objectMapper.valueToTree(tool.getInputSchema()));
            array.add(toolNode);
        }
        return array;
    }

    public static ObjectNode userText(String text, ObjectMapper objectMapper) {
        ObjectNode message = objectMapper.createObjectNode();
        message.put("role", "user");
        message.put("content", text);
        return message;
    }

    public static ObjectNode assistantFromContent(JsonNode content, ObjectMapper objectMapper) {
        ObjectNode message = objectMapper.createObjectNode();
        message.put("role", "assistant");
        message.set("content", content);
        return message;
    }

    /**
     * A {@code user} message carrying a single {@code tool_result} block - how the result of
     * executing a tool is handed back to Claude so it can produce a final answer.
     */
    public static ObjectNode userToolResult(String toolUseId, String resultText, boolean isError, ObjectMapper objectMapper) {
        ObjectNode toolResultBlock = objectMapper.createObjectNode();
        toolResultBlock.put("type", "tool_result");
        toolResultBlock.put("tool_use_id", toolUseId);
        toolResultBlock.put("content", resultText);
        toolResultBlock.put("is_error", isError);

        ArrayNode content = objectMapper.createArrayNode();
        content.add(toolResultBlock);

        ObjectNode message = objectMapper.createObjectNode();
        message.put("role", "user");
        message.set("content", content);
        return message;
    }
}
