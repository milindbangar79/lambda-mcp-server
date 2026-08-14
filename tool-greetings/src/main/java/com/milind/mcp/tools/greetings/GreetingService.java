package com.milind.mcp.tools.greetings;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** The "greetings" tool's business logic: builds a friendly greeting for a given name. */
@Service
public class GreetingService {

    /**
     * @param request must have a non-blank {@code name}
     * @return a greeting message addressed to {@code request.getName()}
     * @throws IllegalArgumentException if {@code request} is {@code null} or {@code name} is blank
     */
    public GreetingResponse greet(GreetingRequest request) {
        if (request == null || !StringUtils.hasText(request.getName())) {
            throw new IllegalArgumentException("'name' is required and must not be blank");
        }
        return new GreetingResponse("Hello, " + request.getName().trim() + "! Welcome to the MCP Lambda server.");
    }
}
