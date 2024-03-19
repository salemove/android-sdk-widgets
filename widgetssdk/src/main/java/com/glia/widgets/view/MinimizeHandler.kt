package com.glia.widgets.view

import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG

internal typealias OnMinimizeCalledListener = () -> Unit

internal class MinimizeHandler {
    private val listeners: MutableList<OnMinimizeCalledListener> = ArrayList()
    fun addListener(listener: OnMinimizeCalledListener) {
        Logger.d(TAG, "addListener")
        listeners.add(listener)
    }

    fun clear() {
        Logger.d(TAG, "clear")
        listeners.clear()
    }

    fun minimize() {
        Logger.d(TAG, "minimizeCalled, number of listeners: " + listeners.size)
        for (listener in listeners) {
            listener()
        }
    }
}
