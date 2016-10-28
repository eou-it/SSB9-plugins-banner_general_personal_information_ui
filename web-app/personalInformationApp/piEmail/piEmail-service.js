personalInformationApp.service('piEmailService', ['notificationCenterService',
    function (notificationCenterService) {
        var messages = [],
            invalidCharRegEx = /[ !#\$%\^&*\(\)\+=\{}\[\]\|"<>\?\\`;]/i;

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

            return this.getErrorEmailAddressFormat(email);
        };

        this.getErrorEmailAddressFormat = function (email) {
            var msg = 'personInfo.email.error.emailAddressFormat';
            if (invalidCharRegEx.test(email.emailAddress)) {
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
