package com.dfn.watchdog.agent.util;

import com.dfn.watchdog.commons.NodeType;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Watchdog agent properties.
 */
public class AgentProperties {
    @JsonProperty
    private String serverIp;
    @JsonProperty
    private int port;
    @JsonProperty
    private short agentId;
    @JsonProperty
    private NodeType agentType;
    @JsonProperty
    private String baseUrl;
    @JsonProperty
    private boolean active;
    @JsonProperty
    private int reconnectDelay;
    @JsonProperty
    private int readTimeout;
    @JsonProperty
    private int heartbeatDelay;
    @JsonProperty
    private int heartbeatPeriod;
    @JsonProperty
    private boolean secured;
    @JsonProperty
    private int agentThreads;
    @JsonProperty
    private int monitorPeriod;
    @JsonProperty
    private String secondaryServerIp;
    @JsonProperty
    private int secondaryServerPort;

    private String tradeConnectIp;

    private int tradeConnectPort;

    public String serverIp() {
        return serverIp;
    }

    public int port() {
        return port;
    }

    public short agentId() {
        return agentId;
    }

    public NodeType agentType() {
        return agentType;
    }

    public String baseUrl() {
        return baseUrl;
    }

    public boolean active() {
        return active;
    }

    public int reconnectDelay() {
        return reconnectDelay;
    }

    public int readTimeout() {
        return readTimeout;
    }

    public int heartbeatDelay() {
        return heartbeatDelay;
    }

    public int heartbeatPeriod() {
        return heartbeatPeriod;
    }

    public boolean secured() {
        return secured;
    }

    public int agentThreads() {
        return agentThreads;
    }

    public int monitorPeriod() {
        return monitorPeriod;
    }

    public void setAgentId(short agentId) {
        this.agentId = agentId;
    }

    public void setAgentType(String agentType) {
        this.agentType = NodeType.valueOf(agentType);
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void activate(boolean active) {
        this.active = active;
    }

    public String toJson() {
        return  "{\n" + "AGENT_NAME: " + agentType + "-" + agentId + " \n" +
                "BASE_URL: " + baseUrl + " \n" +
                "SERVER_IP: " + serverIp + " \n" +
                "PORT: " + port + " \n" +
                "RECONNECT_DELAY: " + reconnectDelay + " \n" +
                "READ_TIMEOUT: " + readTimeout + " \n" +
                "HEARTBEAT_DELAY: " + heartbeatDelay + " \n" +
                "HEARTBEAT_PERIOD: " + heartbeatPeriod + " \n" +
                "SECURED: " + secured + " \n}";
    }

    @Override
    public String toString() {
        return "\nAGENT_NAME: " + agentType + "-" + agentId + " \n" +
               "BASE_URL: " + baseUrl + " \n" +
               "SERVER: " + serverIp + ":" + port + " \n" +
               "RECONNECT_DELAY: " + reconnectDelay + " \n" +
               "READ_TIMEOUT: " + readTimeout + " \n" +
               "HEARTBEAT_DELAY: " + heartbeatDelay + " \n" +
               "HEARTBEAT_PERIOD: " + heartbeatPeriod + " \n" +
               "SECURED: " + secured + " \n" +
               "SECONDARY SERVER IP: " + secondaryServerIp + " \n" +
               "SECONDARY SERVER PORT: " + secondaryServerPort + " \n";
    }

    public String getTradeConnectIp() {
        return tradeConnectIp;
    }

    public void setTradeConnectIp(String tradeConnectIp) {
        this.tradeConnectIp = tradeConnectIp;
    }

    public int getTradeConnectPort() {
        return tradeConnectPort;
    }

    public void setTradeConnectPort(int tradeConnectPort) {
        this.tradeConnectPort = tradeConnectPort;
    }

    public String secondaryServerIp() {
        return secondaryServerIp;
    }

    public int secondaryServerPort() {
        return secondaryServerPort;
    }
}
