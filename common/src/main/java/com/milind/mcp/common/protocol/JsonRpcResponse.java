package com.milind.mcp.common.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Outbound JSON-RPC 2.0 response envelope. Exactly one of {@code result} / {@code error}
 * is populated, per spec. {@code id} echoes the request's id.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonRpcResponse {

    private final String jsonrpc = "2.0";
    private Object id;
    private Object result;
    private JsonRpcError error;

    public static JsonRpcResponse success(Object id, Object result) {
        JsonRpcResponse response = new JsonRpcResponse();
        response.id = id;
        response.result = result;
        return response;
    }

    public static JsonRpcResponse failure(Object id, JsonRpcError error) {
        JsonRpcResponse response = new JsonRpcResponse();
        response.id = id;
        response.error = error;
        return response;
    }

    public static JsonRpcResponse failure(Object id, int code, String message) {
        return failure(id, new JsonRpcError(code, message));
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public JsonRpcError getError() {
        return error;
    }

    public void setError(JsonRpcError error) {
        this.error = error;
    }
}
