personalInformationApp.service('piPersonalDetailsService', ['notificationCenterService',
    function (notificationCenterService) {

        this.displayErrorMessage = function(message) {
            notificationCenterService.setLocalMessageCenter("#personalDetailsErr");
            notificationCenterService.displayNotification(message, "error");
            notificationCenterService.setLocalMessageCenter(null);
        };
    }
]);
