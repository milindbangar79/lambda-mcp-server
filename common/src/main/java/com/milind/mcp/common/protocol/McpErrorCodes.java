package com.milind.mcp.common.protocol;

/**
 * JSON-RPC 2.0 / MCP error codes used by the router.
 *
 * <p>The standard JSON-RPC codes ({@code -32700}..{@code -32600}) come from the
 * JSON-RPC 2.0 spec. The {@code -3200x} range is this server's own reserved space,
 * used for MCP/registry-level failures that aren't strictly protocol errors.
 *
 * <p>{@link #RATE_LIMIT_EXCEEDED} (-32000) is defined here to match the code the
 * Blueprint (Section 8.4 / RL-NFR-07) specifies for throttling responses, but is
 * <b>not raised by this sample</b> — see the README's "Rate limiting" section for
 * why per-tool/per-consumer throttling is out of scope here and left to API Gateway
 * for the global axis only. It's kept as a constant so a future router-level limiter
 * slots in without renegotiating the error contract.
 */
public final class McpErrorCodes {

    public static final int PARSE_ERROR = -32700;
    public static final int INVALID_REQUEST = -32600;
    public static final int METHOD_NOT_FOUND = -32601;
    public static final int INVALID_PARAMS = -32602;
    public static final int INTERNAL_ERROR = -32603;

    /** Reserved by the Blueprint (Section 8.4) for rate-limit rejections. Not used yet. */
    public static final int RATE_LIMIT_EXCEEDED = -32000;

    public static final int TOOL_NOT_FOUND = -32001;
    public static final int TOOL_NOT_PUBLISHED = -32002;

    private McpErrorCodes() {
    }
}
