package com.example.mcp.tools.compoundinterest;

public class CompoundInterestRequest {

    private Double principal;
    private Double rateOfInterest;
    private Double timeInYears;
    /** One of ANNUALLY / SEMI_ANNUALLY / QUARTERLY / MONTHLY / DAILY. Defaults to ANNUALLY. */
    private String compoundingFrequency;

    public CompoundInterestRequest() {
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

    public String getCompoundingFrequency() {
        return compoundingFrequency;
    }

    public void setCompoundingFrequency(String compoundingFrequency) {
        this.compoundingFrequency = compoundingFrequency;
    }
}
