package com.dfn.watchdog.client.database.queues;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by dasunp on Nov, 2018
 * Used as a POJO container to carry the queue counts.
 */
public class QueueConfig {

    @JsonProperty
    private int sessionQueueCount;
    @JsonProperty
    private int messageQueueCount;
    @JsonProperty
    private int responseQueueCount;
    @JsonProperty
    private int sessionQueueTimeout;
    @JsonProperty
    private int messageQueueTimeout;
    @JsonProperty
    private int responseQueueTimeout;

    public QueueConfig() {
        // default constructor.
    }

    public int getSessionQueueCount() {
        return sessionQueueCount;
    }

    public void setSessionQueueCount(int sessionQueueCount) {
        this.sessionQueueCount = sessionQueueCount;
    }

    public int getMessageQueueCount() {
        return messageQueueCount;
    }

    public void setMessageQueueCount(int messageQueueCount) {
        this.messageQueueCount = messageQueueCount;
    }

    public int getResponseQueueCount() {
        return responseQueueCount;
    }

    public void setResponseQueueCount(int responseQueueCount) {
        this.responseQueueCount = responseQueueCount;
    }

    public int getSessionQueueTimeout() {
        return sessionQueueTimeout;
    }

    public void setSessionQueueTimeout(int sessionQueueTimeout) {
        this.sessionQueueTimeout = sessionQueueTimeout;
    }

    public int getMessageQueueTimeout() {
        return messageQueueTimeout;
    }

    public void setMessageQueueTimeout(int messageQueueTimeout) {
        this.messageQueueTimeout = messageQueueTimeout;
    }

    public int getResponseQueueTimeout() {
        return responseQueueTimeout;
    }

    public void setResponseQueueTimeout(int responseQueueTimeout) {
        this.responseQueueTimeout = responseQueueTimeout;
    }
}
