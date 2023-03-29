package com.glia.widgets.core.visitor

import com.glia.androidsdk.comms.VisitorMediaState

interface VisitorMediaUpdatesListener {
    fun onNewVisitorMediaState(visitorMediaState: VisitorMediaState?)
    fun onHoldChanged(isOnHold: Boolean)
}
