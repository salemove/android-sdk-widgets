package com.glia.widgets.callvisualizer.controller

import android.app.Activity
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.callvisualizer.domain.IsCallOrChatScreenActiveUseCase
import com.glia.widgets.core.dialog.Dialog
import com.glia.widgets.core.dialog.DialogController
import com.glia.widgets.core.dialog.model.DialogState
import com.glia.widgets.helper.Logger
import com.glia.widgets.view.Dialogs
import com.google.android.material.theme.overlay.MaterialThemeOverlay

class CallVisualizerMediaUpgradeController(
    private val dialogController: DialogController,
    private val isCallOrChatScreenActiveUseCase: IsCallOrChatScreenActiveUseCase
) {

    private var dialogCallback: DialogController.Callback? = null
    private var alertDialog: AlertDialog? = null

    fun addDialogCallback(resumedActivity: Activity?) {
        // There are separate dialog callbacks for incoming media requests on Call and Chat screens.
        if (isCallOrChatScreenActiveUseCase(resumedActivity)) return

        setupDialogCallback(resumedActivity)
        dialogController.addCallback(dialogCallback)
    }

    fun removeDialogCallback() {
        dialogController.removeCallback(dialogCallback)
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
            dialogController.dismissCurrentDialog()
        }) {
            dialogController.dismissCurrentDialog()
        }
    }

    private fun dismissAlertDialog() {
        Logger.d(TAG, "Dismiss alert dialog")
        alertDialog?.dismiss()
        alertDialog = null
    }

    companion object {
        private val TAG = CallVisualizerMediaUpgradeController::class.java.simpleName
    }
}
