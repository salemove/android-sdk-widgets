package com.glia.widgets.mvp

import com.glia.widgets.mvp.contracts.MvpModelContract

/**
 * Mvp base Model implementation
 */
abstract class MvpModel : MvpModelContract {

    override fun unsubscribe() {
        // Implement cleanup here if needed
    }

    override fun onDestroy() {
        // No cleanup here
    }
}
