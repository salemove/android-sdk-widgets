package com.glia.widgets.view.snackbar.liveobservation

import android.annotation.SuppressLint
import android.app.Activity
import com.glia.widgets.R
import com.glia.widgets.base.BaseActivityStackWatcher
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.WeakReferenceDelegate
import com.glia.widgets.locale.LocaleProvider
import com.glia.widgets.view.snackbar.SnackBarDelegate
import com.glia.widgets.view.snackbar.SnackBarDelegateFactory
import com.glia.widgets.view.unifiedui.theme.UnifiedThemeManager

@SuppressLint("CheckResult")
internal class ActivityWatcherForLiveObservation(
    private val localeProvider: LocaleProvider,
    private val themeManager: UnifiedThemeManager,
    controller: ActivityWatcherForLiveObservationContract.Controller
) : BaseActivityStackWatcher(), ActivityWatcherForLiveObservationContract.Watcher {
    private var resumedActivity: Activity? by WeakReferenceDelegate()

    init {
        controller.setWatcher(this)
        topActivityObserver.subscribe(
            { resumedActivity = it },
            { error -> Logger.e(TAG, "Observable monitoring top activity FAILED", error) }
        )
    }

    override fun showSnackBar() {
        resumedActivity?.apply {
            runOnUiThread {
                makeSnackBar(this).show()
            }
        }
    }

    private fun makeSnackBar(activity: Activity): SnackBarDelegate =
        SnackBarDelegateFactory(
            activity,
            R.string.live_observation_indicator_message,
            localeProvider,
            themeManager.theme
        ).createDelegate()

}
