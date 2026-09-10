package com.glia.widgets.survey

import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.base.BaseController
import com.glia.widgets.base.BaseView

/**
 * @hide
 */
internal interface SurveyContract {

    /**
     * @hide
     */
    interface Controller : BaseController {
        fun init(survey: Survey?)

        fun setView(view: View)

        fun onAnswer(answer: Survey.Answer)

        fun onCancelClicked()

        fun onSubmitClicked()
    }

    /**
     * @hide
     */
    interface View : BaseView<Controller> {
        fun onStateUpdated(state: SurveyState)

        fun scrollTo(index: Int)

        fun hideSoftKeyboard()

        fun onNetworkTimeout()

        fun finish()
    }
}
