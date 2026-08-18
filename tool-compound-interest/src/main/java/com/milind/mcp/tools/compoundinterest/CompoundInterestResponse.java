package com.milind.mcp.tools.compoundinterest;

/**
 * Result of a "compound-interest-calculator" tool call: echoes the inputs (including the
 * resolved {@code compoundingFrequency}) alongside the computed {@code interest} and
 * {@code totalAmount}, plus the {@code formula} used.
 */
public class CompoundInterestResponse {

    private double principal;
    private double rateOfInterest;
    private double timeInYears;
    private String compoundingFrequency;
    private double interest;
    private double totalAmount;
    private String formula = "A = P(1 + r/n)^(nt)";

    public CompoundInterestResponse() {
    }

    public CompoundInterestResponse(double principal, double rateOfInterest, double timeInYears,
                                     String compoundingFrequency, double interest, double totalAmount) {
        this.principal = principal;
        this.rateOfInterest = rateOfInterest;
        this.timeInYears = timeInYears;
        this.compoundingFrequency = compoundingFrequency;
        this.interest = interest;
        this.totalAmount = totalAmount;
    }

    public double getPrincipal() {
        return principal;
    }

    public void setPrincipal(double principal) {
        this.principal = principal;
    }

    public double getRateOfInterest() {
        return rateOfInterest;
    }

    public void setRateOfInterest(double rateOfInterest) {
        this.rateOfInterest = rateOfInterest;
    }

    public double getTimeInYears() {
        return timeInYears;
    }

    public void setTimeInYears(double timeInYears) {
        this.timeInYears = timeInYears;
    }

    public String getCompoundingFrequency() {
        return compoundingFrequency;
    }

    public void setCompoundingFrequency(String compoundingFrequency) {
        this.compoundingFrequency = compoundingFrequency;
    }

    public double getInterest() {
        return interest;
    }

    public void setInterest(double interest) {
        this.interest = interest;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }
}
