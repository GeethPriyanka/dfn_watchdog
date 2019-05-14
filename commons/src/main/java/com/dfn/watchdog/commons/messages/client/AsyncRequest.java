package com.dfn.watchdog.commons.messages.client;

import com.dfn.watchdog.commons.messages.EventMessage;

import java.util.concurrent.Phaser;

/**
 * Asynchronous request.
 */
public class AsyncRequest implements EventMessage {
    int requestId;
    private transient Phaser phaser;
    protected AsyncResponse response;

    AsyncRequest() {
        requestId = ((Double) (Math.random() * 10000)).intValue();
        phaser = new Phaser(1);
    }

    public AsyncRequest(Phaser phaser) {
        requestId = ((Double) (Math.random() * 10000)).intValue();
        this.phaser = phaser;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setPhaser(int i) {
        if (i <= 1) {
            phaser.register();
        } else {
            phaser.bulkRegister(i);
        }
    }

    public Phaser getPhaser() {
        return phaser;
    }

    public AsyncResponse setResponse(AsyncResponse response) {
        return response;
    }

    public AsyncResponse getResponse() {
        return response;
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
