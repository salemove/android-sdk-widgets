package com.glia.widgets.callvisualizer.mvpsample

import com.glia.widgets.mvp.contracts.MvpModelContract
import com.glia.widgets.mvp.contracts.MvpViewContract
import com.glia.widgets.mvp.contracts.MvpViewPresenterContract

class MvpSampleContract {
    interface View: MvpViewContract {
        fun setCounterValue(value: String)
        fun showToast()
    }

    interface Presenter: MvpViewPresenterContract {
        fun onButtonClicked()
    }

    interface Model: MvpModelContract {
        fun sendCounterValue(tag: String, value: Int)
    }
}