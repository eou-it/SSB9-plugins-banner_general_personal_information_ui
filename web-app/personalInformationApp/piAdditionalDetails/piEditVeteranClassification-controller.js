personalInformationAppControllers.controller('piEditVeteranClassificationController',['$scope', '$modalInstance', '$rootScope', '$state',
    '$filter', 'notificationCenterService', 'piVeteranClassificationService', 'personalInformationService', 'piCrudService',
    'veteranClassInfo',
    function ($scope, $modalInstance, $rootScope, $state, $filter, notificationCenterService, piVeteranClassificationService,
              personalInformationService, piCrudService, veteranClassInfo){

        // CONTROLLER VARIABLES
        // --------------------
        //$scope.disabilityStatus = disabilityStatus;
        $scope.isVeteranTextClipped = true;
        $scope.veteranClassInfo = piVeteranClassificationService.encodeVeteranClassToChoice(veteranClassInfo);
        $scope.vetConsts = piVeteranClassificationService.vetChoiceConst;
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

        var isValidVeteranClassification = function() {

            return true;
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
        this.init();

    }
]);