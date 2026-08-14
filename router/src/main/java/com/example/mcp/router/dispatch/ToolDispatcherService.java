package com.example.mcp.router.dispatch;

import java.util.Base64;
import java.util.Map;

import com.example.mcp.router.registry.ToolDefinition;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;
import software.amazon.awssdk.services.lambda.model.LambdaException;

/**
 * The dispatcher (Blueprint Section 4.3): synchronously invokes the per-tool compute
 * unit resolved from the registry and returns its raw JSON response as text.
 *
 * <p>The router is deliberately opaque to what's on the other end of {@code compute_ref}
 * - it forwards the {@code tools/call} arguments verbatim as the invoke payload and hands
 * back whatever JSON the tool returns, unparsed. This keeps the tool contract entirely
 * between the MCP client and the tool's own input/output shape; the router never needs
 * updating when a tool's fields change (Blueprint Section 5.5's polyglot-per-tool point
 * applies just as much to same-language tools).
 */
@Service
public class ToolDispatcherService {

    private static final Logger log = LoggerFactory.getLogger(ToolDispatcherService.class);

    private final LambdaClient lambdaClient;
    private final ObjectMapper objectMapper;

    public ToolDispatcherService(LambdaClient lambdaClient, ObjectMapper objectMapper) {
        this.lambdaClient = lambdaClient;
        this.objectMapper = objectMapper;
    }

    public String invoke(ToolDefinition tool, JsonNode arguments, String correlationId) {
        byte[] payload = toPayloadBytes(arguments);
        String clientContext = buildClientContext(correlationId);

        InvokeRequest request = InvokeRequest.builder()
                .functionName(tool.getFunctionName())
                .payload(SdkBytes.fromByteArray(payload))
                .clientContext(clientContext)
                .build();

        log.info("Dispatching tool='{}' function='{}' correlationId='{}'",
                tool.getName(), tool.getFunctionName(), correlationId);

        InvokeResponse response;
        try {
            response = lambdaClient.invoke(request);
        } catch (LambdaException e) {
            throw new ToolInvocationException(
                    "Failed to invoke tool '" + tool.getName() + "': " + e.getMessage(), e);
        }

        String responseBody = response.payload() != null ? response.payload().asUtf8String() : "";

        if (response.functionError() != null) {
            log.warn("Tool '{}' returned functionError='{}' body={}", tool.getName(), response.functionError(), responseBody);
            throw new ToolInvocationException("Tool '" + tool.getName() + "' failed: " + responseBody);
        }

        return responseBody;
    }

    private byte[] toPayloadBytes(JsonNode arguments) {
        try {
            JsonNode effective = (arguments == null || arguments instanceof NullNode)
                    ? objectMapper.createObjectNode()
                    : arguments;
            return objectMapper.writeValueAsBytes(effective);
        } catch (Exception e) {
            throw new ToolInvocationException("Failed to serialize tool arguments", e);
        }
    }

    /**
     * Propagates the correlation ID to the tool Lambda via the AWS-native ClientContext
     * mechanism (visible to the tool as {@code context.getClientContext().getCustom()}),
     * satisfying Blueprint Section 4.2/11's "propagate a correlation ID that stitches the
     * router -> tool hop" without inventing a bespoke header convention.
     */
    private String buildClientContext(String correlationId) {
        try {
            Map<String, Object> clientContext = Map.of("custom", Map.of("correlationId", correlationId));
            byte[] json = objectMapper.writeValueAsBytes(clientContext);
            return Base64.getEncoder().encodeToString(json);
        } catch (Exception e) {
            log.warn("Failed to build clientContext for correlationId='{}': {}", correlationId, e.getMessage());
            return null;
        }
    }
}
