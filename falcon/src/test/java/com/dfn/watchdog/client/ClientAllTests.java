package com.dfn.watchdog.client;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.cassandraunit.DataLoader;
import org.cassandraunit.dataset.yaml.ClassPathYamlDataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Watchdog agent all test classes.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        WatchdogClientTestSuite.class,
        WebsocketHandlerTest.class
})
public class ClientAllTests {

    @BeforeClass
    public static void beforeAll() {
        init();
        System.out.println("Start Cassandra, Start Netty server, Configure & run agent");
    }

    @AfterClass
    public static void afterAll() {
        System.out.println("Tear down whatever left");
    }

    private static void init() {
        try {
            EmbeddedCassandraServerHelper.startEmbeddedCassandra();
            DataLoader dataLoader = new DataLoader("TestCluster", "127.0.0.1:9171");
            dataLoader.load(new ClassPathYamlDataSet("watchdog-reconciliation.yml"));

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

            WatchdogClient.INSTANCE.configure("./src/test/resources/watchdog-client-test.yml").run();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
