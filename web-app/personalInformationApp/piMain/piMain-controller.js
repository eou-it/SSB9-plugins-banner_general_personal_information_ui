/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
personalInformationAppControllers.controller('piMainController',['$scope', '$rootScope', '$state', '$stateParams', '$modal',
    '$filter', '$q', '$timeout', 'notificationCenterService', 'piCrudService', 'piPhoneService',
    function ($scope, $rootScope, $state, $stateParams, $modal, $filter, $q, $timeout, notificationCenterService,
              piCrudService, piPhoneService) {


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

            piCrudService.get('MaskingRules').$promise.then(function(response) {
                if(response.failure) {
                    notificationCenterService.displayNotification(response.message, $scope.notificationErrorType);
                } else {
                    $scope.maskingRules = response;
                }
            });

            piCrudService.get('Addresses').$promise.then(function(response) {
                if(response.failure) {
                    notificationCenterService.displayNotification(response.message, $scope.notificationErrorType);
                } else {
                    $scope.addressGroup = sortAddresses(response.addresses);
                }
            });

            piCrudService.get('TelephoneNumbers').$promise.then(function(response) {
                if(response.failure) {
                    notificationCenterService.displayNotification(response.message, $scope.notificationErrorType);
                } else {
                    $scope.phones = response.telephones;
                }
            });

            piCrudService.get('Emails').$promise.then(function(response) {
                if(response.failure) {
                    notificationCenterService.displayNotification(response.message, $scope.notificationErrorType);
                } else {
                    $scope.emails = response.emails;
                }
            });

            piCrudService.get('EmergencyContacts').$promise.then(function(response) {
                if(response.failure) {
                    notificationCenterService.displayNotification(response.message, $scope.notificationErrorType);
                } else {
                    $scope.emergencyContacts = response.emergencyContacts;
                }
            });

            if($stateParams.startingTab) {
                $scope.startingTab = $stateParams.startingTab;
            }

            displayNotificationsOnStateLoad();
        };


        // CONTROLLER VARIABLES
        // --------------------
        $scope.maskingRules = null;
        $scope.addressGroup = null;
        $scope.emails = null;
        $scope.phones = null;
        $scope.emergencyContacts = [];


        // CONTROLLER FUNCTIONS
        // --------------------
        /**
         * Get all addresses for specified address type (e.g. Mailing, Permanent).
         * Addresses will be in order of time period, so "Current" will be contiguous, then "Future," etc.
         * @param addrType
         * @returns {Array}
         */
        $scope.getOrderedAddressesForType = function(addrType) {
            var ordered = [];

            _.each($scope.addressGroup[addrType], function(addresses) {
                ordered = ordered.concat(addresses);
            });

            return ordered;
        };

        $scope.cancelNotification = function () {
            notificationCenterService.clearNotifications();
        };

        $scope.openEditAddressModal = function(currentAddress) {

            $modal.open({
                templateUrl: $filter('webAppResourcePath')('personalInformationApp/piAddress/piEditAddress.html'),
                windowClass: 'edit-addr pi-modal',
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

        $scope.openEditEmailModal = function(currentEmail) {

            $modal.open({
                templateUrl: $filter('webAppResourcePath')('personalInformationApp/piEmail/piEditEmail.html'),
                windowClass: 'pi-modal',
                keyboard: true,
                controller: "piEditEmailController",
                scope: $scope,
                resolve: {
                    editEmailProperties: function () {
                        return {
                            currentEmail: currentEmail
                        };
                    }
                }
            });
        };

        $scope.openEditEmergencyContactModal = function(currentEmergencyContact) {
            var numContacts = $scope.emergencyContacts.length;

            $modal.open({
                templateUrl: $filter('webAppResourcePath')('personalInformationApp/piEmergencyContact/piEditEmergencyContact.html'),
                windowClass: 'edit-emer-contact pi-modal',
                keyboard: true,
                controller: "piEditEmergencyContactController",
                scope: $scope,
                resolve: {
                    editEmergencyContactProperties: function () {
                        return {
                            currentEmergencyContact: currentEmergencyContact,
                            highestPriority: currentEmergencyContact ? numContacts : numContacts + 1
                        };
                    }
                }
            });
        };

        // Display address delete confirmation modal
        $scope.confirmAddressDelete = function (address) {
            var deleteAddress = function () {
                $scope.cancelNotification();

                piCrudService.delete('Address', address).$promise.then(function (response) {
                    if (response.failure) {
                        notificationCenterService.displayNotification(response.message, $scope.notificationErrorType);
                    } else {
                        // Refresh address info
                        var addrType = address.addressType.description;
                        var timePeriod = address.isFuture ? 'future' : 'current';
                        var addressIndex = $scope.addressGroup[addrType][timePeriod].indexOf(address);
                        $scope.addressGroup[addrType][timePeriod].splice(addressIndex, 1);
                        if($scope.addressGroup[addrType].future.length === 0 &&
                            $scope.addressGroup[addrType].current.length === 0) {
                            delete $scope.addressGroup[addrType];
                        }
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

        $scope.confirmEmailDelete = function (email) {
            var deleteEmail = function () {
                $scope.cancelNotification();

                piCrudService.delete('Email', email).$promise.then(function (response) {
                    if (response.failure) {
                        notificationCenterService.displayNotification(response.message, $scope.notificationErrorType);
                    } else {
                        // Refresh email info
                        $scope.emails.splice($scope.emails.indexOf(email), 1);
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
                    action: deleteEmail
                }
            ];

            notificationCenterService.displayNotification('personInfo.confirm.email.delete.text', 'warning', false, prompts);
        };

        $scope.confirmEmergencyContactDelete = function (contact) {
            var deleteEmergencyContact = function () {
                $scope.cancelNotification();

                piCrudService.delete('EmergencyContact', contact).$promise.then(function (response) {
                    if (response.failure) {
                        notificationCenterService.displayNotification(response.message, $scope.notificationErrorType);
                    } else {
                        // Refresh contact info
                        $scope.emergencyContacts.splice($scope.emergencyContacts.indexOf(contact), 1);
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
                    action: deleteEmergencyContact
                }
            ];

            notificationCenterService.displayNotification('personInfo.confirm.emergencyContact.delete.text', 'warning', false, prompts);
        };


        // INITIALIZE
        // ----------
        this.init();
    }
]);
