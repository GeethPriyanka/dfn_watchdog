/**
 * Created by dasunp on Aug, 2018
 */
angular.module('watchdogSlaMessages', [])
    .controller('slamessages', ['$scope', '$http', '$q', function ($scope, $http, $q) {

        var self = this;
        var rootRoute = 'watchdogclient';

        $scope.slaMessages = [];
        $scope.currentResponse = {};
        $scope.currentMessageJson = {};
        $scope.currentMessage = {};

        fetchSlaMessages().then(function (value) {
            console.log("Fetched SLA messages.");
            $('#slaMessages').DataTable({
                autoWidth: false,
                scrollX: true,
                select: true,
                lengthMenu: [[10, 15, 25, 50, -1], [10, 15, 25, 50 , "All"]],
                data: $scope.slaMessages,
                columns: [
                    {data: 'unique_request_id', className : 'tableColumn'},
                    {data: 'channel' ,  className : 'tableColumn'},
                    {data: 'client_ip',  className : 'tableColumn'},
                    {data: 'comm_ver',  className : 'tableColumn'},
                    // {data: 'customer_id',  className : 'tableColumn'},
                    {data: 'responseTime', className: 'tableColumn'},
                    {data: 'date',  className : 'tableColumn', type: 'date'},
                    {data: 'login_id', className: 'tableColumn'},
                    {data: 'message', className: 'tableColumn'},
                    {data: 'message_type', className: 'tableColumn'},
                    {data: 'session_id', className: 'tableColumn'},
                    {data: 'tenantCode', className: 'tableColumn'}
                ]
            }).on('click', 'tr', function () {
                $scope.currentMessage.uid = $('td', this).eq(0).text();
                $scope.currentMessage.channel = $('td', this).eq(1).text();
                $scope.currentMessage.cip = $('td', this).eq(2).text();
                $scope.currentMessage.commVer = $('td', this).eq(3).text();
                // $scope.currentMessage.cid = $('td', this).eq(4).text();
                $scope.currentMessage.dt = $('td', this).eq(5).text();
                $scope.currentMessage.lid = $('td', this).eq(6).text();
                var currentMessage = $scope.currentMessage.msg = $('td', this).eq(7).text();
                $scope.currentMessageJson = JSON.parse(currentMessage);
                $http.get(rootRoute.concat("/responses/specific?",'uid=' + $scope.currentMessage.uid)).then(function (response) {
                    $scope.currentResponse = response;
                });
                $scope.$apply();    // Apply the scope
                $('#specificSlaMsgDialog').modal("show");
            });
        }, function (reason) {
            console.error("Error in fetching SLA messages");
        });

        function fetchSlaMessages() {
            return $q(function (resolve, reject) {
                $http.get(rootRoute.concat("/messages/sla")).then(function (slaMessages) {
                    var messages = slaMessages.data;
                    console.log(messages);
                    messages.forEach(function (message) {
                        message.date = parseDates(message.date);
                        $scope.slaMessages.push(message);
                    });
                    resolve({code: 200, reason: 'Success'});
                }).catch(function (reason) {
                    reject({code:500, reason: reason});
                })
            });
        }

        function parseDates(date) {
            var parsedDate = new Date(date);
            return parsedDate.getDate() + "-" + (parsedDate.getMonth() + 1) + "-" + parsedDate. getFullYear()
                + " " + parsedDate.getHours() + ":" + parsedDate.getMinutes() + ":" + parsedDate.getSeconds();
        }

    }]);