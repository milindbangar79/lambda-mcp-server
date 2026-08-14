package com.milind.mcp.tools.greetings;

/**
 * Result of a "greetings" tool call. Serialized to JSON by the Lambda runtime and
 * returned as-is to the router, which wraps the raw JSON as an MCP text content block.
 */
public class GreetingResponse {

    private String message;

    public GreetingResponse() {
    }

    public GreetingResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
