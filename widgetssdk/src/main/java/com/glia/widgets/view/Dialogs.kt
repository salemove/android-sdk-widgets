package com.glia.widgets.view

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
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
            val baseLightColor = theme.baseLightColor?.let { ContextCompat.getColor(context, it) }
            val brandPrimaryColor = theme.brandPrimaryColor?.let { ContextCompat.getColor(context, it) }
            val baseShadeColor = theme.baseShadeColor?.let { ContextCompat.getColor(context, it) }
            val systemNegativeColor = theme.systemNegativeColor?.let { ContextCompat.getColor(context, it) }
            val fontFamily = theme.fontRes?.let { ResourcesCompat.getFont(context, it) }

            findViewById<TextView>(R.id.dialog_title_view).apply {
                text = title
                applyTextTheme(baseDarkColor, fontFamily)
                applyTextTheme(alertTheme?.title)
            }
            findViewById<TextView>(R.id.dialog_message_view).apply {
                text = message
                applyTextTheme(baseDarkColor, fontFamily)
                applyTextTheme(alertTheme?.message)
            }
            findViewById<BaseConfigurableButton>(R.id.decline_button).apply {
                text = negativeButtonText
                setOnClickListener(negativeButtonClickListener)
                applyButtonTheme(
                    backgroundColor = systemNegativeColor,
                    textColor = baseLightColor,
                    textFont = fontFamily
                )
                applyAlertTheme(alertTheme)
            }
            findViewById<BaseConfigurableButton>(R.id.accept_button).apply {
                text = positiveButtonText
                setOnClickListener(positiveButtonClickListener)
                applyButtonTheme(
                    backgroundColor = brandPrimaryColor,
                    textColor = baseLightColor,
                    textFont = fontFamily
                )
                applyAlertTheme(alertTheme)
            }
            findViewById<ImageView>(R.id.logo_view).apply {
                isVisible = theme.whiteLabel ?: false
                applyImageColorTheme(baseShadeColor)
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
        val baseNormalColor = theme.baseNormalColor?.let { ContextCompat.getColor(context, it) }

        val fontFamily = theme.fontRes?.let { ResourcesCompat.getFont(context, it) }

        return showDialog(context, R.layout.alert_dialog, theme.baseLightColor) {
            findViewById<TextView>(R.id.dialog_title_view).apply {
                setText(title)
                applyTextTheme(baseDarkColor, fontFamily)
                applyTextTheme(alertTheme?.title)
            }
            findViewById<TextView>(R.id.dialog_message_view).apply {
                setText(message)
                applyTextTheme(baseDarkColor, fontFamily)
                applyTextTheme(alertTheme?.message)
            }
            findViewById<ImageButton>(R.id.close_dialog_button).apply {
                setOnClickListener(buttonClickListener)
                applyImageColorTheme(baseNormalColor)
                applyImageColorTheme(alertTheme?.closeButtonColor)
            }
        }
    }

    fun showOperatorEndedEngagementDialog(
        context: Context,
        theme: UiTheme,
        buttonClickListener: View.OnClickListener
    ): AlertDialog {
        val baseDarkColor = theme.baseDarkColor?.let { ContextCompat.getColor(context, it) }
        val baseLightColor = theme.baseLightColor?.let { ContextCompat.getColor(context, it) }
        val brandPrimaryColor = theme.brandPrimaryColor?.let { ContextCompat.getColor(context, it) }
        val fontFamily = theme.fontRes?.let { ResourcesCompat.getFont(context, it) }

        return showDialog(
            context,
            R.layout.operator_ended_engagement_dialog,
            theme.baseLightColor
        ) {
            findViewById<TextView>(R.id.dialog_title_view).apply {
                applyTextTheme(baseDarkColor, fontFamily)
                applyTextTheme(alertTheme?.title)
            }
            findViewById<TextView>(R.id.dialog_message_view).apply {
                applyTextTheme(baseDarkColor, fontFamily)
                applyTextTheme(alertTheme?.message)

            }
            findViewById<BaseConfigurableButton>(R.id.ok_button).apply {
                setOnClickListener(buttonClickListener)
                applyButtonTheme(
                    backgroundColor = brandPrimaryColor,
                    textColor = baseLightColor,
                    textFont = fontFamily
                )
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
        val baseLightColor = theme.baseLightColor?.let { ContextCompat.getColor(context, it) }
        val baseDarkColor = theme.baseDarkColor?.let { ContextCompat.getColor(context, it) }
        val systemNegativeColor = theme.systemNegativeColor?.let { ContextCompat.getColor(context, it) }
        val primaryBrandColor = theme.brandPrimaryColor?.let { ContextCompat.getColor(context, it) }
        val baseShadeColor = theme.baseShadeColor?.let { ContextCompat.getColor(context, it) }
        val fontFamily = theme.fontRes?.let { ResourcesCompat.getFont(context, it) }

        return showDialog(context, getUpgradeDialogLayout(theme), theme.baseLightColor) {
            val titleIconView = findViewById<ImageView>(R.id.chat_title_icon).apply {
                applyImageColorTheme(primaryBrandColor)
                applyImageColorTheme(alertTheme?.titleImageColor)
            }
            val titleView = findViewById<TextView>(R.id.dialog_title_view).apply {
                applyTextTheme(baseDarkColor, fontFamily)
                applyTextTheme(alertTheme?.title)
            }
            findViewById<BaseConfigurableButton>(R.id.decline_button).apply {
                setOnClickListener(onCloseClickListener)
                applyButtonTheme(
                    backgroundColor = systemNegativeColor,
                    textColor = baseLightColor,
                    textFont = fontFamily
                )
                applyAlertTheme(alertTheme)
            }
            findViewById<BaseConfigurableButton>(R.id.accept_button).apply {
                setOnClickListener(onAcceptOfferClickListener)
                applyButtonTheme(
                    backgroundColor = primaryBrandColor,
                    textColor = baseLightColor,
                    textFont = fontFamily
                )
                applyAlertTheme(alertTheme)
            }
            findViewById<ImageView>(R.id.logo_view).apply {
                isVisible = theme.whiteLabel ?: false
                applyImageColorTheme(baseShadeColor)
            }

            when (mediaUpgrade.mediaUpgradeMode) {
                MediaUpgrade.MODE_AUDIO -> {
                    titleView.text = context.getString(
                        R.string.glia_dialog_upgrade_audio_title,
                        mediaUpgrade.operatorName
                    )
                    titleIconView.setImageResource(theme.iconUpgradeAudioDialog ?: R.drawable.ic_baseline_mic)
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
            findViewById<TextView>(R.id.title_view)?.apply {
                applyTextTheme(baseDarkColor, fontFamily)
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
        val baseLightColor = theme.baseLightColor?.let { ContextCompat.getColor(context, it) }
        val baseDarkColor = theme.baseDarkColor?.let { ContextCompat.getColor(context, it) }
        val systemNegativeColor = theme.systemNegativeColor?.let { ContextCompat.getColor(context, it) }
        val primaryBrandColor = theme.brandPrimaryColor?.let { ContextCompat.getColor(context, it) }
        val baseShadeColor = theme.baseShadeColor?.let { ContextCompat.getColor(context, it) }
        val fontFamily = theme.fontRes?.let { ResourcesCompat.getFont(context, it) }

        return showDialog(context, getScreenSharingLayout(theme), theme.baseLightColor) {
            findViewById<ImageView>(R.id.title_icon).apply {
                applyImageColorTheme(primaryBrandColor)
                applyImageColorTheme(alertTheme?.titleImageColor)
            }
            findViewById<TextView>(R.id.dialog_title_view).apply {
                text = title
                applyTextTheme(baseDarkColor, fontFamily)
                applyTextTheme(alertTheme?.title)
            }
            findViewById<TextView>(R.id.dialog_message_view).apply {
                text = message
                applyTextTheme(baseDarkColor, fontFamily)
                applyTextTheme(alertTheme?.message)
            }
            findViewById<BaseConfigurableButton>(R.id.decline_button).apply {
                setText(negativeButtonText)
                setOnClickListener {
                    dismiss()
                    negativeButtonClickListener.onClick(it)
                }
                applyButtonTheme(
                    backgroundColor = systemNegativeColor,
                    textColor = baseLightColor,
                    textFont = fontFamily
                )
                applyAlertTheme(alertTheme)
            }
            findViewById<BaseConfigurableButton>(R.id.accept_button).apply {
                setText(positiveButtonText)
                setOnClickListener {
                    dismiss()
                    positiveButtonClickListener.onClick(it)
                }
                applyButtonTheme(
                    backgroundColor = primaryBrandColor,
                    textColor = baseLightColor,
                    textFont = fontFamily
                )
                applyAlertTheme(alertTheme)
            }
            findViewById<ImageView>(R.id.logo_view).apply {
                isVisible = theme.whiteLabel ?: false
                applyImageColorTheme(baseShadeColor)
            }
        }
    }
}
