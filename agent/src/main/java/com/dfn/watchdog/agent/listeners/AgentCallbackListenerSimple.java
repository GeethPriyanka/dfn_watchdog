package com.dfn.watchdog.agent.listeners;

import com.dfn.watchdog.agent.WatchdogAgent;
import com.dfn.watchdog.agent.util.AgentWriteListener;
import com.dfn.watchdog.commons.Node;
import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.State;
import com.dfn.watchdog.commons.View;
import com.dfn.watchdog.commons.messages.EventMessage;
import com.dfn.watchdog.commons.messages.cluster.ChangeState;
import com.dfn.watchdog.commons.messages.inquery.CustomerLogin;
import com.dfn.watchdog.commons.messages.inquery.RouteRequest;
import com.dfn.watchdog.commons.messages.monitoring.LinkStatus;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Simple implementation of the agent interface.
 * This contains the contract with the components.
 */
public class AgentCallbackListenerSimple implements AgentCallbackListener {
    private static final Logger logger = LoggerFactory.getLogger(AgentCallbackListenerSimple.class);

    @Override
    public boolean initializing(State fromState) {
        logger.info("Agent is Initializing... Nothing to initialize");
        return true;
    }

    @Override
    public boolean connecting(State fromState) {
        logger.info("Agent is Connecting...");
        return true;
    }

    @Override
    public boolean connected(State fromState) {
        logger.info("Agent Connected successfully...");
        return true;
    }

    @Override
    public boolean suspended(State fromState) {
        logger.info("Agent Suspended... Server might be unavailable");
        return true;
    }

    @Override
    public boolean leaving(State fromState) {
        logger.info("Agent Leaving the cluster...");
        return true;
    }

    @Override
    public boolean failed(State fromState) {
        logger.info("Agent Failed to connect...");
        return true;
    }

    @Override
    public boolean closed(State fromState) {
        logger.info("Agent changed to closed state...");
        return true;
    }

    @Override
    public boolean backupRecovering(Node node) {
        logger.info("Recovering the backup node: {}", node);
        return true;
    }

    @Override
    public boolean backupRecovered(Node node) {
        logger.info("Recovery complete: {}", node);
        return true;
    }

    @Override
    public boolean backupRecoveryFailed(Node node) {
        logger.info("Recovery failed: {}", node);
        return true;
    }

    @Override
    public short next(long client, String nodeType) {
        return next(client);
    }

    @Override
    public short next(long client) {
        if (WatchdogAgent.INSTANCE.getNode() == null || WatchdogAgent.INSTANCE.getNode().getState() != State.CONNECTED) {
            logger.info("node: " + WatchdogAgent.INSTANCE.getNode());
            return 0;
        }

        Short next = WatchdogAgent.INSTANCE.getClientRouteMap().get(client);
        if (next == null || next == 0) {
            next = requestRouteFromServer(client, NodeType.OMS);
            WatchdogAgent.INSTANCE.getClientRouteMap().put(client, next);
        }

        if (next != 0) {
            Node nextNode = WatchdogAgent.INSTANCE.getView().getNode(next, NodeType.OMS);
            switch (nextNode.getState()) {
                case CONNECTED:
                    break;
                case CLOSED:
                    next = requestRouteFromServer(client, NodeType.OMS);
                    WatchdogAgent.INSTANCE.getClientRouteMap().put(client, next);
                    break;
                case SUSPENDED:
                    logger.warn("Node state is suspended. Keep the message in the queue");
                    break;
                default:
                    logger.warn("Node state is" + nextNode.getState() + ". Keep the message in the queue");
            }
        }
        return next;
    }

    @Override
    public short next(long client, NodeType nodeType) {
        if (nodeType == NodeType.OMS) {
            return next(client);
        } else if (nodeType == NodeType.AURA) {
            return nextAura(client);
        }
        return 0;
    }

    private short nextAura(long client) {
        if (WatchdogAgent.INSTANCE.getNode() == null || WatchdogAgent.INSTANCE.getNode().getState() != State.CONNECTED) {
            logger.info("node: " + WatchdogAgent.INSTANCE.getNode());
            return 0;
        }

        Short next = WatchdogAgent.INSTANCE.getClientRouteMapAura().get(client);
        if (next == null || next == 0) {
            next = requestRouteFromServer(client, NodeType.AURA);
            WatchdogAgent.INSTANCE.getClientRouteMapAura().put(client, next);
        }

        if (next != 0) {
            Node nextNode = WatchdogAgent.INSTANCE.getView().getNode(next, NodeType.AURA);
            switch (nextNode.getState()) {
                case CONNECTED:
                    break;
                case CLOSED:
                    next = requestRouteFromServer(client, NodeType.AURA);
                    WatchdogAgent.INSTANCE.getClientRouteMapAura().put(client, next);
                    break;
                case SUSPENDED:
                    logger.warn("Node state is suspended. Keep the message in the queue");
                    break;
                default:
                    logger.warn("Node state is" + nextNode.getState() + ". Keep the message in the queue");
            }
        }
        return next;
    }

    @Override
    public Node getBackup(short index, NodeType nodeType) {
        short i = index;
        Node nextNode = null;
        Map<Short, Node> nodeMap = WatchdogAgent.INSTANCE.getView().getAllNodesRaw(nodeType);

        short loopBreaker = 1;
        do {
            if (i >= nodeMap.size()) {
                i = 1;
            } else {
                i++;
            }
            if (nodeMap.get(i) != null && nodeMap.get(i).getState() == State.CONNECTED) {
                nextNode = nodeMap.get(i);
                break;
            }

            loopBreaker++; //maximum check for full round
        } while (i != index && (loopBreaker <= nodeMap.size()));

        if (i == index) {
            logger.warn("No {} node found", nodeType);
        }
        return nextNode;
    }

    @Override
    public void sendLogin(long clientId) {
        try {
            Node node = WatchdogAgent.INSTANCE.getNode();
            CustomerLogin loginMessage = new CustomerLogin(clientId, node.getId());
            Channel channel = node.getChannel();
            channel.writeAndFlush(loginMessage).addListener(new AgentWriteListener(loginMessage, channel));
        } catch (Exception e) {
            logger.error("exception in sendLogin", e);
        }
    }

    @Override
    public void blockCustomer(long clientId) {
        logger.info("Block customer request: {}", clientId);
        //oms to block customers
    }

    @Override
    public void sendLinkStatus(String nodeName, State state) {
        try {
            Node node = WatchdogAgent.INSTANCE.getNode();
            LinkStatus linkStatus = new LinkStatus(node.getName(), nodeName, state);
            Channel channel = node.getChannel();
            channel.writeAndFlush(linkStatus).addListener(new AgentWriteListener(linkStatus, channel));
        } catch (Exception e) {
            logger.error("exception in sendLinkStatus", e);
        }
    }

    @Override
    public void sendLinkStatus(String source, String destination, State state) {
        try {
            LinkStatus linkStatus = new LinkStatus(source, destination, state);
            Channel channel = WatchdogAgent.INSTANCE.getNode().getChannel();
            channel.writeAndFlush(linkStatus).addListener(new AgentWriteListener(linkStatus, channel));
        } catch (Exception e) {
            logger.error("exception in sendLinkStatus2", e);
        }
    }

    @Override
    public void sendCustomMetrics() {
        //should override by child classes
    }

    protected void notifyServer(EventMessage message) {
        try {
            Channel channel = WatchdogAgent.INSTANCE.getNode().getChannel();
            channel.writeAndFlush(message).addListener(new AgentWriteListener(message, channel));
        } catch (Exception e) {
            logger.error("exception in notifyServer", e);
        }
    }


    private short requestRouteFromServer(long clientId, NodeType nodeType) {
        RouteRequest routeRequest = new RouteRequest(clientId);
        routeRequest.setNodeType(nodeType.name());

        try {
            Channel channel = WatchdogAgent.INSTANCE.getNode().getChannel();
            channel.writeAndFlush(routeRequest);
            WatchdogAgent.INSTANCE.getRouteRequestMap().put(clientId, routeRequest);
        } catch (Exception e) {
            logger.error("exception in requestRouteFromServer", e);
        }
        try {
            if (!routeRequest.waitForResponse()) {
                logger.warn("Client route request timed out: {}", System.nanoTime());
            }
            WatchdogAgent.INSTANCE.getRouteRequestMap().remove(clientId);
        } catch (InterruptedException e) {
            logger.error("Client route request to server was interrupted: ", e);
            Thread.currentThread().interrupt();
        }
        return routeRequest.getResponse().getNext();
    }

    @Override
    public void shutdown() {
        logger.warn("Shutdown hook invoked by server");
    }

    @Override
    public void restart() {
        logger.warn("Restart invoked by server");
    }

    @Override
    public void startEod(short nodeId) {
        try {
            logger.warn("Preparing to start eod process");
            Node node = WatchdogAgent.INSTANCE.getNode();
            node.changeState(State.EOD);
            ChangeState changeState = new ChangeState(node);
            notifyServer(changeState);
        } catch (Exception e) {
            logger.error("exception in startEod", e);
        }
    }

    @Override
    public void updateConfiguration(View view) {
        logger.info("Update Configuration");
    }
}
