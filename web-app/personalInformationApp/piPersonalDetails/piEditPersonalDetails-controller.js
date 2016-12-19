personalInformationAppControllers.controller('piEditPersonalDetailsController',['$scope', '$modalInstance', 'piPersonalDetailsService','$rootScope', '$state',
    '$filter', 'notificationCenterService', 'piCrudService', 'personalInformationService',
    function ($scope, $modalInstance, piPersonalDetailsService, $rootScope, $state, $filter, notificationCenterService, piCrudService,
              personalInformationService){

        // CONTROLLER VARIABLES
        // --------------------
        $scope.personalDetailsErrMsg = '';


        // CONTROLLER FUNCTIONS
        // --------------------
        $scope.cancelModal = function () {
            $modalInstance.dismiss('cancel');
            notificationCenterService.clearNotifications();
        };

        $scope.savePersonalDetails = function() {
            var handleResponse = function (response) {
                if (response.failure) {
                    $scope.personalDetailsErrMsg = response.message;
                    notificationCenterService.displayNotification(response.message, "error");
                } else {
                    var notifications = [];
                    notifications.push({message: 'personInfo.save.success.message',
                        messageType: $scope.notificationSuccessType,
                        flashType: $scope.flashNotification}
                    );

                    $state.go(personalInformationService.getFullProfileState(),
                        {onLoadNotifications: notifications, startingTab: 'personalDetails'},
                        {reload: true, inherit: false, notify: true}
                    );
                }
            },

            maritalStatusFromScope = $scope.personalDetails.maritalStatus,

            personalDetailsForUpdate = {
                id: $scope.personalDetails.id,
                version: $scope.personalDetails.version,
                preferenceFirstName: $scope.personalDetails.preferenceFirstName,
                maritalStatus: {
                    id: maritalStatusFromScope.id,
                    version: maritalStatusFromScope.version,
                    code: maritalStatusFromScope.code,
                    description: maritalStatusFromScope.description
                }
            };

            piCrudService.update('PersonalDetails', personalDetailsForUpdate).$promise.then(handleResponse);
        };

        this.init = function() {

        };

        // INITIALIZE
        // ----------
        this.init();

    }
]);
