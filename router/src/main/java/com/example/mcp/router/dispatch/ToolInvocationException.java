package com.example.mcp.router.dispatch;

/** Raised when the target tool Lambda returns a functionError, or the invoke itself fails. */
public class ToolInvocationException extends RuntimeException {

    public ToolInvocationException(String message) {
        super(message);
    }

    public ToolInvocationException(String message, Throwable cause) {
        super(message, cause);
    }
}
