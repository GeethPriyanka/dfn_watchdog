package com.dfn.watchdog.handlers.secondary;

import com.dfn.watchdog.WatchdogServer;
import com.dfn.watchdog.commons.Node;
import com.dfn.watchdog.commons.messages.secondary.SecondaryJoin;
import com.dfn.watchdog.commons.messages.secondary.SecondaryMessage;
import com.dfn.watchdog.commons.messages.secondary.SwapAgent;
import com.dfn.watchdog.commons.messages.secondary.WDSecondary;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerSecondaryHandler extends SimpleChannelInboundHandler<SecondaryMessage> {
    private static final Logger logger = LogManager.getLogger(ServerSecondaryHandler.class);

    /**
     * Create an new instance.
     * Calls to super(false) = this is not the last handler.
     */
    public ServerSecondaryHandler() {
        super(false);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, SecondaryMessage secondaryMessage) throws Exception {
        if (secondaryMessage instanceof WDSecondary) {
            logger.info("Secondary Broadcast message received to notify primary disconnection");

            if (WatchdogServer.INSTANCE.isPrimary()) {
                // Secondary disconnected already. I'm the primary. connect me as primary.
                SwapAgent swapAgent = new SwapAgent(true);
                swapAgent.setServerIp(WatchdogServer.INSTANCE.getProperties().host());
                swapAgent.setPort(WatchdogServer.INSTANCE.getProperties().port());
                channelHandlerContext.writeAndFlush(swapAgent);
            } else {
                WatchdogServer.INSTANCE.setAgentNotifiedDisconnection(true);
            }
        } else if (secondaryMessage instanceof SecondaryJoin) {
            Node node = ((SecondaryJoin) secondaryMessage).getNode();
            node.setSecondaryChannel(channelHandlerContext.channel());

            logger.info("Secondary node received. "+ node);

            if (((SecondaryJoin) secondaryMessage).isPrimaryConnected()) {
                WatchdogServer.INSTANCE.setPrimary(false); // node already connected to primary. I'm the secondary
            }
            if (!WatchdogServer.INSTANCE.getSecondaryNodes().contains(node)) {
                WatchdogServer.INSTANCE.getSecondaryNodes().add(node);
            }
        }
    }
}
