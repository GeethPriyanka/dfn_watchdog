/**
 * Created by dasunp on Aug, 2018
 */
angular
    .module('watchdogSessions', [])
    .controller('sessions', ['$scope', '$http', '$location' , '$rootScope' ,'$q', '$route', function ($scope , $http, $location, $rootScope , $q, $route) {

        var rootRoute = "watchdogclient";
        var mesageDistributionChart;
        $scope.contentLoaded = false;
        $scope.errorReason = '';
        $scope.sessions = [];   // Storing the sessions.
        $scope.messages = [];   // Storing the messages.
        $scope.responses = [];  // Storing the responses.
        $scope.graphData = {};
        $scope.services = [];
        $scope.graphDataModified = {};
        $scope.currentSession = {clientChannel:'', clientIp:'', expiryTime: '',
            loginId:'', logoutTime:'', sessionId:'', startTime:'', upTime:'', status: ''};
        $scope.loadedChart = false;

        fetchServices().then(function (value) {
            console.log("Services Fetched..");
        });

        // Fetch and display in the DOM.
        fetchSessions().then(function (value) {
            $('#sessionTbl').DataTable({
                autoWidth: false,
                stateSave: true,
                scrollX: true,
                select: true,
                lengthMenu: [[10, 15, 25, 50, -1], [10, 15, 25, 50 , "All"]],
                data: $scope.sessions,
                columns: [
                    {data: 'clientChannel', className : 'tableColumn'},
                    {data: 'clientIp' ,  className : 'tableColumn'},
                    {data: 'startTime',  className : 'tableColumn'},
                    {data: 'loginId',  className : 'tableColumn'},
                    {data: 'upTime',  className : 'tableColumn'},
                    {data: 'sessionId',  className : 'tableColumn'},
                    {data: 'status', className: 'tableColumn'}
                ]
            }).on('click', 'tr', function () {
                $rootScope.currentSession.clientChannel = $scope.currentSession.clientChannel = $('td', this).eq(0).text();
                $rootScope.currentSession.clientIp = $scope.currentSession.clientIp = $('td', this).eq(1).text();
                $scope.currentSession.startTime= $('td', this).eq(2).text();
                $rootScope.currentSession.loginId = $scope.currentSession.loginId = $('td', this).eq(3).text();
                $scope.currentSession.upTime = $('td', this).eq(4).text();
                $rootScope.currentSession.sessionId = $scope.currentSession.sessionId= $('td', this).eq(5).text();
                $scope.currentSession.status= $('td', this).eq(6).text();
                $scope.$apply();    // Apply the changes.
                $scope.loadedChart = false;
                fetchGraphData($scope.currentSession.sessionId).then(function (v) {
                    $('#sessionDialog').modal('show');
                    drawGraph();
                });
            });
        }, function (reason) {
            $scope.errorReason = reason.msg;
            $('#errorModalSessions').modal('show');
        });

        /**
         * Show specific comments.
         * Use a service to transfer data in between controllers.
         */
        $scope.specific = function () {
            if(Object.keys($scope.graphData).length != 0) {
                $('#sessionDialog').modal('hide');
                $('#pleaseWaitDialog').modal('show');
                window.setTimeout(function () {
                    $('#pleaseWaitDialog').modal('hide');
                    window.setTimeout(function () {
                        window.location = "#/specificmessages";
                    },500);
                },2000);
            } else {
                alert("No messages for the selected session.");
            }
        };

        /**
         * Returns the parsed date
         * @param date
         * @returns parsed date
         */
        $scope.parseDates = function parseDates(date) {
            var parsedDate = new Date(date);
            return parsedDate.getDate() + "-" + (parsedDate.getMonth() + 1) + "-" + parsedDate. getFullYear()
                + " " + parsedDate.getHours() + ":" + parsedDate.getMinutes() + ":" + parsedDate.getSeconds();
        };

        /**
         * Calculate the time difference between two timestamps.
         * Not used in the current implementation.
         * @param startTime
         * @param expirationDate
         * @param logoutTime
         * @returns time difference
         */
        $scope.timeDifference = function timeDifference(startTime, expirationDate, logoutTime){
            return getDateDiff(new Date(startTime), new Date(), new Date(expirationDate), logoutTime).result;
        };

        function fetchSessions() {
            return $q(function (resolve, reject) {
                $http.get(rootRoute.concat("/sessions/active")).then(function (sessionInfo) {
                    var sessions = sessionInfo.data;
                    console.log(sessions);
                    sessions.forEach(function (session) {
                        $scope.sessions.push(session);
                    });
                    $scope.contentLoaded = true;
                    resolve({code: 200, msg: 'Success'});
                })
            }).catch(function (reason) {
                reject({code:500, msg: reason});
            });
        }

        function fetchGraphData(sessionId) {
            return $q(function (resolve, reject) {
                $http.get(rootRoute.concat("/messages/graph?", "sessionId=" ,sessionId)).then(function (graphData) {
                    console.log(graphData.data);
                    var graphD = $scope.graphData = graphData.data;
                    $scope.graphDataModified = {};
                    Object.keys($scope.graphData).forEach(function (key) {
                        var serviceName = findService(key);
                        if(serviceName != '') {
                            $scope.graphDataModified[serviceName.toString()] = $scope.graphData[key];
                        }
                    });
                    resolve({code: 200, msg: 'Success'})
                }).catch(function (reason) {
                    reject({code : 500, msg: reason});
                });
            });
        }

        function drawGraph() {
            var options = {
                title: {
                    display: true,
                    fontSize: 10,
                    position: 'top',
                    text: 'Message Distribution w.r.t. Services.'
                },
                legend: {
                    position: 'top',
                    display: true,
                    fontSize: 8
                },
                tooltips: {
                    enabled : true
                },
                hover: {
                    mode: null
                },
                responsive: false
            };
            var default_colors = ['#3366CC','#DC3912','#FF9900','#109618','#990099','#3B3EAC','#0099C6','#DD4477','#66AA00','#B82E2E','#316395','#994499','#22AA99','#AAAA11','#6633CC','#E67300','#8B0707','#329262','#5574A6','#3B3EAC'];
            var chartData = Object.values($scope.graphDataModified);
            var labels = Object.keys($scope.graphDataModified);
            var data = {
                datasets: [{
                    data: chartData,
                    backgroundColor: default_colors
                }],
                labels: labels
            };
            var ctx = $('#mesageDistribution');
            if(mesageDistributionChart) {
                mesageDistributionChart.data = data;
                mesageDistributionChart.update();
            } else {
                mesageDistributionChart = new Chart(ctx, {
                    type: 'doughnut',
                    data: data,
                    options: options
                });
            }
            $scope.loadedChart = true;
        }

        function fetchServices() {
            return $q(function (resolve, reject) {
                $http.get(rootRoute.concat("/services")).then(function (services) {
                    $scope.services = services.data.services;
                    // console.log(fetchedServices);
                    resolve({code: 200, msg: 'Success'})
                }).catch(function (reason) {
                    reject({code: 500, msg: reason})
                });
            });
        }

        /**
         * @param serviceId
         */
        function findService(serviceId) {
            var result = '';
            $scope.services.forEach(function (service) {
                    if(service.id == serviceId) {
                    result = service['serviceName'];
                }
            });
            return result;
        }

        function reload() {
            $route.reload();
        }

        /*
        * Get the difference between dates.
        */
        function getDateDiff(startDate, endDate, expirationDate, logoutTime) {
            var diff, result, status;
            diff = logoutTime == null ? endDate.getTime() - startDate.getTime() : (new Date(logoutTime)).getTime() - startDate.getTime();
            var days = Math.floor(diff / (60 * 60 * 24 * 1000));
            var hours = Math.floor(diff / (60 * 60 * 1000)) - (days * 24);
            var minutes = Math.floor(diff / (60 * 1000)) - ((days * 24 * 60) + (hours * 60));
            var seconds = Math.floor(diff / 1000) - ((days * 24 * 60 * 60) + (hours * 60 * 60) + (minutes * 60));
            if (logoutTime == null) {   // Not logged out
                if ((new Date()).getTime() > expirationDate.getTime()) {
                    result = "Session Already Expired.";
                    status = "expired"
                } else {
                    result = days + " days, " + hours + " hours, " + minutes + " minutes, " + seconds + " seconds.";
                    status = "alive"
                }
            } else {    // Logged out
                result = days + " days, " + hours + " hours, " + minutes + " minutes, " + seconds + " seconds.";
                status = "loggedout"
            }
            return { result: result, status: status };
        }

    }]);