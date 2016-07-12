/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
personProfileAppControllers.controller('ppMainController',['$scope', '$rootScope', '$state', '$stateParams', '$modal',
    '$filter', '$q', '$timeout', 'notificationCenterService', 'ppAddressService',
    function ($scope, $rootScope, $state, $stateParams, $modal, $filter, $q, $timeout, notificationCenterService,
              ppAddressService) {


        /**
         * Initialize controller
         */
        this.init = function() {

            ppAddressService.getAddresses().$promise.then(function(response) {
                if(response.failure) {
                    notificationCenterService.displayNotification(response.message, $scope.notificationErrorType);
                } else {
                    $scope.addresses = response.addresses;
                }
            });
        };


        // CONTROLLER VARIABLES
        // --------------------
        $scope.addresses = null;

        $scope.openAddAddressModal = function() {

            $modal.open({
                templateUrl: $filter('webAppResourcePath')('personProfileApp/ppAddress/ppAddAddress.html'),
                //windowClass: 'edit-account-modal',
                keyboard: true,
                controller: "ppAddAddressController",
                scope: $scope
            });
        };


        // INITIALIZE
        // ----------
        this.init();
    }
]);
