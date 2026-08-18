package com.milind.mcp.router.registry;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ToolRegistryService}: that {@code gateStatus} correctly gates
 * {@code tools/list} visibility while {@code findByName} lookup (used by the dispatch
 * path) stays gate-status-agnostic, since the router - not the registry - is what
 * decides whether an unpublished tool may be dispatched to.
 */
class ToolRegistryServiceTest {

    private final ToolDefinition published = new ToolDefinition(
            "greetings", "1.0.0", "desc", Map.of(), "mcp-tool-greetings",
            "platform-samples", "published", 1.0, "cheap", 5);
    private final ToolDefinition draft = new ToolDefinition(
            "draft-tool", "0.1.0", "desc", Map.of(), "mcp-tool-draft",
            "platform-samples", "draft", 0.0, "cheap", 5);

    private final ToolRegistryService service = new ToolRegistryService(List.of(published, draft));

    @Test
    void listPublishedToolsExcludesNonPublishedEntries() {
        List<ToolDefinition> result = service.listPublishedTools();

        assertThat(result).containsExactly(published);
    }

    @Test
    void findByNameLocatesToolRegardlessOfGateStatus() {
        Optional<ToolDefinition> found = service.findByName("draft-tool");

        assertThat(found).isPresent();
        assertThat(found.get().isPublished()).isFalse();
    }

    @Test
    void findByNameReturnsEmptyForUnknownTool() {
        assertThat(service.findByName("does-not-exist")).isEmpty();
    }
}
