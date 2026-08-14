package com.example.mcp.tools.compoundinterest;

import org.springframework.stereotype.Service;

@Service
public class CompoundInterestService {

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
