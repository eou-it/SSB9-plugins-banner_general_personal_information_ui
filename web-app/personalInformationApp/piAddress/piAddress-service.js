personalInformationApp.service('piAddressService', ['notificationCenterService',
    function (notificationCenterService) {

        var messages = [];

        this.getErrorAddressType = function (address) {
            var msg = 'personInfo.address.error.addressType';
            if (!address.addressType.code) {
                messages.push({msg: msg, type: 'error'});

                return msg;
            }
            else {
                notificationCenterService.removeNotification(msg);
            }
        };

        this.getErrorFromDate = function (address) {
            var msg = 'personInfo.address.error.fromDate';
            if (!address.fromDate) {
                messages.push({msg: msg, type: 'error'});

                return msg;
            }
            else {
                notificationCenterService.removeNotification(msg);
            }
        };

        this.getErrorStreetLine1 = function(address) {
            var msg = 'personInfo.address.error.streetLine1';
            if (!address.streetLine1) {
                messages.push({msg: msg, type: 'error'});

                return msg;
            }
            else {
                notificationCenterService.removeNotification(msg);
            }
        };

        this.getErrorCity = function(address) {
            var msg = 'personInfo.address.error.city';
            if (!address.city) {
                messages.push({msg: msg, type: 'error'});

                return msg;
            }
            else {
                notificationCenterService.removeNotification(msg);
            }
        };

        this.getErrorStateCountyNation = function(address) {
            var msg = 'personInfo.address.error.stateNationZip';
            if((!address.state.code && !address.nation.code) || (address.state.code && !address.zip)){
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
