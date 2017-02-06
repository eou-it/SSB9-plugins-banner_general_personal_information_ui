personalInformationApp.service('piSecurityQAService', ['$resource', 'notificationCenterService',
    function ($resource, notificationCenterService) {

        this.getQuestions = function () {
            return $resource('../ssb/:controller/:action',
                {controller: 'PersonalInformationQA', action: 'getSecurityQA'}).get();
        };

        this.saveQuestions = function (entity) {
            return $resource('../ssb/:controller/:action',
                {controller: 'PersonalInformationQA', action: 'save'}, {save: {method: 'POST'}}).save(entity);
        };

        var messages = [],
            secQAMessageCenter = '#securityQAErrorMsgCenter';

        this.getQuestionErrors = function(questions, constraints) {
            var errorOccurred = false;
            questions.forEach(function(qstn) {
                qstn.questionSelectErrMsg = '';
                qstn.defQuestionErrMsg = '';
                qstn.answerErrMsg = '';

                if(qstn.questionNum === 0) {
                    if(!qstn.userDefinedQuestion) {
                        qstn.defQuestionErrMsg = 'personInfo.securityQA.error.defineQuesiton';
                    }
                    else if(qstn.userDefinedQuestion.length < constraints.questionMinLength) {
                        qstn.defQuestionErrMsg = 'personInfo.securityQA.error.questionLength';
                    }
                }
                else if (!(qstn.questionNum > 0)) {
                    qstn.questionSelectErrMsg = 'personInfo.securityQA.error.questionRequired';
                }

                if(!qstn.answer) {
                    qstn.answerErrMsg = 'personInfo.securityQA.error.answerRequired';
                }
                else if(qstn.answer.length < constraints.answerMinLength) {
                    qstn.answerErrMsg = 'personInfo.securityQA.error.answerLength';
                }

                errorOccurred = !!qstn.errMsg || errorOccurred;
            });
            return errorOccurred;
        };

        this.getPinErrMsg = function(pin) {
            var msg = 'personInfo.securityQA.error.pin';
            if(!pin){
                messages.push({msg: msg, type: 'error'});

                return msg;
            }
            else {
                notificationCenterService.removeNotification(msg);
            }
        };

        this.displayMessages = function() {
            notificationCenterService.setLocalMessageCenter(secQAMessageCenter);

            _.each(messages, function(message) {
                notificationCenterService.addNotification(message.msg, message.type);
            });

            messages = [];

            notificationCenterService.setLocalMessageCenter(null);
        };

        this.displayErrorMessage = function(message) {
            notificationCenterService.setLocalMessageCenter(secQAMessageCenter);
            notificationCenterService.displayNotification(message, "error");
            notificationCenterService.setLocalMessageCenter(null);
        };
    }
]);
