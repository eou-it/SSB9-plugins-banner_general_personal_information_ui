personalInformationAppControllers.controller('piEditDirectoryProfileController',['$scope', '$modalInstance', '$rootScope', '$state',
    '$filter', 'notificationCenterService', 'piCrudService',
    function ($scope, $modalInstance, $rootScope, $state, $filter, notificationCenterService, piCrudService) {

        // CONTROLLER VARIABLES
        // --------------------
        $scope.directoryProfile;
        $scope.dirProfileErrMsg = '';


        // CONTROLLER FUNCTIONS
        // --------------------
        $scope.cancelModal = function () {
            $modalInstance.dismiss('cancel');
            notificationCenterService.clearNotifications();
        };

        $scope.saveDirectoryProfile = function() {
            var handleResponse = function (response) {
                if (response.failure) {
                    $scope.dirProfileErrMsg = response.message;
                    notificationCenterService.displayNotification(response.message, "error");
                } else {
                    $modalInstance.dismiss('cancel');

                    notificationCenterService.displayNotification(
                        'personInfo.save.success.message',
                        $scope.notificationSuccessType,
                        $scope.flashNotification
                    );
                }
            };

            piCrudService.update('DirectoryProfilePreferences', $scope.directoryProfile).$promise.then(handleResponse);
        };

        this.init = function() {

            piCrudService.get('DirectoryProfile').$promise.then(function(response) {
                if(response.failure) {
                    notificationCenterService.displayNotification(response.message, $scope.notificationErrorType);
                } else {
                    $scope.directoryProfile = response.directoryProfile;
                }
            });
        };

        // INITIALIZE
        // ----------
        this.init();

    }
]);
