package com.dfn.watchdog.agent.handlers.secondary;

import com.dfn.watchdog.agent.WatchdogAgent;
import com.dfn.watchdog.commons.messages.secondary.SecondaryJoin;
import com.dfn.watchdog.commons.messages.secondary.SecondaryMessage;
import com.dfn.watchdog.commons.messages.secondary.SwapAgent;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgentSecondaryHandler extends SimpleChannelInboundHandler<SecondaryMessage> {
    private static final Logger logger = LoggerFactory.getLogger(AgentSecondaryHandler.class);

    /**
     * Create an new instance.
     * Calls to super(false) = this is not the last handler.
     */
    public AgentSecondaryHandler() {
        super(false);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, SecondaryMessage secondaryMessage) throws Exception {
        if (secondaryMessage instanceof SwapAgent && ((SwapAgent) secondaryMessage).isDoSwapServer()) {
            logger.info("going to swap server");
            WatchdogAgent.INSTANCE.swapAgentServer(((SwapAgent) secondaryMessage).getServerIp(), ((SwapAgent) secondaryMessage).getPort());
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        WatchdogAgent.INSTANCE.getNode().setSecondaryChannel(ctx.channel());
        ctx.channel().writeAndFlush(new SecondaryJoin(WatchdogAgent.INSTANCE.getNode(), WatchdogAgent.INSTANCE.getNode().getChannel().isActive()));
    }
}
