package com.glia.widgets.view.dialog.alert

import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.glia.widgets.databinding.AlertDialogBinding
import com.glia.widgets.view.dialog.base.DialogViewBinding

class AlertDialogViewBinding(layoutInflater: LayoutInflater) : DialogViewBinding<AlertDialogBinding> {
    override val binding: AlertDialogBinding = AlertDialogBinding.inflate(layoutInflater)
    override val root: View
        get() = binding.root
    override val titleTv: TextView
        get() = binding.dialogTitleView
    val messageTv: TextView
        get() = binding.dialogMessageView

    val closeBtn: ImageButton
        get() = binding.closeDialogButton
}
