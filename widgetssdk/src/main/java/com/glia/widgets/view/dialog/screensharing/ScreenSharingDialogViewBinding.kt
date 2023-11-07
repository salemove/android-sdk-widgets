package com.glia.widgets.view.dialog.screensharing

import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.viewbinding.ViewBinding
import com.glia.widgets.databinding.ScreensharingDialogBinding
import com.glia.widgets.databinding.ScreensharingDialogVerticalBinding
import com.glia.widgets.view.button.GliaNegativeButton
import com.glia.widgets.view.button.GliaPositiveButton
import com.glia.widgets.view.dialog.base.DialogViewBinding

internal interface BaseScreenSharingDialogViewBinding<T : ViewBinding> : DialogViewBinding<T> {
    val messageTv: TextView
    val positiveBtn: GliaPositiveButton
    val negativeBtn: GliaNegativeButton
    val logoContainer: View
    val poweredByTv: TextView
    val icon: ImageView
}

internal class ScreenSharingDialogViewBinding(layoutInflater: LayoutInflater) : BaseScreenSharingDialogViewBinding<ScreensharingDialogBinding> {
    override val binding: ScreensharingDialogBinding = ScreensharingDialogBinding.inflate(layoutInflater)
    override val root: View
        get() = binding.root
    override val icon: ImageView
        get() = binding.titleIcon
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
    override val messageTv: TextView
        get() = binding.dialogMessageView

}

internal class VerticalScreenSharingDialogViewBinding(layoutInflater: LayoutInflater) :
    BaseScreenSharingDialogViewBinding<ScreensharingDialogVerticalBinding> {
    override val binding: ScreensharingDialogVerticalBinding = ScreensharingDialogVerticalBinding.inflate(layoutInflater)
    override val root: View
        get() = binding.root
    override val icon: ImageView
        get() = binding.titleIcon
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
    override val messageTv: TextView
        get() = binding.dialogMessageView

}
