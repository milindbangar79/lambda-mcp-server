package com.milind.mcp.tools.greetings;

/** Deserialized directly from the router's invoke payload by the Lambda runtime. */
public class GreetingRequest {

    private String name;

    public GreetingRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
