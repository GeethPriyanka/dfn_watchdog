package com.dfn.watchdog.client.util.sla;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by dasunp on Nov, 2018
 */
public class RoundTimeConfiguration {
    @JsonProperty("messageType")
    private int messageType;
    @JsonProperty("time")
    private long time;

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
