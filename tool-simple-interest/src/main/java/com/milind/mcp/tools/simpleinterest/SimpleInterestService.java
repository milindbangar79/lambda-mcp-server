package com.milind.mcp.tools.simpleinterest;

import org.springframework.stereotype.Service;

/**
 * The "simple-interest-calculator" tool's business logic: SI = (P x R x T) / 100,
 * with total amount = principal + interest.
 */
@Service
public class SimpleInterestService {

    /**
     * @param request must have {@code principal}, {@code rateOfInterest}, and
     *                {@code timeInYears} all present and greater than zero
     * @return the computed interest, total amount, and formula used
     * @throws IllegalArgumentException if {@code request} is {@code null} or any
     *                                  required field is missing/non-positive
     */
    public SimpleInterestResponse calculate(SimpleInterestRequest request) {
        validate(request);

        double principal = request.getPrincipal();
        double rate = request.getRateOfInterest();
        double time = request.getTimeInYears();

        double interest = (principal * rate * time) / 100.0;
        double totalAmount = principal + interest;

        return new SimpleInterestResponse(principal, rate, time, interest, totalAmount);
    }

    private void validate(SimpleInterestRequest request) {
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
