package com.milind.mcp.common.model;

import java.util.Map;

/**
 * A tool definition as advertised to MCP clients via {@code tools/list}.
 * Mirrors (a subset of) the registry record schema in Blueprint Section 4.1 —
 * {@code name}/{@code description}/{@code inputSchema} are the MCP-visible fields;
 * governance fields (gate_status, trust_score, ...) stay server-side and are not
 * echoed to clients.
 */
public class McpTool {

    private String name;
    private String description;
    private Map<String, Object> inputSchema;

    public McpTool() {
    }

    public McpTool(String name, String description, Map<String, Object> inputSchema) {
        this.name = name;
        this.description = description;
        this.inputSchema = inputSchema;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getInputSchema() {
        return inputSchema;
    }

    public void setInputSchema(Map<String, Object> inputSchema) {
        this.inputSchema = inputSchema;
    }
}
