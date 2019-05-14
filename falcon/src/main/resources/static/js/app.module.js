/**
 * Main module.
 */
var app = angular.module('watchdogClient', [
    'ngRoute',
    'watchdogHome',
    'watchdogDashboard',
    'watchdogNocDashboard',
    'watchdogNavigation',
    'watchdogView',
    'watchdogRequests',
    'watchdogManage',
    'watchdogReconcile',
    'watchdogSessions',
    'watchdogMessagesDatatables',
    'watchdogSpecificMessages',
    'watchdogSlaMessages',
    'watchdogClientCount'
]);

app.run(['$rootScope', '$http', function ($rootScope, $http) {
    console.log("angular app.run called");

    $rootScope.currentSession = {};

    $rootScope.$on('event', function (ev, data) {
        $rootScope.$apply();
    });

    $http.get('/server/config').then(function (response) {
        $rootScope.serverConfig = response.data;
        connectWebsocket($rootScope.serverConfig["websocketUrl"]);
    });

    var connectWebsocket = function (websocketUrl) {
        // var serverIp = 'ntp-falconws.directfn.net/websocket';

        var webSocket = new WebSocket('ws://' + websocketUrl);
        $rootScope.websocketValue = 'connecting to websocket';
        $rootScope.cpuHistory = {};

        webSocket.onopen = function (message) {
            sendMessage();
            processOpen(message)
        };
        webSocket.onclose = function (message) {
            processClose(message)
        };
        webSocket.onerror = function (message) {
            processError(message)
        };
        webSocket.onmessage = function (message) {
            processMessage(message)
        };

        function sendMessage() {
            var result = webSocket.send("Socket Connected from " + websocketUrl);
        }

        function processOpen(message) {
            $rootScope.websocketValue = 'Connected to ' + websocketUrl;
        }

        function processMessage(message) {
            $rootScope.websocketValue = message.data;

            var wsMessage = JSON.parse(message.data);
            if (wsMessage.messageType == 'view') {
                console.log("view message received");
                $rootScope.view = JSON.parse(message.data);
            } else if (wsMessage.messageType == 'metric') {
                console.log("metric message received");
                var metric = JSON.parse(message.data);
                var view = $rootScope.view;
                //$rootScope.view.nodes[metric.node].metric = metric;

                for (var i = 0, iLen = view.nodes.length; i < iLen; i++) {

                    if (view.nodes[i].nodeName == metric.node) {
                        view.nodes[i].metric = metric;
                        addCpuUsage(view.nodes[i]);
                    }
                }
            } else if (wsMessage.messageType == 'sys_metric') {
                console.log("system metric message received");
                $rootScope.sysMetrics = JSON.parse(message.data);
            } else if (wsMessage.messageType == 'tree') {
                console.log("tree message received");
                $rootScope.nodeTree = JSON.parse(message.data);
            } else if (wsMessage.messageType == 'block') {
                console.log("block message received");
                $rootScope.nodeBlock = JSON.parse(message.data);
            } else if (wsMessage.messageType == 'tpsCount') {
                console.log("TPS message received");
                $rootScope.tpsCount = JSON.parse(message.data);
            } else if (wsMessage.messageType == 'gatewayMetric') {
                console.log("gatewayMetric message received");
                $rootScope.sysMetrics["clients"] = wsMessage.connected;
                if (0 != wsMessage.tps) {
                    $rootScope.sysMetrics["tps"] = wsMessage.tps;
                }
            } else if (wsMessage.messageType == 'omsMetric') {
                console.log("omsMetric message received");
                $rootScope.sysMetrics["requests"] = wsMessage.requests;

                /*function loop() {
                    setTimeout(function () {
                        $rootScope.sysMetrics["requests"] = Math.max(wsMessage.requests, $rootScope.sysMetrics["requests"])
                        if ($rootScope.sysMetrics["requests"] != wsMessage.requests) loop();
                    }, 20)
                }
                loop();*/
            } else if (wsMessage.messageType == 'server_connect') {
                console.log("server_connect message received connected: "+wsMessage.connected);
                if (wsMessage.connected) {
                    $('#navbar').css('background-color', 'green')
                } else {
                    $('#navbar').css('background-color', 'red')
                }
            } else if (wsMessage.messageType == 'showKibanaDashboard') {
                console.log("showKibanaDashboard message received show: "+wsMessage.show);
                $rootScope.showKibanaDashboard = wsMessage.show;
            } else {
                console.log("unhandled message received");
            }

            $rootScope.metric = JSON.parse(message.data);
            $rootScope.$broadcast('websocketValue', message.data);
            $rootScope.$apply();
        }

        function processClose(message) {
            $rootScope.websocketValue = '';
            reconnectWebsocket($rootScope.serverConfig["websocketUrl"]);
        }

        function processError(message) {
            $rootScope.websocketValue = '';
        }

        function addCpuUsage(node) {
            var nodeCpu = $rootScope.cpuHistory[node.nodeName];
            if (nodeCpu == null) {
                nodeCpu = [];
                $rootScope.cpuHistory[node.nodeName] = nodeCpu;
            }
            if (nodeCpu.length > 200) {
                nodeCpu.shift();
                nodeCpu.push(node.metric.systemCpuUsage)
            } else {
                nodeCpu.push(node.metric.systemCpuUsage)
            }
        }

        function reconnectWebsocket(websocketUrl) {
            setTimeout(function() {
                connectWebsocket(websocketUrl);
            }, 5000);
        }
    };

}]);
