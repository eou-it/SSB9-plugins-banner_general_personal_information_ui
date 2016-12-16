personalInformationAppControllers.controller('piEditAddressController',['$scope', '$modalInstance', 'piAddressService','$rootScope', '$state',
    '$filter', 'notificationCenterService', 'piCrudService', 'editAddressProperties', 'personalInformationService',
    function ($scope, $modalInstance, piAddressService, $rootScope, $state, $filter, notificationCenterService, piCrudService, editAddressProperties,
              personalInformationService){

        // CONTROLLER VARIABLES
        // --------------------
        $scope.isCreateNew = true;
        $scope.datePlaceholder = $filter('i18n')('default.date.format').toUpperCase();
        $scope.addressTypeErrMsg = '';
        $scope.fromDateErrMsg = '';
        $scope.dateRangeErrMsg = '';
        $scope.toDateErrMsg = '';
        $scope.streetLine1ErrMsg = '';
        $scope.cityErrMsg = '';
        $scope.stateErrMsg = '';
        $scope.zipErrMsg = '';
        $scope.nationErrMsg = '';
        $scope.addressErrMsg = '';


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
            if(!!$scope.toDateErrMsg) {
                $scope.toDateErrMsg = piAddressService.getErrorDateFormat($scope.address.toDate);
            }
            if(!!$scope.dateRangeErrMsg) {
                $scope.dateRangeErrMsg = piAddressService.getErrorDateRange($scope.address,
                    $scope.addressGroup[$scope.address.addressType.description]);
            }
            if(!!$scope.streetLine1ErrMsg) {
                $scope.streetLine1ErrMsg = piAddressService.getErrorStreetLine1($scope.address);
            }
            if(!!$scope.cityErrMsg){
                $scope.cityErrMsg = piAddressService.getErrorCity($scope.address);
            }
            if(!!$scope.stateErrMsg) {
                $scope.stateErrMsg = piAddressService.getErrorState($scope.address);
            }
            if(!!$scope.zipErrMsg) {
                $scope.zipErrMsg = piAddressService.getErrorZip($scope.address);
            }
            if(!!$scope.nationErrMsg) {
                $scope.nationErrMsg = piAddressService.getErrorNation($scope.address);
            }
        };

        var isValidAddress = function (address) {
            $scope.addressTypeErrMsg = piAddressService.getErrorAddressType(address);
            $scope.fromDateErrMsg = piAddressService.getErrorFromDate(address);
            $scope.toDateErrMsg = piAddressService.getErrorDateFormat(address.toDate);
            $scope.dateRangeErrMsg = piAddressService.getErrorDateRange(address,
                $scope.addressGroup[$scope.address.addressType.description]);
            $scope.streetLine1ErrMsg = piAddressService.getErrorStreetLine1(address);
            $scope.cityErrMsg = piAddressService.getErrorCity(address);
            $scope.stateErrMsg = piAddressService.getErrorState(address);
            $scope.zipErrMsg = piAddressService.getErrorZip(address);
            $scope.nationErrMsg = piAddressService.getErrorNation(address);

            return !($scope.addressTypeErrMsg || $scope.fromDateErrMsg || $scope.toDateErrMsg || $scope.dateRangeErrMsg ||
                $scope.streetLine1ErrMsg || $scope.cityErrMsg || $scope.stateErrMsg || $scope.zipErrMsg || $scope.nationErrMsg);
        };

        $scope.saveAddress = function() {
            if(isValidAddress($scope.address)) {
                var addressToSave = angular.copy($scope.address);
                addressToSave.fromDate = personalInformationService.stringToDate($scope.address.fromDate);
                addressToSave.toDate = personalInformationService.stringToDate($scope.address.toDate);

                var handleResponse = function (response) {
                    if (response.failure) {
                        $scope.addressErrMsg = response.message;
                        notificationCenterService.displayNotification(response.message, "error");
                    }
                    else {
                        notificationCenterService.clearNotifications();

                        var notifications = [];
                        notifications.push({message: 'personInfo.save.success.message',
                            messageType: $scope.notificationSuccessType,
                            flashType: $scope.flashNotification}
                        );

                        $state.go(personalInformationService.getFullProfileState(),
                            {onLoadNotifications: notifications, startingTab: 'address'},
                            {reload: true, inherit: false, notify: true}
                        );
                    }
                };

                if($scope.isCreateNew) {
                    piCrudService.create('Address', addressToSave).$promise.then(handleResponse);
                }
                else {
                    piCrudService.update('Address', addressToSave).$promise.then(handleResponse);
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
