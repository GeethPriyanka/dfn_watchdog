package com.dfn.watchdog.commons.messages.monitoring;

import com.dfn.watchdog.commons.State;

/**
 * Monitoring message which inform a link status change.
 */
public class LinkStatus implements MonitoringMessage {
    private final String sourceNode;
    private final String destinationNode;
    private final State state;

    public LinkStatus(String sourceNode, String destinationNode, State state) {
        this.sourceNode = sourceNode;
        this.destinationNode = destinationNode;
        this.state = state;
    }

    public String getSourceNode() {
        return sourceNode;
    }

    public String getDestinationNode() {
        return destinationNode;
    }

    public State getState() {
        return state;
    }

    @Override
    public String toString() {
        return sourceNode + " " + state + " " + destinationNode;
    }
}
