package com.dfn.watchdog.client.util;

import com.dfn.watchdog.client.database.queues.QueueConfig;
import com.dfn.watchdog.commons.db.DatabaseConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.db.DataSourceFactory;

/**
 * properties in the configuration file watchdog-client.yml.
 */
public class ClientProperties {
    @JsonProperty
    private String serverIp;
    @JsonProperty
    private int port;
    @JsonProperty
    private String host;
    @JsonProperty
    private String websocketUrl;
    @JsonProperty
    private boolean secured;
    @JsonProperty
    private DataSourceFactory standaloneDatabase;
    @JsonProperty
    private boolean enableDatabase;
    @JsonProperty
    private QueueConfig queueConfigs;
    @JsonProperty
    private Boolean elasticSearch;
    @JsonProperty
    private String elasticIp;
    @JsonProperty
    private int elasticPort;
    @JsonProperty
    private DatabaseConfig database;
    @JsonProperty
    private boolean enableQueues;
    @JsonProperty
    private boolean showKibanaDashboard;

    public String serverIp() {
        return serverIp;
    }

    public int port() {
        return port;
    }

    public String host() {
        return host;
    }

    public boolean secured() {
        return secured;
    }

    public boolean elasticSearch() {
        return elasticSearch;
    }

    public String elasticIp() {
        return elasticIp;
    }

    public int elasticPort() {
        return elasticPort;
    }

    public DatabaseConfig database() {
        return database;
    }

    public String websocketUrl() {
        return websocketUrl;
    }

    public DataSourceFactory getStandaloneDatabase() {
        return standaloneDatabase;
    }

    public void setStandaloneDatabase(DataSourceFactory standaloneDatabase) {
        this.standaloneDatabase = standaloneDatabase;
    }

    public boolean isEnableDatabase() {
        return enableDatabase;
    }

    public void setEnableDatabase(boolean enableDatabase) {
        this.enableDatabase = enableDatabase;
    }

    public QueueConfig getQueueCounts() {
        return queueConfigs;
    }

    public void setQueueCounts(QueueConfig queueConfig) {
        this.queueConfigs = queueConfig;
    }

    public boolean isEnableQueues() {
        return enableQueues;
    }

    public void setEnableQueues(boolean enableQueues) {
        this.enableQueues = enableQueues;
    }

    public boolean isShowKibanaDashboard() {
        return showKibanaDashboard;
    }

    public void setShowKibanaDashboard(boolean showKibanaDashboard) {
        this.showKibanaDashboard = showKibanaDashboard;
    }
}
