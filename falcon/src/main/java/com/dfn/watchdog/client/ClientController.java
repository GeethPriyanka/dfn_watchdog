package com.dfn.watchdog.client;

import com.dfn.watchdog.client.api.ClientRoutes;
import com.dfn.watchdog.client.database.DatabaseUtil;
import com.dfn.watchdog.client.database.pojo.MessagePojo;
import com.dfn.watchdog.client.database.pojo.SessionPojo;
import com.dfn.watchdog.client.database.pojo.SessionPojoJson;
import com.dfn.watchdog.client.database.pojo.TimeCountMap;
import com.dfn.watchdog.client.database.queues.MessageQueue;
import com.dfn.watchdog.client.database.queues.ResponseQueue;
import com.dfn.watchdog.client.database.queues.SessionQueue;
import com.dfn.watchdog.client.util.ClientProperties;
import com.dfn.watchdog.client.util.DataSupplier;
import com.dfn.watchdog.client.util.gatewaybeans.GCommonMessageAsync;
import com.dfn.watchdog.client.util.gatewaybeans.GCommonResponseAsync;
import com.dfn.watchdog.commons.Node;
import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.db.*;
import com.dfn.watchdog.commons.messages.client.AsyncRequest;
import com.dfn.watchdog.commons.messages.client.ClientRouteRequest;
import com.dfn.watchdog.commons.messages.client.ClientRouteResponse;
import com.dfn.watchdog.commons.messages.client.RouteResponseAggregated;
import com.dfn.watchdog.commons.messages.commands.StartEod;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

/**
 * watchdog client services.
 */

@RestController
@RequestMapping("/watchdogclient")
public class ClientController {
    private static final Logger logger = LogManager.getLogger(ClientController.class);
    private DatabaseConnection databaseConnection;
    private final ObjectMapper jsonMapper;
    private static final ClientProperties properties = WatchdogClient.INSTANCE.getProperties();

    // == Scheduler Factory ==
    private SchedulerFactory schedulerFactory;

    // ==== Constants ==== //
    private static final String STATUS_CONSTANT = "status";

    // ==== Queues ==== //
    private static MessageQueue messageQueue = new MessageQueue(properties);
    private static ResponseQueue responseQueue = new ResponseQueue(properties);
    private static SessionQueue sessionQueue = new SessionQueue(properties);

    @Autowired
    public ClientController() {
        ClientProperties properties = WatchdogClient.INSTANCE.getProperties();
        try {
            databaseConnection = DatabaseUtils.connectToDatabase(properties.database());
        } catch (Exception e) {
            logger.error("Error on connecting to db", e);
        }
        jsonMapper = new ObjectMapper();
        this.schedulerFactory = new StdSchedulerFactory();
    }

    @RequestMapping("/route/{clientId}")
    public Object requestRoute(@PathVariable String clientId) {
        AsyncRequest request = new ClientRouteRequest(Long.valueOf(clientId));
        RouteResponseAggregated response = (RouteResponseAggregated) WatchdogClient.INSTANCE.getAsyncResult(request);
        System.out.println(response.getEndPoints());
        ClientRoutes routes = new ClientRoutes(response.getClientId(), response.getEndPoints());
        for (ClientRouteResponse r : response.getClientRouteResponseList()) {
            routes.addRoute(r.getSource(), r.getRoute());
        }

        return routes;
    }

    @RequestMapping("/route/all")
    public List<Map<String, String>> requestRouteAll() {
        return databaseConnection.getAllRoutes();
    }

    @RequestMapping("/route/history/{clientId}")
    public List<Map<String, String>> requestRouteHistory(@PathVariable long clientId) {
        return databaseConnection.getRouteHistory(clientId);
    }

    @RequestMapping("/view")
    public List<Map<String, Object>> getCurrentView() {
        Map<String, Object> nodeMap;
        try {
            nodeMap = jsonMapper.readValue(
                    DataSupplier.getViewForTree(WatchdogClient.INSTANCE.getView()),
                    new TypeReference<Map<String, Object>>() {
                    });
        } catch (Exception e) {
            logger.error("Failed to parse view", e);
            nodeMap = new HashMap<>();
        }
        List<Map<String, Object>> viewAsList = new ArrayList<>();
        viewAsList.add(nodeMap);
        return viewAsList;
    }

    @RequestMapping("/slaconfigroundtime")
    public Map<Integer, Long>  getSlaMapConfig(){
        Map<Integer, Long> roundTime = WatchdogClient.INSTANCE.getSlaMapConfig().getRoundTimes();
        return roundTime;
    }

    @RequestMapping("/slaconfigservice")
    public Map<Integer, String>  getSlaMapService(){
        Map<Integer, String> serviceData = WatchdogClient.INSTANCE.getSlaMapConfig().getServiceList();
        return serviceData;
    }

    @RequestMapping("/slaconfigdefaulttime")
    public Map<String, Long>  getSlaMapConfigdefault(){
        Map<String, Long> defaultTimeData = new HashMap<>();
        Long defaultTime = WatchdogClient.INSTANCE.getSlaMapConfig().getDefaultSlaTime().getDefaultTime();
        boolean isEnabled = WatchdogClient.INSTANCE.getSlaMapConfig().getDefaultSlaTime().isEnabled();
        String isEnabledStr;
        if(isEnabled){
            isEnabledStr = "True";
        }else{
            isEnabledStr = "False";
        }
        defaultTimeData.put(isEnabledStr,defaultTime);
        return defaultTimeData;
    }

    @RequestMapping("/manageview")
    public List<Map<String, String>> getManageView() {
        Map<String, Node> wdView = WatchdogClient.INSTANCE.getView().getAllNodes();
        List<Map<String, String>> nodeList = new ArrayList<>();
        for (Node n : wdView.values()) {
            Map<String, String> nodeMap = new HashMap<>(4);
            nodeMap.put("NAME", n.getName());
            nodeMap.put("IP", n.getIpAddress());
            nodeMap.put("STATE", n.getState().toString());
            nodeMap.put("BACKUP_STATE", n.getBackupState().toString());
            nodeList.add(nodeMap);
        }
        return nodeList;
    }

    @RequestMapping("/level1fixlogs")
    public List<Map<String, String>> getLevel1FixLogs() {
        logger.warn("This is deprecated. Need to remove from the client ui as well");
        return new ArrayList<>();
    }

    @RequestMapping("/level1orderaudit")
    public List<Map<String, String>> getLevel1OrderAudits() {
        logger.warn("This is deprecated. Need to remove from the client ui as well");
        return new ArrayList<>();
    }

    @RequestMapping("/level2")
    public List<Map<String, String>> getLevel2Records() {
        logger.warn("This is deprecated. Need to remove from the client ui as well");
        return new ArrayList<>();
    }

    @RequestMapping("/cashresolution")
    public List<Map<String, String>> getCashResolutionRecords() {
        logger.warn("This is deprecated. Need to remove from the client ui as well");
        return new ArrayList<>();
    }

    @RequestMapping("/holdingresolution")
    public List<Map<String, String>> getHoldingResolutionRecords() {
        logger.warn("This is deprecated. Need to remove from the client ui as well");
        return new ArrayList<>();
    }

    @RequestMapping(value = "/orderaudit/{key}", method = RequestMethod.GET)
    public Map<String, Object> getOrderAuditFromKey(@PathVariable String key) {
        logger.warn("This is deprecated. Need to remove from the client ui as well");
        return new HashMap<>();
    }

    @RequestMapping(value = "/orderaudit/all/{clOrdId}", method = RequestMethod.GET)
    public List<Map<String, String>> getOrderAuditFromClOrdId(@PathVariable String clOrdId) {
        logger.warn("This is deprecated. Need to remove from the client ui as well");
        return new ArrayList<>();
    }

    @RequestMapping(value = "/eod/start", method = RequestMethod.GET)
    public String startEod() {
        WatchdogClient.INSTANCE.sendToServer(new StartEod((short) 0, NodeType.OMS));
        return "OK";
    }

    /* ================== GET mappings for database record fetches - used at the UI ========================= */

    @GetMapping(value = "/sessions/active")
    public @ResponseBody List<SessionPojoJson> allActiveSessions() {
        return DatabaseUtil.getInstance(properties).readActiveSessions();
    }

    @GetMapping(value = "/messages")
    public @ResponseBody List<MessagePojo> getAllMessages() {
        return DatabaseUtil.getInstance(properties).readAllMessages();
    }

    @GetMapping(value = "/responses")
    public @ResponseBody Object getAllResponses() {
        return DatabaseUtil.getInstance(properties).readAllResponses();
    }

    @GetMapping(value = "/responses/specific")
    public @ResponseBody List<MessagePojo> getSpecificResponses(@RequestParam("uid") String uid) {
        return DatabaseUtil.getInstance(properties).readSpecificResponses(uid);
    }

    @GetMapping(value = "/messages/specific")
    public @ResponseBody Object getSpecificMessages(@RequestParam String sessionId) {
        return DatabaseUtil.getInstance(properties).readSpecificMessages(sessionId);
    }

    @GetMapping(value = "/messages/sla")
    public @ResponseBody Object getSlaMessages() {
        return DatabaseUtil.getInstance(properties).readSlaMessages();
    }

    /* =================== POST mappings from the gateways. Use these for INSERT queries =========================== */

    /*
        Note: Code inside each endpoint controller could be extracted into a service-layer class.
     */

    @PostMapping("/sessions")
    @ResponseBody
    public Object sessions(@RequestBody SessionPojo session) {
        if(WatchdogClient.INSTANCE.getProperties().isEnableQueues()) {
            if (WatchdogClient.INSTANCE.getProperties().getQueueCounts().getSessionQueueCount() > sessionQueue.getSize()) {   // i.e. queue size is lower than the config, persist in the in-memory queue.
                sessionQueue.enqueue(session);
            } else {    // else, do a batch db update.
                sessionQueue.enqueue(session);
                sessionQueue.emit(properties);
            }
        } else {
            DatabaseUtil.getInstance(properties).insert(session);
        }
        return new HashMap<String, String>(){}.put(STATUS_CONSTANT, "1");
    }

    @PostMapping("/messages")
    @ResponseBody
    public Object messages(@RequestBody GCommonMessageAsync message) {
        if(WatchdogClient.INSTANCE.getProperties().isEnableQueues()) {
            if(WatchdogClient.INSTANCE.getProperties().getQueueCounts().getMessageQueueCount() > messageQueue.getSize()) {   // i.e. queue size is lower than the config, persist in the in-memory queue.
                messageQueue.enqueue(message);
            } else { // else, do a batch db update.
                messageQueue.enqueue(message);
                messageQueue.emit(properties);
            }
        } else {    // if message queue is disabled
            try {
                DatabaseUtil.getInstance(properties).insertMessage(message, (new ObjectMapper()).writeValueAsString(message), new Timestamp(System.currentTimeMillis()));
            } catch (JsonProcessingException e) {
                logger.error(e.getMessage());
                return new HashMap<String, String>(){}.put(STATUS_CONSTANT, "0");
            }
        }
        return new HashMap<String, String>(){}.put(STATUS_CONSTANT, "1");
    }

    @PostMapping("/responses")
    @ResponseBody
    public Object responses(@RequestBody GCommonResponseAsync response) {
        if(WatchdogClient.INSTANCE.getProperties().isEnableQueues()) {
            if(WatchdogClient.INSTANCE.getProperties().getQueueCounts().getResponseQueueCount() > responseQueue.getSize()) {    // i.e. queue size is lower than the config, persist in the in-memory queue.
                responseQueue.enqueue(response);
            } else { // else, do a batch db update.
                responseQueue.enqueue(response);
                responseQueue.emit(properties);
            }
        } else {    // if response queue is disabled
            try {
                DatabaseUtil.getInstance(properties).insertResponse(response, (new ObjectMapper()).writeValueAsString(response), new Timestamp(System.currentTimeMillis()));
            } catch (JsonProcessingException e) {
                logger.error(e.getMessage());
                return new HashMap<String, String>(){}.put(STATUS_CONSTANT, "0");
            }
        }
        return new HashMap<String, String>(){}.put(STATUS_CONSTANT, "1");
    }

    /** ====== Get mapping for the graph ======= **/

    @GetMapping("/messages/graph")
    public Object drawGraph(@RequestParam("sessionId") String sessionid) {
        Map<String , Integer> stringIntegerMap = null;
        try {
            stringIntegerMap = DatabaseUtil.getInstance(properties).drawGraph(sessionid);
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return stringIntegerMap;
    }

    @GetMapping("/services")
    public ServiceConfiguration getServices() {
        ServiceConfiguration serviceConfiguration = null;
        try {
            System.out.println(System.getProperty("user.dir"));
            InputStream configStream = new FileInputStream("falcon/services.yaml");
            final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            serviceConfiguration = mapper.readValue(configStream, ServiceConfiguration.class);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return serviceConfiguration;
    }

    @GetMapping("/clientCountMap")
    public List<TimeCountMap> clientCountMap() {
        try {
            return DatabaseUtil.getInstance(properties).getClientCountMap();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return Collections.emptyList();
    }
}