package com.dfn.watchdog.commons.messages.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregated route response from all gateway components.
 */
public class RouteResponseAggregated extends AsyncResponse {
    private int endPoints;
    private long clientId;
    private List<ClientRouteResponse> clientRouteResponseList;
    private Map<String, String> routes;

    RouteResponseAggregated(int requestId, long clientId) {
        super(requestId, false);
        this.clientId = clientId;
        clientRouteResponseList = new ArrayList<>();
        routes = new HashMap<>();
    }

    public void addRouteResponse(ClientRouteResponse response) {
        clientRouteResponseList.add(response);
    }

    public List<ClientRouteResponse> getClientRouteResponseList() {
        return clientRouteResponseList;
    }

    public void addResponse(ClientRouteResponse response) {
        clientRouteResponseList.add(response);
        routes.put(response.getSource(), response.getRoute());
    }

    public int getEndPoints() {
        return endPoints;
    }

    public void setEndPoints(int endPoints) {
        this.endPoints = endPoints;
    }

    public long getClientId() {
        return clientId;
    }
}
