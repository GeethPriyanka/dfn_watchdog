package com.dfn.watchdog.client.database;

import com.dfn.watchdog.client.database.pojo.MessagePojo;
import com.dfn.watchdog.client.database.pojo.SessionPojo;
import com.dfn.watchdog.client.database.pojo.SessionPojoJson;
import com.dfn.watchdog.client.util.gatewaybeans.GCommonMessage;
import com.dfn.watchdog.client.util.gatewaybeans.GCommonMessageAsync;
import com.dfn.watchdog.client.util.gatewaybeans.GCommonResponseAsync;
import com.dfn.watchdog.client.util.gatewaybeans.GCommonResponseDb;
import org.h2.tools.Server;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by dasunp on Oct, 2018
 */
public interface IDatabaseUtil {

    void startTcpServer(String[] args);

    void stopTcpServer(Server server);

    void initialize();

    void createTables();

    List<SessionPojoJson> readActiveSessions();

    List<MessagePojo> readSpecificMessages(String sessionId);

    List<MessagePojo> readSpecificResponses(String uid);

    List<MessagePojo> readAllResponses();

    List<MessagePojo> readAllMessages();

    boolean readFromDbUtils(long sessionId) throws SQLException;

    void insert(SessionPojo session);

    void insertMessage(GCommonMessageAsync message, String request, Timestamp time);

    void insertResponse(GCommonResponseAsync responseBean, String responseString, Timestamp time);

}
