package com.dfn.watchdog.commons.db;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interface for database connection.
 */
public class CassandraConnection implements DatabaseConnection {
    private final Logger logger = LoggerFactory.getLogger(CassandraConnection.class);

    private Session session;
    private CassandraStatements cassandraStatements;
    private DatabaseConfig dbConfig;

    public CassandraConnection(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public void connect() {
        try {
            Cluster cluster = Cluster.builder()
                    .addContactPoint(dbConfig.getIp())
                    .withPort(dbConfig.getPort())
                    .build();
            session = cluster.connect("watchdog");
            cassandraStatements = new CassandraStatements(session);
        } catch (Exception e) {
            logger.error("Error connecting to cassandra: ", e);
        }
    }

    public void disconnect() {
        try {
            if (session != null) {
                session.close();
                session.getCluster().close();
            }
        } catch (Exception e) {
            logger.error("Failed to properly disconnect Cassandra session", e);
        }
    }

    public String getClientFromDb(long clientId) {
        String next = null;
        Row row = session.execute(cassandraStatements.getClientRoute(clientId)).one();
        if (row != null) {
            next = String.valueOf(row.getInt(0));
        }
        return next;
    }

    public void setClientInDb(long clientId, short next) {
        session.execute(cassandraStatements.insertClient(clientId, next));
        session.execute(cassandraStatements.insertClientHistory(clientId, next));
    }

    public void updateNodeFailure(short failedNode, short backupNode) {
        List<Long> clientRoutes = new ArrayList<>();
        try {
            ResultSet resultSet = session.execute(
                    cassandraStatements.getClientsFromRoute(failedNode));
            for (Row r : resultSet) {
                clientRoutes.add(r.getLong(0));
            }
            session.execute(cassandraStatements.updateRoutes(clientRoutes, backupNode));
            session.executeAsync(cassandraStatements.bulkInsertHistory(clientRoutes, backupNode));
        } catch (Exception e) {
            logger.error("Critical error cannot update client routes in cassandra: ", e);
        }
    }

    public List<Map<String, String>> getAllRoutes() {
        ResultSet rs = session.execute(cassandraStatements.getAllClientRoutes());
        List<Map<String, String>> clientRoutes = new ArrayList<>();
        for (Row r : rs) {
            Map<String, String> route = new HashMap<>(2);
            route.put("client", String.valueOf(r.getLong("client_id")));
            route.put("nextNode", String.valueOf(r.getInt("next_node")));
            clientRoutes.add(route);
        }
        return clientRoutes;
    }

    public List<Map<String, String>> getRouteHistory(long clientId) {
        ResultSet rs = session.execute(cassandraStatements.getClientRouteHistory(clientId));
        List<Map<String, String>> clientRouteHistory = new ArrayList<>();
        for (Row r : rs) {
            Map<String, String> route = new HashMap<>(2);
            route.put("client", String.valueOf(r.getLong("client_id")));
            route.put("nextNode", String.valueOf(r.getInt("next_node")));
            route.put("updateTime", String.valueOf(r.getString("update_time")));
            clientRouteHistory.add(route);
        }
        return clientRouteHistory;
    }

    @Override
    public void checkConnectivity() throws Exception {

    }
}
