package com.dfn.watchdog.commons.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Holder for Oracle statements.
 */
public class OracleStatements {
    private final PreparedStatement clientInsert;
    private final PreparedStatement clientUpdate;
    private final PreparedStatement clientRoute;
    private final PreparedStatement clientsFromRoute;
    private final PreparedStatement updateRoutes;
    private final PreparedStatement clientRouteSelect;
    private final PreparedStatement clientRouteHistorySelect;
    private final SimpleDateFormat dateFormatter;
    private final PreparedStatement loginSelect;

    private final PreparedStatement clientInsertHistory;

    public OracleStatements(Connection connection) throws SQLException {
        clientInsert = connection.prepareStatement("INSERT INTO m115_client_routes (m115_customer_id_u01, " +
                "m115_next_node, m115_last_update) VALUES (?, ?, ?)");
        clientUpdate = connection.prepareStatement("UPDATE M115_CLIENT_ROUTES " +
                "SET m115_next_node = ?, M115_LAST_UPDATE = ? " +
                "WHERE m115_customer_id_u01 = ?");
        clientRoute = connection.prepareStatement(
                "SELECT m115_next_node FROM m115_client_routes where m115_customer_id_u01 = ?");
        clientsFromRoute = connection.prepareStatement(
                "SELECT m115_customer_id_u01 FROM m115_client_routes WHERE m115_next_node = ?");
        updateRoutes = connection.prepareStatement("UPDATE m115_client_routes " +
                "SET m115_next_node = ?, m115_last_update = ? WHERE m115_next_node = ?");

        clientInsertHistory = connection.prepareStatement("INSERT INTO h08_client_routes (h08_customer_id_u01, " +
                "h08_next_node, h08_last_update) VALUES (?, ?, ?)");
        clientRouteSelect = connection.prepareStatement(
                "SELECT M115_CUSTOMER_ID_U01, M115_NEXT_NODE FROM m115_client_routes");
        clientRouteHistorySelect = connection.prepareStatement("SELECT H08_CUSTOMER_ID_U01, " +
                "H08_NEXT_NODE, H08_LAST_UPDATE FROM h08_client_routes where h08_customer_id_u01 = ?");
        loginSelect = connection.prepareStatement("SELECT M37_PASSWORD FROM M37_OTHER_LOGIN WHERE M37_USERNAME = ?");

        dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    }

    public PreparedStatement insertClient(long clientId, short nodeId) throws SQLException {
        String lastUpdate = dateFormatter.format(new Date());
        clientInsert.setLong(1, clientId);
        clientInsert.setInt(2, nodeId);
        clientInsert.setString(3, lastUpdate);
        return clientInsert;
    }

    public PreparedStatement updateClient(long clientId, short nodeId) throws SQLException {
        String lastUpdate = dateFormatter.format(new Date());
        clientUpdate.setInt(1, nodeId);
        clientUpdate.setString(2, lastUpdate);
        clientUpdate.setLong(3, clientId);
        return clientUpdate;
    }

    public PreparedStatement getClientRoute(long clientId) throws SQLException {
        clientRoute.setLong(1, clientId);
        return clientRoute;
    }

    public PreparedStatement getClientsFromRoute(short nodeId) throws SQLException {
        clientsFromRoute.setInt(1, nodeId);
        return clientsFromRoute;
    }

    public PreparedStatement updateRoutes(short failedNode, short newNode) throws SQLException {
        String lastUpdate = dateFormatter.format(new Date());
        updateRoutes.setInt(1, newNode);
        updateRoutes.setString(2, lastUpdate);
        updateRoutes.setInt(3, failedNode);
        return updateRoutes;
    }

    public PreparedStatement insertClientHistory(long clientId, short nodeId) throws SQLException {
        String lastUpdate = dateFormatter.format(new Date());
        clientInsertHistory.setLong(1, clientId);
        clientInsertHistory.setInt(2, nodeId);
        clientInsertHistory.setString(3, lastUpdate);
        return clientInsertHistory;
    }

    public PreparedStatement bulkInsertHistory(List<Long> clientIds, short nodeId) throws SQLException {
        String lastUpdate = dateFormatter.format(new Date());
        for (long cid : clientIds) {
            clientInsertHistory.setLong(1, cid);
            clientInsertHistory.setInt(2, nodeId);
            clientInsertHistory.setString(3, lastUpdate);
            clientInsertHistory.addBatch();
        }
        return clientInsertHistory;
    }

    public PreparedStatement getAllClientRoutes() throws SQLException {
        return clientRouteSelect;
    }

    public PreparedStatement getClientRouteHistory(long customerId) throws SQLException {
        clientRouteHistorySelect.setLong(1, customerId);
        return clientRouteHistorySelect;
    }

    public PreparedStatement getLogins(String username)throws SQLException{
        loginSelect.setString(1,username);
        return loginSelect;
    }

}
