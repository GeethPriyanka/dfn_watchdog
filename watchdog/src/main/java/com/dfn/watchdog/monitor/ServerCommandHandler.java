package com.dfn.watchdog.monitor;

import com.dfn.watchdog.WatchdogServer;
import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.messages.commands.Command;
import com.dfn.watchdog.commons.messages.commands.StartEod;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handles commands sent by the client component.
 * <p>
 * Most of the time act as a bridge between agents and client.
 * Can act upon specific commands.
 */
public class ServerCommandHandler extends SimpleChannelInboundHandler<Command> {
    private static final Logger logger = LogManager.getLogger(ServerCommandHandler.class);

    /**
     * Create an new instance.
     * Calls to super(false) = this is not the last handler.
     */
    public ServerCommandHandler() {
        super(false);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command message) throws Exception {
        logger.info("New command received from client: " + message);

        if (message instanceof StartEod) {
            logger.warn("Start EOD message received");
            WatchdogServer.INSTANCE.startEodProcess();
        } else {
            short id = message.getId();
            NodeType type = message.getType();

            WatchdogServer.INSTANCE.getView().getNode(id, type).getChannel().writeAndFlush(message);
        }
    }
}