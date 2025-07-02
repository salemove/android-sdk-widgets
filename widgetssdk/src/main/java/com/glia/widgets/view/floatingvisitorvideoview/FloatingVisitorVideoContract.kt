package com.glia.widgets.view.floatingvisitorvideoview

import com.glia.androidsdk.comms.MediaState

internal interface FloatingVisitorVideoContract {

    interface View {
        fun show(state: MediaState?)
        fun showFlipCameraButton(flipButtonState: FlipButtonState)
        fun hide()
        fun onResume()
        fun onPause()
        fun showOnHold()
        fun hideOnHold()
    }

    enum class FlipButtonState {
        SWITCH_TO_FACE_CAMERA,
        SWITCH_TO_BACK_CAMERA,
        HIDE
    }
}
