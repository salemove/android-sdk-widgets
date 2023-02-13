package com.glia.widgets.callvisualizer

import com.glia.androidsdk.comms.Media
import com.glia.androidsdk.comms.MediaDirection
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.androidsdk.omnibrowse.Omnibrowse
import com.glia.androidsdk.omnibrowse.OmnibrowseEngagement
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.Utils
import java.util.function.Consumer

class CallVisualizerRepository {

    companion object {
        private val TAG = CallVisualizerRepository::class.java.simpleName
    }

    fun init() {
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
            val dialogController = Dependencies.getControllerFactory().dialogController
            val operatorName = engagement.state.operator.name
            val formattedOperatorName = Utils.formatOperatorName(operatorName)
            if (offer.video == MediaDirection.TWO_WAY) {
                dialogController.showUpgradeVideoDialog2Way(offer, formattedOperatorName)
            } else if (offer.video == MediaDirection.ONE_WAY) {
                dialogController.showUpgradeVideoDialog1Way(offer, formattedOperatorName)
            }
        }
    }
}
