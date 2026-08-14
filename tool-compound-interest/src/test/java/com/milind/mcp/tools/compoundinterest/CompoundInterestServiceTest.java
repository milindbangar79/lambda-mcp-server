package com.milind.mcp.tools.compoundinterest;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class CompoundInterestServiceTest {

    private final CompoundInterestService service = new CompoundInterestService();

    @Test
    void defaultsToAnnualCompoundingWhenFrequencyOmitted() {
        CompoundInterestResponse response = service.calculate(request(1000.0, 10.0, 1.0, null));

        // A = 1000 * (1 + 0.10/1)^(1*1) = 1100
        assertThat(response.getCompoundingFrequency()).isEqualTo("ANNUALLY");
        assertThat(response.getTotalAmount()).isCloseTo(1100.0, within(1e-9));
        assertThat(response.getInterest()).isCloseTo(100.0, within(1e-9));
    }

    @Test
    void quarterlyCompoundingMatchesExpectedFormula() {
        CompoundInterestResponse response = service.calculate(request(1000.0, 8.0, 1.0, "QUARTERLY"));

        // A = 1000 * (1 + 0.08/4)^(4*1) = 1000 * 1.02^4
        assertThat(response.getTotalAmount()).isCloseTo(1082.43216, within(1e-4));
    }

    @Test
    void frequencyIsCaseInsensitive() {
        CompoundInterestResponse response = service.calculate(request(1000.0, 8.0, 1.0, "quarterly"));

        assertThat(response.getCompoundingFrequency()).isEqualTo("QUARTERLY");
    }

    @Test
    void rejectsUnknownFrequency() {
        assertThatThrownBy(() -> service.calculate(request(1000.0, 8.0, 1.0, "FORTNIGHTLY")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("compoundingFrequency");
    }

    @Test
    void rejectsNonPositiveTime() {
        assertThatThrownBy(() -> service.calculate(request(1000.0, 8.0, 0.0, "ANNUALLY")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("timeInYears");
    }

    private CompoundInterestRequest request(Double principal, Double rate, Double years, String frequency) {
        CompoundInterestRequest request = new CompoundInterestRequest();
        request.setPrincipal(principal);
        request.setRateOfInterest(rate);
        request.setTimeInYears(years);
        request.setCompoundingFrequency(frequency);
        return request;
    }
}
