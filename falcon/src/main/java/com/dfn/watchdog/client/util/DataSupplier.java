package com.dfn.watchdog.client.util;

import com.dfn.watchdog.client.WatchdogClient;
import com.dfn.watchdog.commons.Node;
import com.dfn.watchdog.commons.NodeType;
import com.dfn.watchdog.commons.State;
import com.dfn.watchdog.commons.View;
import com.dfn.watchdog.commons.messages.monitoring.ExternalLinkStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains static methods to provide data for web interface.
 */
public class DataSupplier {
    private static final ObjectMapper jsonMapper = new ObjectMapper();

    public static String getViewForTree(View view) throws JsonProcessingException {
        Map<String, Node> wdView = view.getAllNodes();
        Map<String, Object> nodeMap = new HashMap<>();
        List<Map<String, Object>> nodeList = new ArrayList<>();

        nodeMap.put("messageType", "tree");
        nodeMap.put("name", "CONTROLLER");
        nodeMap.put("parent", "null");
        nodeMap.put("value", 12);
        nodeMap.put("outer", 1);
        nodeMap.put("type", "black");
        nodeMap.put("level", "#0d4e0b");
        nodeMap.put("children", nodeList);

        for (Node n : wdView.values()) {
            String type = n.getType().toString();
            String name = n.getName();
            List<Map<String, Object>> bottomNodeList = null;

            Map<String, Object> typeMap;
            for (Map<String, Object> m : nodeList) {
                if (type.equals(m.get("name"))) {
                    bottomNodeList = (List) m.get("children");
                }
            }
            if (bottomNodeList == null) {
                typeMap = new HashMap<>();
                bottomNodeList = new ArrayList<>();
                typeMap.put("name", type);
                typeMap.put("parent", "CONTROLLER");
                typeMap.put("value", 12);
                typeMap.put("outer", 1);
                typeMap.put("type", "black");
                typeMap.put("level", "#052304");
                typeMap.put("children", bottomNodeList);
                nodeList.add(typeMap);
            }

            Map<String, Object> bottomNode = new HashMap<>();
            bottomNode.put("name", name.split("-")[1]);
            bottomNode.put("parent", type);
            bottomNode.put("value", 10);
            bottomNode.put("outer", 1);
            bottomNode.put("type", "black");
            bottomNode.put("level", ClientConstants.treeColorMap.get(n.getState().toString()));
            bottomNodeList.add(bottomNode);
        }
        return jsonMapper.writeValueAsString(nodeMap);
    }

    public static String getViewForBlocks(View view, Map links) throws JsonProcessingException {
        Map<String, Node> wdView = view.getAllNodes();
        Map<String, Object> nodeMap = new HashMap<>();

        Map<String, List<Object>> blockMap = new HashMap<>();
        nodeMap.put("messageType", "block");
        nodeMap.put("block", blockMap);
        nodeMap.put("links", links);

        for (Node n : wdView.values()) {
            String type = n.getType().toString();
            short nodeId = n.getId();
//            String name = n.getName();
            String name = ClientConstants.tempNameMap.get(type) + "-" + nodeId;

            List<Object> nodeList;
            if (blockMap.containsKey(type)) {
                nodeList = blockMap.get(type);
            } else {
                nodeList = new ArrayList<>();
                blockMap.put(type, nodeList);
            }

            Map<String, Object> node = new HashMap<>();
            nodeList.add(node);
            node.put("x", ClientConstants.blockPositionMap.get(n.getType()));
            node.put("y", nodeId);
            node.put("text", name);
            node.put("state", n.getState().toString());
        }

        Map<String, Map<String, State>> modifiedLinks = new HashMap<>();
        nodeMap.put("links", modifiedLinks);
        for (Map.Entry<String, Map<String, State>> m1 : ((Map<String, Map<String, State>>) links).entrySet()) {
            Map<String, State> modifiedInnerLinks = new HashMap<>();
            modifiedLinks.put(ClientConstants.transformName(m1.getKey()), modifiedInnerLinks);
            for (Map.Entry<String, State> m2 : m1.getValue().entrySet()) {
                modifiedInnerLinks.put(ClientConstants.transformName(m2.getKey()), m2.getValue());
            }
        }

        String type = "EXCHANGE";
        Map<String, Object> nodeListAsMap = new HashMap<>();
        Map<String, Map<String, ExternalLinkStatus>> externalLinks = WatchdogClient.INSTANCE.getExternalLinks();
        for (Map.Entry<String, Map<String, ExternalLinkStatus>> m1 : externalLinks.entrySet()) {
            for (Map.Entry<String, ExternalLinkStatus> m2 : m1.getValue().entrySet()) {
                ExternalLinkStatus linkStatus = m2.getValue();
                Map<String, Object> node = new HashMap<>();
                String[] temp = linkStatus.getDestinationNode().split("-");
                node.put("x", ClientConstants.blockPositionMap.get(NodeType.valueOf(temp[0])));
                node.put("y", temp[1]);
                node.put("text", linkStatus.getExternalNodeName());
                node.put("state", State.UNKNOWN);
                nodeListAsMap.put(linkStatus.getExternalNodeName(), node);

                Map<String, State> exchangeConnectivity;
                if (modifiedLinks.containsKey(m1.getKey())) {
                    exchangeConnectivity = modifiedLinks.get(m1.getKey());
                } else {
                    exchangeConnectivity = new HashMap<>();
                    modifiedLinks.put(m1.getKey(), exchangeConnectivity);
                }
                exchangeConnectivity.put(linkStatus.getExternalNodeName(), linkStatus.getState());
            }
        }
        blockMap.put(type, new ArrayList<>(nodeListAsMap.values()));



        /*String type = "EXCHANGE";
        List<Object> nodeList = new ArrayList<>();
        blockMap.put(type, nodeList);

        short nodeId = 1;
        String name = "TDWL";
        Map<String, Object> node = new HashMap<>();
        nodeList.add(node);
        node.put("x", ClientConstants.blockPositionMap.get(NodeType.EXCHANGE));
        node.put("y", nodeId);
        node.put("text", name);
        node.put("state", State.UNKNOWN);

        nodeId = 2;
        name = "DFM";
        node = new HashMap<>();
        nodeList.add(node);
        node.put("x", ClientConstants.blockPositionMap.get(NodeType.EXCHANGE));
        node.put("y", nodeId);
        node.put("text", name);
        node.put("state", State.UNKNOWN);


        HashMap<String, State> exchangeConnectivity = new HashMap<>();
        exchangeConnectivity.put("TDWL", State.CONNECTED);
        exchangeConnectivity.put("DFM", State.CONNECTED);
        modifiedLinks.put("DFIX-1", exchangeConnectivity);

        exchangeConnectivity = new HashMap<>();
        exchangeConnectivity.put("TDWL", State.CLOSED);
        exchangeConnectivity.put("DFM", State.CLOSED);
        modifiedLinks.put("DFIX-2", exchangeConnectivity);*/



        return jsonMapper.writeValueAsString(nodeMap);
    }
}
