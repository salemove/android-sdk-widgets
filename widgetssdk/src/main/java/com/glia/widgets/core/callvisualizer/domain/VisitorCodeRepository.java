package com.glia.widgets.core.callvisualizer.domain;

import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.RequestCallback;
import com.glia.androidsdk.omnibrowse.VisitorCode;
import com.glia.widgets.di.GliaCore;

public class VisitorCodeRepository {
    private final GliaCore gliaCore;

    public VisitorCodeRepository(GliaCore gliaCore) {
        this.gliaCore = gliaCore;
    }

    public void getVisitorCode(RequestCallback<VisitorCode> callback) {
        if (!gliaCore.isInitialized()) {
            throw new GliaException("Widgets SDK is not initialized", GliaException.Cause.INVALID_INPUT);
        }
        gliaCore.getCallVisualizer().getVisitorCode(callback);
    }
}
