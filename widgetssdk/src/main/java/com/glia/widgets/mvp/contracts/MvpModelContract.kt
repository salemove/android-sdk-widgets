package com.glia.widgets.mvp.contracts

/**
 * Presenter -> Model communication interface
 */
interface MvpModelContract {
    fun unsubscribe()
    fun onDestroy()
}
