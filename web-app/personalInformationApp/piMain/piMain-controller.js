/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
personalInformationAppControllers.controller('piMainController',['$scope', '$rootScope', '$state', '$stateParams', '$modal',
    '$filter', '$q', '$timeout', 'notificationCenterService', 'piAddressService',
    function ($scope, $rootScope, $state, $stateParams, $modal, $filter, $q, $timeout, notificationCenterService,
              piAddressService) {


        var displayNotificationsOnStateLoad = function() {
            $timeout(function() {
                _.each($stateParams.onLoadNotifications, function(notification) {
                    notificationCenterService.addNotification(notification.message, notification.messageType, notification.flashType);
                });
            }, 0);
        };

        /**
         * Initialize controller
         */
        this.init = function() {

            piAddressService.getAddresses().$promise.then(function(response) {
                if(response.failure) {
                    notificationCenterService.displayNotification(response.message, $scope.notificationErrorType);
                } else {
                    $scope.addresses = response.addresses;
                }
            });

            displayNotificationsOnStateLoad();
        };


        // CONTROLLER VARIABLES
        // --------------------
        $scope.addresses = null;


        // CONTROLLER FUNCTIONS
        // --------------------
        $scope.cancelNotification = function () {
            notificationCenterService.clearNotifications();
        };

        $scope.openEditAddressModal = function(currentAddress) {

            $modal.open({
                templateUrl: $filter('webAppResourcePath')('personalInformationApp/piAddress/piEditAddress.html'),
                windowClass: 'edit-addr-modal',
                keyboard: true,
                controller: "piEditAddressController",
                scope: $scope,
                resolve: {
                    editAddressProperties: function () {
                        return {
                            currentAddress: currentAddress
                        };
                    }
                }

            });
        };

        // Display address delete confirmation modal
        $scope.confirmAddressDelete = function (address) {
            // TODO: need this?
            //if ($scope.editForm.$dirty) {
            //    showSaveCancelMessage();
            //    return;
            //}

            var deleteAddress = function () {
                $scope.cancelNotification();

                piAddressService.deleteAddress(address).$promise.then(function (response) {
                    if (response.failure) {
                        notificationCenterService.displayNotification(response.message, $scope.notificationErrorType);
                    } else {
                        // Refresh address info
                        var addressIndex = $scope.addresses.indexOf(address);
                        $scope.addresses.splice(addressIndex, 1);
                    }
                });
            };

            var prompts = [
                {
                    label: $filter('i18n')('personInfo.button.prompt.cancel'),
                    action: $scope.cancelNotification
                },
                {
                    label: $filter('i18n')('personInfo.button.delete'),
                    action: deleteAddress
                }
            ];

            notificationCenterService.displayNotification('personInfo.confirm.address.delete.text', 'warning', false, prompts);
        };


        // INITIALIZE
        // ----------
        this.init();
    }
]);
