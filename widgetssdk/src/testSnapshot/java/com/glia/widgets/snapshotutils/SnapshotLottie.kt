package com.glia.widgets.snapshotutils

import com.airbnb.lottie.LottieTask
import java.util.concurrent.Executor
import java.util.concurrent.Executors

interface SnapshotLottie : SnapshotTestLifecycle {
    fun lottieMock() {
        LottieTask.EXECUTOR = Executor(Runnable::run)
        setOnEndListener {
            LottieTask.EXECUTOR = Executors.newCachedThreadPool()
        }
    }
}
