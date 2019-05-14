package com.dfn.watchdog.agent;

import com.datastax.driver.core.ResultSet;
import com.dfn.watchdog.agent.listeners.AgentCallbackListenerSimple;
import org.cassandraunit.CassandraCQLUnit;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.Executors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Ignore
public class CQLScriptLoadTest {

    @Rule
    public CassandraCQLUnit cassandraCQLUnit =
            new CassandraCQLUnit(new ClassPathCQLDataSet("client-routes.cql", "watchdog"));

    @Test
    public void should_have_started_and_execute_cql_script() throws Exception {
        ResultSet result = cassandraCQLUnit.session.execute(
                "select * from watchdog.clientroutes WHERE client_id=10001");
        assertThat(result.iterator().next().getInt("next_node"), is(1));
    }

    @Test
    public void startWatchdogAgentTest() throws Exception {
        WatchdogAgent.INSTANCE.configure(
                new AgentCallbackListenerSimple(),
                Executors.newCachedThreadPool(),
                "./src/test/resources/watchdog-agent-test.yml")
                .build()
                .run();
    }
}
