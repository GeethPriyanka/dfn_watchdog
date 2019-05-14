/**
 * Created by dasunp on Aug, 2018
 * TODO: Implement a summary of messages.
 */
angular
    .module('watchdogMessagesDatatables', [])
    .controller('messages', ['$scope', '$http' , '$q', function ($scope, $http, $q) {

        var rootRoute = 'watchdogclient';

        $scope.data = [];
        $scope.gateways = [];
        $scope.messages = [];
        $scope.responses = [];
        $scope.currentMessage = {
            uid: '', tcode: '', sid: '', mtype: '', lid: '',
            dt: '', cid: '', cver: '', cip:'', ch: '', msg:''
        };
        $scope.errorMessage = '';
        $scope.loadTable = false;
        $scope.toggleTable = function (){
            $scope.loadTable = !($scope.loadTable);
        };
        $scope.chartLoaded = false;

        // Fetch and display in the DOM.
        fetch().then(function (value) {
            $('#messages').DataTable({
                autoWidth: false,
                stateSave: true,
                scrollX: true,
                select: true,
                lengthMenu: [[10, 15, 25, 50, -1], [10, 15, 25, 50 , "All"]],
                order: [[7, "asc"]],
                data: $scope.messages.concat($scope.responses),
                columns: [
                    {data: 'channel', className : 'tableColumn' },
                    {data: 'message_type',  className : 'tableColumn'},
                    {data: 'login_id',  className : 'tableColumn'},
                    {data: 'unique_request_id', className : 'tableColumn'},
                    {data: 'client_ip',  className : 'tableColumn'},
                    {data: 'session_id', className : 'tableColumn'},
                    {data: 'date',  className : 'tableColumn', type: 'date'},
                    {data: 'customer_id',  className : 'tableColumn'},
                    {data: 'comm_ver', className : 'tableColumn' },
                    {data: 'tenantCode' ,  className : 'tableColumn'},
                    {data: 'message',  className : 'tableColumnMessage ellipsis'}
                ]
            }).on('click', 'tr', function () {
                $scope.currentMessage.ch = $('td', this).eq(0).text();
                $scope.currentMessage.mtype = $('td', this).eq(1).text();
                $scope.currentMessage.lid = $('td', this).eq(2).text();
                $scope.currentMessage.uid = $('td', this).eq(3).text();
                $scope.currentMessage.cip = $('td', this).eq(4).text();
                $scope.currentMessage.sid = $('td', this).eq(5).text();
                $scope.currentMessage.dt = $('td', this).eq(6).text();
                $scope.currentMessage.cid = $('td', this).eq(7).text();
                $scope.currentMessage.cver = $('td', this).eq(8).text();
                $scope.currentMessage.tcode = $('td', this).eq(9).text();
                $scope.currentMessage.msg = $('td', this).eq(10).text();
                $scope.$apply();    // Apply the scope
                $('#msgDialog').modal("show");
            })
        }, function (reason) {
            $scope.errorMessage = reason.msg;
            $('#errorModalMessageDataTables').modal('show');
        });

        /**
         * A promise based function to manipulate the DOM 'after' the model (Js) has been updated.
         * This is used because of the asynchronous nature of Javascript.
         * @returns {*}
         */
        function fetch() {
            return $q(function (resolve, reject) {
                $http.get(rootRoute.concat("/gatewayinfo")).then(function (gatewayInfo) {
                    $scope.gateways = gatewayInfo.data;
                    var gateways = gatewayInfo.data;
                    gateways.forEach(function (gateway) {
                        $http.get(rootRoute.concat("/messages?", "host=",
                            gateway.host, "&", "port=", gateway.port)).then(function (fetchedMessages) {
                            var messages = fetchedMessages.data;
                            messages.forEach(function (message) {
                                message.date = parseDates(message.date);
                                $scope.messages.push(message);
                            });
                            generated3pie();
                            $http.get(rootRoute.concat("/responses?", "host=", gateway.host, '&'
                                , 'port=', gateway.port)).then(function (fetchedResponses) {
                                var responses = fetchedResponses.data;
                                responses.forEach(function (response) {
                                    response.date = parseDates(response.date);
                                    response['customer_id'] = '';
                                    $scope.responses.push(response);
                                });
                                resolve({code: 200, msg:'Success'});
                            });
                        });
                    });
                }).catch(function (reason) {
                    reject({code : 500, msg:reason});
                })
            });
        }

        function parseDates(date) {
            var parsedDate = new Date(date);
            return parsedDate.getDate() + "-" + (parsedDate.getMonth() + 1) + "-" + parsedDate. getFullYear()
                + " " + parsedDate.getHours() + ":" + parsedDate.getMinutes() + ":" + parsedDate.getSeconds();
        }

        /**
         * Generate d3 pie chart
         */
        function generated3pie() {
            var data = getData(mineJson()); // data for d3 pie chart
            var svg = d3.select("svg"), // svg selector
                width = svg.attr("width"),
                height = svg.attr("height"),
                radius = Math.min(width, height) / 2,
                g = svg.append("g").attr("transform", "translate(" + width / 2 + "," + height / 2 + ")");
            var color = d3.scaleOrdinal(["#98abc5", "#8a89a6", "#7b6888", "#6b486b", "#a05d56", "#d0743c", "#ff8c00"]);   // schemeCategory10 generates random colors. d3 v4.0
            var pie = d3.pie(); // To calculate arc angles
            var arc = d3.arc().innerRadius(0).outerRadius(radius);  // For now, let's keep it i
            var arcs = g.selectAll("arc")
                .data(pie(data))
                .enter()
                .append("g")
                .attr("class", "arc");
            arcs.append("path")
                .attr("fill", function(d, i) {return color(i); })
                .attr("d", arc);
            arcs.append("text")
                .attr("transform", function(d) {
                    d.innerRadius = 120;
                    return "translate(" + arc.centroid(d) + ")";
                }).attr("dy", ".35em").text(function(d) { return d.value; });
        }

        /**
         * Parses messages array and returns a json suitable for d3 in order to prepare a chart.
         */
        function mineJson() {
            var messageTypes = [1, 2, 4, 5, 6];
            var preparedJson = {};
            messageTypes.forEach(function (messageType) {
                var count = 0;
                $scope.messages.forEach(function (message) {
                    if(message['message_type'] === messageType) count++;
                });
                preparedJson[messageType] =  count;
            });
            return preparedJson;
        }

        /**
         * A utility function to retrieve keys of a json.
         * @param obj
         * @returns {Array}
         */
        function getData(obj) {
            var arr = [];
            for (var key in obj) {
                arr.push(obj[key]);
            }
            return arr;
        }
    }]);