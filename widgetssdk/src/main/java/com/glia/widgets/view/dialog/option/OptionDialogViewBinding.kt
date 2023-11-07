package com.glia.widgets.view.dialog.option

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.viewbinding.ViewBinding
import com.glia.widgets.databinding.OptionsDialogBinding
import com.glia.widgets.databinding.OptionsDialogReversedBinding
import com.glia.widgets.databinding.OptionsDialogVerticalBinding
import com.glia.widgets.databinding.OptionsDialogVerticalReversedBinding
import com.glia.widgets.view.button.GliaNegativeButton
import com.glia.widgets.view.button.GliaPositiveButton
import com.glia.widgets.view.dialog.base.DialogViewBinding

internal interface BaseOptionDialogViewBinding<T : ViewBinding> : DialogViewBinding<T> {
    val messageTv: TextView
    val logoContainer: View
    val poweredByTv: TextView
}

internal interface DefaultOptionDialogViewBinding<T : ViewBinding> : BaseOptionDialogViewBinding<T> {
    val positiveButton: GliaPositiveButton
    val negativeButton: GliaNegativeButton
}

internal class OptionDialogViewBinding(layoutInflater: LayoutInflater) : DefaultOptionDialogViewBinding<OptionsDialogBinding> {
    override val binding: OptionsDialogBinding = OptionsDialogBinding.inflate(layoutInflater)
    override val root: View
        get() = binding.root
    override val positiveButton: GliaPositiveButton
        get() = binding.acceptButton
    override val negativeButton: GliaNegativeButton
        get() = binding.declineButton
    override val logoContainer: View
        get() = binding.logoContainer
    override val poweredByTv: TextView
        get() = binding.poweredByText
    override val titleTv: TextView
        get() = binding.dialogTitleView
    override val messageTv: TextView
        get() = binding.dialogMessageView
}

internal class VerticalOptionDialogViewBinding(layoutInflater: LayoutInflater) : DefaultOptionDialogViewBinding<OptionsDialogVerticalBinding> {
    override val binding: OptionsDialogVerticalBinding = OptionsDialogVerticalBinding.inflate(layoutInflater)
    override val root: View
        get() = binding.root
    override val positiveButton: GliaPositiveButton
        get() = binding.acceptButton
    override val negativeButton: GliaNegativeButton
        get() = binding.declineButton
    override val logoContainer: View
        get() = binding.logoContainer
    override val poweredByTv: TextView
        get() = binding.poweredByText
    override val titleTv: TextView
        get() = binding.dialogTitleView
    override val messageTv: TextView
        get() = binding.dialogMessageView
}

internal interface DefaultReversedOptionDialogViewBinding<T : ViewBinding> : BaseOptionDialogViewBinding<T> {
    val positiveButton: GliaNegativeButton
    val negativeButton: GliaPositiveButton
}

internal class ReversedOptionDialogViewBinding(layoutInflater: LayoutInflater) :
    DefaultReversedOptionDialogViewBinding<OptionsDialogReversedBinding> {
    override val binding: OptionsDialogReversedBinding = OptionsDialogReversedBinding.inflate(layoutInflater)
    override val root: View
        get() = binding.root
    override val positiveButton: GliaNegativeButton
        get() = binding.acceptButton
    override val negativeButton: GliaPositiveButton
        get() = binding.declineButton
    override val logoContainer: View
        get() = binding.logoContainer
    override val poweredByTv: TextView
        get() = binding.poweredByText
    override val titleTv: TextView
        get() = binding.dialogTitleView
    override val messageTv: TextView
        get() = binding.dialogMessageView
}

internal class VerticalReversedOptionDialogViewBinding(layoutInflater: LayoutInflater) :
    DefaultReversedOptionDialogViewBinding<OptionsDialogVerticalReversedBinding> {
    override val binding: OptionsDialogVerticalReversedBinding = OptionsDialogVerticalReversedBinding.inflate(layoutInflater)
    override val root: View
        get() = binding.root
    override val positiveButton: GliaNegativeButton
        get() = binding.acceptButton
    override val negativeButton: GliaPositiveButton
        get() = binding.declineButton
    override val logoContainer: View
        get() = binding.logoContainer
    override val poweredByTv: TextView
        get() = binding.poweredByText
    override val titleTv: TextView
        get() = binding.dialogTitleView
    override val messageTv: TextView
        get() = binding.dialogMessageView
}
