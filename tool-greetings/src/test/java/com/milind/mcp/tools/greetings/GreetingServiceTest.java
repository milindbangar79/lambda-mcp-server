package com.milind.mcp.tools.greetings;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Unit tests for {@link GreetingService}: name trimming/formatting and blank/null-name validation. */
class GreetingServiceTest {

    private final GreetingService service = new GreetingService();

    @Test
    void greetsWithTrimmedName() {
        GreetingRequest request = new GreetingRequest();
        request.setName("  Ada  ");

        GreetingResponse response = service.greet(request);

        assertThat(response.getMessage()).isEqualTo("Hello, Ada! Welcome to the MCP Lambda server.");
    }

    @Test
    void rejectsBlankName() {
        GreetingRequest request = new GreetingRequest();
        request.setName("   ");

        assertThatThrownBy(() -> service.greet(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name");
    }

    @Test
    void rejectsNullRequest() {
        assertThatThrownBy(() -> service.greet(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
