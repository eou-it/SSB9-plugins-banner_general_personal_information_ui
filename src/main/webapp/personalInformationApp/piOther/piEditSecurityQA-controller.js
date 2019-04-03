personalInformationAppControllers.controller('piEditSecurityQAController',['$scope', '$modalInstance', '$rootScope', '$state',
    '$filter', 'notificationCenterService', 'piSecurityQAService', 'personalInformationService',
    function ($scope, $modalInstance, $rootScope, $state, $filter, notificationCenterService, piSecurityQAService,
              personalInformationService){

        // CONTROLLER VARIABLES
        // --------------------
        $scope.pin = null;
        $scope.pinQuestions = [$filter('i18n')('personInfo.selection.defineQuestion')];
        $scope.constraints = {};
        $scope.questions = [];
        $scope.questionAnswerErrMsg = '';
        $scope.pinErrMsg = null;


        // CONTROLLER FUNCTIONS
        // --------------------
        $scope.cancelModal = function () {
            $modalInstance.dismiss('cancel');
            notificationCenterService.clearNotifications();
        };

        $scope.setPinQuestion = function(question, questionNum) {
            question.questionNum = questionNum;
            question.removeErrors();
        };

        $scope.removePinFieldError = function() {
            if($scope.pinErrMsg) {
                $scope.pinErrMsg = piSecurityQAService.getPinErrMsg($scope.pin);
            }
        };

        var createQuestionsForSave = function(qstnArray) {
            var result = [];
            qstnArray.forEach(function(q) {
                if(!$scope.constraints.userDefinedQuesFlag) {
                    q.questionNum++;
                }

                result.push({
                    id: q.id,
                    version: q.version,
                    question: 'question' + (q.questionNum > 0 ? q.questionNum : 0),
                    userDefinedQuestion: (q.questionNum > 0 ? '' : q.userDefinedQuestion),
                    answer: q.answer
                });
            });
            return result;
        };

        var isValidSecurityQuestionAnswers = function() {
            var questionsInvalid = piSecurityQAService.getAllQuestionErrors($scope.questions, $scope.constraints);
            $scope.pinErrMsg = piSecurityQAService.getPinErrMsg($scope.pin);

            return !($scope.pinErrMsg || questionsInvalid);
        };

        $scope.saveQuestions = function() {
            if (isValidSecurityQuestionAnswers()) {
                var payload = {
                    pin: $scope.pin,
                    questions: createQuestionsForSave($scope.questions)
                },
                    handleResponse = function (response) {
                        if (response.failure) {
                            var i18nedMessage = $filter('i18n')(response.message);
                            $scope.questionAnswerErrMsg = i18nedMessage ? i18nedMessage : response.message;
                            piSecurityQAService.displayErrorMessage($scope.questionAnswerErrMsg);
                        }
                        else {
                            notificationCenterService.clearNotifications();
                            $scope.cancelModal();

                            var notifications = [];
                            notifications.push({
                                    message: 'personInfo.save.success.message',
                                    messageType: $scope.notificationSuccessType,
                                    flashType: $scope.flashNotification
                                }
                            );

                            $state.go(personalInformationService.getFullProfileState(),
                                {onLoadNotifications: notifications, startingTab: 'other'},
                                {reload: true, inherit: false, notify: true}
                            );
                        }
                };

                piSecurityQAService.saveQuestions(payload).$promise.then(handleResponse);
            }
            else {
                piSecurityQAService.displayMessages();
            }
        };

        this.init = function() {

            piSecurityQAService.getQuestions().$promise.then(function(response) {
                if(response.failure) {
                    notificationCenterService.displayNotification(response.message, $scope.notificationErrorType);
                } else {
                    $scope.constraints.numQuestions = response.noOfquestions;
                    $scope.constraints.questionMinLength = response.questionMinimumLength;
                    $scope.constraints.answerMinLength = response.answerMinimumLength;
                    $scope.constraints.userDefinedQuesFlag = response.userDefinedQuesFlag === 'Y';
                    $scope.pinQuestions = $scope.constraints.userDefinedQuesFlag ? $scope.pinQuestions.concat(response.questions) : response.questions;

                    var removeErrorFn = function() {
                        piSecurityQAService.removeQuestionFieldErrors(this, $scope.constraints);
                    };
                    var i;
                    for(i = 0; i < $scope.constraints.numQuestions; i++) {
                        if(!response.userQuestions[i]) {
                            $scope.questions.push({
                                questionNum:null,
                                userDefinedQuestion:'',
                                answer:'',
                                removeErrors: removeErrorFn
                            });
                        }
                        else if(response.userQuestions[i].questionNum === 0 && !$scope.constraints.userDefinedQuesFlag) {
                            response.userQuestions[i].removeErrors = removeErrorFn;
                            response.userQuestions[i].questionNum = null;
                            response.userQuestions[i].userDefinedQuestion = '';
                            $scope.questions.push(response.userQuestions[i]);
                        }
                        else {
                            if(!$scope.constraints.userDefinedQuesFlag) {
                                response.userQuestions[i].questionNum--;
                            }
                            response.userQuestions[i].removeErrors = removeErrorFn;
                            $scope.questions.push(response.userQuestions[i]);
                        }
                    }
                }
            });
        };

        // INITIALIZE
        // ----------
        this.init();

    }
]);

