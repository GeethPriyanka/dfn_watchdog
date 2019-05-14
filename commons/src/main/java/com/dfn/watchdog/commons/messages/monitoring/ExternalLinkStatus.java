package com.dfn.watchdog.commons.messages.monitoring;

import com.dfn.watchdog.commons.State;

/**
 * Monitoring message which inform a link status change.
 */
public class ExternalLinkStatus implements MonitoringMessage {
    private final String sourceNode;
    private final String destinationNode;
    private final State state;
    private final String externalNodeName;

    public ExternalLinkStatus(String sourceNode, String destinationNode, State state, String externalNodeName) {
        this.sourceNode = sourceNode;
        this.destinationNode = destinationNode;
        this.state = state;
        this.externalNodeName = externalNodeName;
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

    public String getExternalNodeName() {
        return externalNodeName;
    }

    @Override
    public String toString() {
        return sourceNode + " " + state + " " + destinationNode + "(" + externalNodeName + ")";
    }
}
