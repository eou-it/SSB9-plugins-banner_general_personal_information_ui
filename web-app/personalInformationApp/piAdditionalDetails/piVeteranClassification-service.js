personalInformationApp.service('piVeteranClassificationService', ['notificationCenterService', 'personalInformationService',
    function (notificationCenterService, personalInformationService) {

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

        this.isRecentlySeparated = function(seprDate) {
            var nowDate = new Date(),
            dateSeprDate = personalInformationService.stringToDate(seprDate),
            seprDateInPast = nowDate > dateSeprDate;

            // recently separated if seprDate is within the last 3 years
            nowDate.setFullYear((nowDate.getFullYear()) - 3);
            return seprDateInPast && dateSeprDate >= nowDate;
        };

        this.encodeVeteranClassToChoice = function(vetData) {
            var c = this.vetChoiceConst,
                veteranClassInfo = {
                    id: vetData.id,
                    version: vetData.version,
                    badgeVeteran: false,
                    sdvetIndicator: vetData.sdvetIndicator === 'Y',
                    armedServiceMedalVetIndicator: vetData.armedServiceMedalVetIndicator,
                    activeDutySeprDate: vetData.activeDutySeprDate
                };

            if(vetData.veraIndicator === 'O') {
                veteranClassInfo.choice = c.PROTECTED_VET;
                veteranClassInfo.badgeVeteran = true;
            }
            else if(vetData.veraIndicator === 'B') {
                if(veteranClassInfo.sdvetIndicator || veteranClassInfo.armedServiceMedalVetIndicator ||
                    this.isRecentlySeparated(veteranClassInfo.activeDutySeprDate)) {
                    veteranClassInfo.choice = c.PROTECTED_VET;
                }
                else {
                    veteranClassInfo.choice = c.PROTECTED_VET_UNCLASSIFIED;
                }
            }
            else if(vetData.veraIndicator === 'V') {
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
                activeDutySeprDate: personalInformationService.stringToDate(veteranClassInfo.activeDutySeprDate)
            };

            if(veteranClassInfo.choice === c.PROTECTED_VET) {
                if(veteranClassInfo.badgeVeteran) {
                    result.veraIndicator = 'O';
                }
                else if(veteranClassInfo.sdvetIndicator || veteranClassInfo.armedServiceMedalVetIndicator ||
                    this.isRecentlySeparated(veteranClassInfo.activeDutySeprDate)) {
                    result.veraIndicator = 'B';
                }

                result.sdvetIndicator = veteranClassInfo.sdvetIndicator ? 'Y' : null;
                result.armedServiceMedalVetIndicator = veteranClassInfo.armedServiceMedalVetIndicator;
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

        this.getVeteranClassificationError = function(veteranClassInfo) {
            var msg = 'personinfo.veteran.classification.error.notprotected';
            if(veteranClassInfo.choice !== this.vetChoiceConst.PROTECTED_VET && (veteranClassInfo.sdvetIndicator ||
                veteranClassInfo.armedServiceMedalVetIndicator || veteranClassInfo.badgeVeteran ||
                this.isRecentlySeparated(veteranClassInfo.activeDutySeprDate))) {
                messages.push({msg: msg, type: 'error'});

                return msg;
            }
            else {
                notificationCenterService.removeNotification(msg);
            }
        };

        this.getVeteranProtectedError = function(veteranClassInfo) {
            var msg = 'personinfo.veteran.classification.error.protected';
            if(veteranClassInfo.choice === this.vetChoiceConst.PROTECTED_VET && !(veteranClassInfo.sdvetIndicator ||
                veteranClassInfo.armedServiceMedalVetIndicator || veteranClassInfo.badgeVeteran ||
                this.isRecentlySeparated(veteranClassInfo.activeDutySeprDate))) {
                messages.push({msg: msg, type: 'error'});

                return msg;
            }
            else {
                notificationCenterService.removeNotification(msg);
            }
        };

        this.getSeprDateError = function(date) {
            var msg = 'personInfo.address.error.dateFormat';
            if(date && !personalInformationService.stringToDate(date)){
                messages.push({msg: msg, type: 'error'});

                return msg;
            }
            else {
                notificationCenterService.removeNotification(msg);
            }
        };

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
