package com.dfn.watchdog.commons.messages.inquery;

/**
 * Request to block the customer from agent component.
 * If customer data is inconsistent among the cluster it should be blocked.
 */
public class BlockCustomer implements CustomerListMessage {
    private final long customerId;

    /**
     * @param customerId customer to block
     */
    public BlockCustomer(long customerId) {
        this.customerId = customerId;
    }

    public long getCustomerId() {
        return customerId;
    }
}