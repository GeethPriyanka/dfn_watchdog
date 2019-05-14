package com.dfn.watchdog.client.database;

import com.dfn.watchdog.client.WatchdogClient;
import com.dfn.watchdog.client.database.pojo.*;
import com.dfn.watchdog.client.util.ClientProperties;
import com.dfn.watchdog.client.util.gatewaybeans.GCommonMessageAsync;
import com.dfn.watchdog.client.util.gatewaybeans.GCommonResponseAsync;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.h2.tools.Server;

import java.net.URI;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by dasunp on Oct, 2018
 * Used to 'up' the database server, and creates necessary tables for gateways to write upon.
 */
public final class DatabaseUtil implements IDatabaseUtil{

    private static final Logger logger = LogManager.getLogger(DatabaseUtil.class);

    private static final String ALLOW_OTHERS_TAG = "-tcpAllowOthers";
    private static final String TCP_PORT_FLAG = "-tcpPort";

    private static final String ACTIVE_FLAG = "ACTIVE";

    private static final ObjectMapper mapper = new ObjectMapper();


    private static DatabaseUtil instance;
    private ClientProperties properties;
    private BasicDataSource connectionPool;

    private DatabaseUtil(ClientProperties properties) {
        this.properties = properties;
    }

    public static DatabaseUtil getInstance(ClientProperties properties) {
        if(instance == null) instance = new DatabaseUtil(properties);
        return instance;
    }

    /**
     * Start the server for interacting with the database.
     * Multiple clients can get connected to this server, hence the database.
     * @param args for TCP server
     */
    @Override
    public void startTcpServer(String[] args) {
        try {
            Server server = Server.createTcpServer(args).start();
            logger.info("Standalone database running at port: " + server.getPort());
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Stop a tcp server that is already running
     * @param server that is already up and running
     */
    @Override
    public void stopTcpServer(Server server) {
        logger.info(getTcpServerStatus(server));
        logger.info("Standalone database proceeding to shutdown..");
        server.stop();  // stop the server
        logger.info("Standalone database shutted down.");
    }

    /**
     * Initialize the tcp server, create the database connection pool (Default 3 connections), creates the tables if not exist.
     */
    @Override
    public void initialize() {
        startTcpServer(new String[]{ALLOW_OTHERS_TAG, TCP_PORT_FLAG, String.valueOf(getHostPortFromUrl(properties.getStandaloneDatabase().getUrl()).get("port"))});
        this.connectionPool = new BasicDataSource();
        this.connectionPool.setUsername(properties.getStandaloneDatabase().getUser());
        this.connectionPool.setPassword(properties.getStandaloneDatabase().getPassword());
        this.connectionPool.setDriverClassName(properties.getStandaloneDatabase().getDriverClass());
        this.connectionPool.setUrl(properties.getStandaloneDatabase().getUrl());
        this.connectionPool.setInitialSize(3);
        createTables();
    }

    /**
     * Create database tables if not exist
     */
    @Override
    public void createTables() {
        if(properties.isEnableDatabase()) {
            QueryRunner queryRunner;
            try {
                queryRunner = new QueryRunner(this.connectionPool);
                queryRunner.update(StandaloneDatabaseQueries.SESSION_TABLE_CREATION_QUERY);
                queryRunner.update(StandaloneDatabaseQueries.CREATE_MESSAGES_TABLE_QUERY);
                queryRunner.update(StandaloneDatabaseQueries.CREATE_RESPONSES_QUERY);
                logger.info("Standalone Database creation done successfully.");
            } catch (SQLException e) {
                logger.error(e.getMessage());
            }
        }
    }

    @Override
    public void insert(SessionPojo session) {
        QueryRunner queryRunner;
        if(session.getClientIp() == null) {
            session.setClientIp("NIL/NOT PROVIDED");
        }
        try {
            queryRunner = new QueryRunner(this.connectionPool);
            boolean exists = readFromDbUtils(session.getLoginId());
            if(exists) {
                queryRunner.update(StandaloneDatabaseQueries.UPDATE_EXPIRED_SESSION, "EXPIRED", session.getLoginId());
            }
            queryRunner.update(
                    StandaloneDatabaseQueries.SESSION_INSERTION_QUERY,
                    session.getSessionId(),
                    session.getLoginId(),
                    session.getExpiryTime(),
                    session.getStartTime(),
                    session.getClientIp(),
                    session.getClientChannel(),
                    null,
                    ACTIVE_FLAG    // for now
            );
            logger.info("Session persisted.");
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * JDBC method of using batch inserts.
     * @param sessions
     * @return
     */
    public void insertSessionsBatch(Queue<SessionPojo> sessions) throws SQLException {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = connectionPool.getConnection();
            ps = connection.prepareStatement(StandaloneDatabaseQueries.SESSION_INSERTION_QUERY);
            for (SessionPojo session: sessions) {
                if(session.getClientIp() == null) {
                    session.setClientIp("NIL/NOT PROVIDED");
                }
                ps.setString(1, session.getSessionId());
                ps.setLong(2, session.getLoginId());
                ps.setTimestamp(3, session.getExpiryTime());
                ps.setTimestamp(4, session.getStartTime());
                ps.setString(5, session.getClientIp());
                ps.setInt(6, session.getClientChannel());
                ps.setTimestamp(7, null);
                ps.setString(8, ACTIVE_FLAG);
                ps.addBatch();
            }
            ps.executeBatch();
            sessions.clear();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            ps.close();
            connection.close();
        }
    }

    /**
     * For testing purposes only.
     * @param message
     * @param request
     * @param time
     */
    @Override
    public void insertMessage(GCommonMessageAsync message, String request, Timestamp time) {
        QueryRunner queryRunner;
        try {
            queryRunner = new QueryRunner(this.connectionPool);
            String uid = message.getHeader().getRequestId() == null ? "DEFAULT_UID" : message.getHeader().getRequestId();
            if(message.getData() != null) {
                queryRunner.update
                        (
                                StandaloneDatabaseQueries.INSERT_MESSAGES_QUERY,
                                uid,
                                message.getHeader().getServiceId(),
                                message.getHeader().getChannel(),
                                message.getHeader().getCommVer(),
                                message.getHeader().getLoginId(),
                                message.getHeader().getSessionId(),
                                message.getHeader().getClientIp(),
                                message.getHeader().getTenantCode(),
                                message.getData().getCustomerId(),
                                request,
                                message.getTimestamp(),
                                null,
                                time
                        );
            } else {
                queryRunner.update
                        (
                                StandaloneDatabaseQueries.INSERT_MESSAGES_QUERY,
                                uid,
                                message.getHeader().getServiceId(),
                                message.getHeader().getChannel(),
                                message.getHeader().getCommVer(),
                                message.getHeader().getLoginId(),
                                message.getHeader().getSessionId(),
                                message.getHeader().getClientIp(),
                                message.getHeader().getTenantCode(),
                                0,
                                request,
                                message.getTimestamp(),
                                null,
                                time
                        );
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }

    }

    /**
     * Insert messages as batches
     * @param messages
     * @return
     */
    public void insertMessageBatch(Queue<GCommonMessageAsync> messages) throws SQLException {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = connectionPool.getConnection();
            ps = connection.prepareStatement(StandaloneDatabaseQueries.INSERT_MESSAGES_QUERY);
            for (GCommonMessageAsync message: messages) {
                String uid = message.getHeader().getRequestId() == null ? "DEFAULT_UID" : message.getHeader().getRequestId();
                ps.setString(1, uid);
                ps.setInt(2, message.getHeader().getServiceId());
                ps.setInt(3, message.getHeader().getChannel());
                ps.setString(4, message.getHeader().getCommVer());
                ps.setLong(5, message.getHeader().getLoginId());
                ps.setString(6, message.getHeader().getSessionId());
                ps.setString(7, message.getHeader().getClientIp());
                ps.setString(8, message.getHeader().getTenantCode());
                ps.setLong(9,message.getData() != null ? message.getData().getCustomerId() : 0);
                try {
                    ps.setString(10, mapper.writeValueAsString(message));
                } catch (JsonProcessingException e) {
                    ps.setString(10, "JSON PARSE ERROR!");
                    logger.error(e.getMessage());
                }
                ps.setTimestamp(11, message.getTimestamp());

                ps.setString(12, null);
                ps.addBatch();
            }
            ps.executeBatch();
            messages.clear();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            ps.close();
            connection.close();
        }
    }

    /**
     * Reads only the active sessions.
     * @return Array (list) of SessionPojo Instances.
     */
    @Override
    public List<SessionPojoJson> readActiveSessions() {
        QueryRunner queryRunner = new QueryRunner(this.connectionPool);
        ResultSetHandler<List<SessionPojo>> resultSet = new BeanListHandler<>(SessionPojo.class);
        try {
            List<SessionPojo> sessionPojos = queryRunner.query(StandaloneDatabaseQueries.ACTIVE_SESSIONS_QUERY, resultSet);
            return parseSessions(sessionPojos);
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return  Collections.emptyList();
    }

    /**
     * Parse the sessions without using Java Stream API.
     * @param sessionPojos the resultset fetched from db.
     * @return parsed sessions array.
     */
    private List<SessionPojoJson> parseSessions(List<SessionPojo> sessionPojos) {
        List<SessionPojoJson> sessionPojoJsons = new ArrayList<>();
        sessionPojos.forEach(sessionPojo -> {
            SessionPojoJson sessionPojoJson = new SessionPojoJson();
            sessionPojoJson.setSessionId(sessionPojo.getSessionId());
            sessionPojoJson.setLoginId(sessionPojo.getLoginId());
            sessionPojoJson.setExpiryTime(sessionPojo.getExpiryTime().toString());
            sessionPojoJson.setStartTime(getStringTime(sessionPojo.getStartTime()));
            sessionPojoJson.setClientIp(sessionPojo.getClientIp());
            sessionPojoJson.setClientChannel(sessionPojo.getClientChannel());
            sessionPojoJson.setLogoutTime(null);
            sessionPojoJson.setUpTime(getTimeDifference(sessionPojo.getStartTime()));
            sessionPojoJson.setStatus(sessionPojo.getStatus());
            sessionPojoJsons.add(sessionPojoJson);
        });
        return sessionPojoJsons;
    }

    /**
     * Use if needed.
     * Get a specific SessionPojo instance from the database
     * @param loginId session ID of a Session.
     * @return SessionPojo instance
     */
    @Override
    public boolean readFromDbUtils(long loginId) throws SQLException {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            connection = connectionPool.getConnection();
            ps = connection.prepareStatement(StandaloneDatabaseQueries.SESSION_RETRIEVAL_QUERY);
            ps.setLong(1, loginId);
            rs = ps.executeQuery();
            if(rs.next()) result = true;
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            ps.close();
            connection.close();
        }
        return result;
    }

    /**
     * Retrieve all the persisted messages from the database.
     * @return list of message beans.
     */
    @Override
    public List<MessagePojo> readAllMessages() {
        QueryRunner queryRunner;
        ResultSetHandler<List<MessagePojo>> resultSet = new BeanListHandler<>(MessagePojo.class);
        try {
            queryRunner = new QueryRunner(this.connectionPool);
            return queryRunner.query(StandaloneDatabaseQueries.RETRIEVE_MESSAGES_QUERY, resultSet);
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * Retrieve all responses
     * Deserialized into a MessagePojo instance because the fields are the same.
     * @return
     */
    @Override
    public List<MessagePojo> readAllResponses() {
        QueryRunner queryRunner;
        ResultSetHandler<List<MessagePojo>> resultSet = new BeanListHandler<>(MessagePojo.class);
        try {
            queryRunner = new QueryRunner(this.connectionPool);
            return queryRunner.query(StandaloneDatabaseQueries.RETRIEVE_RESPONSES_QUERY, resultSet);
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * Retrieve messages by session ID
     * @param sessionId of the user
     * @return a list of messages for a particular user.
     */
    @Override
    public List<MessagePojo> readSpecificMessages(String sessionId) {
        QueryRunner queryRunner;
        ResultSetHandler<List<MessagePojo>> resultSet = new BeanListHandler<>(MessagePojo.class);
        try {
            queryRunner = new QueryRunner(this.connectionPool);
            return queryRunner.query(StandaloneDatabaseQueries.RETRIEVE_SPECIFIC_MESSAGE, resultSet, sessionId);
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * Retrieve specific responses.
     * @param uid
     * @return
     */
    @Override
    public List<MessagePojo> readSpecificResponses(String uid) {
        QueryRunner queryRunner;
        ResultSetHandler<List<MessagePojo>> resultSet = new BeanListHandler<>(MessagePojo.class);
        try {
            queryRunner = new QueryRunner(this.connectionPool);
            return queryRunner.query(StandaloneDatabaseQueries.RETRIEVE_SPECIFIC_RESPONSES_QUERY, resultSet, uid);
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * Get the messages according to SLA constraints.
     * @return list of messages going beyond SLA constraints.
     */
    public List<MessagePojo> readSlaMessages() {
        QueryRunner queryRunner;
        List<MessagePojo> slaExceededMessages = new ArrayList<>();
        Map<Integer, Long> slaConfiguration = WatchdogClient.INSTANCE.getSlaMapConfig().getRoundTimes();
        try {
            queryRunner = new QueryRunner(this.connectionPool);
            ResultSetHandler<List<MessagePojo>> resultSet = new BeanListHandler<>(MessagePojo.class);
            List<MessagePojo> list = queryRunner.query(StandaloneDatabaseQueries.RETRIEVE_SLA_MESSAGES_QUERY, resultSet);
            final long[] time = {0};
            list.forEach(message -> {
                logger.warn("Checking message: " + message.getUnique_request_id() + " of type: " + message.getMessage_type());
                if(WatchdogClient.INSTANCE.getSlaMapConfig().getDefaultSlaTime().isEnabled()) {
                    time[0] = WatchdogClient.INSTANCE.getSlaMapConfig().getDefaultSlaTime().getDefaultTime();
                } else {
                    time[0] = slaConfiguration.get(message.getMessage_type());
                }
                if(message.getResponseTime() >= time[0]){
                    slaExceededMessages.add(message);
                }
            });
            return slaExceededMessages;
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return Collections.emptyList();
    }

    @Override
    public void insertResponse(GCommonResponseAsync responseBean, String responseString, Timestamp time) {
        QueryRunner queryRunner;
        try {
            queryRunner = new QueryRunner(this.connectionPool);
            Timestamp messageTimestamp = retrieveSpecificTimestamp(responseBean.getHeader().getRequestId());
            if(messageTimestamp != null) {
                long diff = getTimeDifferenceLong(messageTimestamp, responseBean.getTimestamp());
                queryRunner.update(
                        StandaloneDatabaseQueries.UPDATE_RESPONSE_TIME,
                        diff,
                        responseBean.getHeader().getRequestId()
                );
            } else {
                logger.info("Message Timestamp is null. Unique_Request_ID: " + responseBean.getHeader().getRequestId() + ", pertaining to session: " + responseBean.getHeader().getSessionId());
            }
            queryRunner.update(
                    StandaloneDatabaseQueries.INSERT_RESPONSES_QUERY,
                    responseBean.getHeader().getRequestId(),
                    responseBean.getHeader().getServiceId(),
                    responseBean.getHeader().getChannel(),
                    responseBean.getHeader().getCommVer(),
                    responseBean.getHeader().getLoginId(),
                    responseBean.getHeader().getSessionId(),
                    responseBean.getHeader().getClientIp(),
                    responseBean.getHeader().getTenantCode(),
                    responseString,
                    responseBean.getTimestamp(),
                    time
            );
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

    private Timestamp retrieveSpecificTimestamp(String uid) throws SQLException {
        QueryRunner queryRunner;
        queryRunner = new QueryRunner(this.connectionPool);
        return queryRunner.query(StandaloneDatabaseQueries.RETRIEVE_SPECIFIC_TIMESTAMP, new ScalarHandler<>() , uid);
    }

    /**
     * Insert responses as a batch
     * @param responses
     * @return
     * @throws SQLException
     */
    public void insertResponseBatch(Queue<GCommonResponseAsync> responses) throws SQLException {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = connectionPool.getConnection();
            ps = connection.prepareStatement(StandaloneDatabaseQueries.INSERT_RESPONSES_QUERY);
            for (GCommonResponseAsync response: responses) {
                ps.setString(1, response.getHeader().getRequestId());
                ps.setInt(2, response.getHeader().getServiceId());
                ps.setInt(3, response.getHeader().getChannel());
                ps.setString(4, response.getHeader().getCommVer());
                ps.setLong(5, response.getHeader().getLoginId());
                ps.setString(6, response.getHeader().getSessionId());
                ps.setString(7, response.getHeader().getClientIp());
                ps.setString(8, response.getHeader().getTenantCode());
                try {
                    ps.setString(9, mapper.writeValueAsString(response));
                } catch (JsonProcessingException e) {
                    ps.setString(9, "JSON PARSE ERROR");
                    logger.error(e.getMessage());
                }
                ps.setTimestamp(10, response.getTimestamp());
                ps.addBatch();
            }
            ps.executeBatch();
            responses.clear();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            ps.close();
            connection.close();
        }
    }

    /**
     * Endpoint to draw the graph
     * @param sessionId of the session
     * @return map needed to draw the graph
     * @throws SQLException if an SQL error arises.
     */
    public Map<String, Integer> drawGraph(String sessionId) throws SQLException {
        QueryRunner queryRunner = new QueryRunner(this.connectionPool);
        HashMap<String, Integer> result = new HashMap<>();
        List<Integer> messageTypes = fetchDistinctMessageTypes(sessionId);
        messageTypes.forEach(messageType -> {
            try {
                result.put(String.valueOf(messageType),
                        queryRunner.query(
                                StandaloneDatabaseQueries.COUNT_DISTINCT_MESSAGES,
                                new ScalarHandler<>(),
                                sessionId,
                                messageType
                        )
                );
            } catch (SQLException e) {
                logger.error(e.getMessage());
            }
        });
        return result;
    }


    /**
     * Fetch distinct message types from the db
     * @param sessionId of the session
     * @return list of message types of type Integer.
     * @throws SQLException if an SQL error arises.
     */
    private List<Integer> fetchDistinctMessageTypes(String sessionId) throws SQLException {
        List<Integer> results = new ArrayList<>();
        Connection connection;
        PreparedStatement ps;
        ResultSet rs;
        connection = connectionPool.getConnection();
        ps = connection.prepareStatement(StandaloneDatabaseQueries.DISTINCT_MESSAGE_TYPES);
        ps.setString(1, sessionId);
        rs = ps.executeQuery();
        while (rs.next()) results.add(rs.getInt(1));
        ps.close();
        connection.close();
        return results;
    }

    public List<TimeCountMap> getClientCountMap() throws SQLException {
        QueryRunner queryRunner = new QueryRunner(this.connectionPool);
        ResultSetHandler<List<TimeCountMap>> resultSet = new BeanListHandler<>(TimeCountMap.class);
        return queryRunner.query(StandaloneDatabaseQueries.STARTTIME_COUNT_QUERY, resultSet);
    }

    /**
     * Get the status of the server
     * @param server
     * @return TCP server status with the db.
     */
    private String getTcpServerStatus(Server server) { return server.getStatus(); }

    /**
     * A utility method to get host and port from jdbc uri.
     * @param uri of jdbc
     * @return a map with {host: ... , port : ...}
     */
    private Map getHostPortFromUrl(String uri) {
        URI cleanUri = URI.create(uri.substring(8));
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("host", cleanUri.getHost());
        hashMap.put("port", String.valueOf(cleanUri.getPort()));
        return hashMap;
    }

    /**
     * Calculates the up time based on start time.
     * @param startTime of the session.
     * @return a string formatted with the up time.
     */
    private String getTimeDifference(Timestamp startTime) {
        long milliseconds = (new Timestamp(System.currentTimeMillis())).getTime() - startTime.getTime();
        int seconds = (int) milliseconds / 1000;
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        seconds = (seconds % 3600) % 60;
        return hours + " hours " + minutes + " minutes " + seconds + " seconds " + milliseconds + " milliseconds";
    }

    private long getTimeDifferenceLong(Timestamp start, Timestamp end) {
        return end.getTime() - start.getTime();
    }

    private String getStringTime(Timestamp startTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return dateFormat.format(startTime);
    }
}
