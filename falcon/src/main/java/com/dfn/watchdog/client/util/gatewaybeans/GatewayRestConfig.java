package com.dfn.watchdog.client.util.gatewaybeans;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by dasunp on Aug, 2018
 * Used to deserialize yaml to a bean.
 */
public class GatewayRestConfig {
    @JsonProperty
    private String host;
    @JsonProperty
    private int port;

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
