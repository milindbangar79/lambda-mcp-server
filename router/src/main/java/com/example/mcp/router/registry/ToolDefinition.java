package com.example.mcp.router.registry;

import java.util.Map;

/**
 * A registry record, modeled after Blueprint Section 4.1's schema. This sample hardcodes
 * three of these in {@link com.example.mcp.router.config.ToolRegistryConfig} instead of
 * reading them from DynamoDB — see the README for why, and what a production rollout
 * would change. Fields kept for governance parity ({@code owningTeam}, {@code gateStatus},
 * {@code trustScore}, {@code rateLimitTier}, {@code concurrencyBudget}) are carried through
 * but not enforced by this router; only {@code gateStatus} gates dispatch.
 */
public class ToolDefinition {

    /** Unique key; must match the MCP tool name exposed to clients. */
    private final String name;
    /** Semantic version of the tool's contract. */
    private final String version;
    private final String description;
    private final Map<String, Object> inputSchema;
    /** compute_ref: the backing Lambda's function name (used as the AWS SDK invoke target). */
    private final String functionName;
    private final String owningTeam;
    /** Gate 0-6 publication status. The router refuses to dispatch unless this is "published". */
    private final String gateStatus;
    private final double trustScore;
    /** cheap / standard / expensive / restricted - see Blueprint Sections 8-9. Informational only here. */
    private final String rateLimitTier;
    /** Reserved concurrency budget. Informational only here; not enforced by this sample. */
    private final int concurrencyBudget;

    public ToolDefinition(String name, String version, String description, Map<String, Object> inputSchema,
                           String functionName, String owningTeam, String gateStatus, double trustScore,
                           String rateLimitTier, int concurrencyBudget) {
        this.name = name;
        this.version = version;
        this.description = description;
        this.inputSchema = inputSchema;
        this.functionName = functionName;
        this.owningTeam = owningTeam;
        this.gateStatus = gateStatus;
        this.trustScore = trustScore;
        this.rateLimitTier = rateLimitTier;
        this.concurrencyBudget = concurrencyBudget;
    }

    public boolean isPublished() {
        return "published".equalsIgnoreCase(gateStatus);
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Object> getInputSchema() {
        return inputSchema;
    }

    public String getFunctionName() {
        return functionName;
    }

    public String getOwningTeam() {
        return owningTeam;
    }

    public String getGateStatus() {
        return gateStatus;
    }

    public double getTrustScore() {
        return trustScore;
    }

    public String getRateLimitTier() {
        return rateLimitTier;
    }

    public int getConcurrencyBudget() {
        return concurrencyBudget;
    }
}
