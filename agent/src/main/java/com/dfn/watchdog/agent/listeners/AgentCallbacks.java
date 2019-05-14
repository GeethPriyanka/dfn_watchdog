package com.dfn.watchdog.agent.listeners;

import com.dfn.watchdog.commons.Node;
import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.State;

/**
 * Callbacks and requests from nodes.
 */
public interface AgentCallbacks {

    short next(long client);

    short next(long client, String nodeType);

    Node getBackup(short index, NodeType nodeType);

    void sendLogin(long clientId);

    void blockCustomer(long clientId);

    void sendLinkStatus(String nodeName, State state);

    void sendLinkStatus(String source, String destination, State state);

    void sendCustomMetrics();

    short next(long client, NodeType nodeType);
}
