/*******************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

personalInformationApp.service('answerSurveyService', ['$rootScope', '$filter', '$resource',
    function ($rootScope, $filter, $resource) {
        this.fetchSurveys = function () {
            return $resource('../ssb/:controller/:action',
                {controller: 'AnswerSurvey', action: 'fetchSurveys'}).get();
        };

        this.fetchQuestionAnswers = function (params) {
            return $resource('../ssb/:controller/:action',
                {controller: 'AnswerSurvey', action: 'fetchQuestionAnswers'}).get(params);
        };

        var saveResponseResource = $resource(':controller/:action', {
            controller: 'AnswerSurvey',
            action: 'saveResponse'
        }, {
            save: {
                method: 'POST'
            }
        });

        this.saveResponse = function (data) {
            return saveResponseResource.save(data);
        };
    }
]);