package com.glia.widgets.mvp.contracts

/**
 * Presenter -> Controller communication interface
 */
interface MvpControllerContract<STATE : Any> {

    val subscribed: Boolean

    // Lifecycle
    fun dispatchSubscribed(state: STATE?)
    fun dispatchUnsubscribed()
    fun dispatchDestroyed()

    // Override this to provide state
    fun getState(): STATE? {
        return null
    }
}
