package com.example.mcp.common.protocol;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Inbound JSON-RPC 2.0 request envelope. Every MCP operation (initialize, tools/list,
 * tools/call, ...) arrives shaped like this, which is precisely why a plain HTTP
 * gateway can't tell one MCP method apart from another without opening the body
 * (Blueprint Section 8.1) — {@code method} and {@code params} are the only signal.
 *
 * <p>{@code id} is {@code null} for JSON-RPC notifications (e.g. the client's
 * {@code notifications/initialized}), which must not receive a response body.
 */
public class JsonRpcRequest {

    private String jsonrpc = "2.0";
    private Object id;
    private String method;
    private JsonNode params;

    public JsonRpcRequest() {
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public JsonNode getParams() {
        return params;
    }

    public void setParams(JsonNode params) {
        this.params = params;
    }

    /** A request with no {@code id} is a JSON-RPC notification: fire-and-forget, no response. */
    public boolean isNotification() {
        return id == null;
    }
}
