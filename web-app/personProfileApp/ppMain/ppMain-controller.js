/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
personProfileAppControllers.controller('ppMainController',['$scope', '$rootScope', '$state', '$stateParams', '$modal',
    '$filter', '$q', '$timeout', 'notificationCenterService',
    function ($scope, $rootScope, $state, $stateParams, $modal, $filter, $q, $timeout, notificationCenterService){

        $scope.openAddAddressModal = function() {

            $modal.open({
                templateUrl: $filter('webAppResourcePath')('personProfileApp/ppAddress/ppAddAddress.html'),
                //windowClass: 'edit-account-modal',
                keyboard: true,
                controller: "ppAddAddressController",
                scope: $scope
            });
        };

    }
]);
