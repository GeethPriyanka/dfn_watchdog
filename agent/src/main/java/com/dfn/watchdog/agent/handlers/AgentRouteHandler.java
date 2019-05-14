package com.dfn.watchdog.agent.handlers;

import com.dfn.watchdog.agent.WatchdogAgent;
import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.messages.inquery.RouteRequest;
import com.dfn.watchdog.commons.messages.inquery.RouteResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles route responses from server.
 * <p>
 * Route responses are matched with requests and appended.
 */
public class AgentRouteHandler extends SimpleChannelInboundHandler<RouteResponse> {
    private static final Logger logger = LoggerFactory.getLogger(AgentRouteHandler.class);

    /**
     * Create an new instance.
     * Calls to super(false) = this is not the last handler.
     */
    public AgentRouteHandler() {
        super(false);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, RouteResponse message) throws Exception {
        String nodeTypeStr = message.getNodeType();

        logger.info("Route message received : " + message.getClientId() + ", --> " +
                (nodeTypeStr != null ? nodeTypeStr : "OMS") + "-" + message.getNext());
        long clientId = message.getClientId();

        NodeType nodeType = nodeTypeStr == null ? NodeType.OMS : NodeType.valueOf(nodeTypeStr);

        if (nodeType == NodeType.OMS) {
            WatchdogAgent.INSTANCE.getClientRouteMap().put(message.getClientId(), message.getNext());
        } else if (nodeType == NodeType.AURA) {
            WatchdogAgent.INSTANCE.getClientRouteMapAura().put(message.getClientId(), message.getNext());
        }
        RouteRequest routeRequest = WatchdogAgent.INSTANCE.getRouteRequestMap().get(clientId);
        if (routeRequest == null) {
            logger.warn("Cannot find the client route request");
        } else {
            routeRequest.addResponse(message);
        }
    }
}
