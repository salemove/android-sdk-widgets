package com.glia.widgets.survey

import androidx.annotation.VisibleForTesting
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.internal.survey.domain.GliaSurveyAnswerUseCase

/**
 * @hide
 */
internal class SurveyController(
    private val gliaSurveyAnswerUseCase: GliaSurveyAnswerUseCase
) : SurveyContract.Controller {

    /**
     * @hide
     */
    interface AnswerCallback {
        fun answerCallback(showError: Boolean)
    }

    private var view: SurveyContract.View? = null

    @VisibleForTesting
    @JvmField
    var survey: Survey? = null

    @VisibleForTesting
    @JvmField
    var state: SurveyState = SurveyState.Builder().createSurveyState()

    override fun init(survey: Survey?) {
        if (isAlreadyInit(survey)) {
            setState(state)
            return
        }

        this.survey = survey
        if (survey == null) {
            view?.let {
                it.finish()
                resetController()
            }
            return
        }
        setTitle(survey.title)
        setQuestions(survey)
    }

    @VisibleForTesting
    @JvmName("isAlreadyInit")
    fun isAlreadyInit(survey: Survey?): Boolean {
        val currentSurvey = this.survey
        if (currentSurvey == null || survey == null) {
            return false
        }
        return isEqualsSurveys(currentSurvey, survey) && isStateContainQuestions(state)
    }

    private fun isEqualsSurveys(survey: Survey, otherSurvey: Survey): Boolean {
        return survey.id == otherSurvey.id && survey.engagementId == otherSurvey.engagementId
    }

    private fun isStateContainQuestions(state: SurveyState): Boolean {
        return !state.questions.isNullOrEmpty()
    }

    private fun setTitle(title: String?) {
        setState(
            SurveyState.Builder()
                .copyFrom(state)
                .setTitle(title)
                .createSurveyState()
        )
    }

    private fun setQuestions(survey: Survey) {
        val questionItems = survey.questions.map(::makeQuestionItem)
        setState(
            SurveyState.Builder()
                .copyFrom(state)
                .setQuestions(questionItems)
                .createSurveyState()
        )
    }

    private fun makeQuestionItem(question: Survey.Question): QuestionItem {
        var answer: Survey.Answer? = null
        val questionId = question.id
        if (question.type == Survey.Question.QuestionType.SINGLE_CHOICE) {
            val options = question.options
            val option = options?.firstOrNull { it.isDefault }
            if (option != null) {
                answer = Survey.Answer.makeAnswer(questionId, option.id)
            }
        }
        return QuestionItem(question, answer)
    }

    override fun setView(view: SurveyContract.View) {
        this.view = view
    }

    override fun onAnswer(answer: Survey.Answer) {
        val questions = state.questions ?: return
        questions.firstOrNull { it.question.id == answer.questionId }
            ?.let { item ->
                setAnswer(item, answer)
                hideSoftKeyboardIfNeeds(item)
            }
    }

    private fun hideSoftKeyboardIfNeeds(item: QuestionItem) {
        if (item.question.type != Survey.Question.QuestionType.TEXT) {
            view?.hideSoftKeyboard()
        }
    }

    private fun setAnswer(item: QuestionItem, answer: Survey.Answer) {
        item.answer = answer
        if (item.isShowError) {
            validate(item)
        }
    }

    private fun validate(item: QuestionItem) {
        val showError = try {
            gliaSurveyAnswerUseCase.validate(item)
            false
        } catch (ignore: SurveyValidationException) {
            true
        }
        item.isShowError = showError
        item.answerCallback?.answerCallback(showError)
    }

    override fun onCancelClicked() {
        view?.let {
            it.finish()
            resetController()
        }
    }

    override fun onSubmitClicked() {
        val questionItems = state.questions ?: return
        val currentSurvey = survey ?: return
        gliaSurveyAnswerUseCase.submit(questionItems, currentSurvey) { exception ->
            if (exception == null) {
                view?.let {
                    it.finish()
                    resetController()
                }
                return@submit
            } else if (exception is GliaException) {
                if (exception.cause == GliaException.Cause.NETWORK_TIMEOUT) {
                    view?.onNetworkTimeout()
                }
                // Ignore other Glia exceptions
            }
            questionItems.forEach { item ->
                item.answerCallback?.answerCallback(item.isShowError)
            }
            questionItems.firstOrNull { it.isShowError }
                ?.let { view?.scrollTo(questionItems.indexOf(it)) }
        }
    }

    @Synchronized
    private fun setState(state: SurveyState) {
        this.state = state
        view?.onStateUpdated(state)
    }

    override fun onDestroy() {
        this.view = null
        resetController()
    }

    private fun resetController() {
        state = SurveyState.Builder().createSurveyState()
        survey = null
    }
}
