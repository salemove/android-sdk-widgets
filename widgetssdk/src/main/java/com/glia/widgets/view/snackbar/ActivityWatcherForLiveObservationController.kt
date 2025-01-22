package com.glia.widgets.view.snackbar

import android.annotation.SuppressLint
import com.glia.widgets.engagement.State
import com.glia.widgets.engagement.domain.EngagementStateUseCase
import kotlin.properties.Delegates

@SuppressLint("CheckResult")
internal class ActivityWatcherForLiveObservationController(
    engagementStateUseCase: EngagementStateUseCase,
    private val liveObservationPopupUseCase: LiveObservationPopupUseCase
) : ActivityWatcherForLiveObservationContract.Controller {
    private var liveObservationWatcher: ActivityWatcherForLiveObservationContract.Watcher by Delegates.notNull()

    init {
        engagementStateUseCase()
            .filter { it is State.EngagementStarted }
            .switchMapSingle { liveObservationPopupUseCase() }
            .filter { it }
            .subscribe({ liveObservationWatcher.showSnackBar() }) {}
    }

    override fun setWatcher(watcher: ActivityWatcherForLiveObservationContract.Watcher) {
        liveObservationWatcher = watcher
    }
}
