package com.glia.widgets.view.dialog.option

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.glia.widgets.databinding.SingleButtonOptionDialogBinding
import com.glia.widgets.view.button.GliaPositiveButton
import com.glia.widgets.view.dialog.base.DialogViewBinding

internal class SingleButtonOptionDialogViewBinding(layoutInflater: LayoutInflater) : BaseOptionDialogViewBinding<SingleButtonOptionDialogBinding> {
    override val binding: SingleButtonOptionDialogBinding = SingleButtonOptionDialogBinding.inflate(layoutInflater)
    override val root: View
        get() = binding.root
    val positiveButton: GliaPositiveButton
        get() = binding.acceptButton
    override val logoContainer: View
        get() = binding.logoContainer
    override val poweredByTv: TextView
        get() = binding.poweredByText
    override val titleTv: TextView
        get() = binding.dialogTitleView
    override val messageTv: TextView
        get() = binding.dialogMessageView
}
