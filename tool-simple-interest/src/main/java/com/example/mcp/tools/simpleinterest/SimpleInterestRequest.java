package com.example.mcp.tools.simpleinterest;

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
