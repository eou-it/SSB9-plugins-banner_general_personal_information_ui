personalInformationAppControllers.controller('piEditDirectoryProfileController',['$scope', '$modalInstance', '$rootScope', '$state',
    '$filter', 'notificationCenterService', 'piCrudService', 'personalInformationService',
    function ($scope, $modalInstance, $rootScope, $state, $filter, notificationCenterService, piCrudService,
              personalInformationService){

        // CONTROLLER VARIABLES
        // --------------------
        $scope.directoryProfile


        // CONTROLLER FUNCTIONS
        // --------------------
        $scope.cancelModal = function () {
            $modalInstance.dismiss('cancel');
            notificationCenterService.clearNotifications();
        };

        $scope.saveDirectoryProfile = function() {
            //if(isValidContact($scope.emergencyContact)) {
            //    var handleResponse = function (response) {
            //        if (response.failure) {
            //            $scope.addressErrMsg = response.message;
            //            notificationCenterService.displayNotification(response.message, "error");
            //        } else {
            //            var notifications = [];
            //            notifications.push({message: 'personInfo.save.success.message',
            //                messageType: $scope.notificationSuccessType,
            //                flashType: $scope.flashNotification}
            //            );
            //
            //            $state.go(personalInformationService.getFullProfileState(),
            //                {onLoadNotifications: notifications, startingTab: 'emergencyContact'},
            //                {reload: true, inherit: false, notify: true}
            //            );
            //        }
            //    };
            //
            //    if($scope.isCreateNew) {
            //        piCrudService.create('EmergencyContact', $scope.emergencyContact).$promise.then(handleResponse);
            //    }
            //    else {
            //        piCrudService.update('EmergencyContact', $scope.emergencyContact).$promise.then(handleResponse);
            //    }
            //}
            //else {
            //    piEmergencyContactService.displayMessages();
            //}
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
