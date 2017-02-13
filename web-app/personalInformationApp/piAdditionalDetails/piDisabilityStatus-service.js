personalInformationApp.service('piDisabilityStatusService', ['$resource', 'notificationCenterService', '$filter',
    function ($resource, notificationCenterService, $filter) {

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

        var getQuestionSelectErrMsg = function(question) {
            question.questionSelectErrMsg = '';

            if (question.questionNum !== 0 && !question.questionNum) {
                question.questionSelectErrMsg = $filter('i18n')('personInfo.securityQA.error.questionRequired');
            }

            return question.questionSelectErrMsg;
        };

        var getDefQuestionErrMsg = function(question, constraints) {
            question.defQuestionErrMsg = '';

            if(question.questionNum === 0) {
                if(!question.userDefinedQuestion) {
                    question.defQuestionErrMsg = $filter('i18n')('personInfo.securityQA.error.defineQuesiton');
                }
                else if(question.userDefinedQuestion.length < constraints.questionMinLength) {
                    question.defQuestionErrMsg = $filter('i18n')('personInfo.securityQA.error.questionLength', [constraints.questionMinLength]);
                }
            }

            return question.defQuestionErrMsg;
        };

        var getAnswerErrMsg = function(question, constraints) {
            question.answerErrMsg = '';

            if(!question.answer) {
                question.answerErrMsg = $filter('i18n')('personInfo.securityQA.error.answerRequired');
            }
            else if(question.answer.length < constraints.answerMinLength) {
                question.answerErrMsg = $filter('i18n')('personInfo.securityQA.error.answerLength', [constraints.answerMinLength]);
            }

            return question.answerErrMsg;
        };

        this.removeQuestionFieldErrors = function(question, constraints) {
            if(question.questionSelectErrMsg){
                getQuestionSelectErrMsg(question);
            }

            if(question.defQuestionErrMsg){
                getDefQuestionErrMsg(question, constraints);
            }

            if(question.answerErrMsg){
                getAnswerErrMsg(question, constraints);
            }
        };

        this.getAllQuestionErrors = function(questions, constraints) {
            var errorOccurred = false;
            var i;
            for(i = 0; i < questions.length; i++){
                errorOccurred = getQuestionSelectErrMsg(questions[i]) || errorOccurred;
                errorOccurred = getDefQuestionErrMsg(questions[i], constraints) || errorOccurred;
                errorOccurred = getAnswerErrMsg(questions[i], constraints) || errorOccurred;
            }

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
