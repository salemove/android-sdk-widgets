package com.glia.widgets.survey

import com.glia.androidsdk.engagement.Survey

/**
 * @hide
 */
internal data class QuestionItem @JvmOverloads constructor(
    val question: Survey.Question,
    var answer: Survey.Answer? = null,
    var isShowError: Boolean = false
) {
    var answerCallback: SurveyController.AnswerCallback? = null
}
