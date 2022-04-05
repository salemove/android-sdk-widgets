package com.glia.widgets.core.visitor;

import com.glia.androidsdk.comms.VisitorMediaState;

public interface VisitorMediaUpdatesListener {
    void onNewVisitorMediaState(VisitorMediaState visitorMediaState);

    void onHoldChanged(boolean isOnHold);
}
