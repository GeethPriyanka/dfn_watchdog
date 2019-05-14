package com.dfn.watchdog.agent.handlers;

import com.dfn.watchdog.commons.Node;
import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.State;
import com.dfn.watchdog.commons.View;
import com.dfn.watchdog.commons.messages.EventMessage;
import com.dfn.watchdog.commons.messages.cluster.JoinAck;
import com.dfn.watchdog.commons.messages.cluster.JoinMessage;
import com.dfn.watchdog.commons.netty.EmptyChannel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.Future;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * AgentJoinHandler unit testing.
 */
public class AgentJoinHandlerTestSuite {
    private static final ChannelHandlerContext context = mock(ChannelHandlerContext.class);
    private static final EventLoopGroup eventLoopGroup = mock(EventLoopGroup.class);
    private static final Future future = mock(Future.class);
    private static final ChannelFuture channelFuture = mock(ChannelFuture.class);

    private AgentJoinHandler agentJoinHandler;

    @Before
    public void setUp() throws Exception {
        when(context.channel()).thenReturn(new EmptyChannel());
        when(context.close()).thenReturn(null);
        when(context.writeAndFlush(Mockito.any(EventMessage.class))).thenReturn(channelFuture);
        when(eventLoopGroup.submit(Mockito.any(Runnable.class))).thenReturn(future);
        agentJoinHandler = new AgentJoinHandler();
    }

    @Test
    public void testChannelRead0() throws Exception {
        View view = new View();
        view.addNode(new Node((short) 1, NodeType.OMS, State.CONNECTED));
        view.addNode(new Node((short) 1, NodeType.GATEWAY, State.CONNECTED));
        JoinMessage message = new JoinAck(view);
        agentJoinHandler.channelRead0(context, message);
    }

    @Test
    public void testChannelActive() throws Exception {
        agentJoinHandler.channelActive(context);
        agentJoinHandler.channelInactive(context);
    }
}
