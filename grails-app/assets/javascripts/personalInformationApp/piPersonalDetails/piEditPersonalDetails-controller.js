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

        $scope.isFieldViewable = function(configValue){
            return configValue === 1 || configValue === 2
        };

        $scope.isFieldUpdatable = function(configValue) {
            return configValue === 2
        };

        $scope.savePersonalDetails = function() {
            var handleResponse = function (response) {
                if (response.failure) {
                    $scope.personalDetailsErrMsg = response.message;
                    piPersonalDetailsService.displayErrorMessage(response.message, "error");
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

            maritalStatusFromScope = $scope.personalDetails.maritalStatus ? $scope.personalDetails.maritalStatus : {},

            personalDetailsForUpdate = {
                id: $scope.personalDetails.id,
                version: $scope.personalDetails.version,
                preferenceFirstName: $scope.personalDetails.preferenceFirstName,
                maritalStatus: {
                    id: maritalStatusFromScope.id,
                    version: maritalStatusFromScope.version,
                    code: maritalStatusFromScope.code,
                    description: maritalStatusFromScope.description
                },
                gender: $scope.personalDetails.gender,
                pronoun: $scope.personalDetails.pronoun
            };

            piCrudService.update('PersonalDetails', personalDetailsForUpdate).$promise.then(handleResponse);
        };

        $scope.getMaritalStatuses = piCrudService.getListFn('MaritalStatus');
        $scope.getGenders = piCrudService.getListFn('Gender');
        $scope.getPronouns = piCrudService.getListFn('Pronoun');

        this.init = function() {
            $scope.personalDetails = angular.copy($scope.personalDetails);

            if(!$scope.personalDetails.gender || !$scope.personalDetails.gender.code) {
                $scope.personalDetails.gender = null;
            }

            if(!$scope.personalDetails.pronoun || !$scope.personalDetails.pronoun.code) {
                $scope.personalDetails.pronoun = null;
            }
        };

        // INITIALIZE
        // ----------
        this.init();

    }
]);
