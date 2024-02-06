package com.glia.widgets.operator

import android.app.Activity
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.widgets.engagement.domain.MediaUpgradeOfferData
import com.glia.widgets.helper.OneTimeEvent
import io.reactivex.Flowable

internal interface OperatorRequestContract {
    sealed interface State {
        data class RequestMediaUpgrade(val data: MediaUpgradeOfferData) : State
        data class OpenCallActivity(val mediaType: Engagement.MediaType) : State
        object DismissAlertDialog : State
    }

    interface Controller {
        val state: Flowable<OneTimeEvent<State>>
        fun onMediaUpgradeAccepted(offer: MediaUpgradeOffer, activity: Activity)
        fun onMediaUpgradeDeclined(offer: MediaUpgradeOffer, activity: Activity)
    }
}
