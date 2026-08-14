package com.example.mcp.router.config;

import java.util.List;
import java.util.Map;

import com.example.mcp.router.registry.ToolDefinition;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Hardcoded stand-in for the DynamoDB-backed tool registry the Blueprint (Section 4.1)
 * recommends for production. For a fixed 3-tool sample, a data-driven store buys nothing
 * and adds a table to provision/seed; swap this class for a
 * {@code ToolRegistryService} backed by a DynamoDB client if/when tools need to onboard
 * without a router redeploy.
 *
 * <p>{@code compute_ref} (the {@code functionName} on each {@link ToolDefinition}) is
 * read from environment variables so the same jar works across accounts/stages without
 * a rebuild - set {@code GREETINGS_FUNCTION_NAME}, {@code SIMPLE_INTEREST_FUNCTION_NAME},
 * and {@code COMPOUND_INTEREST_FUNCTION_NAME} on the router Lambda's configuration
 * (see README "Deploying to AWS").
 */
@Configuration
public class ToolRegistryConfig {

    private final ObjectMapper objectMapper;

    public ToolRegistryConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public List<ToolDefinition> mcpToolRegistry(
            @Value("${GREETINGS_FUNCTION_NAME:mcp-tool-greetings}") String greetingsFunctionName,
            @Value("${SIMPLE_INTEREST_FUNCTION_NAME:mcp-tool-simple-interest}") String simpleInterestFunctionName,
            @Value("${COMPOUND_INTEREST_FUNCTION_NAME:mcp-tool-compound-interest}") String compoundInterestFunctionName
    ) throws JsonProcessingException {
        return List.of(
                new ToolDefinition(
                        "greetings",
                        "1.0.0",
                        "Returns a friendly greeting for the given name.",
                        schema("""
                                {
                                  "type": "object",
                                  "properties": {
                                    "name": {
                                      "type": "string",
                                      "description": "Name of the person to greet."
                                    }
                                  },
                                  "required": ["name"]
                                }
                                """),
                        greetingsFunctionName,
                        "platform-samples",
                        "published",
                        1.0,
                        "cheap",
                        5
                ),
                new ToolDefinition(
                        "simple-interest-calculator",
                        "1.0.0",
                        "Calculates simple interest: SI = (P x R x T) / 100.",
                        schema("""
                                {
                                  "type": "object",
                                  "properties": {
                                    "principal": {
                                      "type": "number",
                                      "description": "Principal amount (must be > 0).",
                                      "exclusiveMinimum": 0
                                    },
                                    "rateOfInterest": {
                                      "type": "number",
                                      "description": "Annual interest rate, as a percentage (e.g. 5 for 5%).",
                                      "exclusiveMinimum": 0
                                    },
                                    "timeInYears": {
                                      "type": "number",
                                      "description": "Duration in years (must be > 0).",
                                      "exclusiveMinimum": 0
                                    }
                                  },
                                  "required": ["principal", "rateOfInterest", "timeInYears"]
                                }
                                """),
                        simpleInterestFunctionName,
                        "platform-samples",
                        "published",
                        1.0,
                        "standard",
                        5
                ),
                new ToolDefinition(
                        "compound-interest-calculator",
                        "1.0.0",
                        "Calculates compound interest: A = P(1 + r/n)^(nt).",
                        schema("""
                                {
                                  "type": "object",
                                  "properties": {
                                    "principal": {
                                      "type": "number",
                                      "description": "Principal amount (must be > 0).",
                                      "exclusiveMinimum": 0
                                    },
                                    "rateOfInterest": {
                                      "type": "number",
                                      "description": "Annual interest rate, as a percentage (e.g. 5 for 5%).",
                                      "exclusiveMinimum": 0
                                    },
                                    "timeInYears": {
                                      "type": "number",
                                      "description": "Duration in years (must be > 0).",
                                      "exclusiveMinimum": 0
                                    },
                                    "compoundingFrequency": {
                                      "type": "string",
                                      "description": "How often interest compounds per year.",
                                      "enum": ["ANNUALLY", "SEMI_ANNUALLY", "QUARTERLY", "MONTHLY", "DAILY"],
                                      "default": "ANNUALLY"
                                    }
                                  },
                                  "required": ["principal", "rateOfInterest", "timeInYears"]
                                }
                                """),
                        compoundInterestFunctionName,
                        "platform-samples",
                        "published",
                        1.0,
                        "standard",
                        5
                )
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> schema(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, Map.class);
    }
}
