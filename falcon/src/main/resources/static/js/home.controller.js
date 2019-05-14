/**
 * Created by isurul on 14/3/2017.
 */
angular
    .module('watchdogHome', [])
    .controller('home', function ($http) {
        var self = this;
        $http.get('/resource/').then(function (response) {
            self.greeting = response.data;
        })
    });