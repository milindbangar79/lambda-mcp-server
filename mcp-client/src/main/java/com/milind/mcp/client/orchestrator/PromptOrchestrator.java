package com.milind.mcp.client.orchestrator;

import java.util.List;

import com.milind.mcp.client.llm.AnthropicClient;
import com.milind.mcp.client.llm.AnthropicTurn;
import com.milind.mcp.client.llm.ToolUseRequest;
import com.milind.mcp.client.mcp.McpClient;
import com.milind.mcp.client.mcp.McpClientException;
import com.milind.mcp.client.llm.AnthropicMessages;
import com.milind.mcp.common.model.McpTool;
import com.milind.mcp.common.model.ToolCallResult;
import com.milind.mcp.common.model.ToolContent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * Ties {@link AnthropicClient} and {@link McpClient} together into the loop this whole
 * module exists for: given one natural-language prompt and the tool catalog discovered
 * at startup, ask Claude which tool (if any) answers it and with what arguments, call
 * that tool through the router, hand the result back to Claude, and return its final
 * answer.
 *
 * <p>Bounded to {@link #maxToolCalls} rounds so a model that keeps calling tools (or a
 * tool that keeps failing) can't loop forever - this is a CLI a person is waiting on,
 * not a background job.
 */
public class PromptOrchestrator {

    private final McpClient mcpClient;
    private final AnthropicClient anthropicClient;
    private final ObjectMapper objectMapper;
    private final List<McpTool> toolCatalog;
    private final int maxToolCalls;

    public PromptOrchestrator(McpClient mcpClient, AnthropicClient anthropicClient, ObjectMapper objectMapper,
                               List<McpTool> toolCatalog, int maxToolCalls) {
        this.mcpClient = mcpClient;
        this.anthropicClient = anthropicClient;
        this.objectMapper = objectMapper;
        this.toolCatalog = toolCatalog;
        this.maxToolCalls = maxToolCalls;
    }

    public String handlePrompt(String userPrompt) {
        ArrayNode messages = objectMapper.createArrayNode();
        messages.add(AnthropicMessages.userText(userPrompt, objectMapper));

        for (int round = 0; round < maxToolCalls; round++) {
            AnthropicTurn turn = anthropicClient.sendMessages(messages, toolCatalog);

            if (!turn.isToolUse()) {
                String text = turn.text();
                return text.isBlank() ? "(Claude returned no text - stop_reason=" + turn.getStopReason() + ")" : text;
            }

            ToolUseRequest toolUse = turn.firstToolUse()
                    .orElseThrow(() -> new IllegalStateException(
                            "stop_reason was tool_use but no tool_use content block was present"));

            messages.add(AnthropicMessages.assistantFromContent(turn.getContent(), objectMapper));

            String resultText;
            boolean isError;
            try {
                ToolCallResult result = mcpClient.callTool(toolUse.getName(), toolUse.getInput());
                resultText = textOf(result);
                isError = result.isError();
            } catch (McpClientException e) {
                resultText = "Tool invocation failed: " + e.getMessage();
                isError = true;
            }

            messages.add(AnthropicMessages.userToolResult(toolUse.getId(), resultText, isError, objectMapper));
        }

        return "Stopped after " + maxToolCalls + " tool call(s) without a final answer from Claude.";
    }

    private static String textOf(ToolCallResult result) {
        StringBuilder sb = new StringBuilder();
        for (ToolContent content : result.getContent()) {
            if (!sb.isEmpty()) {
                sb.append('\n');
            }
            sb.append(content.getText());
        }
        return sb.toString();
    }
}
