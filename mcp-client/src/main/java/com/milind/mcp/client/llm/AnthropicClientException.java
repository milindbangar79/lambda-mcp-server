package com.milind.mcp.client.llm;

/** Raised when the Claude API returns an error payload or an unparseable response. */
public class AnthropicClientException extends RuntimeException {

    public AnthropicClientException(String message) {
        super(message);
    }

    public AnthropicClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
