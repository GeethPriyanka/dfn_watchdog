package com.dfn.watchdog.client.util.sla;

import com.dfn.watchdog.client.WatchdogClient;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by dasunp on Nov, 2018
 */
public class SlaConfiguration {

    @JsonProperty("roundTimes")
    private List<RoundTimeConfiguration> roundTimeConfigurations;


    public List<RoundTimeConfiguration> getRoundTimeConfigurations() {
        return roundTimeConfigurations;
    }

    public void setRoundTimeConfigurations(List<RoundTimeConfiguration> roundTimeConfigurations) {
        this.roundTimeConfigurations = roundTimeConfigurations;
    }
}
