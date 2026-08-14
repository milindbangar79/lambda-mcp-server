package com.milind.mcp.common.model;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Params payload for the {@code tools/call} JSON-RPC method: which tool to invoke
 * ({@code name}, matched against the registry) and the raw {@code arguments} object to
 * pass through to it. The router never interprets {@code arguments} itself — it's
 * forwarded verbatim as the invoke payload to whichever Lambda backs the named tool.
 */
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
