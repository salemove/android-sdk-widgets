package com.glia.widgets.view.dialog.base

import androidx.appcompat.app.AlertDialog
import com.glia.widgets.internal.dialog.model.DialogState

internal interface DialogDelegate {
    fun showDialog(showDialogCallback: () -> AlertDialog)
    fun updateDialogState(dialogState: DialogState): Boolean
    fun resetDialogStateAndDismiss()
}

internal class DialogDelegateImpl : DialogDelegate {
    private var alertDialog: AlertDialog? = null
    private var dialogState: DialogState? = null

    override fun showDialog(showDialogCallback: () -> AlertDialog) {
        dismissAlertDialog()
        alertDialog = showDialogCallback()
    }

    override fun updateDialogState(dialogState: DialogState): Boolean {
        if (this.dialogState == dialogState) return false

        this.dialogState = dialogState

        return true
    }

    private fun dismissAlertDialog() {
        alertDialog?.dismiss()
        alertDialog = null
    }

    private fun resetDialogState() {
        dialogState = null
    }

    override fun resetDialogStateAndDismiss() {
        resetDialogState()
        dismissAlertDialog()
    }
}
