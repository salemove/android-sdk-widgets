package com.glia.widgets.internal.survey.domain

import android.text.TextUtils
import androidx.annotation.VisibleForTesting
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.internal.survey.GliaSurveyRepository
import com.glia.widgets.survey.QuestionItem
import com.glia.widgets.survey.SurveyValidationException
import java.util.function.Consumer

internal class GliaSurveyAnswerUseCase(private val repository: GliaSurveyRepository) {
    fun submit(
        questions: List<QuestionItem>?,
        survey: Survey,
        callback: Consumer<RuntimeException?>
    ) {
        trim(questions)
        try {
            validate(questions)
        } catch (exception: SurveyValidationException) {
            callback.accept(exception)
            return
        }
        val answers: List<Survey.Answer> = questions?.mapNotNull { it.answer } ?: emptyList()
        val surveyId = survey.id
        val engagementId = survey.engagementId
        repository.submitSurveyAnswers(
            answers, surveyId, engagementId,
            Consumer { t: GliaException? -> callback.accept(t) })
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
