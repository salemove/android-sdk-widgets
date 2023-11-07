package com.glia.widgets.view.dialog.update

import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.viewbinding.ViewBinding
import com.glia.widgets.databinding.UpgradeDialogBinding
import com.glia.widgets.databinding.UpgradeDialogVerticalBinding
import com.glia.widgets.view.button.GliaNegativeButton
import com.glia.widgets.view.button.GliaPositiveButton
import com.glia.widgets.view.dialog.base.DialogViewBinding

internal interface BaseUpgradeDialogViewBinding<T : ViewBinding> : DialogViewBinding<T> {
    val titleIcon: ImageView
    val positiveBtn: GliaPositiveButton
    val negativeBtn: GliaNegativeButton
    val logoContainer: View
    val poweredByTv: TextView
}

internal class UpgradeDialogViewBinding(layoutInflater: LayoutInflater) : BaseUpgradeDialogViewBinding<UpgradeDialogBinding> {
    override val binding: UpgradeDialogBinding = UpgradeDialogBinding.inflate(layoutInflater)
    override val root: View
        get() = binding.root
    override val titleIcon: ImageView
        get() = binding.chatTitleIcon
    override val positiveBtn: GliaPositiveButton
        get() = binding.acceptButton
    override val negativeBtn: GliaNegativeButton
        get() = binding.declineButton
    override val logoContainer: View
        get() = binding.logoContainer
    override val poweredByTv: TextView
        get() = binding.poweredByText
    override val titleTv: TextView
        get() = binding.dialogTitleView

}

internal class VerticalUpgradeDialogViewBinding(layoutInflater: LayoutInflater) : BaseUpgradeDialogViewBinding<UpgradeDialogVerticalBinding> {
    override val binding: UpgradeDialogVerticalBinding = UpgradeDialogVerticalBinding.inflate(layoutInflater)
    override val root: View
        get() = binding.root
    override val titleIcon: ImageView
        get() = binding.chatTitleIcon
    override val positiveBtn: GliaPositiveButton
        get() = binding.acceptButton
    override val negativeBtn: GliaNegativeButton
        get() = binding.declineButton
    override val logoContainer: View
        get() = binding.logoContainer
    override val poweredByTv: TextView
        get() = binding.poweredByText
    override val titleTv: TextView
        get() = binding.dialogTitleView

}
