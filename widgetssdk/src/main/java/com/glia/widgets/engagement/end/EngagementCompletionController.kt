package com.glia.widgets.engagement.end

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.GliaWidgets
import com.glia.widgets.UiTheme
import com.glia.widgets.call.CallActivity
import com.glia.widgets.chat.ChatActivity
import com.glia.widgets.di.Dependencies
import com.glia.widgets.engagement.EngagementRepository
import com.glia.widgets.engagement.EngagementStateUseCase
import com.glia.widgets.engagement.ReleaseResourcesUseCase
import com.glia.widgets.engagement.SurveyUseCase
import com.glia.widgets.helper.OneTimeEvent
import com.glia.widgets.helper.getFullHybridTheme
import com.glia.widgets.helper.isGlia
import com.glia.widgets.helper.wrapWithMaterialThemeOverlay
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import java.lang.ref.WeakReference

internal interface EngagementCompletionController {

    val state: Flowable<State>
    fun captureTheme(activity: Activity)
    fun onActivityResumed(activity: Activity)
    fun onActivityPaused()

    sealed interface State {
        object Skip : State
        object ReleaseUi : State
        data class ShowSurvey(val survey: Survey, val activity: Activity, val uiTheme: UiTheme) : State
        data class ShowOperatorEndedEngagementDialog(val themedContext: Context, val uiTheme: UiTheme) : State
        data class LaunchDialogHolderActivity(val activity: Activity) : State
    }
}

internal class EngagementCompletionControllerImpl @JvmOverloads constructor(
    private val surveyUseCase: SurveyUseCase,
    private val engagementStateUseCase: EngagementStateUseCase,
    private val releaseResourcesUseCase: ReleaseResourcesUseCase,
    private val resumedActivity: PublishProcessor<WeakReference<Activity>> = PublishProcessor.create()
) : EngagementCompletionController {
    private var _currentUiTheme: UiTheme? = null
    private val currentTheme: UiTheme
        get() = _currentUiTheme ?: UiTheme()

    private val _state: PublishProcessor<EngagementCompletionController.State> = PublishProcessor.create()
    override val state: Flowable<EngagementCompletionController.State> = _state.filter { it !is EngagementCompletionController.State.Skip }

    init {
        initObservables()
    }

    @SuppressLint("CheckResult")
    private fun initObservables() {
        engagementStateUseCase().filter { it is EngagementRepository.State.Finished }.subscribe({
            submitState(EngagementCompletionController.State.ReleaseUi)
            releaseResourcesUseCase()
        }, {})
        Flowable.combineLatest(surveyUseCase(), resumedActivity, ::produceState).subscribe(::submitState) {
            //no op, this should not happen
        }
    }

    private fun submitState(state: EngagementCompletionController.State) {
        _state.onNext(state)
    }

    private fun produceState(
        surveyEvent: OneTimeEvent<EngagementRepository.SurveyState>,
        activityRef: WeakReference<Activity>
    ): EngagementCompletionController.State {
        val activity = activityRef.get()
        val surveyState = surveyEvent.view()

        return when {
            activity == null || surveyState == null -> EngagementCompletionController.State.Skip
            surveyState is EngagementRepository.SurveyState.EmptyFromOperatorRequest && !activity.isGlia ->
                EngagementCompletionController.State.LaunchDialogHolderActivity(activity)

            surveyState is EngagementRepository.SurveyState.Empty -> {
                surveyEvent.markConsumed()
                EngagementCompletionController.State.Skip
            }

            surveyState is EngagementRepository.SurveyState.Value -> {
                surveyEvent.markConsumed()
                EngagementCompletionController.State.ShowSurvey(
                    surveyState.survey,
                    activity,
                    currentTheme
                )
            }

            surveyState is EngagementRepository.SurveyState.EmptyFromOperatorRequest -> {
                surveyEvent.markConsumed()
                EngagementCompletionController.State.ShowOperatorEndedEngagementDialog(
                    activity.wrapWithMaterialThemeOverlay(),
                    currentTheme
                )
            }

            else -> {
                surveyEvent.markConsumed()
                EngagementCompletionController.State.Skip
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
