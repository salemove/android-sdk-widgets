package com.glia.widgets.engagement.end

import android.annotation.SuppressLint
import android.app.Activity
import com.glia.widgets.GliaWidgets
import com.glia.widgets.UiTheme
import com.glia.widgets.call.CallActivity
import com.glia.widgets.chat.ChatActivity
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.OneTimeEvent
import com.glia.widgets.helper.getFullHybridTheme
import com.glia.widgets.helper.isGlia
import com.glia.widgets.helper.wrapWithMaterialThemeOverlay
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.processors.PublishProcessor
import io.reactivex.subjects.CompletableSubject
import java.lang.ref.WeakReference

internal interface EndEngagementController {

    val state: Flowable<EndEngagement.State>
    fun captureTheme(activity: Activity)
    fun onActivityResumed(activity: Activity)
    fun onActivityPaused()
    fun initialize()
    fun resetState()
}

internal class EndEngagementControllerImpl @JvmOverloads constructor(
    private val repository: EngagementEndRepository,
    private val resumedActivity: PublishProcessor<WeakReference<Activity>> = PublishProcessor.create()
) : EndEngagementController {
    private val initTrigger: CompletableSubject = CompletableSubject.create()
    private var _currentUiTheme: UiTheme? = null
    private val currentTheme: UiTheme
        get() = _currentUiTheme ?: UiTheme()

    private val engagementEndOneTimedResult: Flowable<OneTimeEvent<EndEngagement.Result>> =
        initTrigger.andThen(repository.engagementEndResult.map(::OneTimeEvent))

    override val state: Flowable<EndEngagement.State> = Flowable.combineLatest(engagementEndOneTimedResult, resumedActivity, ::produceState)
        .observeOn(AndroidSchedulers.mainThread())

    override fun initialize() {
        initTrigger.onComplete()
    }

    @SuppressLint("CheckResult")
    override fun resetState() {
        engagementEndOneTimedResult.singleOrError().subscribe({ it.markConsumed() }, {})
    }

    private fun produceState(result: OneTimeEvent<EndEngagement.Result>, activityRef: WeakReference<Activity>): EndEngagement.State {
        val activity = activityRef.get()

        return when {
            result.isConsumed || activity == null -> EndEngagement.State.Skip
            result.view() is EndEngagement.Result.Visitor -> {
                result.markConsumed()
                EndEngagement.State.FinishSilently
            }

            result.view() is EndEngagement.Result.Survey -> EndEngagement.State.ShowSurvey(
                activity,
                (result.consume() as EndEngagement.Result.Survey).survey,
                currentTheme
            )

            result.view() is EndEngagement.Result.Operator && activity.isGlia -> {
                result.consume()
                EndEngagement.State.ShowDialog(activity.wrapWithMaterialThemeOverlay(), currentTheme)
            }

            result.view() is EndEngagement.Result.Operator && !activity.isGlia -> EndEngagement.State.LaunchDialogHolderActivity(activity)
            else -> EndEngagement.State.Skip
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
