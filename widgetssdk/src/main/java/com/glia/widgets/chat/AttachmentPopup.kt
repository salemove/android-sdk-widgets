package com.glia.widgets.chat

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import com.glia.widgets.R
import com.glia.widgets.databinding.ChatAttachmentPopupBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.setTintCompat
import com.glia.widgets.view.unifiedui.applyImageColorTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.chat.AttachmentsPopupTheme

internal class AttachmentPopup(anchor: View, private val theme: AttachmentsPopupTheme?) {

    private val margin by lazy { anchor.context.resources.getDimensionPixelSize(R.dimen.glia_chat_attachment_menu_margin) }
    private val binding: ChatAttachmentPopupBinding by lazy { bindLayout(anchor.context) }
    private val popupWindow: PopupWindow by lazy { createPopupMenu() }
    private val stringProvider = Dependencies.getStringProvider()

    private fun createPopupMenu(): PopupWindow {
        val popupView = binding.root
        return PopupWindow(
            popupView,
            popupView.measuredWidth,
            popupView.measuredHeight,
            true
        ).apply {
            animationStyle = androidx.appcompat.R.style.Animation_AppCompat_Dialog
            inputMethodMode = PopupWindow.INPUT_METHOD_NOT_NEEDED
        }
    }

    fun show(
        anchor: View,
        onGalleryClicked: () -> Unit,
        onTakePhotoClicked: () -> Unit,
        onBrowseClicked: () -> Unit
    ) {
        binding.photoLibraryTitle.text = stringProvider.getRemoteString(R.string.chat_attachment_photo_library)
        binding.photoLibraryItem.setOnClickListener {
            popupWindow.dismiss()
            onGalleryClicked()
        }
        binding.photoOrVideoTitle.text = stringProvider.getRemoteString(R.string.chat_attachment_take_photo)
        binding.photoOrVideoItem.setOnClickListener {
            popupWindow.dismiss()
            onTakePhotoClicked()
        }
        binding.browseTitle.text = stringProvider.getRemoteString(R.string.general_browse)
        binding.browseItem.setOnClickListener {
            popupWindow.dismiss()
            onBrowseClicked()
        }
        popupWindow.showAsDropDown(anchor, -margin, -margin, Gravity.END)
    }

    private fun bindLayout(context: Context): ChatAttachmentPopupBinding {
        val binding = ChatAttachmentPopupBinding.inflate(LayoutInflater.from(context)).apply {
            root.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        }

        val background = GradientDrawable().apply {
            cornerRadius =
                context.resources.getDimension(R.dimen.glia_chat_attachment_menu_corner_radius)
            setColor(binding.root.getColorCompat(R.color.glia_attachment_menu_bg))
        }

        if (theme != null) {
            theme.background?.also {
                if (it.isGradient) {
                    background.colors = it.valuesArray
                } else {
                    background.setColor(it.primaryColor)
                }
            }
            theme.dividerColor?.also {
                binding.rootLayout.dividerDrawable.setTintCompat(it.primaryColor)
            }

            theme.photoLibrary?.also { photoLibrary ->
                photoLibrary.text?.also(binding.photoLibraryTitle::applyTextTheme)
                photoLibrary.iconColor?.also(binding.photoLibraryIcon::applyImageColorTheme)
            }
            theme.browse?.also { browse ->
                browse.text?.also(binding.browseTitle::applyTextTheme)
                browse.iconColor?.also(binding.browseIcon::applyImageColorTheme)
            }
            theme.takePhoto?.also { takePhoto ->
                takePhoto.text?.also(binding.photoOrVideoTitle::applyTextTheme)
                takePhoto.iconColor?.also(binding.photoOrVideoIcon::applyImageColorTheme)
            }
        }
        binding.rootLayout.background = background
        return binding
    }
}
