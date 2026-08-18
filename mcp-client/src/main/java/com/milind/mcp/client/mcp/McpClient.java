package com.milind.mcp.client.mcp;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.milind.mcp.client.http.HttpTransport;
import com.milind.mcp.common.model.InitializeResult;
import com.milind.mcp.common.model.McpTool;
import com.milind.mcp.common.model.ToolCallResult;
import com.milind.mcp.common.model.ToolsListResult;
import com.milind.mcp.common.protocol.JsonRpcRequest;
import com.milind.mcp.common.protocol.JsonRpcResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The JSON-RPC/MCP side of this client: speaks to the deployed router over its public
 * API Gateway endpoint - the same {@code POST /mcp} route the README's curl examples
 * hit - using the same {@code common} model classes the router itself uses to build its
 * responses. Reuses that module rather than re-deriving the wire format independently,
 * which is also what keeps this client honest to the actual protocol instead of a
 * remembered approximation of it.
 */
public class McpClient {

    private final String serverUrl;
    private final HttpTransport httpTransport;
    private final ObjectMapper objectMapper;
    private final AtomicInteger requestId = new AtomicInteger(1);

    public McpClient(String serverUrl, HttpTransport httpTransport, ObjectMapper objectMapper) {
        this.serverUrl = serverUrl;
        this.httpTransport = httpTransport;
        this.objectMapper = objectMapper;
    }

    /** The MCP handshake. This router treats it statelessly (echoes the requested protocol
     * version back), but a real client performs it before any other call regardless. */
    public InitializeResult initialize() {
        ObjectNode params = objectMapper.createObjectNode();
        params.put("protocolVersion", "2024-11-05");
        JsonRpcResponse response = send("initialize", params);
        return objectMapper.convertValue(response.getResult(), InitializeResult.class);
    }

    /** Tool discovery: everything the registry currently has {@code gateStatus: "published"} for. */
    public List<McpTool> listTools() {
        JsonRpcResponse response = send("tools/list", null);
        ToolsListResult result = objectMapper.convertValue(response.getResult(), ToolsListResult.class);
        return result.getTools();
    }

    /** Invokes one tool by name with the arguments Claude resolved for it. */
    public ToolCallResult callTool(String name, JsonNode arguments) {
        ObjectNode params = objectMapper.createObjectNode();
        params.put("name", name);
        params.set("arguments", arguments == null ? objectMapper.createObjectNode() : arguments);
        JsonRpcResponse response = send("tools/call", params);
        return objectMapper.convertValue(response.getResult(), ToolCallResult.class);
    }

    private JsonRpcResponse send(String method, JsonNode params) {
        JsonRpcRequest request = new JsonRpcRequest();
        request.setId(requestId.getAndIncrement());
        request.setMethod(method);
        request.setParams(params);

        String requestBody;
        String responseBody;
        try {
            requestBody = objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            throw new McpClientException(-32603, "Failed to serialize request: " + e.getMessage());
        }

        responseBody = httpTransport.postJson(serverUrl, requestBody, Map.of());

        JsonRpcResponse response;
        try {
            response = objectMapper.readValue(responseBody, JsonRpcResponse.class);
        } catch (Exception e) {
            throw new McpClientException(-32700, "Failed to parse router response: " + e.getMessage());
        }

        if (response.getError() != null) {
            throw new McpClientException(response.getError().getCode(), response.getError().getMessage());
        }
        return response;
    }
}
