personProfileAppControllers.controller('ppAddAddressController',['$scope', '$rootScope', '$state', '$stateParams', '$modal',
    '$filter', '$q', '$timeout', 'notificationCenterService',
    function ($scope, $rootScope, $state, $stateParams, $modal, $filter, $q, $timeout, notificationCenterService){
        $scope.address = {
            state: {},
            nation: {}
        };
    }
]);