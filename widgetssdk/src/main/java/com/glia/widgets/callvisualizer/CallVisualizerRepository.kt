package com.glia.widgets.callvisualizer

import com.glia.androidsdk.Engagement
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.IncomingEngagementRequest
import com.glia.androidsdk.comms.Media
import com.glia.androidsdk.comms.MediaDirection
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.androidsdk.omnibrowse.Omnibrowse
import com.glia.androidsdk.omnibrowse.OmnibrowseEngagement
import com.glia.widgets.di.GliaCore
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.formattedName
import java.util.function.Consumer

class CallVisualizerRepository(private val gliaCore: GliaCore) {

    private lateinit var callback: CallVisualizerCallback
    private var visitorContext: String? = null

    fun init(callVisualizerCallback: CallVisualizerCallback) {
        this.callback = callVisualizerCallback
        autoAcceptEngagementRequest()
        showDialogOnMediaUpgradeRequest()
        Logger.d(TAG, "CallVisualizerRepository initialized")
    }

    fun listenForEngagementEnd(engagement: OmnibrowseEngagement, engagementEnded: Runnable) {
        engagement.on(Engagement.Events.END, engagementEnded)
    }

    fun addVisitorContext(visitorContext: String) {
        this.visitorContext = visitorContext
    }

    private fun showDialogOnMediaUpgradeRequest() {
        gliaCore.callVisualizer.on(Omnibrowse.Events.ENGAGEMENT) { engagement: OmnibrowseEngagement ->
            Logger.d(TAG, "New Call Visualizer engagement started")
            val upgradeOfferConsumer = prepareMediaUpgradeOfferConsumer(engagement)
            engagement.media.on(Media.Events.MEDIA_UPGRADE_OFFER, upgradeOfferConsumer)
        }
    }

    private fun autoAcceptEngagementRequest() {
        gliaCore.callVisualizer.on(Omnibrowse.Events.ENGAGEMENT_REQUEST) { engagementRequest: IncomingEngagementRequest ->
            val onResult = Consumer { error: GliaException? ->
                if (error != null) {
                    Logger.e(TAG, "Error during accepting engagement request, reason" + error.message)
                } else {
                    Logger.d(TAG, "Incoming Call Visualizer engagement auto accepted")
                }
            }
            engagementRequest.accept(visitorContext ?: null as String?, onResult)
        }
    }

    private fun prepareMediaUpgradeOfferConsumer(engagement: OmnibrowseEngagement): Consumer<MediaUpgradeOffer> {
        return Consumer { offer: MediaUpgradeOffer ->
            Logger.d(
                TAG,
                "upgradeOfferConsumer, offer: $offer"
            )
            val operatorNameFormatted = engagement.state.operator.formattedName
            if (offer.video == MediaDirection.TWO_WAY) {
                callback.onTwoWayMediaUpgradeRequest(offer, operatorNameFormatted)
            } else if (offer.video == MediaDirection.ONE_WAY) {
                callback.onOneWayMediaUpgradeRequest(offer, operatorNameFormatted)
            }
        }
    }

    fun unregisterEngagementEndListener(engagementEnded: Runnable) {
        gliaCore.currentEngagement.ifPresent { engagement: Engagement ->
            engagement.off(
                Engagement.Events.END,
                engagementEnded
            )
        }
    }
}
