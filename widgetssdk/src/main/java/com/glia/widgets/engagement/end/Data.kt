package com.glia.widgets.engagement.end

import android.app.Activity
import android.content.Context
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.UiTheme

internal object EndEngagement {
    enum class Reason {
        VISITOR,
        VISITOR_SILENTLY,
        OPERATOR;

        val isOperator: Boolean get() = this == OPERATOR
        val isVisitor: Boolean get() = !isOperator

        val shouldRequestSurvey: Boolean get() = this != VISITOR_SILENTLY

    }

    sealed interface Result {
        data class Survey(val survey: com.glia.androidsdk.engagement.Survey) : Result
        object Operator : Result
        object Visitor : Result
    }

    sealed interface State {
        data class ShowSurvey(val activity: Activity, val survey: Survey, val theme: UiTheme) : State
        data class ShowDialog(val themedContext: Context, val theme: UiTheme) : State
        data class LaunchDialogHolderActivity(val activity: Activity) : State
        object FinishSilently : State
        object Skip : State
    }
}
