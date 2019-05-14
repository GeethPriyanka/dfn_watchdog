/**
 * Created by dasunp on Nov, 2018
 */
angular
    .module('watchdogClientCount', [])
    .controller('clientcount', ['$scope', '$http', '$q', '$rootScope', function ($scope, $http, $q, $rootScope) {

        var rootRoute = "watchdogclient";

        var clientCountMap;
        var clientCountGraph;

        fetchClientCounts().then(function (value) {
            drawClientCountGraph();
        });

        function fetchClientCounts() {
            return $q(function (resolve, reject) {
               $http.get(rootRoute.concat("/clientCountMap")).then(function (clientCountMapData) {
                   clientCountMap = clientCountMapData.data;
                   console.log(clientCountMap);
                   resolve({code: 200, msg: 'Success'});
               }).catch(function (reason) {
                   reject({code: 500, msg: reason});
               });
            });
        }
        
        function drawClientCountGraph() {
            var xValues = [];
            var yValues = [];
            clientCountMap.forEach(function (data) {
                xValues.push(data['date']);
                yValues.push(data['value']);
            });
            var chartData = {
                labels: xValues,
                datasets: [
                    {
                        label: "Client Counts",
                        fill: false,
                        lineTension: 0.1,
                        backgroundColor: "rgba(75,192,192,1)",
                        borderColor: "rgba(75,192,192,1)",
                        borderCapStyle: 'butt',
                        borderDash: [],
                        borderDashOffset: 0.0,
                        borderJoinStyle: 'miter',
                        pointBorderColor: "rgba(75,192,192,1)",
                        pointBackgroundColor: "#fff",
                        pointBorderWidth: 1,
                        pointHoverRadius: 5,
                        pointHoverBackgroundColor: "rgba(75,192,192,1)",
                        pointHoverBorderColor: "rgba(220,220,220,1)",
                        pointHoverBorderWidth: 2,
                        pointRadius: 5,
                        pointHitRadius: 10,
                        data: yValues
                    }
                ]
            };
            var chartOptions = {
                legend: {
                    display: true,
                    position: 'right',
                    labels: {
                        boxWidth: 80,
                        fontColor: 'white'
                    }
                }
            };
            var ctx = $('#clientCountChart');
            if(clientCountGraph) {
                clientCountGraph.data = clientCountMap;
                clientCountGraph.update();
            } else {
                clientCountGraph = new Chart(ctx, {
                    type: 'line',
                    data: chartData,
                    options: chartOptions
                });
            }
        }

    }]);