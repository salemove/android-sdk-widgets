package com.glia.widgets.view.snackbar

import android.annotation.SuppressLint
import android.app.Activity
import androidx.annotation.StringRes
import com.glia.widgets.base.BaseSingleActivityWatcher
import com.glia.widgets.helper.GliaActivityManager
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.OneTimeEvent
import com.glia.widgets.helper.TAG
import com.glia.widgets.locale.LocaleProvider
import com.glia.widgets.view.unifiedui.theme.UnifiedThemeManager
import io.reactivex.rxjava3.core.Flowable
import java.lang.ref.WeakReference

@SuppressLint("CheckResult")
internal class ActivityWatcherForSnackbar(
    controller: SnackbarContract.Controller,
    gliaActivityManager: GliaActivityManager,
    private val localeProvider: LocaleProvider,
    private val themeManager: UnifiedThemeManager,
) : BaseSingleActivityWatcher(gliaActivityManager) {

    init {
        Flowable.combineLatest(resumedActivity, controller.state, ::handleState).subscribe()
    }

    private fun handleState(
        activityReference: WeakReference<Activity>,
        event: OneTimeEvent<SnackbarContract.State>
    ) {
        val state = event.value
        val activity = activityReference.get()

        when {
            event.consumed -> Logger.d(TAG, "Skipping. Activity is null or finishing.")
            activity == null || activity.isFinishing -> Logger.d(TAG, "Skipping. Activity is null or finishing.")
            state is SnackbarContract.State.ShowSnackBar -> event.consume { showSnackBar(activity, state.message) }
        }
    }

    private fun showSnackBar(activity: Activity, @StringRes messageRes: Int) = SnackBarDelegateFactory(
        activity,
        messageRes,
        localeProvider,
        themeManager.theme
    ).createDelegate().show()
}
