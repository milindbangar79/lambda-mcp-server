package com.milind.mcp.tools.simpleinterest;

/**
 * Input for the "simple-interest-calculator" tool: {@code principal}, the annual
 * {@code rateOfInterest} as a percentage (e.g. {@code 5} for 5%), and {@code timeInYears}.
 * All three are required and must be greater than zero; see {@link SimpleInterestService}
 * for validation.
 */
public class SimpleInterestRequest {

    private Double principal;
    private Double rateOfInterest;
    private Double timeInYears;

    public SimpleInterestRequest() {
    }

    public Double getPrincipal() {
        return principal;
    }

    public void setPrincipal(Double principal) {
        this.principal = principal;
    }

    public Double getRateOfInterest() {
        return rateOfInterest;
    }

    public void setRateOfInterest(Double rateOfInterest) {
        this.rateOfInterest = rateOfInterest;
    }

    public Double getTimeInYears() {
        return timeInYears;
    }

    public void setTimeInYears(Double timeInYears) {
        this.timeInYears = timeInYears;
    }
}
