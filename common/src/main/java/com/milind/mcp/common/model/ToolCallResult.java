package com.milind.mcp.common.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Result payload for {@code tools/call}. Per the MCP spec, a failure that happens
 * <i>inside</i> the tool (bad input, downstream error) is reported as a normal
 * JSON-RPC success whose result has {@code isError=true} — it is not a JSON-RPC
 * protocol-level error. JSON-RPC errors ({@link com.milind.mcp.common.protocol.JsonRpcError})
 * are reserved for protocol/registry problems: unknown method, unknown tool, unpublished tool.
 */
public class ToolCallResult {

    private List<ToolContent> content;

    /**
     * {@code @JsonProperty("isError")} is load-bearing, not decorative: Jackson's default
     * bean-property naming strips the {@code is} prefix from an {@code isXxx()} getter,
     * so without this annotation the field serializes on the wire as {@code "error"},
     * not the MCP-spec-mandated {@code "isError"} - verified empirically (a plain
     * getter/setter pair round-trips fine in-process, which is exactly why this went
     * unnoticed until an external, spec-literal client - mcp-client - was built against
     * the real wire JSON instead of Java objects).
     */
    @JsonProperty("isError")
    private boolean isError;

    public ToolCallResult() {
    }

    public static ToolCallResult ok(String text) {
        ToolCallResult result = new ToolCallResult();
        result.content = List.of(ToolContent.text(text));
        result.isError = false;
        return result;
    }

    public static ToolCallResult error(String text) {
        ToolCallResult result = new ToolCallResult();
        result.content = List.of(ToolContent.text(text));
        result.isError = true;
        return result;
    }

    public List<ToolContent> getContent() {
        return content;
    }

    public void setContent(List<ToolContent> content) {
        this.content = content;
    }

    public boolean isError() {
        return isError;
    }

    public void setError(boolean error) {
        isError = error;
    }
}
