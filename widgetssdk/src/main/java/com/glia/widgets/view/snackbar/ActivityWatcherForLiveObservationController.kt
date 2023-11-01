package com.glia.widgets.view.snackbar

import kotlin.properties.Delegates

internal class ActivityWatcherForLiveObservationController(
    private var liveObservationUseCase: LiveObservationUseCase,
    private val liveObservationPopupUseCase: LiveObservationPopupUseCase
) : ActivityWatcherForLiveObservationContract.Controller {
    private var liveObservationWatcher: ActivityWatcherForLiveObservationContract.Watcher by Delegates.notNull()

    override fun init() {
        liveObservationUseCase.init()
        liveObservationUseCase {
            liveObservationPopupUseCase { shouldShow ->
                if (shouldShow) {
                    liveObservationWatcher.showSnackBar()
                }
            }
        }
    }

    override fun setWatcher(watcher: ActivityWatcherForLiveObservationContract.Watcher) {
        liveObservationWatcher = watcher
    }
}
