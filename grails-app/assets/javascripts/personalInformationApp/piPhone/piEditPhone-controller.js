/********************************************************************************
  Copyright 2016-2019 Ellucian Company L.P. and its affiliates.
********************************************************************************/
personalInformationAppControllers.controller('piEditPhoneController',['$scope', '$modalInstance', 'piPhoneService','$rootScope', '$state',
    '$filter', 'notificationCenterService', 'piCrudService', 'editPhoneProperties', 'personalInformationService',
    function ($scope, $modalInstance, piPhoneService, $rootScope, $state, $filter, notificationCenterService, piCrudService, editPhoneProperties,
              personalInformationService){

        // CONTROLLER VARIABLES
        // --------------------
        var getNullSafePhone = function(phone){
                return {
                    telephoneType: phone.telephoneType ? phone.telephoneType: {},
                    internationalAccess: phone.internationalAccess,
                    countryPhone: phone.countryPhone,
                    phoneArea: phone.phoneArea,
                    phoneNumber: phone.phoneNumber,
                    phoneExtension: phone.phoneExtension,
                    primaryIndicator: phone.primaryIndicator,
                    unlistIndicator: phone.unlistIndicator
                };
            },
            isValidTelephoneNumber = function () {
                var phone = getNullSafePhone($scope.phone);

                $scope.phoneTypeErrMsg = piPhoneService.getErrorPhoneType(phone);
                $scope.phoneNumberErrMsg = piPhoneService.getErrorPhoneNumber(phone);

                return !($scope.phoneTypeErrMsg || $scope.phoneNumberErrMsg);
            };

        $scope.isCreateNew = true;
        $scope.phoneTypeErrMsg = '';
        $scope.phoneNumberErrMsg = '';
        $scope.emailErrMsg = '';


        // CONTROLLER FUNCTIONS
        // --------------------
        $scope.cancelModal = function () {
            $modalInstance.dismiss('cancel');
            notificationCenterService.clearNotifications();
        };

        $scope.removePhoneFieldErrors = function() {
            var phone = getNullSafePhone($scope.phone);

            if($scope.phoneTypeErrMsg) {
                $scope.phoneTypeErrMsg = piPhoneService.getErrorPhoneType(phone);
            }
            if($scope.phoneNumberErrMsg) {
                $scope.phoneNumberErrMsg = piPhoneService.getErrorPhoneNumber(phone);
            }
        };

        $scope.savePhone = function() {
            if (isValidTelephoneNumber()) {
                var phoneToSave = angular.copy($scope.phone);
                phoneToSave.primaryIndicator = phoneToSave.primaryIndicator ? 'Y' : null;
                phoneToSave.unlistIndicator = phoneToSave.unlistIndicator ? 'Y' : null;
                piPhoneService.trimPhoneNumber(phoneToSave);

                var handleResponse = function (response) {
                    if (response.failure) {
                        $scope.phoneErrMsg = response.message;
                        piPhoneService.displayErrorMessage(response.message);
                    }
                    else {
                        notificationCenterService.clearNotifications();

                        var notifications = [];
                        notifications.push({
                                message: 'personInfo.save.success.message',
                                messageType: $scope.notificationSuccessType,
                                flashType: $scope.flashNotification
                            }
                        );

                        $state.go(personalInformationService.getFullProfileState(),
                            {onLoadNotifications: notifications, startingTab: 'phoneNumber'},
                            {reload: true, inherit: false, notify: true}
                        );
                    }
                };

                if ($scope.isCreateNew) {
                    piCrudService.create('TelephoneNumber', phoneToSave).$promise.then(handleResponse);
                }
                else {
                    piCrudService.update('TelephoneNumber', phoneToSave).$promise.then(handleResponse);
                }
            }
            else {
                piPhoneService.displayMessages($scope.maskingRules.displayInternationalAccess);
            }
        };

        $scope.getPhoneTypes = piCrudService.getListFn('TelephoneType');

        this.init = function() {

            if (editPhoneProperties.currentPhone) {
                // Set up for "update phone"
                $scope.isCreateNew = false;
                $scope.phone = angular.copy(editPhoneProperties.currentPhone);
                $scope.phone.primaryIndicator = $scope.phone.primaryIndicator === 'Y';
                $scope.phone.unlistIndicator = $scope.phone.unlistIndicator === 'Y';
            } else {
                // Create "new phone" object
                $scope.phone = {
                    telephoneType: null,
                    internationalAccess: '',
                    countryPhone: '',
                    phoneArea: '',
                    phoneNumber: '',
                    phoneExtension: '',
                    primaryIndicator: false,
                    unlistIndicator: false
                };
            }
        };

        // INITIALIZE
        // ----------
        this.init();

    }
]);
