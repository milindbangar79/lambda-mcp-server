package com.example.mcp.router;

import java.util.Map;
import java.util.Optional;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.example.mcp.common.protocol.JsonRpcRequest;
import com.example.mcp.common.protocol.JsonRpcResponse;
import com.example.mcp.common.protocol.McpErrorCodes;
import com.example.mcp.router.protocol.McpProtocolService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * The lambda-mcp-server entry point: API Gateway's Lambda-proxy integration invokes this
 * for every request to the MCP endpoint (a single POST route, per Blueprint Section 8.1 -
 * MCP multiplexes every JSON-RPC method through one route).
 *
 * <p>The Spring context is built once in the constructor, which the Lambda Java runtime
 * only calls on a cold start; warm invocations reuse this same instance (and therefore
 * the same context), which is the whole point of not booting Spring per-request.
 */
public class McpRouterHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Logger log = LoggerFactory.getLogger(McpRouterHandler.class);
    private static final Map<String, String> RESPONSE_HEADERS = Map.of(
            "Content-Type", "application/json",
            "Access-Control-Allow-Origin", "*"
    );

    private final ConfigurableApplicationContext applicationContext;
    private final McpProtocolService protocolService;
    private final ObjectMapper objectMapper;

    public McpRouterHandler() {
        this.applicationContext = new SpringApplicationBuilder(RouterApplication.class)
                .web(WebApplicationType.NONE)
                .bannerMode(Banner.Mode.OFF)
                .run();
        this.protocolService = applicationContext.getBean(McpProtocolService.class);
        this.objectMapper = applicationContext.getBean(ObjectMapper.class);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        // The router's own request ID doubles as the correlation ID stitched into every
        // downstream tool invocation (Blueprint Section 4.2/11) - no separate ID scheme needed.
        String correlationId = context.getAwsRequestId();
        MDC.put("correlationId", correlationId);

        try {
            String body = event.getBody();
            log.info("Received MCP request correlationId='{}' bodyLength={}", correlationId, body == null ? 0 : body.length());

            JsonRpcRequest request;
            try {
                request = objectMapper.readValue(body == null ? "{}" : body, JsonRpcRequest.class);
            } catch (Exception e) {
                log.warn("Failed to parse JSON-RPC request correlationId='{}': {}", correlationId, e.getMessage());
                JsonRpcResponse parseError = JsonRpcResponse.failure(null, McpErrorCodes.PARSE_ERROR, "Invalid JSON-RPC request body");
                return respond(200, parseError);
            }

            Optional<JsonRpcResponse> response = protocolService.handle(request, correlationId);

            // A JSON-RPC notification (no id) gets no body - 202 Accepted signals "received,
            // nothing to return" without implying a JSON-RPC result was produced.
            return response.map(r -> respond(200, r)).orElseGet(() -> emptyResponse(202));

        } catch (Exception e) {
            log.error("Unhandled router failure correlationId='{}'", correlationId, e);
            JsonRpcResponse internalError = JsonRpcResponse.failure(null, McpErrorCodes.INTERNAL_ERROR, "Internal server error");
            return respond(500, internalError);
        } finally {
            MDC.remove("correlationId");
        }
    }

    private APIGatewayProxyResponseEvent respond(int statusCode, JsonRpcResponse body) {
        try {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(statusCode)
                    .withHeaders(RESPONSE_HEADERS)
                    .withBody(objectMapper.writeValueAsString(body));
        } catch (Exception e) {
            log.error("Failed to serialize response", e);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withHeaders(RESPONSE_HEADERS)
                    .withBody("{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32603,\"message\":\"Internal server error\"}}");
        }
    }

    private APIGatewayProxyResponseEvent emptyResponse(int statusCode) {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(RESPONSE_HEADERS);
    }
}
