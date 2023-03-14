package com.glia.widgets.core.callvisualizer.domain;

import android.content.Context;

import com.glia.widgets.core.configuration.GliaSdkConfiguration;
import com.glia.widgets.view.VisitorCodeView;

/**
 * Provides controls related to Call Visualizer module
 *
 * Some interactions between operators and visitors start as regular phone calls with no Glia engagement.
 * Screen sharing and video engagements are available for such calls via Call Visualizer.
 *
 * For more information, see the <a href="https://docs.glia.com/glia-mobile/docs/android-widgets-call-visualizer">Call Visualizer guide</a>.
 */
public interface CallVisualizer {
    /**
     * Creates a VisitorCodeView component that can be integrated into the client application
     *
     * The visitor code is generated on demand and is unique for every visitor on a particular site.
     * The first time this function is called for a visitor, the code is generated and returned.
     * For each subsequent call thereafter, the same code will be returned as long as the code has not
     * expired. During that time, the code can be used to initiate an engagement. Once the operator
     * has used the visitor code to initiate an engagement, the code will expire immediately.
     */
    VisitorCodeView createVisitorCodeView(Context context);

    /**
     * Shows the visitor code in a dialog box on top of the current activity within your application.
     *
     * This dialog will overlay on top of any other dialogs without dismissing them.
     *
     * Otherwise, it behaves the same way as {@link #createVisitorCodeView(Context)}.
     */
    void showVisitorCodeDialog(Context context);

    /**
     * Sets visitor context to the upcoming Call Visualizer engagement
     * @param visitorCodeContext
     */
    void addVisitorContext(String visitorCodeContext);

    /**
     * Sets callback that will be called when Call Visualizer engagement is started.
     *
     * Callback won't be triggered for engagement started before the callback has been set or the ongoing engagements.
     * Setting new callback will override the old one.
     *
     * @param runnable The Runnable that will be executed on engagement start
     */
    void onEngagementStart(Runnable runnable);

}
