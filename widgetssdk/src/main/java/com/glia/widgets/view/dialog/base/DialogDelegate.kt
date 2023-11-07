package com.glia.widgets.view.dialog.base

import androidx.appcompat.app.AlertDialog
import com.glia.widgets.core.dialog.model.DialogState

internal interface DialogDelegate {
    fun showDialogIfNoDialogPresent(showDialogCallback: () -> AlertDialog)
    fun forceShowDialog(showDialogCallback: () -> AlertDialog)
    fun updateDialogState(dialogState: DialogState): Boolean
    fun dismissAlertDialog()
    fun resetDialogStateAndDismiss()
}

internal class DialogDelegateImpl : DialogDelegate {
    private var alertDialog: AlertDialog? = null
    private var dialogState: DialogState? = null

    override fun showDialogIfNoDialogPresent(showDialogCallback: () -> AlertDialog) {
        if (alertDialog?.isShowing == true) return
        alertDialog = showDialogCallback()
    }

    override fun forceShowDialog(showDialogCallback: () -> AlertDialog) {
        dismissAlertDialog()
        alertDialog = showDialogCallback()
    }

    override fun updateDialogState(dialogState: DialogState): Boolean {
        if (this.dialogState?.mode == dialogState.mode) return false

        this.dialogState = dialogState

        return true
    }

    override fun dismissAlertDialog() {
        alertDialog?.dismiss()
        alertDialog = null
    }

    override fun resetDialogStateAndDismiss() {
        dialogState = null
        dismissAlertDialog()
    }
}
