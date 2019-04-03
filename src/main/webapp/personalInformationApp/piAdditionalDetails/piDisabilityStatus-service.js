personalInformationApp.service('piDisabilityStatusService', ['notificationCenterService',
    function (notificationCenterService) {

        var messages = [],
            disabilityMessageCenter = '#disabilityErrorMsgCenter';

        this.getDisabilityStatusError = function(disabilityStatus) {
            var msg = 'personinfo.disability.error.noneSelected';
            if (!disabilityStatus) {
                messages.push({msg: msg, type: 'error'});

                return msg;
            }
            else {
                notificationCenterService.removeNotification(msg);
            }
        };

        this.displayMessages = function() {
            notificationCenterService.setLocalMessageCenter(disabilityMessageCenter);

            _.each(messages, function(message) {
                notificationCenterService.addNotification(message.msg, message.type);
            });

            messages = [];

            notificationCenterService.setLocalMessageCenter(null);
        };

        this.displayErrorMessage = function(message) {
            notificationCenterService.setLocalMessageCenter(disabilityMessageCenter);
            notificationCenterService.displayNotification(message, "error");
            notificationCenterService.setLocalMessageCenter(null);
        };
    }
]);
