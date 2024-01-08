package com.glia.widgets.engagement.completion

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.GliaWidgets
import com.glia.widgets.UiTheme
import com.glia.widgets.call.CallActivity
import com.glia.widgets.chat.ChatActivity
import com.glia.widgets.di.Dependencies
import com.glia.widgets.engagement.domain.ReleaseResourcesUseCase
import com.glia.widgets.helper.OneTimeEvent
import com.glia.widgets.helper.getFullHybridTheme
import com.glia.widgets.helper.isGlia
import com.glia.widgets.helper.unSafeSubscribe
import com.glia.widgets.helper.wrapWithMaterialThemeOverlay
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import java.lang.ref.WeakReference
import com.glia.widgets.engagement.completion.EngagementCompletionController.State as ControllerState
import com.glia.widgets.engagement.completion.EngagementCompletionUseCase.State as UseCaseState

internal interface EngagementCompletionController {

    val state: Flowable<State>
    fun captureTheme(activity: Activity)
    fun onActivityResumed(activity: Activity)
    fun onActivityPaused()

    sealed interface State {
        /* This state should be used when we don't expect any input from the UI layer, e.g., waiting for DialogHolderActivity to launch */
        object Ignore : State

        /* This state should release UI (kill all the Glia activities and services) */
        object ReleaseUi : State

        /* This state should launch Survey activity */
        data class ShowSurvey(val survey: Survey, val activity: Activity, val uiTheme: UiTheme) : State

        /* This state should show a Dialog that indicates engagement end by the operator */
        data class ShowOperatorEndedEngagementDialog(val themedContext: Context, val uiTheme: UiTheme, val onHandledCallback: () -> Unit) : State

        /* This state should show a Dialog that indicates that the queue is unstaffed */
        data class ShowQueueUnstaffedDialog(val themedContext: Context, val uiTheme: UiTheme, val onHandledCallback: () -> Unit) : State

        /* This state should show a Dialog that indicates an unexpected error */
        data class ShowUnexpectedDialog(val themedContext: Context, val uiTheme: UiTheme, val onHandledCallback: () -> Unit) : State

        /* This state should launch DialogHolderActivity to show Dialogs that require Material Theme support */
        data class LaunchDialogHolderActivity(val activity: Activity) : State
    }
}

internal class EngagementCompletionControllerImpl @JvmOverloads constructor(
    private val engagementCompletionUseCase: EngagementCompletionUseCase,
    private val releaseResourcesUseCase: ReleaseResourcesUseCase,
    private val resumedActivity: PublishProcessor<WeakReference<Activity>> = PublishProcessor.create()
) : EngagementCompletionController {
    private var _currentUiTheme: UiTheme? = null
    private val currentTheme: UiTheme
        get() = _currentUiTheme ?: UiTheme()

    private val _state: PublishProcessor<EngagementCompletionController.State> = PublishProcessor.create()
    override val state: Flowable<EngagementCompletionController.State> = _state.filter { it !is EngagementCompletionController.State.Ignore }

    init {
        initObservables()
    }

    @SuppressLint("CheckResult")
    private fun initObservables() {
        Flowable.combineLatest(engagementCompletionUseCase(), resumedActivity, ::produceState).unSafeSubscribe(::submitState)
    }

    private fun submitState(state: EngagementCompletionController.State) {
        _state.onNext(state)
    }

    private fun produceState(
        event: OneTimeEvent<EngagementCompletionUseCase.State>,
        activityRef: WeakReference<Activity>
    ): EngagementCompletionController.State {
        val activity = activityRef.get()
        val state = event.view()

        return when {
            //No matter activity is null or finishing, we must release all resources since queueing canceled or engagement ended,
            //so this case should always be the first
            state is UseCaseState.QueuingOrEngagementEnded -> {
                event.markConsumed()
                releaseResourcesUseCase()
                EngagementCompletionController.State.ReleaseUi
            }

            activity == null || activity.isFinishing || state == null -> EngagementCompletionController.State.Ignore

            (state is UseCaseState.UnexpectedErrorHappened || state is UseCaseState.QueueUnstaffed || state is UseCaseState.OperatorEndedEngagement)
                && !activity.isGlia -> ControllerState.LaunchDialogHolderActivity(activity)

            state is UseCaseState.SurveyLoaded -> {
                event.markConsumed()
                ControllerState.ShowSurvey(state.survey, activity, currentTheme)
            }

            state is UseCaseState.OperatorEndedEngagement -> {
                ControllerState.ShowOperatorEndedEngagementDialog(activity.wrapWithMaterialThemeOverlay(), currentTheme, event::markConsumed)
            }

            state is UseCaseState.QueueUnstaffed -> {
                ControllerState.ShowQueueUnstaffedDialog(activity.wrapWithMaterialThemeOverlay(), currentTheme, event::markConsumed)
            }

            state is UseCaseState.UnexpectedErrorHappened -> {
                ControllerState.ShowUnexpectedDialog(activity.wrapWithMaterialThemeOverlay(), currentTheme, event::markConsumed)
            }

            else -> {
                event.markConsumed()
                EngagementCompletionController.State.Ignore
            }
        }
    }

    override fun captureTheme(activity: Activity) {
        if (activity !is CallActivity && activity !is ChatActivity) return
        val themeFromIntent: UiTheme? = activity.intent?.getParcelableExtra(GliaWidgets.UI_THEME)
        val themeFromGlobalSetting: UiTheme? = Dependencies.getSdkConfigurationManager().uiTheme
        _currentUiTheme = themeFromGlobalSetting.getFullHybridTheme(themeFromIntent)
    }

    override fun onActivityResumed(activity: Activity) {
        resumedActivity.onNext(WeakReference(activity))
    }

    override fun onActivityPaused() {
        resumedActivity.onNext(WeakReference(null))
    }
}
