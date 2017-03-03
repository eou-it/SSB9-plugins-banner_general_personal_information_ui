personalInformationApp.service('piSecurityQAService', ['$resource', 'notificationCenterService', '$filter',
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

        var removeMessage = function(message) {
            messages = _.filter(messages, function(elem){ return elem.msg !== message; });
        };

        var displayQuestionSelectErrMsg = function(question) {
            var msg = question.questionSelectErrMsg,
                questionTagText = $filter('i18n')('personInfo.securityQA.error.audibleQuestionTag', [question.tag]);
            messages.push({msg: msg, type: 'error'});
            notificationCenterService.addNotification(msg, 'error');

            if(!question.qstnSelErrDisplayed) {
                question.qstnSelErrDisplayed = true;
                $(secQAMessageCenter).append('<span id="qstnSel'+question.tag+'">' + questionTagText + msg + '</span>');
            }
        };

        var concealQuestionSelectErrMsg = function(question) {
            removeMessage(question.questionSelectErrMsg);
            notificationCenterService.removeNotification(question.questionSelectErrMsg);
            $('#qstnSel'+question.tag).remove();
            question.qstnSelErrDisplayed = false;
        };

        var displayDefQuestionErrMsg = function(question) {
            var msg = question.defQuestionErrMsg,
                questionTagText = $filter('i18n')('personInfo.securityQA.error.audibleQuestionTag', [question.tag]);
            messages.push({msg: msg, type: 'error'});
            notificationCenterService.addNotification(msg, 'error');

            if(!question.qstnDefErrDisplayed) {
                question.qstnDefErrDisplayed = true;
                $(secQAMessageCenter).append('<span id="qstnDef'+question.tag+'">' + questionTagText + msg + '</span>');
            }
        };

        var concealDefQuestionErrMsg = function(question) {
            removeMessage(question.defQuestionErrMsg);
            notificationCenterService.removeNotification(question.defQuestionErrMsg);
            $('#qstnDef'+question.tag).remove();
            question.qstnDefErrDisplayed = false;
        };

        var displayAnswerErrMsg = function(question) {
            var msg = question.answerErrMsg,
                questionTagText = $filter('i18n')('personInfo.securityQA.error.audibleQuestionTag', [question.tag]);
            messages.push({msg: msg, type: 'error'});
            notificationCenterService.addNotification(msg, 'error');

            if(!question.answerErrDisplayed) {
                question.answerErrDisplayed = true;
                $(secQAMessageCenter).append('<span id="qanswer' + question.tag + '">' + questionTagText + msg + '</span>');
            }
        };

        var concealAnswerErrMsg = function(question) {
            removeMessage(question.answerErrMsg);
            notificationCenterService.removeNotification(question.answerErrMsg);
            $('#qanswer'+question.tag).remove();
            question.answerErrDisplayed = false;
        };

        var getQuestionSelectErrMsg = function(question) {

            var msg = $filter('i18n')('personInfo.securityQA.error.questionRequired');

            if (question.questionNum !== 0 && !question.questionNum) {
                concealQuestionSelectErrMsg(question);
                question.questionSelectErrMsg = msg;
                displayQuestionSelectErrMsg(question);
            }
            else {
                concealQuestionSelectErrMsg(question);
                question.questionSelectErrMsg = '';
            }

            return question.questionSelectErrMsg;
        };

        var getDefQuestionErrMsg = function(question, constraints) {

            if(question.questionNum === 0) {
                if(!question.userDefinedQuestion) {
                    concealDefQuestionErrMsg(question);
                    question.defQuestionErrMsg = $filter('i18n')('personInfo.securityQA.error.defineQuesiton');
                    displayDefQuestionErrMsg(question);
                }
                else {
                    concealDefQuestionErrMsg(question);
                    question.defQuestionErrMsg = '';

                    if(question.userDefinedQuestion.length < constraints.questionMinLength) {
                        question.defQuestionErrMsg = $filter('i18n')('personInfo.securityQA.error.questionLength', [constraints.questionMinLength]);
                        displayDefQuestionErrMsg(question);
                    }
                }
            }

            return question.defQuestionErrMsg;
        };

        var getAnswerErrMsg = function(question, constraints) {

            if(!question.answer) {
                concealAnswerErrMsg(question);
                question.answerErrMsg = $filter('i18n')('personInfo.securityQA.error.answerRequired');
                displayAnswerErrMsg(question);
            }
            else {
                concealAnswerErrMsg(question);
                question.answerErrMsg = '';

                if(question.answer.length < constraints.answerMinLength) {
                    question.answerErrMsg = $filter('i18n')('personInfo.securityQA.error.answerLength', [constraints.answerMinLength]);
                    displayAnswerErrMsg(question);
                }
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
                questions[i].tag = i + 1;
                errorOccurred = getQuestionSelectErrMsg(questions[i]) || errorOccurred;
                errorOccurred = getDefQuestionErrMsg(questions[i], constraints) || errorOccurred;
                errorOccurred = getAnswerErrMsg(questions[i], constraints) || errorOccurred;
            }

            return errorOccurred;
        };

        this.getPinErrMsg = function(pin) {
            var msg = $filter('i18n')('personInfo.securityQA.error.pin');
            if(!pin){
                messages.push({msg: msg, type: 'error'});

                return msg;
            }
            else {
                notificationCenterService.removeNotification(msg);
            }
        };

        this.displayMessages = function() {
            notificationCenterService.localMessageCenter = secQAMessageCenter;

            _.each(messages, function(message) {
                notificationCenterService.addNotification(message.msg, message.type);
            });

            messages = [];

            notificationCenterService.localMessageCenter = null;
        };

        this.displayErrorMessage = function(message) {
            notificationCenterService.setLocalMessageCenter(secQAMessageCenter);
            notificationCenterService.displayNotification(message, "error");
            notificationCenterService.setLocalMessageCenter(null);
        };
    }
]);
