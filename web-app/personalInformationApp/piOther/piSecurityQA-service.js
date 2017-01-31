personalInformationApp.service('piSecurityQAService', ['$resource', 'notificationCenterService',
    function ($resource, notificationCenterService) {

        this.getQuestions = function () {
            return $resource('../ssb/:controller/:action',
                {controller: 'PersonalInformationQA', action: 'getSecurityQA'}).get();
        };

        this.saveQuestions = function (entity) {
            return $resource('../ssb/:controller/:action',
                {controller: 'PersonalInformationQA', action: 'save'}, {save: {method: 'POST'}}).save(entity);
        };

        this.displayErrorMessage = function(message) {
            notificationCenterService.setLocalMessageCenter("#personalDetailsErr");
            notificationCenterService.displayNotification(message, "error");
            notificationCenterService.setLocalMessageCenter(null);
        };
    }
]);
