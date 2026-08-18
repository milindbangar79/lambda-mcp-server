package com.milind.mcp.client.mcp;

/** Raised when the router returns a JSON-RPC-level error (unknown method, unknown/unpublished tool, ...). */
public class McpClientException extends RuntimeException {

    private final int code;

    public McpClientException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
