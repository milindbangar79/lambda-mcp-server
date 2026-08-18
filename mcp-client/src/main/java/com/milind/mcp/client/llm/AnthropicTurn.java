package com.milind.mcp.client.llm;

import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * One assistant turn from the Messages API: its raw {@code content} block array (kept
 * raw so it can be re-appended verbatim into the next request's message history, which
 * the API requires for multi-turn tool use) plus the parsed {@code stop_reason}.
 */
public class AnthropicTurn {

    private final ArrayNode content;
    private final String stopReason;

    public AnthropicTurn(ArrayNode content, String stopReason) {
        this.content = content;
        this.stopReason = stopReason;
    }

    public ArrayNode getContent() {
        return content;
    }

    public String getStopReason() {
        return stopReason;
    }

    public boolean isToolUse() {
        return "tool_use".equals(stopReason);
    }

    /** The first {@code tool_use} block in this turn, if any - this client only ever asks for one tool per turn. */
    public Optional<ToolUseRequest> firstToolUse() {
        for (JsonNode block : content) {
            if ("tool_use".equals(textOf(block, "type"))) {
                return Optional.of(new ToolUseRequest(
                        textOf(block, "id"),
                        textOf(block, "name"),
                        block.get("input")));
            }
        }
        return Optional.empty();
    }

    /** Concatenates every {@code text} block in this turn - what to print when Claude didn't call a tool. */
    public String text() {
        StringBuilder sb = new StringBuilder();
        for (JsonNode block : content) {
            if ("text".equals(textOf(block, "type"))) {
                if (!sb.isEmpty()) {
                    sb.append('\n');
                }
                sb.append(textOf(block, "text"));
            }
        }
        return sb.toString();
    }

    private static String textOf(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null ? null : value.asText();
    }
}
