package com.glia.widgets.callvisualizer.mvpsample

import com.glia.widgets.mvp.MvpPresenter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MvpSamplePresenter(
    view: MvpSampleContract.View,
    model: MvpSampleContract.Model
) : MvpPresenter<MvpSampleContract.View, MvpSampleContract.Model, SampleState>(view, model), MvpSampleContract.Presenter {

    companion object {
        const val EVENT_SEND_COUNTER = "com.glia.widgets.callvisualizer.mvpsample.MvpSamplePresenter.EVENT_SEND_COUNTER"
    }

    private var presenterState: SampleState = createDefaultState()

    private fun createDefaultState(): SampleState {
        return SampleState(0)
    }

    override fun subscribe(state: SampleState?) {
        super.subscribe(state)
        state?.let {
            // Presenter has a state eg not initial start, restore state and view
            restoreState(state)
        } ?: initState()
        EventBus.getDefault().register(this)
    }

    override fun getState(): SampleState? {
        return presenterState
    }

    private fun initState() {
        view.setCounterValue(presenterState.counterValue.toString())
    }

    private fun restoreState(state: SampleState) {
        presenterState = state.copy()
        view.setCounterValue(presenterState.counterValue.toString())
    }

    override fun unsubscribe() {
        super.unsubscribe()
        EventBus.getDefault().unregister(this)
    }

    override fun onButtonClicked() {
        model.sendCounterValue(EVENT_SEND_COUNTER, presenterState.counterValue + 1)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: SimpleIntEvent) {
        if (event.isFor(EVENT_SEND_COUNTER)) {
            when (event) {
                is SimpleIntEvent.Success -> incrementCounter(event.int)
                is SimpleIntEvent.Failure -> view.showToast()
            }
        }
    }

    private fun incrementCounter(int: Int) {
        presenterState = presenterState.copy(counterValue = int)
        view.setCounterValue(presenterState.counterValue.toString())
    }

}

