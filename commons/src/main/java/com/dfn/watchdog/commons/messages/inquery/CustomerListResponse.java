package com.dfn.watchdog.commons.messages.inquery;

import java.util.HashSet;
import java.util.Set;

/**
 * Customer list currently loaded to the OMS.
 */
public class CustomerListResponse implements CustomerListMessage {
    private short nodeId;
    private Set<Long> customerSet;

    public CustomerListResponse(short nodeId) {
        this.nodeId = nodeId;
        customerSet = new HashSet<>();
    }

    public CustomerListResponse(short nodeId, Set<Long> customerSet) {
        this.nodeId = nodeId;
        this.customerSet = customerSet;
    }

    public boolean addToCustomers(long id) {
        return customerSet.add(id);
    }

    public Set<Long> getCustomerSet() {
        return customerSet;
    }

    public void setCustomerSet(Set<Long> customerSet) {
        this.customerSet = customerSet;
    }

    public short getNodeId() {
        return nodeId;
    }

    public void setNodeId(short nodeId) {
        this.nodeId = nodeId;
    }
}
