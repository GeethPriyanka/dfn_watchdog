package com.dfn.watchdog.agent.listeners;

import com.dfn.watchdog.commons.Node;
import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.State;
import com.dfn.watchdog.commons.View;

/**
 * Empty interface for disabling the watchdog.
 */
public class AgentCallbackListenerEmpty implements AgentCallbackListener {
    @Override
    public void shutdown() {
        //nothing to implement
    }

    @Override
    public void restart() {
        //nothing to implement
    }

    @Override
    public void startEod(short nodeId) {
        //nothing to implement
    }

    @Override
    public void updateConfiguration(View view) {
        //nothing to implement
    }

    @Override
    public boolean initializing(State fromState) {
        //nothing to implement
        return true;
    }

    @Override
    public boolean connecting(State fromState) {
        //nothing to implement
        return true;
    }

    @Override
    public boolean connected(State fromState) {
        //nothing to implement
        return true;
    }

    @Override
    public boolean suspended(State fromState) {
        //nothing to implement
        return true;
    }

    @Override
    public boolean leaving(State fromState) {
        //nothing to implement
        return true;
    }

    @Override
    public boolean failed(State fromState) {
        //nothing to implement
        return true;
    }

    @Override
    public boolean closed(State fromState) {
        //nothing to implement
        return true;
    }

    @Override
    public boolean backupRecovering(Node node) {
        //nothing to implement
        return true;
    }

    @Override
    public boolean backupRecovered(Node node) {
        //nothing to implement
        return true;
    }

    @Override
    public boolean backupRecoveryFailed(Node node) {
        //nothing to implement
        return true;
    }

    @Override
    public short next(long client) {
        //nothing to implement
        return 1;
    }

    @Override
    public short next(long client, String nodeType) {
        //nothing to implement
        return 1;
    }

    @Override
    public Node getBackup(short index, NodeType nodeType) {
        //nothing to implement
        return null;
    }

    @Override
    public void sendLogin(long clientId) {
        //nothing to implement
    }

    @Override
    public void blockCustomer(long clientId) {
        //nothing to implement
    }

    @Override
    public void sendLinkStatus(String nodeName, State state) {
        //nothing to implement
    }

    @Override
    public void sendLinkStatus(String source, String destination, State state) {
        //nothing to implement
    }

    @Override
    public void sendCustomMetrics() {
        //nothing to implement
    }

    @Override
    public short next(long client, NodeType nodeType) {
        return 1;
    }
}
