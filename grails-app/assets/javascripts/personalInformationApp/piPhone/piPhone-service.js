personalInformationApp.service('piPhoneService', ['notificationCenterService',
    function (notificationCenterService) {
        var messages = [],
            phoneMessageCenter = "#phoneErrorMsgCenter",
            trimmableCharRegex = /[\s-]/g,
            DOMESTIC_PHONE_NUM_ERROR = 'personInfo.phone.error.phoneNumber',
            INTL_PHONE_NUM_ERROR = 'personInfo.phone.error.intlPhoneNumber';

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
            var msg = DOMESTIC_PHONE_NUM_ERROR;
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

        this.displayMessages = function(useIntlOverride) {
            var notificationMsg;

            notificationCenterService.setLocalMessageCenter(phoneMessageCenter);

            _.each(messages, function(message) {
                notificationMsg = (useIntlOverride && message.msg === DOMESTIC_PHONE_NUM_ERROR) ? INTL_PHONE_NUM_ERROR : message.msg;

                notificationCenterService.addNotification(notificationMsg, message.type);
            });

            messages = [];

            notificationCenterService.setLocalMessageCenter(null);
        };

        this.displayErrorMessage = function(message) {
            notificationCenterService.setLocalMessageCenter(phoneMessageCenter);
            notificationCenterService.displayNotification(message, "error");
            notificationCenterService.setLocalMessageCenter(null);
        };
    }
]);
