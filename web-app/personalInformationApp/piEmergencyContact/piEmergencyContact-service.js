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
            if ((contact.city || contact.zip || contact.state.code || contact.nation.code) && !contact.streetLine1) {
                messages.push({msg: msg, type: 'error'});

                return msg;
            }
            else {
                notificationCenterService.removeNotification(msg);
            }
        };

        this.getErrorCity = function(contact) {
            var msg = 'personInfo.address.error.city';
            if ((contact.streetLine1 || contact.state.code || contact.zip || contact.nation.code) && !contact.city) {
                messages.push({msg: msg, type: 'error'});

                return msg;
            }
            else {
                notificationCenterService.removeNotification(msg);
            }
        };

        this.getErrorState = function(contact) {
            var msg = 'personInfo.address.error.stateNationZip';
            if(!contact.nation.code && contact.zip && !contact.state.code) {
                messages.push({msg: msg, type: 'error'});

                return msg;
            }
            else {
                notificationCenterService.removeNotification(msg);
            }
        };

        this.getErrorZip = function(contact) {
            var msg = 'personInfo.address.error.stateNationZip';
            if(contact.state.code && !contact.zip) {
                messages.push({msg: msg, type: 'error'});

                return msg;
            }
            else {
                notificationCenterService.removeNotification(msg);
            }
        };

        this.getErrorNation = function(contact) {
            var msg = 'personInfo.address.error.stateNationZip';
            if(contact.city && (!contact.nation.code && !contact.state.code && !contact.zip)) {
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
