package com.milind.mcp.client.llm;

import java.util.List;
import java.util.Map;

import com.milind.mcp.client.config.ClientConfig;
import com.milind.mcp.client.http.FakeHttpTransport;
import com.milind.mcp.common.model.McpTool;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnthropicClientTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private FakeHttpTransport transport;
    private AnthropicClient client;

    @BeforeEach
    void setUp() {
        transport = new FakeHttpTransport();
        Map<String, String> env = Map.of(
                "ANTHROPIC_API_KEY", "sk-ant-test-key",
                "MCP_SERVER_URL", "https://example.com/mcp"
        );
        ClientConfig config = ClientConfig.fromEnvironment(env::get);
        client = new AnthropicClient(config, transport, objectMapper);
    }

    @Test
    void sendsApiKeyAndVersionHeadersAndToolsArray() {
        transport.queueResponse("""
                {"id":"msg_1","type":"message","role":"assistant",
                 "content":[{"type":"text","text":"Hi there"}],
                 "stop_reason":"end_turn"}
                """);

        ArrayNode messages = objectMapper.createArrayNode();
        messages.add(AnthropicMessages.userText("hello", objectMapper));
        McpTool tool = new McpTool("greetings", "Greets someone", Map.of("type", "object"));

        AnthropicTurn turn = client.sendMessages(messages, List.of(tool));

        assertThat(turn.text()).isEqualTo("Hi there");
        assertThat(turn.isToolUse()).isFalse();

        var request = transport.lastRequest();
        assertThat(request.url()).isEqualTo(ClientConfig.DEFAULT_ANTHROPIC_BASE_URL + "/v1/messages");
        assertThat(request.headers()).containsEntry("x-api-key", "sk-ant-test-key");
        assertThat(request.headers()).containsEntry("anthropic-version", ClientConfig.ANTHROPIC_VERSION);
        assertThat(request.jsonBody()).contains("\"model\":\"" + ClientConfig.DEFAULT_MODEL + "\"");
        assertThat(request.jsonBody()).contains("\"name\":\"greetings\"");
    }

    @Test
    void parsesToolUseStopReason() {
        transport.queueResponse("""
                {"id":"msg_2","type":"message","role":"assistant",
                 "content":[
                   {"type":"text","text":"Let me look that up."},
                   {"type":"tool_use","id":"toolu_1","name":"greetings","input":{"name":"Ada"}}
                 ],
                 "stop_reason":"tool_use"}
                """);

        AnthropicTurn turn = client.sendMessages(objectMapper.createArrayNode(), List.of());

        assertThat(turn.isToolUse()).isTrue();
        ToolUseRequest toolUse = turn.firstToolUse().orElseThrow();
        assertThat(toolUse.getName()).isEqualTo("greetings");
        assertThat(toolUse.getInput().get("name").asText()).isEqualTo("Ada");
    }

    @Test
    void apiErrorResponseThrowsAnthropicClientException() {
        transport.queueResponse("""
                {"type":"error","error":{"type":"authentication_error","message":"invalid x-api-key"}}
                """);

        assertThatThrownBy(() -> client.sendMessages(objectMapper.createArrayNode(), List.of()))
                .isInstanceOf(AnthropicClientException.class)
                .hasMessageContaining("invalid x-api-key");
    }
}
