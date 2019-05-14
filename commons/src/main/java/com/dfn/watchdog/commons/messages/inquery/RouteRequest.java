package com.dfn.watchdog.commons.messages.inquery;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Route request to get next node to route.
 */
public class RouteRequest implements RouteInquiry {
    private final long clientId;
    private final transient CountDownLatch latch;
    private transient RouteResponse response;
    private String nodeType;

    public RouteRequest(long clientId) {
        this.clientId = clientId;
        latch = new CountDownLatch(1);
        response = new RouteResponse(clientId, (short) 0);
    }

    public long getClientId() {
        return clientId;
    }

    public void addResponse(RouteResponse response) {
        this.response = response;
        latch.countDown();
    }

    public RouteResponse getResponse() {
        return response;
    }

    public boolean waitForResponse() throws InterruptedException {
        return latch.await(2, TimeUnit.SECONDS);
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
        if (response != null) {
            response.setNodeType(nodeType);
        }
    }
}
