package com.milind.mcp.common.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * The {@code error} member of a {@link JsonRpcResponse}, per the JSON-RPC 2.0 spec:
 * a numeric {@code code} (see {@link McpErrorCodes}), a human-readable {@code message},
 * and an optional structured {@code data} payload for machine-readable detail (e.g. the
 * {@code limit}/{@code window}/{@code retryAfter} fields a future rate-limit error would carry).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonRpcError {

    private int code;
    private String message;
    private Object data;

    public JsonRpcError() {
    }

    public JsonRpcError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public JsonRpcError(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
