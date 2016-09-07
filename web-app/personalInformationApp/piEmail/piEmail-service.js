personalInformationApp.service('piEmailService', ['notificationCenterService',
    function (notificationCenterService) {
        var messages = [];

        this.getErrorEmailType = function (email) {
            var msg = 'personInfo.email.error.emailType';
            if (!email.emailType.code) {
                messages.push({msg: msg, type: 'error'});

                return msg;
            }
            else {
                notificationCenterService.removeNotification(msg);
            }
        };

        this.getErrorEmailAddress = function (email) {
            var msg = 'personInfo.email.error.emailAddress';
            if (!email.emailAddress) {
                messages.push({msg: msg, type: 'error'});

                return msg;
            }
            else {
                notificationCenterService.removeNotification(msg);
            }
        };

        this.displayMessages = function() {
            _.each(messages, function(message) {
                notificationCenterService.addNotification(message.msg, message.type);
            });

            messages = [];
        };
    }
]);
