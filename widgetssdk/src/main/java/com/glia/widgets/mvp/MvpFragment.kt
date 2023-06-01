package com.glia.widgets.mvp

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.glia.widgets.mvp.contracts.MvpControllerContract
import com.glia.widgets.mvp.contracts.MvpViewContract
import com.glia.widgets.mvp.contracts.MvpViewPresenterContract

/**
 * Base Fragment for implementing an MVP view within one fragment.
 * The fragment acts as a controller (creating the MVP components) and as the View implementation, too.
 *
 * PRESENTER - Presenter implementation class
 * STATE - Optional State object class. Used in the presenter. If not needed then use 'Any'
 */
abstract class MvpFragment<PRESENTER, STATE> : Fragment(), MvpViewContract
        where STATE : Any,
              PRESENTER : MvpControllerContract<STATE>,
              PRESENTER : MvpViewPresenterContract {

    protected abstract fun createPresenter(): PRESENTER

    protected lateinit var presenter: PRESENTER
    private lateinit var controller: MvpController<PRESENTER, STATE>

    fun isPresenterInitialised(): Boolean {
        return ::presenter.isInitialized
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = createPresenter()
        val initialState = if (savedInstanceState == null) createInitialState() else null
        controller = MvpController(presenter, MvpStateBundler(createStateKey()), initialState)
        if (savedInstanceState != null) {
            controller.restoreState(savedInstanceState)
        }
        if (SubscribeLifecycleState.CREATED == getSubscribeLifecycleState()) {
            controller.subscribe()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (SubscribeLifecycleState.VIEW_CREATED == getSubscribeLifecycleState()) {
            controller.subscribe()
        }
    }

    override fun onStart() {
        super.onStart()
        if (SubscribeLifecycleState.STARTED == getSubscribeLifecycleState()) {
            controller.subscribe()
        }
    }

    override fun onStop() {
        super.onStop()
        if (SubscribeLifecycleState.STARTED == getSubscribeLifecycleState()) {
            controller.unsubscribe()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        controller.saveState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (SubscribeLifecycleState.VIEW_CREATED == getSubscribeLifecycleState()) {
            controller.unsubscribe()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (SubscribeLifecycleState.CREATED == getSubscribeLifecycleState()) {
            controller.unsubscribe()
        }
        controller.destroy()
    }

    protected open fun createInitialState(): STATE? {
        return null
    }

    protected open fun getSubscribeLifecycleState(): SubscribeLifecycleState {
        return SubscribeLifecycleState.VIEW_CREATED
    }

    override fun finish() {
        activity?.finishAfterTransition()
    }

    private fun createStateKey(): String {
        return "${this::class.java.name}.STATE"
    }
}
