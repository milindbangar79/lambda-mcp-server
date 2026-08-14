package com.example.mcp.common.model;

import java.util.List;

/** Result payload for the {@code tools/list} JSON-RPC method. */
public class ToolsListResult {

    private List<McpTool> tools;

    public ToolsListResult() {
    }

    public ToolsListResult(List<McpTool> tools) {
        this.tools = tools;
    }

    public List<McpTool> getTools() {
        return tools;
    }

    public void setTools(List<McpTool> tools) {
        this.tools = tools;
    }
}
