package com.dfn.watchdog.agent;

import com.dfn.watchdog.agent.handlers.AgentChangeStateHandlerTestSuite;
import com.dfn.watchdog.agent.handlers.AgentHandlerMessageTestSuite;
import com.dfn.watchdog.agent.handlers.AgentHeartbeatHandlerTestSuite;
import com.dfn.watchdog.agent.handlers.AgentJoinHandlerTestSuite;
import com.dfn.watchdog.agent.listeners.AgentCallbackListenerSimple;
import com.dfn.watchdog.agent.listeners.AgentCallbackListenerSimpleTestSuite;
import com.dfn.watchdog.commons.Node;
import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.State;
import com.dfn.watchdog.commons.View;
import com.dfn.watchdog.commons.netty.EmptyChannel;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.util.concurrent.Executors;

/**
 * Watchdog agent all test classes.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        AgentCallbackListenerSimpleTestSuite.class,
        WatchdogAgentTestSuite.class,
        AgentHandlerMessageTestSuite.class,
        AgentHeartbeatHandlerTestSuite.class,
        AgentJoinHandlerTestSuite.class,
        AgentChangeStateHandlerTestSuite.class
})
public class AgentAllTests {

    @BeforeClass
    public static void beforeAll() {
        init();
        System.out.println("Start Netty server, Configure & run agent");
    }

    @AfterClass
    public static void afterAll() {
        System.out.println("Tear down whatever left");
    }

    private static void init() {
        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(new NioEventLoopGroup(), new NioEventLoopGroup())
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new SimpleChannelInboundHandler<Object>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                            System.out.println("message received: " + msg);
                        }

                        @Override
                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                            cause.printStackTrace();
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.bind("127.0.0.1", 7802).sync();

            WatchdogAgent.INSTANCE.configure(
                    new AgentCallbackListenerSimple(),
                    Executors.newCachedThreadPool(),
                    "./src/test/resources/watchdog-agent-test.yml")
                    .build()
                    .run();
            View view = new View();
            view.addNode(new Node((short) 0, NodeType.OMS, State.CONNECTED));
            view.addNode(new Node((short) 1, NodeType.OMS, State.CONNECTED));
            view.addNode(new Node((short) 1, NodeType.GATEWAY, State.CONNECTED));
            WatchdogAgent.INSTANCE.installView(view);
            Node node = WatchdogAgent.INSTANCE.getNode();
            node.setChannel(new EmptyChannel());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
