package com.dfn.watchdog.agent;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.thrift.transport.TTransportException;
import org.cassandraunit.DataLoader;
import org.cassandraunit.dataset.yaml.ClassPathYamlDataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

@Ignore
public class StartEmbeddedCassandraTest {

    @Before
    public void before() throws TTransportException, IOException, InterruptedException, ConfigurationException {
        EmbeddedCassandraServerHelper.startEmbeddedCassandra("cassandra-test.yml");

        DataLoader dataLoader = new DataLoader("TestCluster", "127.0.0.1:9171");
        dataLoader.load(new ClassPathYamlDataSet("watchdog-clientroutes.yml"));
//        EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
    }

    @Test
    public void connectToEmbeddedCassandraOn9142() throws Exception {
        try {
            Cluster cluster = Cluster.builder()
                    .addContactPoint("127.0.0.1")
                    .withPort(9142)
                    .build();
            Session cassandraSession = cluster.connect();
            ResultSet resultSet = cassandraSession.execute("SELECT * FROM watchdog.clientroutes");
            Row row = resultSet.one();
            System.out.println(row.getLong(1));
            System.out.println(row.getVarint(2));
        } catch (Exception e) {
            throw new Exception("Could not connect to the embedded Cassandra");
        }
    }

}
