package com.dfn.watchdog.client;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Created by dasunp on Nov, 2018
 */
public class SlaMapConfiguration {

    @JsonProperty("defaultTime")
    private DefaultSlaTime defaultSlaTime;

    @JsonProperty("roundTimeMapping")
    private Map<Integer, Long> roundTimes;

    @JsonProperty("serviceList")
    private Map<Integer, String> serviceList;

    public Map<Integer, String> getServiceList() {
        return serviceList;
    }

    public void setServiceList(Map<Integer, String> serviceList) {
        this.serviceList = serviceList;
    }

    public Map<Integer, Long> getRoundTimes() {
        return roundTimes;
    }

    public void setRoundTimes(Map<Integer, Long> roundTimes) {
        this.roundTimes = roundTimes;
    }

    public DefaultSlaTime getDefaultSlaTime() {
        return defaultSlaTime;
    }

    public void setDefaultSlaTime(DefaultSlaTime defaultSlaTime) {
        this.defaultSlaTime = defaultSlaTime;
    }
}
