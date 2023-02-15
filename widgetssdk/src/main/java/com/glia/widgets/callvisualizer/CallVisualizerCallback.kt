package com.glia.widgets.callvisualizer

import com.glia.androidsdk.comms.MediaUpgradeOffer

interface CallVisualizerCallback {
    fun onOneWayMediaUpgradeRequest(mediaUpgradeOffer: MediaUpgradeOffer, operatorName: String)
    fun onTwoWayMediaUpgradeRequest(mediaUpgradeOffer: MediaUpgradeOffer, operatorName: String)
}
