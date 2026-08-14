package com.milind.mcp.router.registry;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

/**
 * The registry client (Blueprint Section 4.3). In this sample it's a thin read-only
 * wrapper over a hardcoded list of {@link ToolDefinition}s (see
 * {@link com.milind.mcp.router.config.ToolRegistryConfig}); in the production
 * architecture this is where a DynamoDB (or Aurora) lookup would go, keyed by tool
 * name, with the same public contract so {@link com.milind.mcp.router.protocol.McpProtocolService}
 * wouldn't need to change.
 */
@Service
public class ToolRegistryService {

    private final List<ToolDefinition> tools;

    public ToolRegistryService(List<ToolDefinition> tools) {
        this.tools = List.copyOf(tools);
    }

    /** Tools visible to {@code tools/list} - published only, mirroring the router's dispatch gate. */
    public List<ToolDefinition> listPublishedTools() {
        return tools.stream().filter(ToolDefinition::isPublished).toList();
    }

    public Optional<ToolDefinition> findByName(String name) {
        return tools.stream().filter(t -> t.getName().equals(name)).findFirst();
    }
}
