package com.milind.mcp.common.model;

import com.fasterxml.jackson.databind.JsonNode;

/** Params payload for the {@code tools/call} JSON-RPC method. */
public class ToolCallParams {

    private String name;
    private JsonNode arguments;

    public ToolCallParams() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JsonNode getArguments() {
        return arguments;
    }

    public void setArguments(JsonNode arguments) {
        this.arguments = arguments;
    }
}
