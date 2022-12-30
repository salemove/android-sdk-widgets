package com.glia.widgets.view

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.glia.widgets.GliaWidgets
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.core.dialog.model.DialogState.MediaUpgrade
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Utils
import com.glia.widgets.view.button.BaseConfigurableButton
import com.glia.widgets.view.button.GliaPositiveButton
import com.glia.widgets.view.unifiedui.exstensions.applyButtonTheme
import com.glia.widgets.view.unifiedui.exstensions.applyImageColorTheme
import com.glia.widgets.view.unifiedui.exstensions.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.alert.AlertTheme
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object Dialogs {
    private val alertTheme: AlertTheme?
        get() = Dependencies.getGliaThemeManager().theme?.alertTheme

    private fun BaseConfigurableButton.applyAlertTheme(theme: AlertTheme?) {
        if (this is GliaPositiveButton) {
            applyButtonTheme(theme?.positiveButton)
        } else {
            applyButtonTheme(theme?.negativeButton)
        }
    }

    private fun isUseVerticalAlignment(theme: UiTheme) =
        alertTheme?.isVerticalAxis ?: Utils.getGliaAlertDialogButtonUseVerticalAlignment(theme)

    private fun getOptionsAlertDialogLayout(
        theme: UiTheme, isButtonsColorsReversed: Boolean
    ): Int {
        val useVerticalAlignment = isUseVerticalAlignment(theme)

        return when {
            useVerticalAlignment && isButtonsColorsReversed -> R.layout.options_dialog_vertical_reversed
            useVerticalAlignment -> R.layout.options_dialog_vertical
            isButtonsColorsReversed -> R.layout.options_dialog_reversed
            else -> R.layout.options_dialog
        }
    }

    private fun getUpgradeDialogLayout(theme: UiTheme) =
        if (isUseVerticalAlignment(theme))
            R.layout.upgrade_dialog_vertical
        else
            R.layout.upgrade_dialog

    private fun getScreenSharingLayout(theme: UiTheme): Int =
        if (isUseVerticalAlignment(theme)) R.layout.screensharing_dialog_vertical else R.layout.screensharing_dialog

    private fun setDialogBackground(dialog: Dialog, @ColorRes tintRes: Int?) {
        val decorView = dialog.window?.decorView ?: return

        alertTheme?.backgroundColor?.primaryColorStateList?.let(decorView::setBackgroundTintList)
            ?: tintRes?.let { ContextCompat.getColorStateList(dialog.context, it) }?.also {
                decorView.backgroundTintList = it
            }
    }

    private fun showDialogBasedOnView(
        context: Context,
        theme: UiTheme,
        view: VisitorCodeView,
        cancelable: Boolean
    ): AlertDialog {
        return MaterialAlertDialogBuilder(context)
            .setView(view)
            .setCancelable(cancelable)
            .show()
            .also { setDialogBackground(it, theme.gliaChatBackgroundColor) }
    }

    private fun showDialog(
        context: Context,
        @LayoutRes layoutRes: Int,
        @ColorRes backgroundTint: Int? = null,
        cancelable: Boolean = false,
        onLayout: Dialog.() -> Unit
    ): AlertDialog = MaterialAlertDialogBuilder(context)
        //With setView(int layoutResId) GliaNegativeButton and GliaPositiveButton didn't get the right themes
        .setView(LayoutInflater.from(context).inflate(layoutRes, null))
        .setCancelable(cancelable)
        .setBackgroundInsetBottom(Dependencies.getResourceProvider().convertDpToIntPixel(24f))
        .show()
        .also { setDialogBackground(it, backgroundTint) }
        .also(onLayout)

    @JvmOverloads
    fun showOptionsDialog(
        context: Context,
        theme: UiTheme,
        title: String,
        message: String,
        positiveButtonText: String,
        negativeButtonText: String,
        positiveButtonClickListener: View.OnClickListener,
        negativeButtonClickListener: View.OnClickListener,
        cancelListener: DialogInterface.OnCancelListener,
        isButtonsColorsReversed: Boolean = false
    ): AlertDialog {
        return showDialog(
            context,
            getOptionsAlertDialogLayout(theme, isButtonsColorsReversed),
            theme.baseLightColor
        ) {
            setOnCancelListener(cancelListener)

            val baseDarkColor = theme.baseDarkColor?.let { ContextCompat.getColor(context, it) }
            val fontFamily = theme.fontRes?.let { ResourcesCompat.getFont(context, it) }

            findViewById<TextView>(R.id.dialog_title_view).apply {
                text = title
                baseDarkColor?.also(::setTextColor)
                fontFamily?.also(::setTypeface)
                alertTheme?.title.also(::applyTextTheme)
            }
            findViewById<TextView>(R.id.dialog_message_view).apply {
                text = message
                baseDarkColor?.also(::setTextColor)
                fontFamily?.also(::setTypeface)
                alertTheme?.message.also(::applyTextTheme)
            }
            findViewById<BaseConfigurableButton>(R.id.decline_button).apply {
                setTheme(theme)
                text = negativeButtonText
                fontFamily?.also(::setTypeface)
                setOnClickListener(negativeButtonClickListener)
                applyAlertTheme(alertTheme)
            }
            findViewById<BaseConfigurableButton>(R.id.accept_button).apply {
                setTheme(theme)
                text = positiveButtonText
                fontFamily?.also(::setTypeface)
                setOnClickListener(positiveButtonClickListener)
                applyAlertTheme(alertTheme)
            }
            findViewById<ImageView>(R.id.logo_view).apply {
                isVisible = theme.whiteLabel ?: false
                theme.baseShadeColor?.let { ContextCompat.getColorStateList(context, it) }
                    ?.also(::setImageTintList)
            }
        }
    }

    fun showAlertDialog(
        context: Context,
        theme: UiTheme,
        @StringRes title: Int,
        @StringRes message: Int,
        buttonClickListener: View.OnClickListener
    ): AlertDialog {
        val baseDarkColor = theme.baseDarkColor?.let { ContextCompat.getColor(context, it) }
        val baseNormalColorStateList =
            theme.baseNormalColor?.let { ContextCompat.getColorStateList(context, it) }

        val fontFamily = theme.fontRes?.let { ResourcesCompat.getFont(context, it) }

        return showDialog(context, R.layout.alert_dialog, theme.baseLightColor) {
            findViewById<TextView>(R.id.dialog_title_view).apply {
                setText(title)
                baseDarkColor?.also(::setTextColor)
                fontFamily?.also(::setTypeface)
                alertTheme?.title.also(::applyTextTheme)
            }
            findViewById<TextView>(R.id.dialog_message_view).apply {
                setText(message)
                baseDarkColor?.also(::setTextColor)
                fontFamily?.also(::setTypeface)
                alertTheme?.message.also(::applyTextTheme)
            }
            findViewById<ImageButton>(R.id.close_dialog_button).apply {
                baseNormalColorStateList?.also(::setImageTintList)
                setOnClickListener(buttonClickListener)
                alertTheme?.closeButtonColor.also(::applyImageColorTheme)
            }
        }
    }

    fun showOperatorEndedEngagementDialog(
        context: Context,
        theme: UiTheme,
        buttonClickListener: View.OnClickListener
    ): AlertDialog {
        val baseDarkColor = theme.baseDarkColor?.let { ContextCompat.getColor(context, it) }
        val fontFamily = theme.fontRes?.let { ResourcesCompat.getFont(context, it) }

        return showDialog(
            context,
            R.layout.operator_ended_engagement_dialog,
            theme.baseLightColor
        ) {
            findViewById<TextView>(R.id.dialog_title_view).apply {
                baseDarkColor?.also(::setTextColor)
                fontFamily?.also(::setTypeface)
                alertTheme?.title.also(::applyTextTheme)
            }
            findViewById<TextView>(R.id.dialog_message_view).apply {
                baseDarkColor?.also(::setTextColor)
                fontFamily?.also(::setTypeface)
                alertTheme?.message.also(::applyTextTheme)
            }
            findViewById<BaseConfigurableButton>(R.id.ok_button).apply {
                setTheme(theme)
                setOnClickListener(buttonClickListener)
                fontFamily?.also(::setTypeface)
                applyAlertTheme(alertTheme)
            }
        }
    }

    fun showUpgradeDialog(
        context: Context,
        theme: UiTheme,
        mediaUpgrade: MediaUpgrade,
        onAcceptOfferClickListener: View.OnClickListener,
        onCloseClickListener: View.OnClickListener
    ): AlertDialog {
        val baseDarkColor = theme.baseDarkColor?.let { ContextCompat.getColor(context, it) }
        val primaryBrandColorStateList =
            theme.brandPrimaryColor?.let { ContextCompat.getColorStateList(context, it) }

        val fontFamily = theme.fontRes?.let { ResourcesCompat.getFont(context, it) }

        return showDialog(context, getUpgradeDialogLayout(theme), theme.baseLightColor) {
            val titleIconView = findViewById<ImageView>(R.id.chat_title_icon).apply {
                primaryBrandColorStateList?.also(::setImageTintList)
                alertTheme?.titleImageColor.also(::applyImageColorTheme)
            }
            val titleView = findViewById<TextView>(R.id.dialog_title_view).apply {
                baseDarkColor?.also(::setTextColor)
                fontFamily?.also(::setTypeface)
                alertTheme?.title.also(::applyTextTheme)
            }
            findViewById<BaseConfigurableButton>(R.id.decline_button).apply {
                setTheme(theme)
                fontFamily?.also(::setTypeface)
                setOnClickListener(onCloseClickListener)
                applyAlertTheme(alertTheme)
            }
            findViewById<BaseConfigurableButton>(R.id.accept_button).apply {
                setTheme(theme)
                fontFamily?.also(::setTypeface)
                setOnClickListener(onAcceptOfferClickListener)
                applyAlertTheme(alertTheme)
            }
            findViewById<ImageView>(R.id.logo_view).apply {
                isVisible = theme.whiteLabel ?: false
                theme.baseShadeColor?.let { ContextCompat.getColorStateList(context, it) }
                    ?.also(::setImageTintList)
            }

            when (mediaUpgrade.mediaUpgradeMode) {
                MediaUpgrade.MODE_AUDIO -> {
                    titleView.text = context.getString(
                        R.string.glia_dialog_upgrade_audio_title,
                        mediaUpgrade.operatorName
                    )
                    titleIconView.setImageResource(theme.iconUpgradeAudioDialog!!)
                    titleIconView.contentDescription =
                        context.getString(R.string.glia_chat_audio_icon_content_description)
                }
                MediaUpgrade.MODE_VIDEO_ONE_WAY -> {
                    titleView.text = context.getString(
                        R.string.glia_dialog_upgrade_video_1_way_title,
                        mediaUpgrade.operatorName
                    )
                    titleIconView.setImageResource(theme.iconUpgradeVideoDialog ?: R.drawable.ic_baseline_videocam)
                }
                MediaUpgrade.MODE_VIDEO_TWO_WAY -> {
                    titleView.text = context.getString(
                        R.string.glia_dialog_upgrade_video_2_way_title,
                        mediaUpgrade.operatorName
                    )
                    titleIconView.setImageResource(theme.iconUpgradeVideoDialog ?: R.drawable.ic_baseline_videocam)
                    titleIconView.contentDescription =
                        context.getString(R.string.glia_chat_video_icon_content_description)
                }
            }
        }
    }

    fun showVisitorCodeDialog(
        context: Context,
        theme: UiTheme
    ): AlertDialog {
        val baseDarkColor = theme.baseDarkColor?.let { ContextCompat.getColor(context, it) }
        val fontFamily = theme.fontRes?.let { ResourcesCompat.getFont(context, it) }
        val view = GliaWidgets.getCallVisualizer().createVisitorCodeView(context).apply {
            setClosable(true)
        }
        return showDialogBasedOnView(
            context = context,
            view = view,
            theme = theme,
            cancelable = true
        ).apply {
            findViewById<TextView>(R.id.title_view).apply {
                baseDarkColor?.also {
                    this?.setTextColor(it)
                    this?.typeface = fontFamily
                }
            }
        }
    }

    fun showScreenSharingDialog(
        context: Context,
        theme: UiTheme,
        title: String,
        message: String,
        @StringRes positiveButtonText: Int,
        @StringRes negativeButtonText: Int,
        positiveButtonClickListener: View.OnClickListener,
        negativeButtonClickListener: View.OnClickListener
    ): AlertDialog {
        val baseDarkColor = theme.baseDarkColor?.let { ContextCompat.getColor(context, it) }
        val primaryBrandColorStateList =
            theme.brandPrimaryColor?.let { ContextCompat.getColorStateList(context, it) }

        val fontFamily = theme.fontRes?.let { ResourcesCompat.getFont(context, it) }

        return showDialog(context, getScreenSharingLayout(theme), theme.baseLightColor) {
            findViewById<ImageView>(R.id.title_icon).apply {
                primaryBrandColorStateList?.also(::setImageTintList)
                alertTheme?.titleImageColor.also(::applyImageColorTheme)
            }
            findViewById<TextView>(R.id.dialog_title_view).apply {
                text = title
                baseDarkColor?.also(::setTextColor)
                fontFamily?.also(::setTypeface)
                alertTheme?.title.also(::applyTextTheme)
            }
            findViewById<TextView>(R.id.dialog_message_view).apply {
                text = message
                baseDarkColor?.also(::setTextColor)
                fontFamily?.also(::setTypeface)
                alertTheme?.message.also(::applyTextTheme)
            }
            findViewById<BaseConfigurableButton>(R.id.decline_button).apply {
                setTheme(theme)
                setText(negativeButtonText)
                fontFamily?.also(::setTypeface)
                setOnClickListener {
                    dismiss()
                    negativeButtonClickListener.onClick(it)
                }
                applyAlertTheme(alertTheme)
            }
            findViewById<BaseConfigurableButton>(R.id.accept_button).apply {
                setTheme(theme)
                setText(positiveButtonText)
                fontFamily?.also(::setTypeface)
                setOnClickListener {
                    dismiss()
                    positiveButtonClickListener.onClick(it)
                }
                applyAlertTheme(alertTheme)
            }
            findViewById<ImageView>(R.id.logo_view).apply {
                isVisible = theme.whiteLabel ?: false
                theme.baseShadeColor?.let { ContextCompat.getColorStateList(context, it) }
                    ?.also(::setImageTintList)
            }
        }
    }

    fun showMessageCenterUnavailableDialog(
        context: Context,
        theme: UiTheme
    ): AlertDialog {
        val baseDarkColor = theme.baseDarkColor?.let { ContextCompat.getColor(context, it) }
        val fontFamily = theme.fontRes?.let { ResourcesCompat.getFont(context, it) }

        val alertDialog = showDialog(context, R.layout.alert_dialog, theme.baseLightColor, false) {
            findViewById<TextView>(R.id.dialog_title_view).apply {
                setText(R.string.glia_dialog_message_center_unavailable_title)
                baseDarkColor?.also(::setTextColor)
                fontFamily?.also(::setTypeface)
                alertTheme?.title.also(::applyTextTheme)
            }
            findViewById<TextView>(R.id.dialog_message_view).apply {
                setText(R.string.glia_dialog_message_center_unavailable_message)
                baseDarkColor?.also(::setTextColor)
                fontFamily?.also(::setTypeface)
                alertTheme?.message.also(::applyTextTheme)
            }
            findViewById<ImageButton>(R.id.close_dialog_button).apply {
                visibility = View.INVISIBLE
            }
        }

        val window = alertDialog.window
        window?.setGravity(Gravity.BOTTOM)
        enableOutsideTouch(window)

        return alertDialog
    }

    private fun enableOutsideTouch(window: Window?) {
        window?.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        )
        window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }
}
