personalInformationAppControllers.controller('piEditDisabilityStatusController',['$scope', '$modalInstance', '$rootScope', '$state',
    '$filter', 'notificationCenterService', 'piDisabilityStatusService', 'personalInformationService', 'piCrudService',
    'disabilityStatus',
    function ($scope, $modalInstance, $rootScope, $state, $filter, notificationCenterService, piDisabilityStatusService,
              personalInformationService, piCrudService, disabilityStatus){

        // CONTROLLER VARIABLES
        // --------------------
        $scope.disabilityStatus = disabilityStatus;
        $scope.isDisablilityTextClipped = true;
        $scope.disabilityErrMsg = '';
        $scope.disabilityUpdateErrMsg = '';


        // CONTROLLER FUNCTIONS
        // --------------------
        $scope.cancelModal = function () {
            $modalInstance.dismiss('cancel');
            notificationCenterService.clearNotifications();
        };

        $scope.toggleDisabilityTextVisibility = function() {
            $scope.isDisablilityTextClipped = !$scope.isDisablilityTextClipped;
        };

        var isValidDisabilityStatus = function() {
            $scope.disabilityErrMsg = piDisabilityStatusService.getDisabilityStatusError($scope.disabilityStatus);

            return !($scope.disabilityErrMsg);
        };

        $scope.saveDisabilityStatus = function() {
            if (isValidDisabilityStatus()) {
                var disabilityStatusCode = {code: $scope.disabilityStatus},
                handleResponse = function (response) {
                        if (response.failure) {
                            $scope.disabilityUpdateErrMsg = response.message;
                            piDisabilityStatusService.displayErrorMessage(response.message);
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
                piDisabilityStatusService.displayMessages();
            }
        };

        this.init = function() {
        };

        // INITIALIZE
        // ----------
        this.init();

    }
]);