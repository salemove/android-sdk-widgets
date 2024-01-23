package com.glia.widgets.snapshotutils

interface SnapshotTestLifecycle {
    fun setOnEndListener(listener: OnTestEnded)
}

typealias OnTestEnded = () -> Unit
