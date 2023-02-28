package com.glia.widgets.core.callvisualizer.domain;

import android.content.Context;

import com.glia.widgets.view.VisitorCodeView;

/**
 * Provides controls related to Call Visualizer module
 * TODO: improve docs in MOB-1816
 */
public interface CallVisualizer {
    /**
     * This builds a VisitorCodeView component that can be integrated in the client application
     */
    VisitorCodeView createVisitorCodeView(Context context);

    /**
     * This shows Visitor Code inside a Dialog on top of current Activity inside of your application
     */
    void showVisitorCodeDialog(Context context);

}
