package com.example.mcp.tools.greetings;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class GreetingService {

    public GreetingResponse greet(GreetingRequest request) {
        if (request == null || !StringUtils.hasText(request.getName())) {
            throw new IllegalArgumentException("'name' is required and must not be blank");
        }
        return new GreetingResponse("Hello, " + request.getName().trim() + "! Welcome to the MCP Lambda server.");
    }
}
