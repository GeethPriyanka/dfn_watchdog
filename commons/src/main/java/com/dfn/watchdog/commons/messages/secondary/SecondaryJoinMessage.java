package com.dfn.watchdog.commons.messages.secondary;

public class SecondaryJoinMessage implements SecondaryMessage {
    private boolean isAck = false;


    public boolean isAck() {
        return isAck;
    }

    public void setAck(boolean ack) {
        isAck = ack;
    }
}
