package com.milind.mcp.tools.compoundinterest;

import org.springframework.stereotype.Service;

/**
 * The "compound-interest-calculator" tool's business logic: A = P(1 + r/n)^(nt), where
 * n is the compounding periods per year resolved from {@link CompoundingFrequency}.
 */
@Service
public class CompoundInterestService {

    /**
     * @param request must have {@code principal}, {@code rateOfInterest}, and
     *                {@code timeInYears} all present and greater than zero;
     *                {@code compoundingFrequency} is optional (defaults to ANNUALLY)
     * @return the computed interest, total amount, resolved compounding frequency, and formula used
     * @throws IllegalArgumentException if {@code request} is {@code null}, a required field is
     *                                  missing/non-positive, or {@code compoundingFrequency} is unrecognized
     */
    public CompoundInterestResponse calculate(CompoundInterestRequest request) {
        validate(request);

        double principal = request.getPrincipal();
        double ratePercent = request.getRateOfInterest();
        double time = request.getTimeInYears();
        CompoundingFrequency frequency = CompoundingFrequency.fromRequestValue(request.getCompoundingFrequency());
        int n = frequency.getPeriodsPerYear();

        double ratePerPeriod = (ratePercent / 100.0) / n;
        double totalAmount = principal * Math.pow(1 + ratePerPeriod, n * time);
        double interest = totalAmount - principal;

        return new CompoundInterestResponse(principal, ratePercent, time, frequency.name(), interest, totalAmount);
    }

    private void validate(CompoundInterestRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        requirePositive(request.getPrincipal(), "principal");
        requirePositive(request.getRateOfInterest(), "rateOfInterest");
        requirePositive(request.getTimeInYears(), "timeInYears");
    }

    private void requirePositive(Double value, String field) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("'" + field + "' is required and must be greater than 0");
        }
    }
}
