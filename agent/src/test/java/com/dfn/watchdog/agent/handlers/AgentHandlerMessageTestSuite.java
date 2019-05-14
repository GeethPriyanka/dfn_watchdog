package com.dfn.watchdog.agent.handlers;

import com.dfn.watchdog.agent.WatchdogAgent;
import com.dfn.watchdog.agent.util.AgentProperties;
import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.messages.client.ClientRouteRequest;
import com.dfn.watchdog.commons.messages.cluster.Heartbeat;
import com.dfn.watchdog.commons.messages.inquery.BlockCustomer;
import com.dfn.watchdog.commons.netty.EmptyChannel;
import io.netty.channel.ChannelHandlerContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * AgentHandlerMessage unit testing.
 */
public class AgentHandlerMessageTestSuite {
    private static final ChannelHandlerContext context = mock(ChannelHandlerContext.class);

    private AgentHandlerMessage agentHandlerMessage;

    @Before
    public void setUp() throws Exception {
        when(context.channel()).thenReturn(new EmptyChannel());
        when(context.close()).thenReturn(null);
        agentHandlerMessage = new AgentHandlerMessage();
    }

    @Test
    public void testSetNode() {
        WatchdogAgent.INSTANCE.setNode((short) 1, "GATEWAY");
        AgentProperties properties = WatchdogAgent.INSTANCE.getProperties();
        Assert.assertEquals(properties.agentId(), 1);
        Assert.assertEquals(properties.agentType(), NodeType.GATEWAY);
    }

    @Test
    public void testChannelRead0() throws Exception {
        ClientRouteRequest message1 = new ClientRouteRequest(123);
        agentHandlerMessage.channelRead0(context, message1);

        BlockCustomer message2 = new BlockCustomer(123);
        agentHandlerMessage.channelRead0(context, message2);

        Heartbeat message3 = new Heartbeat((short) 1, NodeType.OMS);
        agentHandlerMessage.channelRead0(context, message3);
    }

    @Test
    public void testChannelActive() throws Exception {
        agentHandlerMessage.channelActive(context);
    }

    @Test
    public void testChannelInactive() throws Exception {
        agentHandlerMessage.channelInactive(context);
    }

    @Test
    public void testExceptionCaught() throws Exception {
        agentHandlerMessage.exceptionCaught(context, new Throwable("test throwable"));
    }
}
