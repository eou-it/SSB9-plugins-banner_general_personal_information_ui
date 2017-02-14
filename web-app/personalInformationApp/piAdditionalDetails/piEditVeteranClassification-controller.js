personalInformationAppControllers.controller('piEditVeteranClassificationController',['$scope', '$modalInstance', '$rootScope', '$state',
    '$filter', 'notificationCenterService', 'piVeteranClassificationService', 'personalInformationService', 'piCrudService',
    function ($scope, $modalInstance, $rootScope, $state, $filter, notificationCenterService, piVeteranClassificationService,
              personalInformationService, piCrudService){

        // CONTROLLER VARIABLES
        // --------------------
        //$scope.disabilityStatus = disabilityStatus;
        $scope.isVeteranTextClipped = true;
        $scope.disabilityErrMsg = '';
        $scope.disabilityUpdateErrMsg = '';


        // CONTROLLER FUNCTIONS
        // --------------------
        $scope.cancelModal = function () {
            $modalInstance.dismiss('cancel');
            notificationCenterService.clearNotifications();
        };

        $scope.toggleVeteranTextVisibility = function() {
            $scope.isVeteranTextClipped = !$scope.isVeteranTextClipped;
        };

        var isValidDisabilityStatus = function() {
            $scope.disabilityErrMsg = piVeteranClassificationService.getDisabilityStatusError($scope.disabilityStatus);

            return !($scope.disabilityErrMsg);
        };

        $scope.saveDisabilityStatus = function() {
            if (isValidDisabilityStatus()) {
                var disabilityStatusCode = {code: $scope.disabilityStatus},
                    handleResponse = function (response) {
                        if (response.failure) {
                            $scope.disabilityUpdateErrMsg = response.message;
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
                    };

                piCrudService.update('DisabilityStatus', disabilityStatusCode).$promise.then(handleResponse);
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