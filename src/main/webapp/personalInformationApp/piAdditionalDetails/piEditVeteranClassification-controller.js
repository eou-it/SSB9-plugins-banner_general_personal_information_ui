personalInformationAppControllers.controller('piEditVeteranClassificationController',['$scope', '$modalInstance', '$rootScope', '$state',
    '$filter', 'notificationCenterService', 'piVeteranClassificationService', 'personalInformationService', 'piCrudService',
    'veteranClassInfo',
    function ($scope, $modalInstance, $rootScope, $state, $filter, notificationCenterService, piVeteranClassificationService,
              personalInformationService, piCrudService, veteranClassInfo){

        // CONTROLLER VARIABLES
        // --------------------
        $scope.isVeteranTextClipped = true;
        $scope.veteranClassInfo = piVeteranClassificationService.encodeVeteranClassToChoice(veteranClassInfo);
        $scope.vetConsts = piVeteranClassificationService.vetChoiceConst;
        $scope.veteranErrMsg = '';
        $scope.seprDateErrMsg = '';
        $scope.veteranUpdateErrMsg = '';


        // CONTROLLER FUNCTIONS
        // --------------------
        $scope.cancelModal = function () {
            $modalInstance.dismiss('cancel');
            notificationCenterService.clearNotifications();
        };

        $scope.toggleVeteranTextVisibility = function() {
            $scope.isVeteranTextClipped = !$scope.isVeteranTextClipped;
        };

        $scope.setSeprDate = function(date){
            $scope.veteranClassInfo.activeDutySeprDate = date;
        };

        $scope.removeVeteranErrors = function() {
            if($scope.seprDateErrMsg) {
                $scope.seprDateErrMsg = piVeteranClassificationService.getSeprDateError($scope.veteranClassInfo.activeDutySeprDate);
            }
        };

        var isValidVeteranClassification = function() {
            $scope.seprDateErrMsg = piVeteranClassificationService.getSeprDateError($scope.veteranClassInfo.activeDutySeprDate);
            if(!$scope.seprDateErrMsg) {
                $scope.veteranErrMsg = piVeteranClassificationService.getVeteranProtectedError($scope.veteranClassInfo);
                $scope.veteranErrMsg = !$scope.veteranErrMsg ? piVeteranClassificationService.getVeteranClassificationError($scope.veteranClassInfo) : $scope.veteranErrMsg;
            }

            return !($scope.seprDateErrMsg || $scope.veteranErrMsg);
        };

        $scope.saveVeteranClassification = function() {
            if (isValidVeteranClassification()) {
                var handleResponse = function (response) {
                        if (response.failure) {
                            $scope.veteranUpdateErrMsg = response.message;
                            piVeteranClassificationService.displayErrorMessage(response.message);
                        }
                        else {
                            notificationCenterService.clearNotifications();
                            $scope.cancelModal();

                            var notifications = [];
                            notifications.push({
                                    message: 'personInfo.save.success.message',
                                    messageType: $scope.notificationSuccessType,
                                    flashType: $scope.flashNotification
                                }
                            );

                            $state.go(personalInformationService.getFullProfileState(),
                                {onLoadNotifications: notifications, startingTab: 'additionalDetails'},
                                {reload: true, inherit: false, notify: true}
                            );
                        }
                    },
                veteranInfoToSave = piVeteranClassificationService.decodeChoiceToVeteranClass($scope.veteranClassInfo);

                piCrudService.update('VeteranClassification', veteranInfoToSave).$promise.then(handleResponse);
            }
            else {
                piVeteranClassificationService.displayMessages();
            }
        };

        this.init = function() {
        };

        // INITIALIZE
        // ----------
        //this.init();

    }
]);
