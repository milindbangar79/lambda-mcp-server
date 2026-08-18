package com.milind.mcp.client.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnthropicTurnTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void textConcatenatesAllTextBlocks() {
        ArrayNode content = objectMapper.createArrayNode();
        content.addObject().put("type", "text").put("text", "Hello");
        content.addObject().put("type", "text").put("text", "world");

        AnthropicTurn turn = new AnthropicTurn(content, "end_turn");

        assertThat(turn.isToolUse()).isFalse();
        assertThat(turn.text()).isEqualTo("Hello\nworld");
        assertThat(turn.firstToolUse()).isEmpty();
    }

    @Test
    void firstToolUseExtractsIdNameAndInput() {
        ArrayNode content = objectMapper.createArrayNode();
        content.addObject().put("type", "text").put("text", "Let me check that for you.");
        content.addObject()
                .put("type", "tool_use")
                .put("id", "toolu_abc123")
                .put("name", "greetings")
                .putObject("input").put("name", "Ada");

        AnthropicTurn turn = new AnthropicTurn(content, "tool_use");

        assertThat(turn.isToolUse()).isTrue();
        ToolUseRequest toolUse = turn.firstToolUse().orElseThrow();
        assertThat(toolUse.getId()).isEqualTo("toolu_abc123");
        assertThat(toolUse.getName()).isEqualTo("greetings");
        assertThat(toolUse.getInput().get("name").asText()).isEqualTo("Ada");
    }
}
