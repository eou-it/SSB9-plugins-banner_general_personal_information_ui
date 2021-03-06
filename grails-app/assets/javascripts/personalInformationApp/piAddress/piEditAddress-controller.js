personalInformationAppControllers.controller('piEditAddressController',['$scope', '$modalInstance', 'piAddressService','$rootScope', '$state',
    '$filter', 'notificationCenterService', 'piCrudService', 'editAddressProperties', 'personalInformationService',
    function ($scope, $modalInstance, piAddressService, $rootScope, $state, $filter, notificationCenterService, piCrudService, editAddressProperties,
              personalInformationService){

        // CONTROLLER VARIABLES
        // --------------------
        $scope.isCreateNew = true;
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
        var getNullSafeAddress = function(address){
                return {
                    id: address.id,
                    county: address.county ? address.county : {},
                    state: address.state ? address.state : {},
                    nation: address.nation ? address.nation : {},
                    addressType: address.addressType ? address.addressType : {},
                    city: address.city,
                    fromDate: address.fromDate,
                    toDate: address.toDate,
                    houseNumber: address.houseNumber,
                    streetLine1: address.streetLine1,
                    streetLine2: address.streetLine2,
                    streetLine3: address.streetLine3,
                    streetLine4: address.streetLine4,
                    zip: address.zip
                };
            },
            getSaveSafeAddress = function(address) {
                return {
                    id: address.id,
                    county: address.county ? (address.county.code ? address.county : null) : null,
                    state: address.state ? (address.state.code ? address.state : null) : null,
                    nation: address.nation ? (address.nation.code ? address.nation : null) : null,
                    addressType: address.addressType ? address.addressType : {},
                    city: address.city,
                    fromDate: address.fromDate,
                    toDate: address.toDate,
                    houseNumber: address.houseNumber,
                    streetLine1: address.streetLine1,
                    streetLine2: address.streetLine2,
                    streetLine3: address.streetLine3,
                    streetLine4: address.streetLine4,
                    zip: address.zip
                };
            },
            isValidAddress = function (scopeAddress) {
                var address = getNullSafeAddress(angular.copy(scopeAddress));

                $scope.addressTypeErrMsg = piAddressService.getErrorAddressType(address);
                $scope.fromDateErrMsg = piAddressService.getErrorFromDate(address);
                $scope.toDateErrMsg = piAddressService.getErrorDateFormat(address.toDate);
                $scope.dateRangeErrMsg = piAddressService.getErrorDateRange(address,
                    $scope.addressGroup[address.addressType.description]);
                $scope.streetLine1ErrMsg = piAddressService.getErrorStreetLine1(address);
                $scope.cityErrMsg = piAddressService.getErrorCity(address);
                $scope.stateErrMsg = piAddressService.getErrorState(address);
                $scope.zipErrMsg = piAddressService.getErrorZip(address);
                $scope.nationErrMsg = piAddressService.getErrorNation(address);

                return !($scope.addressTypeErrMsg || $scope.fromDateErrMsg || $scope.toDateErrMsg || $scope.dateRangeErrMsg ||
                    $scope.streetLine1ErrMsg || $scope.cityErrMsg || $scope.stateErrMsg || $scope.zipErrMsg || $scope.nationErrMsg);
            };

        $scope.cancelModal = function () {
            $modalInstance.dismiss('cancel');
            notificationCenterService.clearNotifications();
        };

        $scope.removeAddressFieldErrors = function() {
            var address = getNullSafeAddress(angular.copy($scope.address));

            if($scope.addressTypeErrMsg) {
                $scope.addressTypeErrMsg = piAddressService.getErrorAddressType(address);
            }
            if($scope.fromDateErrMsg) {
                $scope.fromDateErrMsg = piAddressService.getErrorFromDate(address);
            }
            if($scope.toDateErrMsg) {
                $scope.toDateErrMsg = piAddressService.getErrorDateFormat(address.toDate);
            }
            if($scope.dateRangeErrMsg) {
                $scope.dateRangeErrMsg = piAddressService.getErrorDateRange(address,
                    $scope.addressGroup[$scope.address.addressType.description]);
            }
            if($scope.streetLine1ErrMsg) {
                $scope.streetLine1ErrMsg = piAddressService.getErrorStreetLine1(address);
            }
            if($scope.cityErrMsg){
                $scope.cityErrMsg = piAddressService.getErrorCity(address);
            }
            if($scope.stateErrMsg) {
                $scope.stateErrMsg = piAddressService.getErrorState(address);
            }
            if($scope.zipErrMsg) {
                $scope.zipErrMsg = piAddressService.getErrorZip(address);
            }
            if($scope.nationErrMsg) {
                $scope.nationErrMsg = piAddressService.getErrorNation(address);
            }
        };

        $scope.saveAddress = function() {
            if(isValidAddress($scope.address)) {
                var copiedAddress = angular.copy($scope.address),
                    addressToSave = _.extend(copiedAddress, getSaveSafeAddress(copiedAddress));

                addressToSave.fromDate = personalInformationService.stringToDate($scope.address.fromDate);
                addressToSave.toDate = personalInformationService.stringToDate($scope.address.toDate);

                var handleResponse = function (response) {
                    if (response.failure) {
                        $scope.addressErrMsg = response.message;
                        piAddressService.displayErrorMessage(response.message);
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

        $scope.getAddressTypes = piCrudService.getListFn('AddressType');
        $scope.getNations = piCrudService.getListFn('Nation');
        $scope.getStates = piCrudService.getListFn('State');
        $scope.getCounties = piCrudService.getListFn('County');

        this.init = function() {

            if (editAddressProperties.currentAddress) {
                // Set up for "update address"
                $scope.isCreateNew = false;
                $scope.address = angular.copy(editAddressProperties.currentAddress);
                if($scope.address.nation) {
                    $scope.address.nation = {
                        code: $scope.address.nation.code,
                        description: $scope.address.nation.nation
                    };
                }
            } else {
                // Create "new address" object
                $scope.address = {
                    county: null,
                    state: null,
                    nation: null,
                    addressType: null,
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
