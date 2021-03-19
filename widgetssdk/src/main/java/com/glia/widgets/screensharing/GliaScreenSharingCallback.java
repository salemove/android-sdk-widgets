package com.glia.widgets.screensharing;

import com.glia.androidsdk.GliaException;

public interface GliaScreenSharingCallback {
    void onScreenSharingRequest();

    void onScreenSharingRequestError(GliaException exception);
}
