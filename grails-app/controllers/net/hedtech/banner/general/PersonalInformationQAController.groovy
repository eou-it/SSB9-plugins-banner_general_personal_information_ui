/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general

import grails.converters.JSON
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.overall.PinQuestion
import net.hedtech.banner.security.BannerGrantedAuthorityService

class PersonalInformationQAController {

    def securityQAService
    def generalForStoringResponsesAndPinQuestionService

    static scope = "session"

    int noOfQuestions
    Map questions = [:]
    int questionMinimumLength
    int answerMinimumLength
    String userDefinedQuesFlag
    List questionList = []
    private boolean globalVarsSet = false
    private static final QUESTION_LABEL = "question"
    private static final USER_DEFINED_QUESTION_DISABLED = "N"


    def getSecurityQA() {
        setGlobalVariables()
        def model = [
                questions: questionList,
                userDefinedQuesFlag: userDefinedQuesFlag,
                noOfquestions: noOfQuestions,
                questionMinimumLength: questionMinimumLength,
                answerMinimumLength: answerMinimumLength,
                userQuestions: getUserQuestions()
        ]

        render model as JSON
    }

    private void setGlobalVariables() {
        if(!globalVarsSet) {
            questionList = loadQuestionList()
            Map result = securityQAService.getUserDefinedPreference()
            if (result != null) {
                noOfQuestions = result.GUBPPRF_NO_OF_QSTNS?.intValue()
                questionMinimumLength = result.GUBPPRF_QSTN_MIN_LENGTH?.intValue()
                answerMinimumLength = result.GUBPPRF_ANSR_MIN_LENGTH?.intValue()
                userDefinedQuesFlag = result.GUBPPRF_EDITQSTN_IND
            }
            globalVarsSet = true
        }
    }

    private List loadQuestionList() {
        List ques = PinQuestion.fetchQuestions()
        ques.each {
            questions.put(it.description, it.pinQuestionId)
        }
        questions.keySet().collect()
    }

    private List getUserQuestions() {
        def pidm = PersonalInformationControllerUtility.getPrincipalPidm()
        def userQuestions = generalForStoringResponsesAndPinQuestionService.fetchQuestionForPidm(pidm)
        List resultList = []
        userQuestions.each {
            def qstnNum
            def userDefQuestion
            if(it.pinQuestion){
                qstnNum = questionList.indexOf(it.pinQuestion.description)+1
                userDefQuestion = ''
            }
            else {
                qstnNum = 0;
                userDefQuestion = it.questionDescription
            }
            def question = [
                    id: it.id,
                    version: it.version,
                    questionNum: qstnNum,
                    userDefinedQuestion: userDefQuestion,
                    answer: ''
            ]

            resultList << question
        }

        return resultList
    }

    def save() {

        log.info("save")

        def userQuestionsAnswers = request?.JSON ?: params

        setGlobalVariables()

        String pidm = BannerGrantedAuthorityService.getPidm()
        List selectedQA = loadSelectedQuestionAnswerFromParams(userQuestionsAnswers)

        try {
            securityQAService.saveOrUpdateSecurityQAResponse(pidm, selectedQA, userQuestionsAnswers.pin)
            render([failure: false] as JSON)
        }
        catch (ApplicationException ae) {

            render PersonalInformationControllerUtility.returnFailureMessage(ae) as JSON
        }
    }

    private List loadSelectedQuestionAnswerFromParams(userQuestionsAnswers) {
        List selectedQA = []
        def question = userQuestionsAnswers.questions

        for (int index = 0; index < noOfQuestions; index++) {

            def questionAnswered
            def userDefQsn
            if (USER_DEFINED_QUESTION_DISABLED.equals(userDefinedQuesFlag)) {
                userDefQsn = null
            } else {
                userDefQsn = question[index].userDefinedQuestion
            }
            questionAnswered = getAnsweredQuestion(question[index], userDefQsn)

            selectedQA.add(questionAnswered)
        }
        return selectedQA
    }

    private Map getAnsweredQuestion(questionFromParam, userDefinedQstn) {
        def question = null
        def questionNo = null
        int questionId = questionFromParam.question.split(QUESTION_LABEL)[1].toInteger()

        if (questionId != 0) {
            question = questionList.get(questionId - 1)
            questionNo = questions.find { it.key == question }?.value
        }

        def questionAnswered = [
                question: question,
                questionNo: questionNo,
                userDefinedQuestion: userDefinedQstn,
                answer: questionFromParam.answer,
                id: questionFromParam.id,
                version: questionFromParam.version
        ]

        return questionAnswered
    }

}
