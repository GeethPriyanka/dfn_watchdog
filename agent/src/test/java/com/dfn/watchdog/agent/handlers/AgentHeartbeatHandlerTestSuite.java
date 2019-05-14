package com.dfn.watchdog.agent.handlers;

import com.dfn.watchdog.commons.*;
import com.dfn.watchdog.commons.messages.cluster.HeartbeatAck;
import com.dfn.watchdog.commons.netty.EmptyChannel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.ScheduledFuture;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * AgentHeartbeatHandler unit testing.
 */
public class AgentHeartbeatHandlerTestSuite {
    private static final ChannelHandlerContext context = mock(ChannelHandlerContext.class);
    private static final EventLoopGroup eventLoopGroup = mock(EventLoopGroup.class);
    private static final ScheduledFuture future = mock(ScheduledFuture.class);

    private AgentHeartbeatHandler agentHeartbeatHandler;

    @Before
    public void setUp() throws Exception {
        when(context.channel()).thenReturn(new EmptyChannel());
        when(context.close()).thenReturn(null);
        when(eventLoopGroup.scheduleAtFixedRate(
                Mockito.any(Runnable.class), Mockito.anyLong(), Mockito.anyLong(), Mockito.any(TimeUnit.class)))
                .thenReturn(future);
        agentHeartbeatHandler = new AgentHeartbeatHandler();
    }

    @Test
    public void testChannelRead0() throws Exception {
        View view = new View();
        view.addNode(new Node((short) 1, NodeType.OMS, State.CONNECTED));
        view.addNode(new Node((short) 1, NodeType.GATEWAY, State.CONNECTED));
        HeartbeatAck message3 = new HeartbeatAck(new BroadcastView(view.getNodeMap()));
        agentHeartbeatHandler.channelRead0(context, message3);
    }

    @Test
    public void testChannelActive() throws Exception {

        agentHeartbeatHandler.channelActive(context);
        agentHeartbeatHandler.channelInactive(context);
    }

    @Test
    public void testExceptionCaught() throws Exception {
        agentHeartbeatHandler.exceptionCaught(context, new Throwable("test throwable"));
    }
}
