package com.dfn.watchdog;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.cassandraunit.DataLoader;
import org.cassandraunit.dataset.yaml.ClassPathYamlDataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Watchdog server all test classes.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
//        ServerRouteHandlerTestSuite.class
})
public class WatchdogAllTests {
    @BeforeClass
    public static void beforeAll() {
        init();
        System.out.println("Start Cassandra, Start Netty server, Configure & run server");
    }

    @AfterClass
    public static void afterAll() {
        System.out.println("Tear down whatever left");
    }

    private static void init() {
        try {
            EmbeddedCassandraServerHelper.startEmbeddedCassandra();
            DataLoader dataLoader = new DataLoader("TestCluster", "127.0.0.1:9171");
            dataLoader.load(new ClassPathYamlDataSet("watchdog-clientroutes.yml"));

            Cluster cluster = Cluster.builder()
                    .addContactPoint("127.0.0.1")
                    .withPort(9142)
                    .build();
            Session cassandraSession = cluster.connect("watchdog");
            cassandraSession.execute("CREATE TABLE watchdog.clientroutes (" +
                    " client_id bigint, next_node int, PRIMARY KEY (client_id));");
            cassandraSession.execute("CREATE INDEX next_node ON watchdog.clientroutes (next_node);");
            cassandraSession.execute("CREATE TABLE watchdog.clientroutes_history (" +
                    " client_id bigint, next_node int, PRIMARY KEY (update_time, client_id);");
            cassandraSession.execute("CREATE INDEX next_node ON watchdog.clientroutes_history (next_node);");

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(new NioEventLoopGroup())
                    .channel(NioSocketChannel.class)
                    .remoteAddress("127.0.0.1", 7802)
                    .handler(new SimpleChannelInboundHandler<Object>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                            System.out.println("message received: " + msg);
                        }

                        @Override
                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                            cause.printStackTrace();
                        }
                    });
            bootstrap.connect();

            WatchdogServer.INSTANCE.configure("./src/test/resources/watchdog-test.yml");
            WatchdogServer.INSTANCE.run();

            WatchdogServer.INSTANCE.getDeadNodeFuture().cancel(false);
            WatchdogServer.INSTANCE.getServerChannelFuture().channel().close();
            WatchdogServer.INSTANCE.getServerChannelFuture().channel().parent().close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
