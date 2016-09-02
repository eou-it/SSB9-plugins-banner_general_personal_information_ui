personalInformationAppControllers.controller('piEditEmailController',['$scope', '$modalInstance', 'piEmailService','$rootScope', '$state',
    '$filter', 'notificationCenterService', 'piCrudService', 'editEmailProperties',
    function ($scope, $modalInstance, piEmailService, $rootScope, $state, $filter, notificationCenterService, piCrudService, editEmailProperties){

        // CONTROLLER VARIABLES
        // --------------------
        $scope.isCreateNew = true;


        // CONTROLLER FUNCTIONS
        // --------------------
        $scope.cancelModal = function () {
            $modalInstance.dismiss('cancel');
            notificationCenterService.clearNotifications();
        };

        $scope.removeAddressFieldErrors = function(){
            console.log('boo!');
        };

        $scope.saveEmail = function() {
                var handleResponse = function (response) {
                    if (response.failure) {
                        notificationCenterService.displayNotification(response.message, "error");
                    }
                    else {
                        var notifications = [];
                        notifications.push({message: 'personInfo.save.success.message',
                                messageType: $scope.notificationSuccessType,
                                flashType: $scope.flashNotification}
                        );

                        $state.go('personalInformationMain',
                            {onLoadNotifications: notifications, startingTab: 'email'},
                            {reload: true, inherit: false, notify: true}
                        );
                    }
                };

                if($scope.isCreateNew) {
                    piCrudService.create('Email', $scope.email).$promise.then(handleResponse);
                }
                else {
                    piCrudService.update('Email', $scope.email).$promise.then(handleResponse);
                }
        };

        this.init = function() {

            if (editEmailProperties.currentEmail) {
                // Set up for "update address"
                $scope.isCreateNew = false;
                $scope.email = angular.copy(editEmailProperties.currentEmail);
            } else {
                // Create "new address" object
                $scope.email = {
                    emailType: {},
                    emailAddress: '',
                    commentData: '',
                    preferredIndicator: false
                };

            }
        };

        // INITIALIZE
        // ----------
        this.init();

    }
]);
