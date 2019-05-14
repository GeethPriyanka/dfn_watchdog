package com.dfn.watchdog.client.api;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by isurul on 20/3/2017.
 */
public class ClientRoutes {
    long clientId;
    int endPoints;
    Map<String, String> routes;

    public ClientRoutes(long clientId, int endPoints) {
        this.clientId = clientId;
        this.endPoints = endPoints;

        routes = new HashMap<>();
    }

    public int getEndPoints() {
        return endPoints;
    }

    public void setEndPoints(int endPoints) {
        this.endPoints = endPoints;
    }

    public void addRoute(String source, String destination) {
        routes.put(source, destination);
    }

    public long getClientId() {
        return clientId;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    public Map<String, String> getRoutes() {
        return routes;
    }

    public void setRoutes(Map<String, String> routes) {
        this.routes = routes;
    }
}
