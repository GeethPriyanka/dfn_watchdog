/**
 * Created by isurul on 14/3/2017.
 */
angular
    .module('watchdogReconcile', [])
    .controller('reconcile', ['$scope', '$http', function ($scope, $http) {
        var self = this;

        $http.get('/watchdogclient/level1fixlogs').then(function (response) {
            $scope.level1fixlogs = response.data;
        });

        $http.get('/watchdogclient/level1orderaudit').then(function (response) {
            $scope.level1orderaudit = response.data;
        });

        $http.get('/watchdogclient/level2').then(function (response) {
            $scope.level2 = response.data;
        });

        $http.get('/watchdogclient/cashresolution').then(function (response) {
            $scope.cashResolution = response.data;
        });

        $http.get('/watchdogclient/holdingresolution').then(function (response) {
            $scope.holdingResolution = response.data;
        });

        $scope.getRoutes = function () {
            $http.get('/watchdogclient/route/' + $scope.clientId
            ).then(function (response) {
                    $scope.routes = response.data;
                });
        }
    }]);