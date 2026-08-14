package com.milind.mcp.common.model;

import java.util.Map;

/** Result payload for the {@code initialize} JSON-RPC method. */
public class InitializeResult {

    private String protocolVersion;
    private Map<String, Object> capabilities;
    private ServerInfo serverInfo;

    public InitializeResult() {
    }

    public InitializeResult(String protocolVersion, Map<String, Object> capabilities, ServerInfo serverInfo) {
        this.protocolVersion = protocolVersion;
        this.capabilities = capabilities;
        this.serverInfo = serverInfo;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public Map<String, Object> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Map<String, Object> capabilities) {
        this.capabilities = capabilities;
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public void setServerInfo(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }
}
