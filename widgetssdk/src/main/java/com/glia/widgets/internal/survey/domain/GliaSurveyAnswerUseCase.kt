package com.glia.widgets.internal.survey.domain

import android.text.TextUtils
import androidx.annotation.VisibleForTesting
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.internal.survey.GliaSurveyRepository
import com.glia.widgets.survey.QuestionItem
import com.glia.widgets.survey.SurveyValidationException

internal class GliaSurveyAnswerUseCase(private val repository: GliaSurveyRepository) {
    /**
     * Sends the answers when they pass validation, without waiting for the result: the survey is best effort.
     *
     * @return false when a required question has no answer, in which case nothing is sent.
     */
    fun submit(questions: List<QuestionItem>?, survey: Survey): Boolean {
        trim(questions)
        try {
            validate(questions)
        } catch (exception: SurveyValidationException) {
            return false
        }
        val answers: List<Survey.Answer> = questions?.mapNotNull { it.answer } ?: emptyList()
        repository.submitSurveyAnswers(answers, survey.id, survey.engagementId)
        return true
    }

    @VisibleForTesting
    @Throws(SurveyValidationException::class)
    fun validate(questions: List<QuestionItem>?) {
        if (questions == null) {
            return
        }
        var isError = false
        for (i in questions.indices) {
            val item = questions[i]
            try {
                validate(item)
                item.isShowError = false
            } catch (ignore: SurveyValidationException) {
                item.isShowError = true
                isError = true
            }
        }
        if (isError) {
            throw SurveyValidationException()
        }
    }

    @Throws(SurveyValidationException::class)
    fun validate(item: QuestionItem) {
        if (item.question.isRequired) {
            if (item.question.type == Survey.Question.QuestionType.TEXT) {
                if (item.answer == null || TextUtils.isEmpty(item.answer!!.getResponse())) {
                    throw SurveyValidationException()
                }
            } else {
                if (item.answer == null) {
                    throw SurveyValidationException()
                }
            }
        }
    }

    @VisibleForTesting
    fun trim(questions: List<QuestionItem>?) {
        if (questions == null) {
            return
        }
        for (item in questions) {
            if (item.question.type == Survey.Question.QuestionType.TEXT) {
                if (item.answer == null) {
                    continue
                }
                val response = (item.answer!!.getResponse<Any>() as String).trim { it <= ' ' }
                var trimmedAnswer: Survey.Answer? = null
                if (!TextUtils.isEmpty(response)) {
                    trimmedAnswer = Survey.Answer.makeAnswer(item.question.id, response)
                }
                item.answer = trimmedAnswer
            }
        }
    }
}
