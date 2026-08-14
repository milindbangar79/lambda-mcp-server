package com.milind.mcp.router.protocol;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.milind.mcp.common.model.InitializeResult;
import com.milind.mcp.common.model.ToolCallResult;
import com.milind.mcp.common.model.ToolsListResult;
import com.milind.mcp.common.protocol.JsonRpcRequest;
import com.milind.mcp.common.protocol.JsonRpcResponse;
import com.milind.mcp.common.protocol.McpErrorCodes;
import com.milind.mcp.router.dispatch.ToolDispatcherService;
import com.milind.mcp.router.dispatch.ToolInvocationException;
import com.milind.mcp.router.registry.ToolDefinition;
import com.milind.mcp.router.registry.ToolRegistryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class McpProtocolServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private ToolRegistryService toolRegistryService;
    private ToolDispatcherService toolDispatcherService;
    private McpProtocolService protocolService;

    private ToolDefinition publishedTool;
    private ToolDefinition draftTool;

    @BeforeEach
    void setUp() {
        toolRegistryService = mock(ToolRegistryService.class);
        toolDispatcherService = mock(ToolDispatcherService.class);
        protocolService = new McpProtocolService(toolRegistryService, toolDispatcherService, objectMapper);

        publishedTool = new ToolDefinition("greetings", "1.0.0", "Greets someone",
                Map.of("type", "object"), "mcp-tool-greetings", "platform-samples",
                "published", 1.0, "cheap", 5);
        draftTool = new ToolDefinition("draft-tool", "0.1.0", "Not yet published",
                Map.of("type", "object"), "mcp-tool-draft", "platform-samples",
                "draft", 0.0, "cheap", 5);
    }

    @Test
    void notificationsReceiveNoResponse() {
        JsonRpcRequest request = new JsonRpcRequest();
        request.setMethod("notifications/initialized");
        request.setId(null);

        Optional<JsonRpcResponse> response = protocolService.handle(request, "corr-1");

        assertThat(response).isEmpty();
    }

    @Test
    void initializeEchoesRequestedProtocolVersionAndReturnsServerInfo() {
        JsonRpcRequest request = jsonRpcRequest("initialize", Map.of("protocolVersion", "2024-11-05"));

        JsonRpcResponse response = protocolService.handle(request, "corr-1").orElseThrow();

        assertThat(response.getError()).isNull();
        InitializeResult result = objectMapper.convertValue(response.getResult(), InitializeResult.class);
        assertThat(result.getProtocolVersion()).isEqualTo("2024-11-05");
        assertThat(result.getServerInfo().getName()).isEqualTo("lambda-mcp-server");
    }

    @Test
    void toolsListReturnsOnlyPublishedTools() {
        when(toolRegistryService.listPublishedTools()).thenReturn(List.of(publishedTool));
        JsonRpcRequest request = jsonRpcRequest("tools/list", null);

        JsonRpcResponse response = protocolService.handle(request, "corr-1").orElseThrow();

        ToolsListResult result = objectMapper.convertValue(response.getResult(), ToolsListResult.class);
        assertThat(result.getTools()).hasSize(1);
        assertThat(result.getTools().get(0).getName()).isEqualTo("greetings");
    }

    @Test
    void toolsCallDispatchesToRegisteredToolAndReturnsRawResultAsText() {
        when(toolRegistryService.findByName("greetings")).thenReturn(Optional.of(publishedTool));
        when(toolDispatcherService.invoke(eq(publishedTool), any(JsonNode.class), eq("corr-1")))
                .thenReturn("{\"message\":\"Hello, Ada!\"}");

        JsonRpcRequest request = jsonRpcRequest("tools/call",
                Map.of("name", "greetings", "arguments", Map.of("name", "Ada")));

        JsonRpcResponse response = protocolService.handle(request, "corr-1").orElseThrow();

        ToolCallResult result = objectMapper.convertValue(response.getResult(), ToolCallResult.class);
        assertThat(result.isError()).isFalse();
        assertThat(result.getContent().get(0).getText()).isEqualTo("{\"message\":\"Hello, Ada!\"}");
    }

    @Test
    void toolsCallForUnknownToolReturnsJsonRpcError() {
        when(toolRegistryService.findByName("nonexistent")).thenReturn(Optional.empty());

        JsonRpcRequest request = jsonRpcRequest("tools/call", Map.of("name", "nonexistent"));

        JsonRpcResponse response = protocolService.handle(request, "corr-1").orElseThrow();

        assertThat(response.getResult()).isNull();
        assertThat(response.getError().getCode()).isEqualTo(McpErrorCodes.TOOL_NOT_FOUND);
    }

    @Test
    void toolsCallForUnpublishedToolReturnsJsonRpcError() {
        when(toolRegistryService.findByName("draft-tool")).thenReturn(Optional.of(draftTool));

        JsonRpcRequest request = jsonRpcRequest("tools/call", Map.of("name", "draft-tool"));

        JsonRpcResponse response = protocolService.handle(request, "corr-1").orElseThrow();

        assertThat(response.getError().getCode()).isEqualTo(McpErrorCodes.TOOL_NOT_PUBLISHED);
    }

    @Test
    void toolExecutionFailureIsReturnedAsIsErrorResultNotJsonRpcError() {
        when(toolRegistryService.findByName("greetings")).thenReturn(Optional.of(publishedTool));
        when(toolDispatcherService.invoke(eq(publishedTool), any(JsonNode.class), eq("corr-1")))
                .thenThrow(new ToolInvocationException("Tool 'greetings' failed: boom"));

        JsonRpcRequest request = jsonRpcRequest("tools/call",
                Map.of("name", "greetings", "arguments", Map.of("name", "Ada")));

        JsonRpcResponse response = protocolService.handle(request, "corr-1").orElseThrow();

        assertThat(response.getError()).isNull();
        ToolCallResult result = objectMapper.convertValue(response.getResult(), ToolCallResult.class);
        assertThat(result.isError()).isTrue();
        assertThat(result.getContent().get(0).getText()).contains("boom");
    }

    @Test
    void unknownMethodReturnsMethodNotFound() {
        JsonRpcRequest request = jsonRpcRequest("resources/list", null);

        JsonRpcResponse response = protocolService.handle(request, "corr-1").orElseThrow();

        assertThat(response.getError().getCode()).isEqualTo(McpErrorCodes.METHOD_NOT_FOUND);
    }

    private JsonRpcRequest jsonRpcRequest(String method, Map<String, Object> params) {
        JsonRpcRequest request = new JsonRpcRequest();
        request.setId("1");
        request.setMethod(method);
        if (params != null) {
            request.setParams(objectMapper.valueToTree(params));
        }
        return request;
    }
}
