package com.milind.mcp.client.mcp;

import com.milind.mcp.client.http.FakeHttpTransport;
import com.milind.mcp.common.model.InitializeResult;
import com.milind.mcp.common.model.McpTool;
import com.milind.mcp.common.model.ToolCallResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class McpClientTest {

    private static final String SERVER_URL = "https://example.execute-api.us-east-1.amazonaws.com/prod/mcp";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private FakeHttpTransport transport;
    private McpClient client;

    @BeforeEach
    void setUp() {
        transport = new FakeHttpTransport();
        client = new McpClient(SERVER_URL, transport, objectMapper);
    }

    @Test
    void initializeSendsProtocolVersionAndParsesServerInfo() {
        transport.queueResponse("""
                {"jsonrpc":"2.0","id":1,"result":{
                  "protocolVersion":"2024-11-05",
                  "capabilities":{"tools":{"listChanged":false}},
                  "serverInfo":{"name":"lambda-mcp-server","version":"1.0.0"}
                }}
                """);

        InitializeResult result = client.initialize();

        assertThat(result.getProtocolVersion()).isEqualTo("2024-11-05");
        assertThat(result.getServerInfo().getName()).isEqualTo("lambda-mcp-server");

        assertThat(transport.lastRequest().url()).isEqualTo(SERVER_URL);
        assertThat(transport.lastRequest().jsonBody()).contains("\"method\":\"initialize\"");
        assertThat(transport.lastRequest().jsonBody()).contains("\"protocolVersion\":\"2024-11-05\"");
    }

    @Test
    void listToolsParsesRegisteredTools() {
        transport.queueResponse("""
                {"jsonrpc":"2.0","id":1,"result":{"tools":[
                  {"name":"greetings","description":"Greets someone","inputSchema":{"type":"object"}},
                  {"name":"simple-interest-calculator","description":"SI","inputSchema":{"type":"object"}}
                ]}}
                """);

        List<McpTool> tools = client.listTools();

        assertThat(tools).hasSize(2);
        assertThat(tools).extracting(McpTool::getName)
                .containsExactly("greetings", "simple-interest-calculator");
        assertThat(transport.lastRequest().jsonBody()).contains("\"method\":\"tools/list\"");
    }

    @Test
    void callToolSendsNameAndArgumentsAndParsesResult() {
        transport.queueResponse("""
                {"jsonrpc":"2.0","id":1,"result":{
                  "content":[{"type":"text","text":"{\\"message\\":\\"Hello, Ada!\\"}"}],
                  "isError":false
                }}
                """);

        ObjectNode arguments = objectMapper.createObjectNode();
        arguments.put("name", "Ada");

        ToolCallResult result = client.callTool("greetings", arguments);

        assertThat(result.isError()).isFalse();
        assertThat(result.getContent().get(0).getText()).contains("Hello, Ada!");

        String sentBody = transport.lastRequest().jsonBody();
        assertThat(sentBody).contains("\"method\":\"tools/call\"");
        assertThat(sentBody).contains("\"name\":\"greetings\"");
        assertThat(sentBody).contains("\"name\":\"Ada\"");
    }

    @Test
    void jsonRpcErrorIsSurfacedAsMcpClientException() {
        transport.queueResponse("""
                {"jsonrpc":"2.0","id":1,"error":{"code":-32001,"message":"Unknown tool: nonexistent"}}
                """);

        assertThatThrownBy(() -> client.callTool("nonexistent", objectMapper.createObjectNode()))
                .isInstanceOf(McpClientException.class)
                .hasMessageContaining("Unknown tool: nonexistent")
                .satisfies(e -> assertThat(((McpClientException) e).getCode()).isEqualTo(-32001));
    }

    @Test
    void httpFailurePropagatesFromTransport() {
        // no response queued at all -> FakeHttpTransport throws
        assertThatThrownBy(() -> client.listTools())
                .isInstanceOf(IllegalStateException.class);
    }
}
