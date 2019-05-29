package com.dfn.watchdog.commons.db;

import java.util.List;
import java.util.Map;

/**
 * Interface for database connection.
 */

public interface DatabaseConnection {
    public void connect();

    public void disconnect();

    public String getClientFromDb(long clientId);

    public void setClientInDb(long clientId, short next);

    public void updateNodeFailure(short failedNode, short backupNode);

    //client related
    public List<Map<String, String>> getAllRoutes();

    public List<Map<String, String>> getRouteHistory(long clientId);

    public String getLogins(String username);

    public void checkConnectivity() throws Exception ;
}
