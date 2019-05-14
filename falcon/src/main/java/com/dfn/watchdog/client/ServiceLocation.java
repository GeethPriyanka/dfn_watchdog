package com.dfn.watchdog.client;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Service routing locations.
 */
public class ServiceLocation {
    private int id;
    @JsonProperty("locationName")
    private String name;
    private String server;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getServer() {
        return server;
    }
}
