personalInformationAppControllers.controller('piEditEmailController',['$scope', '$modalInstance', 'piEmailService','$rootScope', '$state',
    '$filter', 'notificationCenterService', 'piCrudService', 'editEmailProperties',
    function ($scope, $modalInstance, piEmailService, $rootScope, $state, $filter, notificationCenterService, piCrudService, editEmailProperties){

        // CONTROLLER VARIABLES
        // --------------------
        $scope.isCreateNew = true;
        $scope.emailTypeErrMsg = '';
        $scope.emailAddressErrMsg = '';
        $scope.emailErrMsg = '';


        // CONTROLLER FUNCTIONS
        // --------------------
        $scope.cancelModal = function () {
            $modalInstance.dismiss('cancel');
            notificationCenterService.clearNotifications();
        };

        $scope.removeEmailFieldErrors = function() {
            if(!!$scope.emailTypeErrMsg) {
                $scope.emailTypeErrMsg = piEmailService.getErrorEmailType($scope.email);
            }
            if(!!$scope.emailAddressErrMsg) {
                $scope.emailAddressErrMsg = piEmailService.getErrorEmailAddress($scope.email);
            }
        };

        var isValidEmail = function () {
            $scope.emailTypeErrMsg = piEmailService.getErrorEmailType($scope.email);
            $scope.emailAddressErrMsg = piEmailService.getErrorEmailAddress($scope.email);

            return !($scope.emailTypeErrMsg || $scope.emailAddressErrMsg);
        };

        $scope.saveEmail = function() {
            if (isValidEmail()) {
                var handleResponse = function (response) {
                    if (response.failure) {
                        notificationCenterService.displayNotification(response.message, "error");
                        $scope.emailErrMsg = response.message;
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

                        $state.go('personalInformationMain',
                            {onLoadNotifications: notifications, startingTab: 'email'},
                            {reload: true, inherit: false, notify: true}
                        );
                    }
                };

                if ($scope.isCreateNew) {
                    piCrudService.create('Email', $scope.email).$promise.then(handleResponse);
                }
                else {
                    piCrudService.update('Email', $scope.email).$promise.then(handleResponse);
                }
            }
            else {
                piEmailService.displayMessages();
            }
        };

        this.init = function() {

            if (editEmailProperties.currentEmail) {
                // Set up for "update email"
                $scope.isCreateNew = false;
                $scope.email = angular.copy(editEmailProperties.currentEmail);
            } else {
                // Create "new email" object
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
