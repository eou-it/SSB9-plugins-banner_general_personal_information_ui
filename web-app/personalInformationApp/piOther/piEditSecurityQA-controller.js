personalInformationAppControllers.controller('piEditSecurityQAController',['$scope', '$modalInstance', '$rootScope', '$state',
    '$filter', 'notificationCenterService', 'piSecurityQAService', 'personalInformationService',
    function ($scope, $modalInstance, $rootScope, $state, $filter, notificationCenterService, piSecurityQAService,
              personalInformationService){

        // CONTROLLER VARIABLES
        // --------------------
        $scope.pin = null;
        $scope.pinQuestions = ["Not Selected"];
        $scope.questions = [];
        $scope.questionAnswerErrMsg = '';


        // CONTROLLER FUNCTIONS
        // --------------------
        $scope.cancelModal = function () {
            $modalInstance.dismiss('cancel');
            notificationCenterService.clearNotifications();
        };

        $scope.setPinQuestion = function(question, questionNum) {
            question.questionNum = questionNum;
        };

        var processQuestions = function(qstnArray) {
            var result = [];
            qstnArray.forEach(function(q) {
                result.push({
                    id: q.id,
                    version: q.version,
                    question: 'question'+ (q.questionNum > 0 ? q.questionNum : 0),
                    userDefinedQuestion: q.userDefinedQuestion,
                    answer: q.answer
                });
            });
            return result;
        };

        $scope.saveQuestions = function() {
            if (true) {
                var payload = {
                    pin: $scope.pin,
                    questions: processQuestions($scope.questions)
                },
                    handleResponse = function (response) {
                        if (response.failure) {
                            $scope.questionAnswerErrMsg = response.message;
                            piSecurityQAService.displayErrorMessage(response.message);
                        }
                        else {
                            notificationCenterService.clearNotifications();

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
                    $scope.pinQuestions = $scope.pinQuestions.concat(response.questions);
                    $scope.numQuestions = response.noOfquestions;
                    var i;
                    for(i = 0; i < $scope.numQuestions; i++) {
                        if(response.userQuestions[i]) {
                            $scope.questions.push(response.userQuestions[i]);
                        }
                        else {
                            $scope.questions.push({
                                questionNum:null,
                                userDefinedQuestion:'',
                                answer:''
                            });
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

