package com.milind.mcp.tools.compoundinterest;

/** Compounding periods per year (n in A = P(1 + r/n)^(nt)). */
public enum CompoundingFrequency {
    ANNUALLY(1),
    SEMI_ANNUALLY(2),
    QUARTERLY(4),
    MONTHLY(12),
    DAILY(365);

    private final int periodsPerYear;

    CompoundingFrequency(int periodsPerYear) {
        this.periodsPerYear = periodsPerYear;
    }

    public int getPeriodsPerYear() {
        return periodsPerYear;
    }

    /**
     * Parses the request's {@code compoundingFrequency} string (case-insensitive),
     * defaulting to {@link #ANNUALLY} when omitted or blank.
     *
     * @throws IllegalArgumentException if {@code value} is non-blank but not one of the enum names
     */
    public static CompoundingFrequency fromRequestValue(String value) {
        if (value == null || value.isBlank()) {
            return ANNUALLY;
        }
        try {
            return CompoundingFrequency.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "'compoundingFrequency' must be one of " + java.util.Arrays.toString(values())
                            + " but was '" + value + "'");
        }
    }
}
