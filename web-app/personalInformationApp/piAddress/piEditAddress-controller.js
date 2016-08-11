personalInformationAppControllers.controller('piEditAddressController',['$scope', '$modal', '$modalInstance', 'piAddressService','$rootScope', '$state', '$stateParams',
    '$filter', '$q', '$timeout', 'notificationCenterService', 'editAddressProperties',
    function ($scope, $modal, $modalInstance, piAddressService, $rootScope, $state, $stateParams, $filter, $q, $timeout, notificationCenterService, editAddressProperties){

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
            if(piAddressService.isValidAddress($scope.address)) {
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
                    piAddressService.saveNewAddress($scope.address).$promise.then(handleResponse);
                }
                else {
                    piAddressService.updateAddress($scope.address).$promise.then(handleResponse);
                }
            }
            else {
                piAddressService.displayMessages();
            }
        };

        $scope.setFromDate = function(data){
            $scope.address.fromDate = data;
        }

        $scope.setToDate = function(data){
            $scope.address.toDate = data;
        }

        this.init = function() {

            if (editAddressProperties.currentAddress) {
                // Set up for "update address"
                $scope.isCreateNew = false;
                $scope.address = angular.copy(editAddressProperties.currentAddress);
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
