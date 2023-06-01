package com.glia.widgets.mvp

import com.glia.widgets.mvp.contracts.MvpControllerContract
import com.glia.widgets.mvp.contracts.MvpModelContract
import com.glia.widgets.mvp.contracts.MvpViewContract
import com.glia.widgets.mvp.contracts.MvpViewPresenterContract

/**
 * Mvp base Presenter implementation.
 * VIEW - View impl
 * MODEL - Model impl
 * STATE - Optional state class. Use 'Any' if not needed.
 *
 * An instance of STATE can be provided by overriding getState() function. The instance returned
 * by getState() (called before unsubscribe()) will be returned to the presenter in subscribe()
 *
 * Presenter is not saved anywhere and it has 2 lifecycle methods:
 * subscribe() - Everything is set up. Can start loading internal data if needed
 * unsubscribe() - Time to do any resource cleanup before the presenter is destroyed
 */
open class MvpPresenter<VIEW : MvpViewContract, MODEL : MvpModelContract, STATE : Any>(
    protected val view: VIEW,
    protected val model: MODEL
) : MvpViewPresenterContract, MvpControllerContract<STATE> {

    private var _subscribed: Boolean = false
    override val subscribed: Boolean
        get() = _subscribed

    final override fun dispatchSubscribed(state: STATE?) {
        _subscribed = true
        subscribe(state)
    }

    final override fun dispatchUnsubscribed() {
        _subscribed = false
        model.unsubscribe()
        unsubscribe()
    }

    final override fun dispatchDestroyed() {
        model.onDestroy()
        onDestroy()
    }

    /**
     * Separate overridable functions. We need to ensure that our own logic is always called even if super() call is not there.
     * To make that possible, we'll provide these open lifecycle methods to our implementations, but always call our internal dispatch* methods
     */
    open fun subscribe(state: STATE?) {
        // Do nothing
    }

    open fun unsubscribe() {
        // Do nothing
    }

    open fun onDestroy() {
        // Do nothing
    }

    // Override this to provide state
    override fun getState(): STATE? {
        return null
    }

    override fun onBackPressed() {
        view.finish()
    }
}
