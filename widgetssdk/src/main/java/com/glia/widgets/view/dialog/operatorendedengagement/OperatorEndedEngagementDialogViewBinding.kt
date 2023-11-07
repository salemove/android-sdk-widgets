package com.glia.widgets.view.dialog.operatorendedengagement

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.glia.widgets.databinding.OperatorEndedEngagementDialogBinding
import com.glia.widgets.view.button.GliaPositiveButton
import com.glia.widgets.view.dialog.base.DialogViewBinding

internal class OperatorEndedEngagementDialogViewBinding(layoutInflater: LayoutInflater) : DialogViewBinding<OperatorEndedEngagementDialogBinding> {
    override val binding: OperatorEndedEngagementDialogBinding = OperatorEndedEngagementDialogBinding.inflate(layoutInflater)
    override val root: View
        get() = binding.root
    override val titleTv: TextView
        get() = binding.dialogTitleView
    val messageTv: TextView
        get() = binding.dialogMessageView
    val button: GliaPositiveButton
        get() = binding.okButton
}
