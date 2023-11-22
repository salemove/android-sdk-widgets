package com.glia.widgets.view.snackbar

import android.annotation.SuppressLint
import com.glia.widgets.engagement.EngagementStateUseCase
import com.glia.widgets.engagement.State
import kotlin.properties.Delegates

@SuppressLint("CheckResult")
internal class ActivityWatcherForLiveObservationController(
    engagementStateUseCase: EngagementStateUseCase,
    private val liveObservationPopupUseCase: LiveObservationPopupUseCase
) : ActivityWatcherForLiveObservationContract.Controller {
    private var liveObservationWatcher: ActivityWatcherForLiveObservationContract.Watcher by Delegates.notNull()

    init {
        engagementStateUseCase()
            .filter { it is State.StartedOmniCore || it is State.StartedCallVisualizer }
            .switchMapSingle { liveObservationPopupUseCase() }
            .filter { it }
            .subscribe({ liveObservationWatcher.showSnackBar() }) {}
    }

    override fun setWatcher(watcher: ActivityWatcherForLiveObservationContract.Watcher) {
        liveObservationWatcher = watcher
    }
}
