package com.milind.mcp.common.protocol;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    /**
     * A request with no {@code id} is a JSON-RPC notification: fire-and-forget, no response.
     *
     * <p>{@code @JsonIgnore} is load-bearing, not decorative: without it, Jackson's default
     * bean introspection treats this as a serializable {@code notification} property (it
     * matches the {@code isXxx()} getter pattern despite having no backing field), which a
     * client serializing this class would then send over the wire - and the router's own
     * default {@code ObjectMapper} rejects unrecognized properties by default, so every such
     * request would fail to parse. Verified empirically while building {@code mcp-client},
     * the first thing in this repo to actually serialize a {@code JsonRpcRequest} rather
     * than only ever deserializing one.
     */
    @JsonIgnore
    public boolean isNotification() {
        return id == null;
    }
}
