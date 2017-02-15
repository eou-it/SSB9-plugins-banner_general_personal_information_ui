personalInformationApp.service('piVeteranClassificationService', ['notificationCenterService',
    function (notificationCenterService) {

        // CONSTANTS
        // ----------
        this.vetChoiceConst = {
            PROTECTED_VET: '1',
            PROTECTED_VET_UNCLASSIFIED: '2',
            UNPROTECTED_VET: '3',
            NOT_A_VET: '4'
        };

        var messages = [],
            veteranMessageCenter = '#veteransErrorMsgCenter';

        this.isRecentlySeperated = function(seprDate) {

            return false;
        };

        this.encodeVeteranClassToChoice = function(veteranClassInfo) {
            var c = this.vetChoiceConst;
            veteranClassInfo.badgeVeteran = false;
            veteranClassInfo.sdvetIndicator = veteranClassInfo.sdvetIndicator === 'Y';

            if(veteranClassInfo.veraIndicator === 'O') {
                veteranClassInfo.choice = c.PROTECTED_VET;
                veteranClassInfo.badgeVeteran = true;
            }
            else if(veteranClassInfo.veraIndicator === 'B') {
                if(veteranClassInfo.sdvetIndicator || veteranClassInfo.armedServiceMedalVetIndicator ||
                    this.isRecentlySeperated(veteranClassInfo.activeDutySeprDate)) {
                    veteranClassInfo.choice = c.PROTECTED_VET;
                }
                else {
                    veteranClassInfo.choice = c.PROTECTED_VET_UNCLASSIFIED;
                }
            }
            else if(veteranClassInfo.veraIndicator === 'V') {
                veteranClassInfo.choice = c.UNPROTECTED_VET;
            }
            else {
                veteranClassInfo.choice = c.NOT_A_VET;
            }

            return veteranClassInfo;
        };

        this.decodeChoiceToVeteranClass = function(veteranClassInfo) {
            var c = this.vetChoiceConst;
            var result = {
                id: veteranClassInfo.id,
                version: veteranClassInfo.version,
                armedServiceMedalVetIndicator: false,
                sdvetIndicator: null,
                activeDutySeprDate: veteranClassInfo.activeDutySeprDate
            };

            if(veteranClassInfo.choice === c.PROTECTED_VET) {
                if(veteranClassInfo.badgeVeteran) {
                    result.veraIndicator = 'O';
                }
                else if(veteranClassInfo.sdvetIndicator || veteranClassInfo.armedServiceMedalVetIndicator ||
                    this.isRecentlySeperated(veteranClassInfo.activeDutySeprDate)) {
                    result.veraIndicator = 'B';
                }
                else {
                    //throw an error, no choice under OPT 1 was selected
                    throw 'NoClassSelectedError';
                }
                result.sdvetIndicator = veteranClassInfo.sdvetIndicator ? 'Y' : null;
                result.armedServiceMedalVetIndicator = veteranClassInfo.armedServiceMedalVetIndicator;
                //result.activeDutySeprDate = veteranClassInfo.activeDutySeprDate;
            }
            else if(veteranClassInfo.choice === c.PROTECTED_VET_UNCLASSIFIED) {
                result.veraIndicator = 'B';
            }
            else if(veteranClassInfo.choice === c.UNPROTECTED_VET) {
                result.veraIndicator = 'V';
            }
            else if(veteranClassInfo.choice === c.NOT_A_VET) {
                result.veraIndicator = null;
            }

            return result;
        };

        /*
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
        */

        this.displayMessages = function() {
            notificationCenterService.setLocalMessageCenter(veteranMessageCenter);

            _.each(messages, function(message) {
                notificationCenterService.addNotification(message.msg, message.type);
            });

            messages = [];

            notificationCenterService.setLocalMessageCenter(null);
        };

        this.displayErrorMessage = function(message) {
            notificationCenterService.setLocalMessageCenter(veteranMessageCenter);
            notificationCenterService.displayNotification(message, "error");
            notificationCenterService.setLocalMessageCenter(null);
        };
    }
]);
