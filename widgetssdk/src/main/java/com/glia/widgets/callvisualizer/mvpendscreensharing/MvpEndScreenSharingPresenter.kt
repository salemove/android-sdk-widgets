package com.glia.widgets.callvisualizer.mvpendscreensharing

import com.glia.widgets.mvp.MvpPresenter

class MvpEndScreenSharingPresenter(
    view: MvpEndScreenSharingContract.View,
    model: MvpEndScreenSharingContract.Model
) : MvpPresenter<MvpEndScreenSharingContract.View, MvpEndScreenSharingContract.Model, EndScreenSharingState>(view, model), MvpEndScreenSharingContract.Presenter {

}
