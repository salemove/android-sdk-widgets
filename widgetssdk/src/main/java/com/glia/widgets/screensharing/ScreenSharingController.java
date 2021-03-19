package com.glia.widgets.screensharing;

import android.content.Context;

import com.glia.androidsdk.GliaException;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.Utils;
import com.glia.widgets.model.GliaScreenSharingRepository;

public class ScreenSharingController {
    private static final String TAG = "GliaScreenSharingController";
    private final GliaScreenSharingRepository repository;
    private GliaScreenSharingCallback viewCallback;

    public ScreenSharingController(
            GliaScreenSharingRepository gliaScreenSharingRepository,
            GliaScreenSharingCallback callback
    ) {
        Logger.d(TAG, "init");
        repository = gliaScreenSharingRepository;
        viewCallback = callback;
        initControllerCallback();
    }

    private void initControllerCallback() {
        repository.init(new GliaScreenSharingCallback() {
            @Override
            public void onScreenSharingRequest() {
                if (viewCallback != null) viewCallback.onScreenSharingRequest();
            }

            @Override
            public void onScreenSharingRequestError(GliaException exception) {
                if (viewCallback != null) viewCallback.onScreenSharingRequestError(exception);
            }
        });
    }

    public void setGliaScreenSharingCallback(GliaScreenSharingCallback callback) {
        Logger.d(TAG, "setCallback");
        viewCallback = callback;
    }

    public void onScreenSharingAccepted(Context context) {
        Logger.d(TAG, "onScreenSharingAccepted");
        repository.onScreenSharingAccepted(Utils.getActivity(context));
    }

    public void onScreenSharingDeclined() {
        Logger.d(TAG, "onScreenSharingDeclined");
        repository.onScreenSharingDeclined();
    }

    public void onDestroy(boolean retain) {
        Logger.d(TAG, "onDestroy retain=" + retain);
        viewCallback = null;
        if (!retain) {
            repository.onDestroy();
        }
    }
}
