package com.dfn.watchdog.commons.db;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Holder for Cassandra statements.
 */
public class CassandraStatements {
    private final PreparedStatement clientInsert;
    private final PreparedStatement clientRoute;
    private final PreparedStatement clientsFromRoute;
    private final PreparedStatement updateRoutes;
    private final PreparedStatement clientRouteSelect;
    private final PreparedStatement clientRouteHistorySelect;
    private final SimpleDateFormat dateFormatter;
    private final PreparedStatement loginSelect;

    private final PreparedStatement clientInsertHistory;

    public CassandraStatements(Session session) {
        clientInsert = session.prepare("INSERT INTO watchdog.clientroutes (client_id, next_node, update_time) VALUES (?, ?, ?)");
        clientRoute = session.prepare("SELECT next_node FROM watchdog.clientroutes where client_id = ?");
        clientsFromRoute = session.prepare("SELECT client_id FROM watchdog.clientroutes WHERE next_node = ?");
        updateRoutes = session.prepare("UPDATE watchdog.clientroutes SET next_node = ?, update_time = ? WHERE client_id IN ?");

        clientInsertHistory = session.prepare("INSERT INTO watchdog.clientroutes_history (client_id, next_node, update_time) VALUES (?, ?, ?)");
        clientRouteSelect = session.prepare("select * from watchdog.clientroutes");
        clientRouteHistorySelect = session.prepare("select * from watchdog.clientroutes_history where client_id = ?");
        loginSelect = session.prepare(""); //TODO

        dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    }

    public BoundStatement insertClient(long clientId, short nodeId) {
        String lastUpdate = dateFormatter.format(new Date());
        return clientInsert.bind().setLong(0, clientId).setInt(1, nodeId).setString(2, lastUpdate);
    }

    public BoundStatement getClientRoute(long clientId) {
        return clientRoute.bind().setLong(0, clientId);
    }

    public BoundStatement getClientsFromRoute(short nodeId) {
        return clientsFromRoute.bind().setInt(0, nodeId);
    }

    public BoundStatement updateRoutes(List<Long> clientIds, short nodeId) {
        String lastUpdate = dateFormatter.format(new Date());
        return updateRoutes.bind().setInt(0, nodeId).setString(1, lastUpdate).setList(2, clientIds);
    }

    public BoundStatement insertClientHistory(long clientId, short nodeId) {
        String lastUpdate = dateFormatter.format(new Date());
        return clientInsertHistory.bind().setLong(0, clientId).setInt(1, nodeId).setString(2, lastUpdate);
    }

    public BatchStatement bulkInsertHistory(List<Long> clientIds, short nodeId) {
        String lastUpdate = dateFormatter.format(new Date());
        BatchStatement batchStatement = new BatchStatement();
        for (long cid : clientIds) {
            batchStatement.add(clientInsertHistory.bind().setLong(0, cid).setInt(1, nodeId).setString(2, lastUpdate));
        }
        return batchStatement;
    }

    public BoundStatement getAllClientRoutes() {
        return clientRouteSelect.bind();
    }

    public BoundStatement getClientRouteHistory(long clientId) {
        return clientRouteHistorySelect.bind(clientId);
    }

    public BoundStatement getLogins(String username){
        //not implemented properly TODO
        return loginSelect.bind(username);
    }

}
