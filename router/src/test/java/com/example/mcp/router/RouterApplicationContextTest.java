package com.example.mcp.router;

import java.util.List;
import java.util.Optional;

import com.example.mcp.common.model.ToolsListResult;
import com.example.mcp.common.protocol.JsonRpcRequest;
import com.example.mcp.common.protocol.JsonRpcResponse;
import com.example.mcp.router.protocol.McpProtocolService;
import com.example.mcp.router.registry.ToolDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Boots the same Spring context {@link McpRouterHandler} builds in production (minus the
 * Lambda-specific env vars, so compute_ref falls back to the config defaults) and drives
 * it through the wire-format JSON-RPC entry point, to catch bean-wiring mistakes that
 * mocked unit tests can't see.
 */
class RouterApplicationContextTest {

    @BeforeAll
    static void setAwsRegionForLocalTest() {
        // The real Lambda runtime always injects AWS_REGION; outside of it (e.g. this test
        // run) the SDK's default region provider chain has nothing to resolve against, so
        // the LambdaClient bean fails to construct without this. Production is unaffected.
        System.setProperty("aws.region", "us-east-1");
    }

    @Test
    void contextBootsAndRegistersAllThreeTools() {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(RouterApplication.class)
                .web(WebApplicationType.NONE)
                .run()) {

            List<ToolDefinition> registry = context.getBean("mcpToolRegistry", List.class);
            assertThat(registry).extracting(t -> ((ToolDefinition) t).getName())
                    .containsExactlyInAnyOrder("greetings", "simple-interest-calculator", "compound-interest-calculator");
        }
    }

    @Test
    void toolsListOverTheWireReturnsAllPublishedTools() throws Exception {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(RouterApplication.class)
                .web(WebApplicationType.NONE)
                .run()) {

            McpProtocolService protocolService = context.getBean(McpProtocolService.class);
            ObjectMapper objectMapper = context.getBean(ObjectMapper.class);

            JsonRpcRequest request = objectMapper.readValue(
                    "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/list\"}", JsonRpcRequest.class);

            Optional<JsonRpcResponse> response = protocolService.handle(request, "test-correlation-id");

            assertThat(response).isPresent();
            ToolsListResult result = objectMapper.convertValue(response.get().getResult(), ToolsListResult.class);
            assertThat(result.getTools()).hasSize(3);
        }
    }
}
