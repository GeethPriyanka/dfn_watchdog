/**
 * Created by dasunp on Aug, 2018
 */
angular.
module('watchdogSpecificMessages', [])
    .controller('specific-messages', ['$scope', '$http','$rootScope', '$routeParams', '$q', function ($scope, $http, $rootScope ,  $routeParams , $q) {

        var self = this;
        var BreakException = {};
        var rootRoute = 'watchdogclient';
        var sessionId = $rootScope.currentSession.sessionId; // Get the query
        var clientIp = $rootScope.currentSession.clientIp;
        var loginId =  $rootScope.currentSession.loginId;
        // parameter of session ID from the URI.
        $scope.visibilityOfChannel = false;
        $scope.messageTypes = [1,6,4,5,200,2];
        $scope.currentMessage = {
            uid: '', tcode: '', sid: '', mtype: '', lid: '',
            dt: '', cid: '', cver: '', cip:'', ch: '', msg:''
        };
        $scope.currentMessageJson = {};
        $scope.currentResponse = {};
        $scope.errorReason = '';
        $scope.sessionId = sessionId;   // Stores the sessionId
        $scope.clientIp = clientIp;
        $scope.loginId = loginId;
        $scope.channel = '';
        // parameter retrieved by the URI parameter.
        $scope.gateways = [];   // Store the gateways.
        $scope.messages = [];   // Store the messages.
        $scope.responses = [];  // Store the responses.
        $scope.dataDisplay = {
            dataLoad: false,
            data: {
                clientIp: '',
                loginId: '',
                sessionId:'',
                com_ver:'',
                tenantCode: ''
            }
        };
        $scope.tenantCode = '';

        fetchSpecificMessages().then(function (value) {
            $scope.messages.length !== 0 ? $scope.visibilityOfChannel = true : '';
            $('#specificMessages').DataTable({
                autoWidth: false,
                stateSave: true,
                scrollX: true,
                select: true,
                lengthMenu: [[10, 15, 25, 50, -1], [10, 15, 25, 50 , "All"]],
                data: $scope.messages,
                columns: [
                    {data: 'message_type',  className : 'tableColumn'},
                    {data: 'unique_request_id', className : 'tableColumn'},
                    {data: 'date',  className : 'tableColumn'},
                    {data: 'responseTime',  className : 'tableColumnMessage ellipsis'},
                    // {data: 'customer_id',  className : 'tableColumn'},
                    {data: 'message',  className : 'tableColumnMessage ellipsis'}
                ]
            }).on('click', 'tr', function () {
                $scope.currentMessage.mtype = $('td', this).eq(0).text();
                $scope.currentMessage.uid = $('td', this).eq(1).text();
                $scope.currentMessage.dt = $('td', this).eq(2).text();
                // $scope.currentMessage.cid = $('td', this).eq(3).text();
                var currentMessage = $scope.currentMessage.msg = $('td', this).eq(4).text();
                $scope.currentMessageJson = JSON.parse(currentMessage);
                $http.get(rootRoute.concat("/responses/specific?",'uid=' + $scope.currentMessage.uid)).then(function (response) {
                    $scope.currentResponse = response;
                });
                $scope.$apply();    // Apply the scope
                $('#specificMsgDialog').modal("show");
            });
        }, function (reason) {
            $scope.errorReason = reason.reason;
            $('#errorModalSpecificMessages').modal('show');
        });

        function fetchSpecificMessages() {
            return $q(function (resolve, reject) {
                $http.get(rootRoute.concat("/messages/specific?", "sessionId=", $scope.sessionId)).then(function (fetchedMessages) {
                    var messages = fetchedMessages.data;
                    console.log(messages);
                    if(messages.length !== 0) $scope.channel = messages[0].channel;
                    messages.forEach(function (message) {
                        message.date = parseDates(message.date);
                        $scope.messages.push(message);
                    });
                    $scope.tenantCode = getTenantCode();
                    resolve({code: 200, reason: 'Success'});
                }).catch(function (reason) {
                    reject({code: 500, reason : reason});
                });
            });
        }

        function getTenantCode() {
            return $scope.messages[0]['tenantCode'];
        }


        /**
         * A date parser which returns a string.
         * e.g. 29-11-2018
         * @param date
         * @returns {string}
         */
        function parseDates(date) {
            var parsedDate = new Date(date);
            return parsedDate.getDate() + "-" + (parsedDate.getMonth() + 1) + "-" + parsedDate. getFullYear()
                + " " + parsedDate.getHours() + ":" + parsedDate.getMinutes() + ":" + parsedDate.getSeconds();
        }

        /**
         * Extract the customer ID from the array of messages
         * @param arrayOfMessages
         * @return the customer ID.
         */
        function extractCustomerId(arrayOfMessages) {
            arrayOfMessages.forEach(function (message) {
                var customer_id = message.customer_id;
                if(customer_id != 0) return customer_id;
            });
        }

    }]);