package com.dfn.watchdog.handlers;

import com.dfn.watchdog.WatchdogServer;
import com.dfn.watchdog.commons.Node;
import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.State;
import com.dfn.watchdog.commons.View;
import com.dfn.watchdog.commons.messages.cluster.PurgeMappings;
import com.dfn.watchdog.commons.messages.inquery.RouteRequest;
import com.dfn.watchdog.commons.messages.inquery.RouteResponse;
import com.dfn.watchdog.util.ServerWriteListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * Handles client route requests.
 *
 * If request is not in the Cassandra db, calculate the next route.
 * Add this route to Cassandra and reply the agent.
 * If route is in the db, reply agent.
 */
public class ServerRouteHandler extends SimpleChannelInboundHandler<RouteRequest> {
    private static final Logger logger = LogManager.getLogger(ServerRouteHandler.class);

    /**
     * Create an new instance.
     * Calls to super(false) = this is not the last handler.
     */
    public ServerRouteHandler() {
        super(false);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RouteRequest message) throws Exception {
        long clientId = message.getClientId();
        logger.info("Route Inquiry Message received for id : " + clientId + " nodetype: "+ message.getNodeType());

        Short next;

        NodeType nodeType = NodeType.OMS;
        if (message.getNodeType() != null) {
            nodeType = NodeType.valueOf(message.getNodeType());
        }


        if (WatchdogServer.INSTANCE.getProperties().isActivePassiveEnabled()) {
            next = getActiveServerId(nodeType); //active/passive selection
            if (nodeType == NodeType.OMS) {
                WatchdogServer.INSTANCE.setClientInDb(clientId, next);
            }
        } else if (nodeType == NodeType.AURA) {
            next = 1;
        } else {
            if (WatchdogServer.INSTANCE.getView().getServerState() != State.CONNECTED) {
                next = 0;
            } else {
                String nextString = WatchdogServer.INSTANCE.getClientFromDb(clientId);
                if (nextString == null || Short.parseShort(nextString) == 0) {
                    next = getNext(clientId);
                    WatchdogServer.INSTANCE.setClientInDb(clientId, next);
                } else {
                    next = Short.parseShort(nextString);
                    Node nextNode = WatchdogServer.INSTANCE.getView().getNode(next, NodeType.OMS);
                    switch (nextNode.getState()) {
                        case CONNECTED:
                        case SUSPENDED:
                            break;
                        default:
                            next = getNext(clientId);
                            WatchdogServer.INSTANCE.setClientInDb(clientId, next);
                    }
                }
            }
        }

        RouteResponse reply = new RouteResponse(clientId, next, message.getNodeType());
        logger.info("Sending route response: " + clientId + "-> " + nodeType.name() + "-" + next);
        ctx.writeAndFlush(reply).addListener(new ServerWriteListener(reply, ctx.channel()));
    }

    private short getNext(long clientId) {
        Node nextNode;
        View view = WatchdogServer.INSTANCE.getView();
        short index = (short) (clientId % view.getDefaultSize(NodeType.OMS));
        ++index;

        nextNode = WatchdogServer.INSTANCE.getView().getNode(index, NodeType.OMS);
        if (nextNode.getState() != State.CONNECTED) {
            short i = index;
            Map<Short, Node> nodeMap = WatchdogServer.INSTANCE.getView().getAllNodesRaw(NodeType.OMS);
            do {
                if (i == nodeMap.size()) {
                    i = 1;
                } else {
                    i++;
                }
                if (nodeMap.get(i).getState() == State.CONNECTED) {
                    nextNode = nodeMap.get(i);
                    logger.info(clientId + " --> OMS-" + nextNode.getId());
                    break;
                }
            } while (i != index);

            if (i == index) {
                logger.warn("No OMS node found.");
                return 0;
            }
        }
        return nextNode.getId();
    }

    private short getActiveServerId(NodeType nodeType) {
        short currentActiveId = WatchdogServer.INSTANCE.getActiveOmsId();
        Node node = WatchdogServer.INSTANCE.getView().getNode(currentActiveId, nodeType);

        if (node != null && node.getState() == State.CONNECTED) {
            if (nodeType == NodeType.OMS) {
                return WatchdogServer.INSTANCE.getActiveOmsId();
            } else if (nodeType == NodeType.AURA) {
                return WatchdogServer.INSTANCE.getActiveAuraId();
            }
        } else {
            for (short i = 1; i <= WatchdogServer.INSTANCE.getView().getAllNodes(nodeType).size(); i++) {
                node = WatchdogServer.INSTANCE.getView().getNode(i, nodeType);
                if (node.getState() == State.CONNECTED || node.getState() == State.SUSPENDED) {
                    if (nodeType == NodeType.OMS) {
                        WatchdogServer.INSTANCE.setActiveOmsId(node.getId());
                    } else if (nodeType == NodeType.AURA) {
                        WatchdogServer.INSTANCE.setActiveAuraId(node.getId());
                    }
                    WatchdogServer.INSTANCE.broadcastMessage(new PurgeMappings((short) 0));
                    logger.info("active " + nodeType + " changed to: " + node.getId());
                    return node.getId();
                }
            }
        }
        return 0;
    }
}