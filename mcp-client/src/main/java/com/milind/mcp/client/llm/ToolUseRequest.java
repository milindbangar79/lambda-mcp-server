package com.milind.mcp.client.llm;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * A {@code tool_use} content block Claude emitted: which tool it picked, the arguments
 * it built for it (already shaped to match that tool's {@code inputSchema}, since the
 * schema was given to Claude verbatim as the tool's {@code input_schema}), and the
 * block's own id - required to correlate the eventual {@code tool_result} back to it.
 */
public class ToolUseRequest {

    private final String id;
    private final String name;
    private final JsonNode input;

    public ToolUseRequest(String id, String name, JsonNode input) {
        this.id = id;
        this.name = name;
        this.input = input;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public JsonNode getInput() {
        return input;
    }
}
