package com.example.mcp.router;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Root Spring Boot configuration for the router. No {@code main()} is needed: the
 * Lambda runtime never runs one, it instantiates {@link McpRouterHandler} directly
 * and calls {@code handleRequest}. Spring Boot auto-detects {@code WebApplicationType.NONE}
 * on its own here since no web starter is on the classpath.
 */
@SpringBootApplication
public class RouterApplication {
}
