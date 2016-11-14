personalInformationApp.service('piPhoneService', ['notificationCenterService',
    function (notificationCenterService) {
        var messages = [];

        this.getErrorPhoneType = function (phone) {
            var msg = 'personInfo.phone.error.phoneType';
            if (!phone.telephoneType.code) {
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
