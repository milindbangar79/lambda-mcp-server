package com.milind.mcp.tools.simpleinterest;

/**
 * Result of a "simple-interest-calculator" tool call: echoes the inputs back alongside
 * the computed {@code interest} and {@code totalAmount} (principal + interest), plus the
 * {@code formula} used, so a client can display the calculation without hardcoding it.
 */
public class SimpleInterestResponse {

    private double principal;
    private double rateOfInterest;
    private double timeInYears;
    private double interest;
    private double totalAmount;
    private String formula = "SI = (P x R x T) / 100";

    public SimpleInterestResponse() {
    }

    public SimpleInterestResponse(double principal, double rateOfInterest, double timeInYears,
                                   double interest, double totalAmount) {
        this.principal = principal;
        this.rateOfInterest = rateOfInterest;
        this.timeInYears = timeInYears;
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
