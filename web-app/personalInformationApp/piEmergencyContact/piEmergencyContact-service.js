personalInformationApp.service('piEmergencyContactService', ['notificationCenterService',
    function (notificationCenterService) {

        var messages = [];

        this.getErrorFirstName = function(contact) {
            var msg = 'personInfo.person.error.firstName';
            if (!contact.firstName) {
                messages.push({msg: msg, type: 'error'});

                return msg;
            }
            else {
                notificationCenterService.removeNotification(msg);
            }
        };

        this.getErrorLastName = function(contact) {
            var msg = 'personInfo.person.error.lastName';
            if (!contact.lastName) {
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
