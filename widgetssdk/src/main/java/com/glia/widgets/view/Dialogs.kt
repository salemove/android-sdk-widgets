package com.glia.widgets.view

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.annotation.NonNull
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.glia.widgets.GliaWidgets
import com.glia.widgets.R
import com.glia.widgets.StringKey
import com.glia.widgets.StringKeyPair
import com.glia.widgets.StringProvider
import com.glia.widgets.UiTheme
import com.glia.widgets.callvisualizer.CallVisualizerSupportActivity
import com.glia.widgets.core.dialog.model.DialogState.MediaUpgrade
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.applyButtonTheme
import com.glia.widgets.helper.applyImageColorTheme
import com.glia.widgets.helper.applyTextTheme
import com.glia.widgets.helper.asActivity
import com.glia.widgets.helper.isAlertDialogButtonUseVerticalAlignment
import com.glia.widgets.view.button.BaseConfigurableButton
import com.glia.widgets.view.button.GliaPositiveButton
import com.glia.widgets.view.unifiedui.applyButtonTheme
import com.glia.widgets.view.unifiedui.applyImageColorTheme
import com.glia.widgets.view.unifiedui.applyTextColorTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.alert.AlertTheme
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object Dialogs {

    private val stringProvider: StringProvider = Dependencies.getStringProvider()
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
        alertTheme?.isVerticalAxis ?: theme.isAlertDialogButtonUseVerticalAlignment()

    private fun getOptionsAlertDialogLayout(
        theme: UiTheme,
        isButtonsColorsReversed: Boolean
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
        if (isUseVerticalAlignment(theme)) {
            R.layout.upgrade_dialog_vertical
        } else {
            R.layout.upgrade_dialog
        }

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
        horizontalInset: Int = 0,
        cancelable: Boolean = true
    ): AlertDialog {
        val dialog = MaterialAlertDialogBuilder(context)
            .setView(view)
            .setBackgroundInsetStart(horizontalInset)
            .setBackgroundInsetEnd(horizontalInset)
            .setCancelable(cancelable)
            .setOnCancelListener {
                Dependencies.getControllerFactory().dialogController.dismissVisitorCodeDialog()
                context.asActivity()?.let {
                    if (it is CallVisualizerSupportActivity) {
                        it.overridePendingTransition(0, 0)
                        it.finish()
                    }
                }
            }
            .show()
            .also { setDialogBackground(it, theme.gliaChatBackgroundColor) }
        return dialog
    }

    private fun showDialog(
        context: Context,
        @LayoutRes layoutRes: Int,
        @ColorRes backgroundTint: Int? = null,
        cancelable: Boolean = false,
        onShow: ((AlertDialog) -> Unit)? = null,
        onLayout: Dialog.() -> Unit
    ): AlertDialog {
        val verticalInset = context.resources.getDimensionPixelSize(R.dimen.glia_large_x_large)
        val alertDialog = MaterialAlertDialogBuilder(context)
            // With setView(int layoutResId) GliaNegativeButton and GliaPositiveButton didn't get the right themes
            .setView(LayoutInflater.from(context).inflate(layoutRes, null))
            .setCancelable(cancelable)
            .setBackgroundInsetBottom(verticalInset)
            .setBackgroundInsetTop(verticalInset)
            .create()

        onShow?.also { listener -> alertDialog.setOnShowListener { listener.invoke(alertDialog) } }

        alertDialog.show()

        return alertDialog.also {
            it.show()
            setDialogBackground(it, backgroundTint)
            onLayout(it)
        }
    }

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
                    backgroundColor = if (isButtonsColorsReversed) { brandPrimaryColor } else { systemNegativeColor },
                    textColor = baseLightColor,
                    textFont = fontFamily
                )
                applyAlertTheme(alertTheme)
            }
            findViewById<BaseConfigurableButton>(R.id.accept_button).apply {
                text = positiveButtonText
                setOnClickListener(positiveButtonClickListener)
                applyButtonTheme(
                    backgroundColor = if (isButtonsColorsReversed) { systemNegativeColor } else { brandPrimaryColor },
                    textColor = baseLightColor,
                    textFont = fontFamily
                )
                applyAlertTheme(alertTheme)
            }
            setupPoweredByGlia(this, theme.whiteLabel?.not() ?: true, baseShadeColor)
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
                text = stringProvider.getRemoteString(title)
                setText(title)
                applyTextTheme(baseDarkColor, fontFamily)
                applyTextTheme(alertTheme?.title)
            }
            findViewById<TextView>(R.id.dialog_message_view).apply {
                text = stringProvider.getRemoteString(message)
                applyTextTheme(baseDarkColor, fontFamily)
                applyTextTheme(alertTheme?.message)
            }
            findViewById<ImageButton>(R.id.close_dialog_button).apply {
                contentDescription = stringProvider.getRemoteString(R.string.general_close_accessibility)
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
                text = stringProvider.getRemoteString(R.string.engagement_ended_header)
                applyTextTheme(baseDarkColor, fontFamily)
                applyTextTheme(alertTheme?.title)
            }
            findViewById<TextView>(R.id.dialog_message_view).apply {
                text = stringProvider.getRemoteString(R.string.engagement_ended_message)
                applyTextTheme(baseDarkColor, fontFamily)
                applyTextTheme(alertTheme?.message)
            }
            findViewById<BaseConfigurableButton>(R.id.ok_button).apply {
                text = stringProvider.getRemoteString(R.string.general_ok)
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
                text = stringProvider.getRemoteString(R.string.general_decline)
                applyButtonTheme(
                    backgroundColor = systemNegativeColor,
                    textColor = baseLightColor,
                    textFont = fontFamily
                )
                applyAlertTheme(alertTheme)
            }
            findViewById<BaseConfigurableButton>(R.id.accept_button).apply {
                setOnClickListener(onAcceptOfferClickListener)
                text = stringProvider.getRemoteString(R.string.general_accept)
                applyButtonTheme(
                    backgroundColor = primaryBrandColor,
                    textColor = baseLightColor,
                    textFont = fontFamily
                )
                applyAlertTheme(alertTheme)
            }

            setupPoweredByGlia(this, theme.whiteLabel?.not() ?: true, baseShadeColor)


            when (mediaUpgrade.mediaUpgradeMode) {
                MediaUpgrade.MODE_AUDIO -> {
                    titleView.text = stringProvider.getRemoteString(R.string.media_upgrade_audio_title, StringKeyPair(StringKey.OPERATOR_NAME, mediaUpgrade.operatorName))
                    titleIconView.setImageResource(theme.iconUpgradeAudioDialog ?: R.drawable.ic_baseline_mic)
                }
                MediaUpgrade.MODE_VIDEO_ONE_WAY -> {
                    titleView.text = stringProvider.getRemoteString(R.string.media_upgrade_video_one_way_title, StringKeyPair(StringKey.OPERATOR_NAME, mediaUpgrade.operatorName))
                    titleIconView.setImageResource(theme.iconUpgradeVideoDialog ?: R.drawable.ic_baseline_videocam)
                }
                MediaUpgrade.MODE_VIDEO_TWO_WAY -> {
                    titleView.text = stringProvider.getRemoteString(R.string.media_upgrade_video_two_way_title, StringKeyPair(StringKey.OPERATOR_NAME, mediaUpgrade.operatorName))
                    titleIconView.setImageResource(theme.iconUpgradeVideoDialog ?: R.drawable.ic_baseline_videocam)
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
            theme = theme
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
                text = stringProvider.getRemoteString(negativeButtonText)
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
                text = stringProvider.getRemoteString(positiveButtonText)
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

            setupPoweredByGlia(this, theme.whiteLabel?.not() ?: true, baseShadeColor)
        }
    }

    fun showMessageCenterUnavailableDialog(
        context: Context,
        theme: UiTheme,
        onShow: ((AlertDialog) -> Unit)? = null
    ): AlertDialog {
        val baseDarkColor = theme.baseDarkColor?.let { ContextCompat.getColor(context, it) }
        val fontFamily = theme.fontRes?.let { ResourcesCompat.getFont(context, it) }

        return showDialog(context, R.layout.alert_dialog, theme.baseLightColor, false, onShow) {
            findViewById<TextView>(R.id.dialog_title_view).apply {
                text = stringProvider.getRemoteString(R.string.message_center_unavailable_title)
                baseDarkColor?.also(::setTextColor)
                fontFamily?.also(::setTypeface)
                alertTheme?.title.also(::applyTextTheme)
            }
            findViewById<TextView>(R.id.dialog_message_view).apply {
                text = stringProvider.getRemoteString(R.string.message_center_unavailable_message)
                baseDarkColor?.also(::setTextColor)
                fontFamily?.also(::setTypeface)
                alertTheme?.message.also(::applyTextTheme)
            }
            findViewById<ImageButton>(R.id.close_dialog_button).apply {
                isGone = true
            }
            window?.apply {
                setGravity(Gravity.BOTTOM)
                allowOutsideTouch()
            }
        }
    }

    private fun Window.allowOutsideTouch() {
        setFlags(
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        )
        clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }

    private fun setupPoweredByGlia(dialog: Dialog, @NonNull notWhitelabel: Boolean, baseShadeColor: Int?){
        dialog.findViewById<View>(R.id.logo_container)?.isVisible = notWhitelabel
        if (notWhitelabel) {
            dialog.findViewById<TextView>(R.id.powered_by_text)?.apply {
                text = stringProvider.getRemoteString(R.string.general_powered)
                baseShadeColor?.run {
                    applyTextColorTheme(ColorTheme(false, listOf(this)))
                }
            }
            dialog.findViewById<ImageView>(R.id.logo_view)?.applyImageColorTheme(baseShadeColor)
        }
    }
}
