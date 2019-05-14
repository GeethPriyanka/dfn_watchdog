package com.dfn.watchdog.commons.messages.inquery;

/**
 * Notify the server if customer logged on to the system.
 */
public class CustomerLogin implements CustomerListMessage {
    private final long customerId;
    private final short nodeId;

    /**
     * @param customerId Logged in customer
     * @param nodeId Current agent node
     */
    public CustomerLogin(long customerId, short nodeId) {
        this.customerId = customerId;
        this.nodeId = nodeId;
    }

    public long getCustomerId() {
        return  customerId;
    }

    public short getNodeId() {
        return  nodeId;
    }
}
