package com.glia.widgets.view.snackbar.liveobservation

internal interface ActivityWatcherForLiveObservationContract {
    interface Controller {
        fun setWatcher(watcher: Watcher)
    }

    interface Watcher {
        fun showSnackBar()
    }
}
