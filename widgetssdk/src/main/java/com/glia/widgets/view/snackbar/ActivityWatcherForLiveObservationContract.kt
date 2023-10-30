package com.glia.widgets.view.snackbar

internal interface ActivityWatcherForLiveObservationContract {
    interface Controller {
        fun setWatcher(watcher: Watcher)
        fun init()
    }

    interface Watcher {
        fun showSnackBar()
    }
}
