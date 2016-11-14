personalInformationAppControllers.controller('piEditPhoneController',['$scope', '$modalInstance', 'piPhoneService','$rootScope', '$state',
    '$filter', 'notificationCenterService', 'piCrudService', 'editPhoneProperties', 'personalInformationService',
    function ($scope, $modalInstance, piPhoneService, $rootScope, $state, $filter, notificationCenterService, piCrudService, editPhoneProperties,
              personalInformationService){

        // CONTROLLER VARIABLES
        // --------------------
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
            if(!!$scope.phoneTypeErrMsg) {
                $scope.phoneTypeErrMsg = piPhoneService.getErrorPhoneType($scope.phone);
            }
            if(!!$scope.phoneNumberErrMsg) {
                //$scope.phoneNumberErrMsg = piPhoneService.getErrorEmailAddress($scope.phone);
            }
        };

        var isValidTelephoneNumber = function () {
            $scope.phoneTypeErrMsg = piPhoneService.getErrorPhoneType($scope.phone);
            //$scope.phoneNumberErrMsg = piPhoneService.getErrorEmailAddress($scope.phone);

            return !($scope.phoneTypeErrMsg || $scope.phoneNumberErrMsg);
        };

        $scope.savePhone = function() {
            if (isValidTelephoneNumber()) {
                var phoneToSave = angular.copy($scope.phone);
                phoneToSave.unlistIndicator = phoneToSave.unlistIndicator ? 'Y' : null;

                var handleResponse = function (response) {
                    if (response.failure) {
                        notificationCenterService.displayNotification(response.message, "error");
                        $scope.phoneErrMsg = response.message;
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
                            {onLoadNotifications: notifications, startingTab: 'phone'},
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
                piPhoneService.displayMessages();
            }
        };

        this.init = function() {

            if (editPhoneProperties.currentPhone) {
                // Set up for "update phone"
                $scope.isCreateNew = false;
                $scope.phone = angular.copy(editPhoneProperties.currentPhone);
            } else {
                // Create "new phone" object
                $scope.phone = {
                    telephoneType: {},
                    internationalAccess: '',
                    countryPhone: '',
                    phoneArea: '',
                    phoneNumber: '',
                    phoneExtension: '',
                    unlistIndicator: false
                };
            }
        };

        // INITIALIZE
        // ----------
        this.init();

    }
]);
