package com.glia.widgets.view.dialog.confirmation

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.viewbinding.ViewBinding
import com.glia.widgets.databinding.ConfirmationDialogBinding
import com.glia.widgets.databinding.ConfirmationDialogVerticalBinding
import com.glia.widgets.view.button.GliaNegativeButton
import com.glia.widgets.view.button.GliaPositiveButton
import com.glia.widgets.view.dialog.base.DialogViewBinding
import com.google.android.material.button.MaterialButton

internal interface BaseConfirmationDialogViewBinding<T : ViewBinding> : DialogViewBinding<T> {
    val messageTv: TextView
    val logoContainer: View
    val poweredByTv: TextView
}

internal interface DefaultConfirmationDialogViewBinding<T : ViewBinding> : BaseConfirmationDialogViewBinding<T> {
    val link1Button: MaterialButton
    val link2Button: MaterialButton
    val positiveButton: GliaPositiveButton
    val negativeButton: GliaNegativeButton
    val additionalButtonsSpace: View
}

internal class ConfirmationDialogViewBinding(layoutInflater: LayoutInflater) : DefaultConfirmationDialogViewBinding<ConfirmationDialogBinding> {
    override val binding: ConfirmationDialogBinding = ConfirmationDialogBinding.inflate(layoutInflater)
    override val root: View
        get() = binding.root
    override val link1Button: MaterialButton
        get() = binding.link1Button
    override val link2Button: MaterialButton
        get() = binding.link2Button
    override val positiveButton: GliaPositiveButton
        get() = binding.acceptButton
    override val negativeButton: GliaNegativeButton
        get() = binding.declineButton
    override val additionalButtonsSpace: View
        get() = binding.additionalButtonsSpace
    override val logoContainer: View
        get() = binding.logoContainer
    override val poweredByTv: TextView
        get() = binding.poweredByText
    override val titleTv: TextView
        get() = binding.dialogTitleView
    override val messageTv: TextView
        get() = binding.dialogMessageView
}

internal class VerticalConfirmationDialogViewBinding(layoutInflater: LayoutInflater) :
    DefaultConfirmationDialogViewBinding<ConfirmationDialogVerticalBinding> {
    override val binding: ConfirmationDialogVerticalBinding = ConfirmationDialogVerticalBinding.inflate(layoutInflater)
    override val root: View
        get() = binding.root
    override val link1Button: MaterialButton
        get() = binding.link1Button
    override val link2Button: MaterialButton
        get() = binding.link2Button
    override val positiveButton: GliaPositiveButton
        get() = binding.acceptButton
    override val negativeButton: GliaNegativeButton
        get() = binding.declineButton
    override val additionalButtonsSpace: View
        get() = binding.additionalButtonsSpace
    override val logoContainer: View
        get() = binding.logoContainer
    override val poweredByTv: TextView
        get() = binding.poweredByText
    override val titleTv: TextView
        get() = binding.dialogTitleView
    override val messageTv: TextView
        get() = binding.dialogMessageView
}

internal interface DefaultReversedConfirmationDialogViewBinding<T : ViewBinding> : BaseConfirmationDialogViewBinding<T> {
    val positiveButton: GliaNegativeButton
    val negativeButton: GliaPositiveButton
}
