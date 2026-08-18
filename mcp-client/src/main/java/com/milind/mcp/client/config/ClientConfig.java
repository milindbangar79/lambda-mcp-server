package com.milind.mcp.client.config;

import java.util.function.Function;

/**
 * Configuration read from environment variables - "simple references", not a secrets
 * manager or config file, matching the sample's scope. In particular the Claude API key
 * is never hardcoded, never logged, and never passed as a CLI argument (which would leak
 * into shell history); it's read once from {@code ANTHROPIC_API_KEY} and held only in
 * memory for the process lifetime.
 *
 * <table>
 * <caption>Environment variables</caption>
 * <tr><th>Variable</th><th>Required</th><th>Purpose</th></tr>
 * <tr><td>{@code ANTHROPIC_API_KEY}</td><td>yes</td><td>Claude API key, sent as the {@code x-api-key} header</td></tr>
 * <tr><td>{@code MCP_SERVER_URL}</td><td>yes</td><td>The deployed router's API Gateway invoke URL, e.g. {@code https://xxxx.execute-api.us-east-1.amazonaws.com/prod/mcp}</td></tr>
 * <tr><td>{@code CLAUDE_MODEL}</td><td>no</td><td>Defaults to {@value #DEFAULT_MODEL}</td></tr>
 * <tr><td>{@code ANTHROPIC_API_BASE_URL}</td><td>no</td><td>Defaults to {@value #DEFAULT_ANTHROPIC_BASE_URL}</td></tr>
 * </table>
 */
public class ClientConfig {

    public static final String DEFAULT_MODEL = "claude-sonnet-5";
    public static final String DEFAULT_ANTHROPIC_BASE_URL = "https://api.anthropic.com";
    public static final String ANTHROPIC_VERSION = "2023-06-01";

    private final String anthropicApiKey;
    private final String mcpServerUrl;
    private final String model;
    private final String anthropicBaseUrl;

    private ClientConfig(String anthropicApiKey, String mcpServerUrl, String model, String anthropicBaseUrl) {
        this.anthropicApiKey = anthropicApiKey;
        this.mcpServerUrl = mcpServerUrl;
        this.model = model;
        this.anthropicBaseUrl = anthropicBaseUrl;
    }

    /** Reads configuration from the process environment, using {@link System#getenv(String)}. */
    public static ClientConfig fromEnvironment() {
        return fromEnvironment(System::getenv);
    }

    /** Testable variant: reads configuration via the supplied lookup function instead of the real environment. */
    public static ClientConfig fromEnvironment(Function<String, String> env) {
        String apiKey = requireNonBlank(env, "ANTHROPIC_API_KEY",
                "ANTHROPIC_API_KEY is not set. Export your Claude API key, e.g.:\n"
                        + "  export ANTHROPIC_API_KEY=sk-ant-...");
        String serverUrl = requireNonBlank(env, "MCP_SERVER_URL",
                "MCP_SERVER_URL is not set. Export the deployed router's API Gateway invoke URL, e.g.:\n"
                        + "  export MCP_SERVER_URL=https://xxxx.execute-api.us-east-1.amazonaws.com/prod/mcp");
        String model = env.apply("CLAUDE_MODEL");
        String baseUrl = env.apply("ANTHROPIC_API_BASE_URL");

        return new ClientConfig(
                apiKey,
                serverUrl,
                (model == null || model.isBlank()) ? DEFAULT_MODEL : model,
                (baseUrl == null || baseUrl.isBlank()) ? DEFAULT_ANTHROPIC_BASE_URL : baseUrl
        );
    }

    private static String requireNonBlank(Function<String, String> env, String key, String errorMessage) {
        String value = env.apply(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(errorMessage);
        }
        return value;
    }

    public String getAnthropicApiKey() {
        return anthropicApiKey;
    }

    public String getMcpServerUrl() {
        return mcpServerUrl;
    }

    public String getModel() {
        return model;
    }

    public String getAnthropicBaseUrl() {
        return anthropicBaseUrl;
    }
}
