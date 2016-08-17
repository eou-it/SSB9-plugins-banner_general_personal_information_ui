personalInformationAppControllers.controller('piEditAddressController',['$scope', '$modal', '$modalInstance', 'piAddressService','$rootScope', '$state', '$stateParams',
    '$filter', '$q', '$timeout', 'notificationCenterService', 'editAddressProperties',
    function ($scope, $modal, $modalInstance, piAddressService, $rootScope, $state, $stateParams, $filter, $q, $timeout, notificationCenterService, editAddressProperties){

        // CONTROLLER VARIABLES
        // --------------------
        $scope.isCreateNew = true;
        $scope.addressTypeErrMsg = '';
        $scope.fromDateErrMsg = '';
        $scope.streetLine1ErrMsg = '';
        $scope.cityErrMsg = '';
        $scope.stateCountyNationErrMsg = '';


        // CONTROLLER FUNCTIONS
        // --------------------
        $scope.cancelModal = function () {
            $modalInstance.dismiss('cancel');
            notificationCenterService.clearNotifications();
        };

        $scope.removeAddressFieldErrors = function() {
            if(!!$scope.addressTypeErrMsg) {
                $scope.addressTypeErrMsg = piAddressService.getErrorAddressType($scope.address);
            }
            if(!!$scope.fromDateErrMsg) {
                $scope.fromDateErrMsg = piAddressService.getErrorFromDate($scope.address);
            }
            if(!!$scope.streetLine1ErrMsg) {
                $scope.streetLine1ErrMsg = piAddressService.getErrorStreetLine1($scope.address);
            }
            if(!!$scope.cityErrMsg){
                $scope.cityErrMsg = piAddressService.getErrorCity($scope.address);
            }
            if(!!$scope.stateCountyNationErrMsg) {
                $scope.stateCountyNationErrMsg = piAddressService.getErrorStateCountyNation($scope.address);
            }
        };

        var isValidAddress = function (address) {
            $scope.addressTypeErrMsg = piAddressService.getErrorAddressType(address);
            $scope.fromDateErrMsg = piAddressService.getErrorFromDate(address);
            $scope.streetLine1ErrMsg = piAddressService.getErrorStreetLine1(address);
            $scope.cityErrMsg = piAddressService.getErrorCity(address);
            $scope.stateCountyNationErrMsg = piAddressService.getErrorStateCountyNation(address);

            return !($scope.addressTypeErrMsg || $scope.fromDateErrMsg || $scope.streetLine1ErrMsg || $scope.cityErrMsg ||
                $scope.stateCountyNationErrMsg);
        };

        $scope.saveAddress = function() {
            if(isValidAddress($scope.address)) {
                $scope.address.fromDate = new Date(Date.parse($scope.address.fromDate));
                $scope.address.toDate = new Date(Date.parse($scope.address.toDate));

                var handleResponse = function (response) {
                    if (response.failure) {
                        notificationCenterService.displayNotification(response.message, "error");
                    }
                    else {
                        var notifications = [];
                        notifications.push({message: 'personInfo.save.success.message',
                            messageType: $scope.notificationSuccessType,
                            flashType: $scope.flashNotification}
                        );

                        $state.go('personalInformationMain',
                            {onLoadNotifications: notifications},
                            {reload: true, inherit: false, notify: true}
                        );
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
        };

        $scope.setToDate = function(data){
            $scope.address.toDate = data;
        };

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
