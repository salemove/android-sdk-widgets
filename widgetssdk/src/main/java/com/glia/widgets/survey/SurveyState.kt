package com.glia.widgets.survey

/**
 * @hide
 */
internal class SurveyState private constructor(
    val title: String?,
    val questions: List<QuestionItem>?
) {
    internal class Builder {
        private var title: String? = null
        private var questions: List<QuestionItem>? = null

        fun setTitle(title: String?): Builder {
            this.title = title
            return this
        }

        fun setQuestions(questions: List<QuestionItem>?): Builder {
            this.questions = questions
            return this
        }

        fun copyFrom(state: SurveyState?): Builder {
            if (state == null) {
                return this
            }
            this.title = state.title
            this.questions = state.questions
            return this
        }

        fun createSurveyState(): SurveyState {
            return SurveyState(title, questions)
        }
    }
}
