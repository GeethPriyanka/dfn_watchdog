/**
 * Created by isurul on 14/3/2017.
 */
angular.
module('watchdogClient').
config(function ($routeProvider, $httpProvider) {
    $routeProvider.
    when('/', {
        templateUrl: 'dashboard.html',
        controller: 'dashboard',
        controllerAs: 'controller'
    }).
    when('/login', {
        templateUrl: 'login.html',
        controller: 'navigation',
        controllerAs: 'controller'
    }).
    when('/home', {
        templateUrl: 'home.html',
        controller: 'home',
        controllerAs: 'controller'
    }).
    when('/dashboard', {
        templateUrl: 'dashboard.html',
        controller: 'dashboard',
        controllerAs: 'controller'
    }).
    when('/nocDashboard', {
        templateUrl: 'noc-dashboard.html',
        controller: 'nocDashboard',
        controllerAs: 'controller'
    }).
    when('/view', {
        templateUrl: 'view.html',
        controller: 'view',
        controllerAs: 'controller'
    }).
    when('/requests', {
        templateUrl: 'requests.html',
        controller: 'requests',
        controllerAs: 'controller'
    }).
    when('/manage', {
        templateUrl: 'manage.html',
        controller: 'manage',
        controllerAs: 'controller'
    }).
    when('/reconcile', {
        templateUrl: 'reconcile.html',
        controller: 'reconcile',
        controllerAs: 'controller'
    }).
    when('/sessions', {
        templateUrl: 'sessions.html',
        controller: 'sessions',
        controllerAs: 'controller'
    }).
    when('/messages' , {
        templateUrl: 'messages.html',
        controller: 'messages',
        controllerAs: 'controller'
    }).
    when('/specificmessages', {
        templateUrl: 'specificMessages.html',
        controller:'specific-messages',
        controllerAs: 'controller'
    }).
    when('/slamessages', {
        templateUrl: 'slamessages.html',
        controller: 'slamessages',
        controllerAs: 'controller'
    }).
    when('/clientcount', {
        templateUrl: 'clientcount.html',
        controller: 'clientcount',
        controllerAs: 'controller'
    }).
    otherwise('/');
    $httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';
});