package com.dfn.watchdog.commons.messages.cluster;

/**
 * Created by isurul on 6/2/2017.
 */
public class JoinNack implements JoinMessage {
    String message;

    public JoinNack(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "message: " + message;
    }
}