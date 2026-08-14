package com.example.mcp.router.protocol;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.mcp.common.model.InitializeResult;
import com.example.mcp.common.model.McpTool;
import com.example.mcp.common.model.ServerInfo;
import com.example.mcp.common.model.ToolCallParams;
import com.example.mcp.common.model.ToolCallResult;
import com.example.mcp.common.model.ToolsListResult;
import com.example.mcp.common.protocol.JsonRpcError;
import com.example.mcp.common.protocol.JsonRpcRequest;
import com.example.mcp.common.protocol.JsonRpcResponse;
import com.example.mcp.common.protocol.McpErrorCodes;
import com.example.mcp.router.dispatch.ToolDispatcherService;
import com.example.mcp.router.dispatch.ToolInvocationException;
import com.example.mcp.router.registry.ToolDefinition;
import com.example.mcp.router.registry.ToolRegistryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * The protocol handler (Blueprint Section 4.3): parses/validates the JSON-RPC envelope
 * and implements the MCP methods this server supports - {@code initialize},
 * {@code tools/list}, {@code tools/call}. {@code resources/*} and anything else falls
 * through to {@code METHOD_NOT_FOUND}, since this sample only hosts tools.
 *
 * <p>This class deliberately does not do rate limiting - see the README for why
 * throttling in this sample lives entirely at API Gateway (the global axis only, per
 * Blueprint Section 8.3) rather than as a router-owned token bucket.
 */
@Service
public class McpProtocolService {

    private static final Logger log = LoggerFactory.getLogger(McpProtocolService.class);

    private static final String SERVER_NAME = "lambda-mcp-server";
    private static final String SERVER_VERSION = "1.0.0";
    private static final String DEFAULT_PROTOCOL_VERSION = "2024-11-05";

    private final ToolRegistryService toolRegistryService;
    private final ToolDispatcherService toolDispatcherService;
    private final ObjectMapper objectMapper;

    public McpProtocolService(ToolRegistryService toolRegistryService,
                               ToolDispatcherService toolDispatcherService,
                               ObjectMapper objectMapper) {
        this.toolRegistryService = toolRegistryService;
        this.toolDispatcherService = toolDispatcherService;
        this.objectMapper = objectMapper;
    }

    /**
     * @return empty when the request is a JSON-RPC notification (no {@code id}) - callers
     * must not write an HTTP body in that case, per the JSON-RPC 2.0 spec.
     */
    public Optional<JsonRpcResponse> handle(JsonRpcRequest request, String correlationId) {
        if (request.isNotification()) {
            log.info("Ignoring notification method='{}' correlationId='{}'", request.getMethod(), correlationId);
            return Optional.empty();
        }

        Object id = request.getId();
        String method = request.getMethod();

        try {
            Object result = switch (method == null ? "" : method) {
                case "initialize" -> handleInitialize(request.getParams());
                case "tools/list" -> handleToolsList();
                case "tools/call" -> handleToolsCall(request.getParams(), correlationId);
                default -> null;
            };

            if (result == null) {
                return Optional.of(JsonRpcResponse.failure(id, McpErrorCodes.METHOD_NOT_FOUND,
                        "Method not found: " + method));
            }

            return Optional.of(JsonRpcResponse.success(id, result));

        } catch (McpProtocolException e) {
            return Optional.of(JsonRpcResponse.failure(id, e.getCode(), e.getMessage()));
        } catch (Exception e) {
            log.error("Unhandled error processing method='{}' correlationId='{}'", method, correlationId, e);
            return Optional.of(JsonRpcResponse.failure(id, McpErrorCodes.INTERNAL_ERROR, "Internal server error"));
        }
    }

    private InitializeResult handleInitialize(JsonNode params) {
        String requestedVersion = (params != null && params.hasNonNull("protocolVersion"))
                ? params.get("protocolVersion").asText()
                : DEFAULT_PROTOCOL_VERSION;

        Map<String, Object> capabilities = Map.of(
                "tools", Map.of("listChanged", false)
        );

        return new InitializeResult(requestedVersion, capabilities, new ServerInfo(SERVER_NAME, SERVER_VERSION));
    }

    private ToolsListResult handleToolsList() {
        List<McpTool> tools = toolRegistryService.listPublishedTools().stream()
                .map(t -> new McpTool(t.getName(), t.getDescription(), t.getInputSchema()))
                .toList();
        return new ToolsListResult(tools);
    }

    private ToolCallResult handleToolsCall(JsonNode params, String correlationId) {
        if (params == null || !params.hasNonNull("name")) {
            throw new McpProtocolException(McpErrorCodes.INVALID_PARAMS, "tools/call requires a 'name' parameter");
        }

        ToolCallParams callParams = objectMapper.convertValue(params, ToolCallParams.class);

        ToolDefinition tool = toolRegistryService.findByName(callParams.getName())
                .orElseThrow(() -> new McpProtocolException(McpErrorCodes.TOOL_NOT_FOUND,
                        "Unknown tool: " + callParams.getName()));

        if (!tool.isPublished()) {
            throw new McpProtocolException(McpErrorCodes.TOOL_NOT_PUBLISHED,
                    "Tool '" + tool.getName() + "' is not published (gateStatus=" + tool.getGateStatus() + ")");
        }

        try {
            String rawResult = toolDispatcherService.invoke(tool, callParams.getArguments(), correlationId);
            return ToolCallResult.ok(rawResult);
        } catch (ToolInvocationException e) {
            // Execution failures are MCP-level tool errors, not JSON-RPC protocol errors -
            // they come back as a normal success envelope with isError=true so the client
            // sees the tool's own error text rather than a generic transport failure.
            return ToolCallResult.error(e.getMessage());
        }
    }

    /** Internal signal for a JSON-RPC-level failure (as opposed to a tool execution failure). */
    private static class McpProtocolException extends RuntimeException {
        private final int code;

        McpProtocolException(int code, String message) {
            super(message);
            this.code = code;
        }

        int getCode() {
            return code;
        }
    }
}
