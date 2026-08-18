package com.milind.mcp.client.config;

import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClientConfigTest {

    @Test
    void readsRequiredValuesAndAppliesDefaults() {
        Map<String, String> env = Map.of(
                "ANTHROPIC_API_KEY", "sk-ant-test-key",
                "MCP_SERVER_URL", "https://example.execute-api.us-east-1.amazonaws.com/prod/mcp"
        );

        ClientConfig config = ClientConfig.fromEnvironment(env::get);

        assertThat(config.getAnthropicApiKey()).isEqualTo("sk-ant-test-key");
        assertThat(config.getMcpServerUrl()).isEqualTo("https://example.execute-api.us-east-1.amazonaws.com/prod/mcp");
        assertThat(config.getModel()).isEqualTo(ClientConfig.DEFAULT_MODEL);
        assertThat(config.getAnthropicBaseUrl()).isEqualTo(ClientConfig.DEFAULT_ANTHROPIC_BASE_URL);
    }

    @Test
    void honorsOptionalOverrides() {
        Map<String, String> env = Map.of(
                "ANTHROPIC_API_KEY", "sk-ant-test-key",
                "MCP_SERVER_URL", "https://example.execute-api.us-east-1.amazonaws.com/prod/mcp",
                "CLAUDE_MODEL", "claude-opus-5",
                "ANTHROPIC_API_BASE_URL", "https://custom.internal.proxy"
        );

        ClientConfig config = ClientConfig.fromEnvironment(env::get);

        assertThat(config.getModel()).isEqualTo("claude-opus-5");
        assertThat(config.getAnthropicBaseUrl()).isEqualTo("https://custom.internal.proxy");
    }

    @Test
    void rejectsMissingApiKey() {
        Map<String, String> env = Map.of("MCP_SERVER_URL", "https://example.com/mcp");

        assertThatThrownBy(() -> ClientConfig.fromEnvironment(env::get))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ANTHROPIC_API_KEY");
    }

    @Test
    void rejectsMissingServerUrl() {
        Map<String, String> env = Map.of("ANTHROPIC_API_KEY", "sk-ant-test-key");

        assertThatThrownBy(() -> ClientConfig.fromEnvironment(env::get))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("MCP_SERVER_URL");
    }

    @Test
    void rejectsBlankApiKey() {
        Map<String, String> env = Map.of(
                "ANTHROPIC_API_KEY", "   ",
                "MCP_SERVER_URL", "https://example.com/mcp"
        );

        assertThatThrownBy(() -> ClientConfig.fromEnvironment(env::get))
                .isInstanceOf(IllegalStateException.class);
    }
}
