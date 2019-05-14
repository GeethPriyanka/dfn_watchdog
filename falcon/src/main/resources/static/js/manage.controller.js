/**
 * Draws the table of nodes.
 */
angular
    .module('watchdogManage', [])
    .controller('manage', ['$scope', '$http', function ($scope, $http) {
        var self = this;
        var manageView;
        $scope.clientId = "";
        $scope.results = [];

        $http.get('/watchdogclient/manageview').then(function (response) {
            $scope.manageView = response.data;
            drawTable();
        });

        $scope.startEod = function () {
            $http.get('/watchdogclient/eod/start/'
            ).then(function (response) {
                $scope.startEodMessage = response.data;
            });
        };
    }]);