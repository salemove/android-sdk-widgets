package com.glia.widgets.core.callvisualizer.domain;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

/**
 * Provides controls related to Call Visualizer module
 * <p>
 * Some interactions between operators and visitors start as regular phone calls with no Glia engagement.
 * Screen sharing and video engagements are available for such calls via Call Visualizer.
 * <p>
 * For more information, see the <a href="https://docs.glia.com/glia-mobile/docs/android-widgets-call-visualizer">Call Visualizer guide</a>.
 */
public interface CallVisualizer {
    /**
     * Creates a VisitorCodeView component that can be integrated into the client application
     * <p>
     * The visitor code is generated on demand and is unique for every visitor on a particular site.
     * The first time this function is called for a visitor, the code is generated and returned.
     * For each subsequent call thereafter, the same code will be returned as long as the code has not
     * expired. During that time, the code can be used to initiate an engagement. Once the operator
     * has used the visitor code to initiate an engagement, the code will expire immediately.
     */
    View createVisitorCodeView(@NonNull Context context);

    /**
     * Shows the visitor code in a dialog box on top of the current activity within your application.
     * <p>
     * This dialog will overlay on top of any other dialogs without dismissing them.
     * <p>
     * Otherwise, it behaves the same way as {@link #createVisitorCodeView(Context)}.
     *
     * @deprecated Use {@link #showVisitorCodeDialog()}
     */
    @Deprecated(since = "2.3.2")
    default void showVisitorCodeDialog(@NonNull Context context) {
        showVisitorCodeDialog();
    }

    /**
     * Shows the visitor code in a dialog box on top of the current activity within your application.
     * <p>
     * This dialog will overlay on top of any other dialogs without dismissing them.
     * <p>
     * Otherwise, it behaves the same way as {@link #createVisitorCodeView(Context)}.
     */
    void showVisitorCodeDialog();

    /**
     * Sets visitor context to the upcoming Call Visualizer engagement
     *
     * @param visitorContextAssetId is a visitor context asset ID
     */
    void addVisitorContext(@NonNull String visitorContextAssetId);

    /**
     * Sets callback that will be called when Call Visualizer engagement is started.
     * <p>
     * Callback won't be triggered for engagement started before the callback has been set or the ongoing engagements.
     * Setting new callback will override the old one.
     *
     * @param runnable The Runnable that will be executed on Call Visualizer engagement start
     */
    void onEngagementStart(@NonNull Runnable runnable);

    /**
     * Sets callback that will be called when Call Visualizer engagement is ended.
     * <p>
     * Callback won't be triggered for engagement ended before the callback has been set.
     * Setting new callback will override the old one.
     *
     * @param runnable The Runnable that will be executed on Call Visualizer engagement end
     */
    void onEngagementEnd(@NonNull Runnable runnable);

}
