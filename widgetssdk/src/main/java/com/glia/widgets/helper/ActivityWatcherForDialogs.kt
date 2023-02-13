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

class ActivityWatcherForDialogs(
    private val app: Application,
    private val callVisualizerController: CallVisualizerController
) : Application.ActivityLifecycleCallbacks {

    companion object {
        private val TAG = ActivityWatcherForDialogs::class.java.simpleName
    }

    private var dialogController: DialogController? = null
    private var dialogCallback: DialogController.Callback? = null
    private var alertDialog: AlertDialog? = null

    /**
     * Returns last activity that called [Activity.onResume], but didn't call [Activity.onPause] yet
     * @return Currently resumed activity.
     */
    @VisibleForTesting
    var resumedActivity: WeakReference<Activity?> = WeakReference(null)

    fun init(dialogController: DialogController) {
        app.registerActivityLifecycleCallbacks(this)
        this.dialogController = dialogController
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityDestroyed(activity: Activity) {}


    override fun onActivityResumed(activity: Activity) {
        resumedActivity = WeakReference(activity)
        addDialogCallback(resumedActivity.get())
    }

    override fun onActivityPaused(activity: Activity) {
        resumedActivity.clear()
        removeDialogCallback()
    }


    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    private fun addDialogCallback(resumedActivity: Activity?) {
        // There are separate dialog callbacks for incoming media requests on Call and Chat screens.
        if (callVisualizerController.isCallOrChatScreenActiveUseCase(resumedActivity)) return

        setupDialogCallback(resumedActivity)
        dialogController?.addCallback(dialogCallback)
    }

    private fun removeDialogCallback() {
        dialogController?.removeCallback(dialogCallback)
    }

    @VisibleForTesting
    fun setupDialogCallback(resumedActivity: Activity?) {
        dialogCallback = DialogController.Callback {
            when (it.mode) {
                Dialog.MODE_NONE -> dismissAlertDialog()
                Dialog.MODE_MEDIA_UPGRADE -> resumedActivity?.runOnUiThread {
                    showUpgradeDialog(resumedActivity, it as DialogState.MediaUpgrade)
                }
            }
        }
    }

    private fun showUpgradeDialog(
        resumedActivity: Activity,
        mediaUpgrade: DialogState.MediaUpgrade
    ) {
        Logger.d(TAG, "Show upgrade dialog")
        val builder = UiTheme.UiThemeBuilder()
        val theme = builder.build()
        val contextWithStyle = MaterialThemeOverlay.wrap(
            resumedActivity,
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

    private fun dismissAlertDialog() {
        Logger.d(TAG, "Dismiss alert dialog")
        alertDialog?.dismiss()
        alertDialog = null
    }
}
