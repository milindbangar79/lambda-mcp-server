package com.milind.mcp.client.orchestrator;

import java.util.List;
import java.util.Map;

import com.milind.mcp.client.llm.AnthropicClient;
import com.milind.mcp.client.llm.AnthropicTurn;
import com.milind.mcp.client.mcp.McpClient;
import com.milind.mcp.client.mcp.McpClientException;
import com.milind.mcp.common.model.McpTool;
import com.milind.mcp.common.model.ToolCallResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PromptOrchestratorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private McpClient mcpClient;
    private AnthropicClient anthropicClient;
    private List<McpTool> toolCatalog;

    @BeforeEach
    void setUp() {
        mcpClient = mock(McpClient.class);
        anthropicClient = mock(AnthropicClient.class);
        toolCatalog = List.of(new McpTool("greetings", "Greets someone", Map.of("type", "object")));
    }

    private PromptOrchestrator orchestrator(int maxToolCalls) {
        return new PromptOrchestrator(mcpClient, anthropicClient, objectMapper, toolCatalog, maxToolCalls);
    }

    private AnthropicTurn textTurn(String text) {
        ArrayNode content = objectMapper.createArrayNode();
        content.addObject().put("type", "text").put("text", text);
        return new AnthropicTurn(content, "end_turn");
    }

    private AnthropicTurn toolUseTurn(String toolUseId, String toolName, ObjectNode input) {
        ArrayNode content = objectMapper.createArrayNode();
        ObjectNode block = content.addObject();
        block.put("type", "tool_use");
        block.put("id", toolUseId);
        block.put("name", toolName);
        block.set("input", input);
        return new AnthropicTurn(content, "tool_use");
    }

    @Test
    void claudeAnswersDirectlyWithoutCallingATool() {
        when(anthropicClient.sendMessages(any(), anyList())).thenReturn(textTurn("Paris is the capital of France."));

        String answer = orchestrator(3).handlePrompt("What is the capital of France?");

        assertThat(answer).isEqualTo("Paris is the capital of France.");
        verify(mcpClient, never()).callTool(any(), any());
        verify(anthropicClient, times(1)).sendMessages(any(), anyList());
    }

    @Test
    void resolvesToolCallsItAndReturnsFinalAnswer() {
        ObjectNode input = objectMapper.createObjectNode().put("name", "Ada");
        AnthropicTurn firstTurn = toolUseTurn("toolu_1", "greetings", input);
        AnthropicTurn secondTurn = textTurn("Hello Ada! Nice to meet you.");

        when(anthropicClient.sendMessages(any(), anyList())).thenReturn(firstTurn).thenReturn(secondTurn);
        when(mcpClient.callTool(eq("greetings"), any(JsonNode.class)))
                .thenReturn(ToolCallResult.ok("{\"message\":\"Hello, Ada!\"}"));

        String answer = orchestrator(3).handlePrompt("Greet Ada");

        assertThat(answer).isEqualTo("Hello Ada! Nice to meet you.");

        ArgumentCaptor<JsonNode> argsCaptor = ArgumentCaptor.forClass(JsonNode.class);
        verify(mcpClient, times(1)).callTool(eq("greetings"), argsCaptor.capture());
        assertThat(argsCaptor.getValue().get("name").asText()).isEqualTo("Ada");

        verify(anthropicClient, times(2)).sendMessages(any(), anyList());
    }

    @Test
    void toolFailureIsReportedBackToClaudeAsErrorToolResult() {
        ObjectNode input = objectMapper.createObjectNode().put("name", "");
        AnthropicTurn firstTurn = toolUseTurn("toolu_2", "greetings", input);
        AnthropicTurn secondTurn = textTurn("I couldn't complete that - the name was missing.");

        when(anthropicClient.sendMessages(any(), anyList())).thenReturn(firstTurn).thenReturn(secondTurn);
        when(mcpClient.callTool(eq("greetings"), any(JsonNode.class)))
                .thenThrow(new McpClientException(-32001, "Unknown tool: greetings"));

        String answer = orchestrator(3).handlePrompt("Greet");

        assertThat(answer).isEqualTo("I couldn't complete that - the name was missing.");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<ArrayNode> messagesCaptor = ArgumentCaptor.forClass(ArrayNode.class);
        verify(anthropicClient, times(2)).sendMessages(messagesCaptor.capture(), anyList());

        ArrayNode secondCallMessages = messagesCaptor.getAllValues().get(1);
        JsonNode toolResultBlock = secondCallMessages.get(2).get("content").get(0);
        assertThat(toolResultBlock.get("is_error").asBoolean()).isTrue();
        assertThat(toolResultBlock.get("content").asText()).contains("Unknown tool: greetings");
    }

    @Test
    void stopsAfterMaxToolCallsWithoutFinalAnswer() {
        ObjectNode input = objectMapper.createObjectNode().put("name", "Ada");
        AnthropicTurn alwaysToolUse = toolUseTurn("toolu_x", "greetings", input);

        when(anthropicClient.sendMessages(any(), anyList())).thenReturn(alwaysToolUse);
        when(mcpClient.callTool(eq("greetings"), any(JsonNode.class)))
                .thenReturn(ToolCallResult.ok("{\"message\":\"Hello, Ada!\"}"));

        String answer = orchestrator(2).handlePrompt("Greet Ada repeatedly");

        assertThat(answer).contains("Stopped after 2 tool call(s)");
        verify(anthropicClient, times(2)).sendMessages(any(), anyList());
        verify(mcpClient, times(2)).callTool(eq("greetings"), any(JsonNode.class));
    }
}
