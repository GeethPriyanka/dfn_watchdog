package com.dfn.watchdog.commons.messages.client;

/**
 * Route request from watchdog client.
 */
public class ClientRouteRequest extends AsyncRequest {
    private long clientId;

    public ClientRouteRequest(long clientId) {
        super();
        this.clientId = clientId;
        response = new RouteResponseAggregated(requestId, clientId);
    }

    public long getClientId() {
        return clientId;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof AsyncRequest && ((AsyncRequest) o).getRequestId() == requestId;
    }

    @Override
    public int hashCode() {
        return requestId;
    }
}
