personProfileAppControllers.controller('ppEditAddressController',['$scope', '$modal', '$modalInstance', 'ppAddressService','$rootScope', '$state', '$stateParams',
    '$filter', '$q', '$timeout', 'notificationCenterService', 'editAddressProperties',
    function ($scope, $modal, $modalInstance, ppAddressService, $rootScope, $state, $stateParams, $filter, $q, $timeout, notificationCenterService, editAddressProperties){

        // CONTROLLER VARIABLES
        // --------------------
        $scope.isCreateNew = true;


        // CONTROLLER FUNCTIONS
        // --------------------
        $scope.cancelModal = function () {
            $modalInstance.dismiss('cancel');
            notificationCenterService.clearNotifications();
        };

        $scope.saveAddress = function() {
            if(ppAddressService.isValidAddress($scope.address)) {
                // TODO this can probably be removed when date picker implemented
                $scope.address.fromDate = new Date(Date.parse($scope.address.fromDate));
                $scope.address.toDate = new Date(Date.parse($scope.address.toDate));

                var handleResponse = function (response) {
                    if (response.failure) {
                        notificationCenterService.displayNotification(response.message, "error");
                    }
                    else {
                        $scope.cancelModal();
                    }
                };

                if($scope.isCreateNew) {
                    ppAddressService.saveNewAddress($scope.address).$promise.then(handleResponse);
                }
                else {
                    ppAddressService.updateAddress($scope.address).$promise.then(handleResponse);
                }
            }
            else {
                ppAddressService.displayMessages();
            }
        };

        this.init = function() {

            if (editAddressProperties.currentAddress) {
                // Set up for "update address"
                $scope.isCreateNew = false;
                $scope.address = editAddressProperties.currentAddress;
            } else {
                // Create "new address" object
                $scope.address = {
                    county: {},
                    state: {},
                    nation: {},
                    addressType:{},
                    city: null,
                    fromDate: null,
                    toDate: null,
                    houseNumber: null,
                    streetLine1: null,
                    streetLine2: null,
                    streetLine3: null,
                    streetLine4: null,
                    zip: null
                };

            }
        };

        // INITIALIZE
        // ----------
        this.init();

    }
]);