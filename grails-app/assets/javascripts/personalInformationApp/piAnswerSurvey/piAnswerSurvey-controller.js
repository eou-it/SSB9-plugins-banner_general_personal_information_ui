/*********************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/

personalInformationAppControllers.controller('piAnswerSurveyController', ['$scope', '$rootScope', 'answerSurveyService', 'answer_survey_constants', '$state', '$timeout',
    function ($scope, $rootScope, answerSurveyService, answer_survey_constants, $state, $timeout) {
        var formDirty = false;
        var beforeUpdate;
        var previousSelectedSurvey;
        var KEY_ENTER = 13;
        var CLICK = 1;
        var SPACEBAR = 32;

        $scope.questionDetails;
        $scope.selectedSurvey = {};
        $scope.infoTexts = [
            $.i18n.prop('personInfo.answerSurvey.survey.surveyTitle.infoText'),
            $.i18n.prop('personInfo.answerSurvey.survey.questions.nextPrevious.infoText'),
            $.i18n.prop('personInfo.answerSurvey.survey.questions.finishLater.infoText'),
            $.i18n.prop('personInfo.answerSurvey.survey.questions.surveyComplete.infoText'),
            $.i18n.prop('personInfo.answerSurvey.survey.questions.removeSurvey.infoText'),
            $.i18n.prop('personInfo.answerSurvey.survey.questions.back.infoText')
        ];
        $scope.questionNumber = 1;
        $scope.totalQuestions = 5;

        var isOnIOS = navigator.userAgent.match(/iPad|iPhone|iPod/i);
        var eventName = isOnIOS ? "pagehide" : "beforeunload";
        $(window).on(eventName, function() {
            if (formDirty) {
                return $.i18n.prop('personInfo.answerSurvey.dirtyCheck.message');
            }
        });

        var $stateChangeStartUnbind = $scope.$on('$stateChangeStart', function (event, toState) {
            if (formDirty) {
                event.preventDefault();
                dirtyCheckNotification(proceedToToState, toState);
            }
        });

        var proceedToToState = function (toState) {
            $state.go(toState);
        };

        $scope.fetchQuesAnswers = function () {
            if (formDirty) {
                dirtyCheckNotification(fetchQuestionsCall);
            } else {
                fetchQuestionsCall();
            }
        };

        var fetchQuestionsCall = function () {
            previousSelectedSurvey = $scope.selectedSurvey.selected;
            answerSurveyService.fetchQuestionAnswers($scope.selectedSurvey.selected).$promise.then(function (response) {
                if (response.questionDetails.length > 0) {
                    setQuestionDetailsToScope(response);
                }
            });
        };

        var dirtyCheckNotification = function (yesCallback, data) {
            var n = new Notification({
                message: $.i18n.prop("personInfo.answerSurvey.dirtyCheck.message"),
                type: "warning"
            });
            n.addPromptAction($.i18n.prop("personInfo.answerSurvey.prompt.no"), function () {
                notifications.remove(n);
                $scope.selectedSurvey.selected = previousSelectedSurvey;
                $scope.$apply();
                $('#bannerMenuDiv').focus();
            });
            n.addPromptAction($.i18n.prop("personInfo.answerSurvey.prompt.yes"), function () {
                formDirty = false;
                notifications.remove(n);
                yesCallback(data);
            });
            notifications.addNotification(n);
            $timeout(function() {
                angular.element('#notification-center').find('.notification-flyout-item.primary').focus();
            }, 100);
        };

        var removeSurveyConfirmation = function () {
            let nextQuestion = parseInt($scope.questionNumber) + 1;
            let data = {};
            var n = new Notification({
                message: $.i18n.prop("personInfo.answerSurvey.removeSurvey.confirm.message"),
                type: "warning"
            });
            n.addPromptAction($.i18n.prop("personInfo.answerSurvey.prompt.no"), function () {
                $('.remove-survey a').focus();
                notifications.remove(n);
            });
            n.addPromptAction($.i18n.prop("personInfo.answerSurvey.prompt.yes"), function () {
                formDirty = false;
                notifications.remove(n);
                data = getSubmitData(nextQuestion, answer_survey_constants.REMOVE_SURVEY);
                saveResponsePostCall(data);
            });
            notifications.addNotification(n);
            $timeout(function() {
                angular.element('#notification-center').find('.notification-flyout-item.primary').focus();
            }, 100);
        };

        $scope.saveSurveyResponse = function (event, action) {
            if (event.which === KEY_ENTER || event.which === CLICK || event.which === SPACEBAR) {
                let data = {};
                let nextDisp = parseInt($scope.questionNumber) + 1;
                switch (action) {
                    case 1 :
                        nextDisp = parseInt($scope.questionNumber) - 1;
                        data = getSubmitData(nextDisp, answer_survey_constants.PREVIOUS_QUESTION);
                        break;
                    case 2 :
                        data = getSubmitData(nextDisp, answer_survey_constants.NEXT_QUESTION);
                        break;
                    case 3 :
                        data = getSubmitData(nextDisp, answer_survey_constants.FINISH_LATER);
                        break;
                    case 4 :
                        data = getSubmitData(nextDisp, answer_survey_constants.SURVEY_COMPLETE);
                        break;
                    case 5 :
                        removeSurveyConfirmation();
                        return;
                    case 6 :
                        nextDisp = 0;
                        data = getSubmitData(nextDisp, answer_survey_constants.RETURN_TO_BEGINNING);
                        break;
                }
                saveResponsePostCall(data);
            }
        };

        var getSubmitData = function (nextDisp, submitAction) {
            let selectedData = $scope.selectedSurvey.selected;
            let data = {
                'surveyName': selectedData.surveyName,
                'questionNo': $scope.questionNumber,
                'nextDisp': nextDisp,
                'responses': $scope.questionDetails.responseList,
                'radioValue': $scope.questionDetails.radioValue,
                'comment': $scope.questionDetails.comment,
                'submitAction': submitAction
            };
            return data;
        };

        $scope.$watch('questionDetails', function (newVal) {
            if (!angular.equals(newVal, beforeUpdate)) {
                formDirty = true;
            } else {
                formDirty = false;
            }
        }, true);

        var saveResponsePostCall = function (data) {
            answerSurveyService.saveResponse(data).$promise.then(function (response) {
                formDirty = false;
                if (response.questionDetails && response.questionDetails.length > 0) {
                    setQuestionDetailsToScope(response);
                    $timeout(function() {
                        $('.return-to-beginning').focus();
                    }, 100);
                } else {
                    sessionStorage.setItem('answerSurveyMessage', response.message);
                    window.location = $rootScope.applicationContextRoot + '/ssb/personalInformation';
                }
            });
        };

        var setQuestionDetailsToScope = function (response) {
            $scope.questionDetails = response.questionDetails[0];
            beforeUpdate = angular.copy($scope.questionDetails);
            $scope.questionNumber = $scope.questionDetails.questionNo;
            $scope.totalQuestions = response.questionsCount;
            $scope.questionIndex = $.i18n.prop('personInfo.answerSurvey.survey.questions.numberIndex', [$scope.questionNumber, $scope.totalQuestions]);
            $scope.questionText = $scope.questionDetails.questionText;
            $scope.commentLabel = $scope.questionDetails.commentsLabel || $.i18n.prop('personInfo.answerSurvey.survey.comment.label');
            $scope.hidePrevious = parseInt($scope.questionNumber) === 1;
            $scope.hideNext = parseInt($scope.questionNumber) === $scope.totalQuestions;
        };

        this.init = function () {
            formDirty = false;
            answerSurveyService.fetchSurveys().$promise.then(function (response) {
                if (response.success) {
                    $scope.surveys = response.surveys;
                } else {
                    $scope.error = response.error
                }
            });
            $('#bannerMenuDiv').focus();
        };
        this.init();

        $scope.$on('$destroy', function() {
            $(window).off(eventName);
            $stateChangeStartUnbind();
        });
    }
]);