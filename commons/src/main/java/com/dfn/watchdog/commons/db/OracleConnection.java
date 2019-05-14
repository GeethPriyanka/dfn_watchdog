package com.dfn.watchdog.commons.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interface for database connection.
 */
public class OracleConnection implements DatabaseConnection {
    private final Logger logger = LoggerFactory.getLogger(OracleConnection.class);

    private Connection connection;
    private OracleStatements oracleStatements;
    private DatabaseConfig dbConfig;

    public OracleConnection(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    private void createConnection() {
        try {
            connection = DriverManager.getConnection(
                    dbConfig.getUrl(), dbConfig.getUser(), dbConfig.getPassword());
            oracleStatements = new OracleStatements(connection);
        } catch (Exception e) {
            logger.error("Failed to create oracle connection", e);
        }
    }

    public void connect() {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (Exception e) {
            logger.error("Failed to locate oracle drivers", e);
        }
    }

    public void disconnect() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (Exception e) {
            logger.error("Failed to close oracle connection properly", e);
        }
    }

    public String getClientFromDb(long clientId) {
        String nodeId = null;
        createConnection();

        try (ResultSet resultSet = oracleStatements.getClientRoute(clientId).executeQuery()) {
            if (resultSet.next()) {
                nodeId = String.valueOf(resultSet.getInt(1));
            }
        } catch (SQLException e) {
            logger.error("Critical error. Failed to get route from oracle", e);
        } finally {
            disconnect();
        }
        return nodeId;
    }

    public void setClientInDb(long clientId, short next) {
        createConnection();

        try (ResultSet resultSet = oracleStatements.getClientRoute(clientId).executeQuery()) {
            if (resultSet.next()) {
                oracleStatements.updateClient(clientId, next).executeQuery();
            } else {
                oracleStatements.insertClient(clientId, next).executeQuery();
            }
            oracleStatements.insertClientHistory(clientId, next).executeQuery();
        } catch (SQLException e) {
            logger.error("Critical error. Failed to update route in oracle", e);
        } finally {
            disconnect();
        }
    }

    public void updateNodeFailure(short failedNode, short backupNode) {
        List<Long> clientRoutes = new ArrayList<>();
        createConnection();

        try (ResultSet resultSet = oracleStatements.getClientsFromRoute(failedNode).executeQuery()) {
            while (resultSet.next()) {
                clientRoutes.add(resultSet.getLong(1));
            }
            oracleStatements.updateRoutes(failedNode, backupNode).executeQuery();
            oracleStatements.bulkInsertHistory(clientRoutes, backupNode).executeBatch();
        } catch (Exception e) {
            logger.error("Critical error. Cannot update routes of failed node in oracle.", e);
        } finally {
            disconnect();
        }
    }

    public List<Map<String, String>> getAllRoutes() {
        List<Map<String, String>> clientRoutes = new ArrayList<>();
        createConnection();

        try (ResultSet resultSet = oracleStatements.getAllClientRoutes().executeQuery()) {
            while (resultSet.next()) {
                Map<String, String> route = new HashMap<>(2);
                route.put("client", String.valueOf(resultSet.getLong(1)));
                route.put("nextNode", String.valueOf(resultSet.getShort(2)));
                clientRoutes.add(route);
            }
        } catch (Exception e) {
            logger.error("Failed to get routes from oracle", e);
        } finally {
            disconnect();
        }
        return clientRoutes;
    }

    public List<Map<String, String>> getRouteHistory(long customerId) {
        List<Map<String, String>> clientRouteHistory = new ArrayList<>();
        createConnection();

        try (ResultSet resultSet = oracleStatements.getClientRouteHistory(customerId).executeQuery()) {
            while (resultSet.next()) {
                Map<String, String> route = new HashMap<>(2);
                route.put("client", String.valueOf(resultSet.getLong(1)));
                route.put("nextNode", String.valueOf(resultSet.getShort(2)));
                route.put("updateTime", String.valueOf(resultSet.getString(3)));
                clientRouteHistory.add(route);
            }
        } catch (Exception e) {
            logger.error("Failed to get route history from oracle", e);
        } finally {
            disconnect();
        }
        return clientRouteHistory;
    }

    private void reconnect() {
        try {
            if (!(connection != null && connection.isValid(1))) {
                connect();
                logger.info("WD reconnected with Oracle");
            }
        } catch (Exception e) {
            logger.error("Failed to reconnect with oracle", e);
        }
    }

    @Override
    public void checkConnectivity() throws Exception {
        try {
            connection = DriverManager.getConnection(
                    dbConfig.getUrl(), dbConfig.getUser(), dbConfig.getPassword());
        } catch (Exception e) {
            logger.error("Failed to create oracle connection", e);
            throw e;
        }
    }
}
