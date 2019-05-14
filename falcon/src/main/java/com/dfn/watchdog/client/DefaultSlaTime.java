package com.dfn.watchdog.client;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by dasunp on Nov, 2018
 */
public class DefaultSlaTime {

    @JsonProperty("enabled")
    private boolean enabled;
    @JsonProperty("defaultTime")
    private long defaultTime;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getDefaultTime() {
        return defaultTime;
    }

    public void setDefaultTime(long defaultTime) {
        this.defaultTime = defaultTime;
    }
}
