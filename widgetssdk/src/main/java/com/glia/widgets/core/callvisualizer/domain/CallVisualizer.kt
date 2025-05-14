package com.glia.widgets.core.callvisualizer.domain

import android.content.Context
import android.view.View

/**
 * Provides controls related to Call Visualizer module
 *
 * Some interactions between operators and visitors start as regular phone calls with no Glia engagement.
 * Screen sharing and video engagements are available for such calls via Call Visualizer.
 *
 * For more information, see the [Call Visualizer guide](https://docs.glia.com/glia-mobile/docs/android-widgets-call-visualizer).
 */
interface CallVisualizer {
    /**
     * Creates a VisitorCodeView component that can be integrated into the client application
     *
     * The visitor code is generated on demand and is unique for every visitor on a particular site.
     * The first time this function is called for a visitor, the code is generated and returned.
     * For each subsequent call thereafter, the same code will be returned as long as the code has not
     * expired. During that time, the code can be used to initiate an engagement. Once the operator
     * has used the visitor code to initiate an engagement, the code will expire immediately.
     */
    fun createVisitorCodeView(context: Context): View?

    /**
     * Shows the visitor code in a dialog box on top of the current activity within your application.
     *
     * This dialog will overlay on top of any other dialogs without dismissing them.
     *
     * Otherwise, it behaves the same way as [.createVisitorCodeView].
     *
     */
    @Deprecated("Use {@link #showVisitorCodeDialog()}", ReplaceWith("showVisitorCodeDialog()"))
    fun showVisitorCodeDialog(context: Context) {
        showVisitorCodeDialog()
    }

    /**
     * Shows the visitor code in a dialog box on top of the current activity within your application.
     *
     * This dialog will overlay on top of any other dialogs without dismissing them.
     *
     * Otherwise, it behaves the same way as [createVisitorCodeView].
     */
    fun showVisitorCodeDialog()

    /**
     * Sets visitor context to the upcoming Call Visualizer engagement
     *
     * @param visitorContextAssetId is a visitor context asset ID
     */
    fun addVisitorContext(visitorContextAssetId: String)

    /**
     * Sets callback that will be called when Call Visualizer engagement is started.
     *
     * Callback won't be triggered for engagement started before the callback has been set or the ongoing engagements.
     * Setting new callback will override the old one.
     *
     * @param runnable The Runnable that will be executed on Call Visualizer engagement start
     */
    fun onEngagementStart(runnable: Runnable)

    /**
     * Sets callback that will be called when Call Visualizer engagement is ended.
     *
     * Callback won't be triggered for engagement ended before the callback has been set.
     * Setting new callback will override the old one.
     *
     * @param runnable The Runnable that will be executed on Call Visualizer engagement end
     */
    fun onEngagementEnd(runnable: Runnable)
}
