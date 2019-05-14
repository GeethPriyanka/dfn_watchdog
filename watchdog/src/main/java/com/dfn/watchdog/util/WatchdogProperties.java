package com.dfn.watchdog.util;

import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.db.DatabaseConfig;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Server properties.
 * Loaded from the YAML file. Some can be changed in code before server startup.
 */
public class WatchdogProperties {
    @JsonProperty
    private String host;
    @JsonProperty
    private int port;
    @JsonProperty
    private int heartbeatDelay;
    @JsonProperty
    private int heartbeatPeriod;
    @JsonProperty
    private int readTimeout;
    @JsonProperty
    private int nodeTimeout;
    @JsonProperty
    private int reconnectDelay;
    @JsonProperty
    private boolean secured;
    @JsonProperty
    private int clientPort;
    @JsonProperty
    private Map<NodeType, Integer> cluster;
    @JsonProperty
    private DatabaseConfig database;
    @JsonProperty
    private String secondaryServerIp;
    @JsonProperty
    private int secondaryServerPort;
    @JsonProperty
    private boolean primary;

    @JsonProperty
    private boolean activePassiveEnabled;

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public int reconnectDelay() {
        return reconnectDelay;
    }

    public int readTimeout() {
        return readTimeout;
    }

    public int nodeTimeout() {
        return nodeTimeout;
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

    public Map<NodeType, Integer> clusterConfig() {
        return cluster;
    }

    public Integer clusterConfig(NodeType type) {
        return cluster.get(type);
    }

    public int clientPort() {
        return clientPort;
    }

    public DatabaseConfig database() {
        return database;
    }

    public String prettyPrint() {
        return "\n{ \n" +
                "\t" + "HOST: " + host + "\n" +
                "\t" + "PORT: " + port + "\n" +
                "\t" + "RECONNECT_DELAY: " + reconnectDelay + "\n" +
                "\t" + "READ_TIMEOUT: " + readTimeout + "\n" +
                "\t" + "HEARTBEAT_DELAY: " + heartbeatDelay + "\n" +
                "\t" + "HEARTBEAT_PERIOD: " + heartbeatPeriod + "\n" +
                "\t" + "SECURED: " + secured + "\n" +
                "\t" + "MONITOR_PORT: " + clientPort + "\n" +
                "\t" + "DEFAULT CLUSTER: " + cluster + "\n" +
                "\t" + "SECONDARY SERVER IP: " + secondaryServerIp + "\n" +
                "\t" + "SECONDARY SERVER PORT: " + secondaryServerPort + "\n" +
                "\t" + "PRIMARY: " + primary + "\n" +
                "}\n";
    }

    public String getSecondaryServerIp() {
        return secondaryServerIp;
    }

    public int getSecondaryServerPort() {
        return secondaryServerPort;
    }

    public boolean Primary() {
        return primary;
    }

    public boolean isActivePassiveEnabled() {
        return activePassiveEnabled;
    }

    public void setActivePassiveEnabled(boolean activePassiveEnabled) {
        this.activePassiveEnabled = activePassiveEnabled;
    }
}
