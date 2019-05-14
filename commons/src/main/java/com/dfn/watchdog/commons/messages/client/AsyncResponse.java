package com.dfn.watchdog.commons.messages.client;

import com.dfn.watchdog.commons.messages.EventMessage;

/**
 * Created by isurul on 20/3/2017.
 */
public class AsyncResponse implements EventMessage {
    protected boolean initial;
    protected int requestId;

    public AsyncResponse(int requestId, boolean initial) {
        this.requestId = requestId;
        this.initial = initial;
    }

    public boolean isInitial() {
        return initial;
    }

    public int getRequestId() {
        return  requestId;
    }
}
