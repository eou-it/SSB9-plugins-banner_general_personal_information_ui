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

        this.getErrorStreetLine1 = function(contact) {
            var msg = 'personInfo.address.error.streetLine1';
            if (!contact.streetLine1) {
                messages.push({msg: msg, type: 'error'});

                return msg;
            }
            else {
                notificationCenterService.removeNotification(msg);
            }
        };

        this.getErrorCity = function(contact) {
            var msg = 'personInfo.address.error.city';
            if (!contact.city) {
                messages.push({msg: msg, type: 'error'});

                return msg;
            }
            else {
                notificationCenterService.removeNotification(msg);
            }
        };

        this.getErrorStateCountyNation = function(contact) {
            var msg = 'personInfo.address.error.stateNationZip';
            if((!contact.state.code && !contact.nation.code) || (contact.state.code && !contact.zip)){
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
