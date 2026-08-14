package com.milind.mcp.tools.simpleinterest;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class SimpleInterestServiceTest {

    private final SimpleInterestService service = new SimpleInterestService();

    @Test
    void calculatesSimpleInterestAndTotalAmount() {
        SimpleInterestRequest request = request(1000.0, 5.0, 2.0);

        SimpleInterestResponse response = service.calculate(request);

        // SI = (1000 x 5 x 2) / 100 = 100
        assertThat(response.getInterest()).isCloseTo(100.0, within(1e-9));
        assertThat(response.getTotalAmount()).isCloseTo(1100.0, within(1e-9));
    }

    @Test
    void rejectsNonPositivePrincipal() {
        assertThatThrownBy(() -> service.calculate(request(0.0, 5.0, 2.0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("principal");
    }

    @Test
    void rejectsMissingRate() {
        SimpleInterestRequest request = request(1000.0, null, 2.0);

        assertThatThrownBy(() -> service.calculate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("rateOfInterest");
    }

    @Test
    void rejectsNullRequest() {
        assertThatThrownBy(() -> service.calculate(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private SimpleInterestRequest request(Double principal, Double rate, Double years) {
        SimpleInterestRequest request = new SimpleInterestRequest();
        request.setPrincipal(principal);
        request.setRateOfInterest(rate);
        request.setTimeInYears(years);
        return request;
    }
}
