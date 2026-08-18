package com.milind.mcp.client.llm;

import java.util.List;
import java.util.Map;

import com.milind.mcp.common.model.McpTool;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnthropicMessagesTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void toolsArrayMapsMcpToolFieldsToAnthropicToolSpecShape() {
        McpTool tool = new McpTool("greetings", "Greets someone", Map.of("type", "object"));

        ArrayNode array = AnthropicMessages.toolsArray(List.of(tool), objectMapper);

        assertThat(array).hasSize(1);
        JsonNode toolNode = array.get(0);
        assertThat(toolNode.get("name").asText()).isEqualTo("greetings");
        assertThat(toolNode.get("description").asText()).isEqualTo("Greets someone");
        assertThat(toolNode.get("input_schema").get("type").asText()).isEqualTo("object");
    }

    @Test
    void userTextBuildsPlainStringContentMessage() {
        ObjectNode message = AnthropicMessages.userText("Hello Claude", objectMapper);

        assertThat(message.get("role").asText()).isEqualTo("user");
        assertThat(message.get("content").asText()).isEqualTo("Hello Claude");
    }

    @Test
    void assistantFromContentPreservesRawContentArray() {
        ArrayNode content = objectMapper.createArrayNode();
        content.addObject().put("type", "text").put("text", "hi");

        ObjectNode message = AnthropicMessages.assistantFromContent(content, objectMapper);

        assertThat(message.get("role").asText()).isEqualTo("assistant");
        assertThat(message.get("content")).isEqualTo(content);
    }

    @Test
    void userToolResultWrapsResultInToolResultBlock() {
        ObjectNode message = AnthropicMessages.userToolResult("toolu_123", "{\"message\":\"hi\"}", false, objectMapper);

        assertThat(message.get("role").asText()).isEqualTo("user");
        JsonNode block = message.get("content").get(0);
        assertThat(block.get("type").asText()).isEqualTo("tool_result");
        assertThat(block.get("tool_use_id").asText()).isEqualTo("toolu_123");
        assertThat(block.get("content").asText()).isEqualTo("{\"message\":\"hi\"}");
        assertThat(block.get("is_error").asBoolean()).isFalse();
    }
}
