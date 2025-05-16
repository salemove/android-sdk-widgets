package com.glia.widgets.helper

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner

internal class ApplicationLifecycleManager {
    private val lifecycleEventObserverList: MutableList<LifecycleEventObserver> = ArrayList()
    private val lifecycle: Lifecycle = ProcessLifecycleOwner.get().lifecycle

    fun isAtLeast(state: Lifecycle.State): Boolean = lifecycle.currentState.isAtLeast(state)

    fun addObserver(observer: LifecycleEventObserver) {
        lifecycle.addObserver(observer)
        lifecycleEventObserverList.add(observer)
    }

    fun removeObserver(observer: LifecycleEventObserver) {
        lifecycle.removeObserver(observer)
        lifecycleEventObserverList.remove(observer)
    }

    fun onDestroy() {
        for (observer in lifecycleEventObserverList) {
            lifecycle.removeObserver(observer)
        }
        lifecycleEventObserverList.clear()
    }
}
