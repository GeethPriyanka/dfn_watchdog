package com.dfn.watchdog.agent.handlers;

import com.dfn.watchdog.agent.WatchdogAgent;
import com.dfn.watchdog.commons.messages.commands.Command;
import com.dfn.watchdog.commons.messages.commands.Restart;
import com.dfn.watchdog.commons.messages.commands.Shutdown;
import com.dfn.watchdog.commons.messages.commands.StartEod;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles commands sent by the server.
 */
public class AgentCommandHandler extends SimpleChannelInboundHandler<Command> {
    private static final Logger logger = LoggerFactory.getLogger(AgentCommandHandler.class);

    /**
     * Create an new instance.
     * Calls to super(false) = this is not the last handler.
     */
    public AgentCommandHandler() {
        super(false);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Command message) throws Exception {
        logger.warn("New Command message received: ", message);
        if (message instanceof Shutdown) {
            WatchdogAgent.INSTANCE.getListener().shutdown();
        } else if (message instanceof Restart) {
            WatchdogAgent.INSTANCE.getListener().restart();
        } else if (message instanceof StartEod) {
            WatchdogAgent.INSTANCE.getListener().startEod(message.getId());
        } else {
            logger.warn("Unhandled command message received: ", message);
        }
    }
}
