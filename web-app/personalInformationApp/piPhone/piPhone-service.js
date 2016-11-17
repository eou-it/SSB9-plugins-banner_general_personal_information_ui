personalInformationApp.service('piPhoneService', ['notificationCenterService',
    function (notificationCenterService) {
        var messages = [],
            trimmableCharRegex = /[\s-]/g;

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

        this.getErrorPhoneNumber = function (phone) {
            var msg = 'personInfo.phone.error.phoneNumber';
            if (!phone.internationalAccess && !phone.phoneNumber) {
                messages.push({msg: msg, type: 'error'});

                return msg;
            }
            else {
                notificationCenterService.removeNotification(msg);
            }
        };

        this.trimPhoneNumber = function (phone) {
            if(phone.phoneNumber) {
                phone.phoneNumber = phone.phoneNumber.replace(trimmableCharRegex,'');
            }
            if(phone.internationalAccess) {
                phone.internationalAccess = phone.internationalAccess.replace(trimmableCharRegex,'');
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
