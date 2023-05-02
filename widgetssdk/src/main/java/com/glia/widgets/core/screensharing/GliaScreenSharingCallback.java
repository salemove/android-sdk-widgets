package com.glia.widgets.core.screensharing;

import com.glia.androidsdk.GliaException;

public interface GliaScreenSharingCallback {
    void onScreenSharingRequest();

    void onScreenSharingStarted();

    void onScreenSharingEnded();

    void onScreenSharingRequestError(GliaException exception);

    void onScreenSharingRequestSuccess();

    void onForceStopScreenSharing();

    void hideScreenSharingEnabledNotification();
}
