personalInformationAppControllers.controller('piEditEmailController',['$scope', '$modalInstance', 'piEmailService','$rootScope', '$state',
    '$filter', 'notificationCenterService', 'piCrudService', 'editEmailProperties', 'personalInformationService',
    function ($scope, $modalInstance, piEmailService, $rootScope, $state, $filter, notificationCenterService, piCrudService, editEmailProperties,
              personalInformationService){

        // CONTROLLER VARIABLES
        // --------------------
        $scope.isCreateNew = true;
        $scope.emailTypeErrMsg = '';
        $scope.emailAddressErrMsg = '';
        $scope.emailErrMsg = '';


        // CONTROLLER FUNCTIONS
        // --------------------
        var getNullSafeEmail = function(email) {
                return {
                    emailType: email.emailType ? email.emailType : {},
                    emailAddress: email.emailAddress,
                    commentData: email.commentData,
                    preferredIndicator: email.preferredIndicator
                };
            },
            isValidEmail = function () {
                var email = getNullSafeEmail($scope.email);

                $scope.emailTypeErrMsg = piEmailService.getErrorEmailType(email);
                $scope.emailAddressErrMsg = piEmailService.getErrorEmailAddress(email);

                return !($scope.emailTypeErrMsg || $scope.emailAddressErrMsg);
            };

        $scope.cancelModal = function () {
            $modalInstance.dismiss('cancel');
            notificationCenterService.clearNotifications();
        };

        $scope.removeEmailFieldErrors = function() {
            var email = getNullSafeEmail($scope.email);

            if($scope.emailTypeErrMsg) {
                $scope.emailTypeErrMsg = piEmailService.getErrorEmailType(email);
            }
            if($scope.emailAddressErrMsg) {
                $scope.emailAddressErrMsg = piEmailService.getErrorEmailAddress(email);
            }
        };

        $scope.saveEmail = function() {
            if (isValidEmail()) {
                var handleResponse = function (response) {
                    if (response.failure) {
                        $scope.emailErrMsg = response.message;
                        piEmailService.displayErrorMessage(response.message);
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

                        $state.go(personalInformationService.getFullProfileState(),
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

        $scope.getEmailTypes = piCrudService.getListFn('EmailType');

        this.init = function() {

            if (editEmailProperties.currentEmail) {
                // Set up for "update email"
                $scope.isCreateNew = false;
                $scope.email = angular.copy(editEmailProperties.currentEmail);
            } else {
                // Create "new email" object
                $scope.email = {
                    emailType: null,
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
