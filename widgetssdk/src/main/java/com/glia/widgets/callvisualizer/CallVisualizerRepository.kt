package com.glia.widgets.callvisualizer

import com.glia.androidsdk.comms.Media
import com.glia.androidsdk.comms.MediaDirection
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.androidsdk.omnibrowse.Omnibrowse
import com.glia.androidsdk.omnibrowse.OmnibrowseEngagement
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Logger
import java.util.function.Consumer

class CallVisualizerRepository {

    private lateinit var callback: CallVisualizerCallback

    companion object {
        private val TAG = CallVisualizerRepository::class.java.simpleName
    }

    fun init(callVisualizerCallback: CallVisualizerCallback) {
        this.callback = callVisualizerCallback
        Dependencies.glia().callVisualizer.on(Omnibrowse.Events.ENGAGEMENT)
        { engagement: OmnibrowseEngagement ->
            Logger.d(TAG, "New Call Visualizer engagement started")
            val upgradeOfferConsumer = prepareMediaUpgradeOfferConsumer(engagement)
            engagement.media.on(Media.Events.MEDIA_UPGRADE_OFFER, upgradeOfferConsumer)
        }
        Logger.d(TAG, "CallVisualizerRepository initialized")
    }

    private fun prepareMediaUpgradeOfferConsumer(engagement: OmnibrowseEngagement): Consumer<MediaUpgradeOffer> {
        return Consumer { offer: MediaUpgradeOffer ->
            Logger.d(
                TAG,
                "upgradeOfferConsumer, offer: $offer"
            )
            val operatorName = engagement.state.operator.name
            if (offer.video == MediaDirection.TWO_WAY) {
                callback.onTwoWayMediaUpgradeRequest(offer, operatorName)
            } else if (offer.video == MediaDirection.ONE_WAY) {
                callback.onOneWayMediaUpgradeRequest(offer, operatorName)
            }
        }
    }
}
