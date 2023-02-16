package com.glia.widgets.helper

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.callvisualizer.controller.CallVisualizerController
import com.glia.widgets.core.dialog.Dialog
import com.glia.widgets.core.dialog.DialogController
import com.glia.widgets.core.dialog.model.DialogState
import com.glia.widgets.view.Dialogs
import com.google.android.material.theme.overlay.MaterialThemeOverlay
import java.lang.ref.WeakReference

internal class ActivityWatcherForDialogs(
    private val app: Application,
    private val controller: CallVisualizerController
) : Application.ActivityLifecycleCallbacks {

    companion object {
        private val TAG = ActivityWatcherForDialogs::class.java.simpleName
    }

    private var dialogController: DialogController? = null

    @VisibleForTesting
    var dialogCallback: DialogController.Callback? = null

    @VisibleForTesting
    var alertDialog: AlertDialog? = null

    /**
     * Returns last activity that called [Activity.onResume], but didn't call [Activity.onPause] yet
     * @return Currently resumed activity.
     */
    private var _resumedActivity: WeakReference<Activity> = WeakReference(null)

    @VisibleForTesting
    val resumedActivity: Activity? get() = _resumedActivity.get()


    fun init(dialogController: DialogController) {
        app.registerActivityLifecycleCallbacks(this)
        this.dialogController = dialogController
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityDestroyed(activity: Activity) {}


    override fun onActivityResumed(activity: Activity) {
        _resumedActivity = WeakReference(activity)
        addDialogCallback()
    }

    override fun onActivityPaused(activity: Activity) {
        _resumedActivity.clear()
        removeDialogCallback()
    }


    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    private fun addDialogCallback() {
        // There are separate dialog callbacks for incoming media requests on Call and Chat screens.
        if (controller.isGliaActivity(resumedActivity)) return

        setupDialogCallback()
        dialogController?.addCallback(dialogCallback)
    }

    private fun removeDialogCallback() {
        if (controller.isGliaActivity(resumedActivity)) return

        dialogController?.removeCallback(dialogCallback)
    }

    @VisibleForTesting
    fun setupDialogCallback() {
        val activity = resumedActivity ?: return

        dialogCallback = DialogController.Callback {
            when (it.mode) {
                Dialog.MODE_NONE -> dismissAlertDialog()
                Dialog.MODE_MEDIA_UPGRADE -> activity.runOnUiThread {
                    showUpgradeDialog(it as DialogState.MediaUpgrade)
                }
                else -> Logger.d(TAG, "Unexpected dialog mode received")
            }
        }
    }

    private fun showUpgradeDialog(mediaUpgrade: DialogState.MediaUpgrade) {
        val activity = resumedActivity ?: return

        Logger.d(TAG, "Show upgrade dialog")
        val builder = UiTheme.UiThemeBuilder()
        val theme = builder.build()
        val contextWithStyle = MaterialThemeOverlay.wrap(
            activity,
            null,
            R.attr.gliaChatStyle,
            R.style.Application_Glia_Chat
        )

        alertDialog = Dialogs.showUpgradeDialog(contextWithStyle, theme, mediaUpgrade, {
            dialogController?.dismissCurrentDialog()
        }) {
            dialogController?.dismissCurrentDialog()
        }
    }

    @VisibleForTesting
    fun dismissAlertDialog() {
        Logger.d(TAG, "Dismiss alert dialog")
        alertDialog?.dismiss()
        alertDialog = null
    }
}
