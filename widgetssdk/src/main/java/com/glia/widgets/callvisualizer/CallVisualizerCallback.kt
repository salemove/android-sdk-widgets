package com.glia.widgets.callvisualizer

import com.glia.androidsdk.comms.MediaUpgradeOffer

interface CallVisualizerCallback {
    fun onOneWayMediaUpgradeRequest(
        mediaUpgradeOffer: MediaUpgradeOffer,
        operatorNameFormatted: String
    )

    fun onTwoWayMediaUpgradeRequest(
        mediaUpgradeOffer: MediaUpgradeOffer,
        operatorNameFormatted: String
    )

    fun onEngagementRequested()
    fun onLiveObservationOptInDialogAllowed()
    fun onLiveObservationOptInDialogRejected()
}
