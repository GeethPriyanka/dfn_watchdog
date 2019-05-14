/**
 * Display client routes.
 */
angular
    .module('watchdogRequests', [])
    .controller('requests', ['$scope', '$http', function ($scope, $http) {
        $scope.clientId = "";
        $scope.results = [];

        $scope.getRoutes = function () {
            $http.get('/watchdogclient/route/' + $scope.clientId
            ).then(function (response) {
                $scope.routes = response.data;
            });
        };

        $scope.getRoutesHistory = function () {
            $http.get('/watchdogclient/route/history/' + $scope.clientIdHistory
            ).then(function (response) {
                $scope.routesHistory = response.data;
            });
        };

        $http.get('/watchdogclient/route/all').then(function (response) {
            $scope.clientRoutesAll = response.data;
            console.log(response.data);
        });

    }]);