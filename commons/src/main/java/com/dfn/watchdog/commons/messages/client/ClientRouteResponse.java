package com.dfn.watchdog.commons.messages.client;

/**
 * Created by isurul on 17/3/2017.
 */
public class ClientRouteResponse extends AsyncResponse {
    private long clientId;
    private String source;
    private String route;
    private int nodes;

    public ClientRouteResponse(int requestId, boolean initial) {
        super(requestId, initial);
    }

    public long getClientId() {
        return clientId;
    }

    public String getSource() {
        return source;
    }

    public String getRoute() {
        return route;
    }

    public ClientRouteResponse setClientId(long clientId) {
        this.clientId = clientId;
        return this;
    }

    public ClientRouteResponse setSource(String source) {
        this.source = source;
        return this;
    }

    public ClientRouteResponse setRoute(String route) {
        this.route = route;
        return this;
    }

    public int getNodes() {
        return nodes;
    }

    public ClientRouteResponse setNodes(int nodes) {
        this.nodes = nodes;
        return this;
    }
}
