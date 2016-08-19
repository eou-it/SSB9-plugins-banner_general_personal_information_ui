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
        },

        /**
         * Sort addresses by type (e.g. Mailing, Permanent) and whether current or future.
         * @param addresses
         * @returns Object containing a "current" array and "future" array.
         */
        sortAddresses = function(addresses) {
            var sorted = {},
                addrType,
                timePeriod;

            _.each(addresses, function(addr) {
                addrType = addr.addressType.description;

                if (!(addrType in sorted)) {
                    sorted[addrType] = {
                        current: [],
                        future: []
                    };
                }

                timePeriod = addr.isFuture ? 'future' : 'current';
                sorted[addrType][timePeriod].push(addr);
            });

            return sorted;
        };

        /**
         * Initialize controller
         */
        this.init = function() {

            piAddressService.getAddresses().$promise.then(function(response) {
                if(response.failure) {
                    notificationCenterService.displayNotification(response.message, $scope.notificationErrorType);
                } else {
                    $scope.addressGroup = sortAddresses(response.addresses);
                }
            });

            displayNotificationsOnStateLoad();
        };


        // CONTROLLER VARIABLES
        // --------------------
        $scope.addressGroup = null;


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
            var deleteAddress = function () {
                $scope.cancelNotification();

                piAddressService.deleteAddress(address).$promise.then(function (response) {
                    if (response.failure) {
                        notificationCenterService.displayNotification(response.message, $scope.notificationErrorType);
                    } else {
                        // Refresh address info
                        var addrType = address.addressType.description;
                        var timePeriod = address.isFuture ? 'future' : 'current';
                        var addressIndex = $scope.addressGroup[addrType][timePeriod].indexOf(address);
                        $scope.addressGroup[addrType][timePeriod].splice(addressIndex, 1);
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
