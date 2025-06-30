package com.glia.widgets.operator

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.widgets.engagement.domain.MediaUpgradeOfferData
import com.glia.widgets.helper.OneTimeEvent
import io.reactivex.rxjava3.core.Flowable

internal interface OperatorRequestContract {
    sealed interface State {
        data class RequestMediaUpgrade(val data: MediaUpgradeOfferData) : State
        data class OpenCallActivity(val mediaType: Engagement.MediaType) : State
        object DismissAlertDialog : State
        data class DisplayToast(val message: String) : State
        object ShowOverlayDialog : State
        object OpenOverlayPermissionScreen : State
    }

    interface Controller {
        val state: Flowable<OneTimeEvent<State>>
        fun onMediaUpgradeAccepted(offer: MediaUpgradeOffer, activity: Activity)
        fun onMediaUpgradeDeclined(offer: MediaUpgradeOffer, activity: Activity)
        fun onOverlayPermissionRequestAccepted(activity: Activity)
        fun onOverlayPermissionRequestDeclined(activity: Activity)
        fun overlayPermissionScreenOpened()
        fun failedToOpenOverlayPermissionScreen()
    }
}
