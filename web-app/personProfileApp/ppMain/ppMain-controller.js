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

            // If any address is flagged for delete (happens via user checking a checkbox, which
            // in turn sets the deleteMe property to true), set selectedForDelete.address to true,
            // enabling the "Delete" button.
            $scope.$watch('addresses', function () {
                // Determine if any addresses are selected for delete
                $scope.selectedForDelete.address = _.any($scope.addresses, function(addr) {
                    return addr.deleteMe;
                });
            }, true);
        };


        // CONTROLLER VARIABLES
        // --------------------
        $scope.addresses = null;

        $scope.selectedForDelete = {
            address: false
        };


        // CONTROLLER FUNCTIONS
        // --------------------
        $scope.cancelNotification = function () {
            notificationCenterService.clearNotifications();
        };

        $scope.openAddAddressModal = function() {

            $modal.open({
                templateUrl: $filter('webAppResourcePath')('personProfileApp/ppAddress/ppAddAddress.html'),
                //windowClass: 'edit-account-modal',
                keyboard: true,
                controller: "ppAddAddressController",
                scope: $scope
            });
        };

        // Display address delete confirmation modal
        $scope.confirmAddressDelete = function () {
            // If no address is selected for deletion, this functionality is disabled
            if (!$scope.selectedForDelete.address) return;


            // TODO: need this?
            //if ($scope.editForm.$dirty) {
            //    showSaveCancelMessage();
            //    return;
            //}

            var prompts = [
                {
                    label: $filter('i18n')('personInfo.button.prompt.cancel'),
                    action: $scope.cancelNotification
                },
                {
                    label: $filter('i18n')('personInfo.button.delete'),
                    action: $scope.deleteAddresses
                }
            ];

            notificationCenterService.displayNotification('personInfo.confirm.payroll.delete.text', 'warning', false, prompts);
        };

        $scope.deleteAddresses = function () {
            var addresses = $scope.addresses,
                addressesToDelete = _.where(addresses, {deleteMe: true}),
                index;

            $scope.cancelNotification();

            ppAddressService.deleteAddresses(addressesToDelete).$promise.then(function (response) {
                var notifications = [];

                if (response[0].failure) {
                    notificationCenterService.displayNotification(response[0].message, $scope.notificationErrorType);
                } else {
                    // Refresh address info
                    $scope.addresses = _.difference(addresses, addressesToDelete);
                }
            });
        };


        // INITIALIZE
        // ----------
        this.init();
    }
]);
