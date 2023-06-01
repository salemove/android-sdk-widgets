package com.glia.widgets.callvisualizer.mvpendscreensharing

import android.os.Parcelable
import com.glia.widgets.mvp.MvpActivity
import kotlinx.parcelize.Parcelize

class MvpEndScreenSharingActivity: MvpActivity<MvpEndScreenSharingPresenter, EndScreenSharingState>(), MvpEndScreenSharingContract.View {

    override fun createPresenter(): MvpEndScreenSharingPresenter {
        return MvpEndScreenSharingPresenter(this, MvpEndScreenSharingModel)
    }
}

@Parcelize
data class EndScreenSharingState(val a: Int) : Parcelable