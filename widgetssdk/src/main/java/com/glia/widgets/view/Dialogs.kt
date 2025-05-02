package com.glia.widgets.view

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.internal.dialog.model.ConfirmationDialogLinks
import com.glia.widgets.internal.dialog.model.Link
import com.glia.widgets.di.Dependencies
import com.glia.widgets.engagement.domain.MediaUpgradeOfferData
import com.glia.widgets.helper.isOneWayVideo
import com.glia.widgets.helper.isTwoWayVideo
import com.glia.widgets.locale.LocaleProvider
import com.glia.widgets.locale.LocaleString
import com.glia.widgets.locale.StringKey
import com.glia.widgets.locale.StringKeyPair
import com.glia.widgets.view.dialog.base.DialogPayload
import com.glia.widgets.view.dialog.base.DialogService
import com.glia.widgets.view.dialog.base.DialogType
import com.google.android.material.dialog.MaterialAlertDialogBuilder

internal object Dialogs {
    private val dialogService: DialogService by lazy { DialogService(Dependencies.gliaThemeManager.theme) }

    private val localeProvider: LocaleProvider by lazy { Dependencies.localeProvider }

    private val poweredByText: LocaleString by lazy { LocaleString(R.string.general_powered) }
    private val closeBtnAccessibility: LocaleString by lazy { LocaleString(R.string.general_close_accessibility) }
    private val yes: LocaleString by lazy { LocaleString(R.string.general_yes) }
    private val ok: LocaleString by lazy { LocaleString(R.string.general_ok) }
    private val no: LocaleString by lazy { LocaleString(R.string.general_no) }
    private val allow: LocaleString by lazy { LocaleString(R.string.general_allow) }
    private val cancel: LocaleString by lazy { LocaleString(R.string.general_cancel) }
    private val accept: LocaleString by lazy { LocaleString(R.string.general_accept) }
    private val decline: LocaleString by lazy { LocaleString(R.string.general_decline) }
    private val backingOperatorName: Int by lazy { R.string.engagement_default_operator }

    fun showNoMoreOperatorsAvailableDialog(context: Context, uiTheme: UiTheme, buttonClickListener: View.OnClickListener): AlertDialog {
        val payload = DialogPayload.AlertDialog(
            title = LocaleString(R.string.engagement_queue_closed_header),
            message = LocaleString(R.string.engagement_queue_closed_message),
            buttonVisible = true,
            buttonDescription = closeBtnAccessibility,
            buttonClickListener = buttonClickListener
        )

        return dialogService.showDialog(context, uiTheme, DialogType.AlertDialog(payload))
    }

    fun showUnexpectedErrorDialog(context: Context, uiTheme: UiTheme, buttonClickListener: View.OnClickListener): AlertDialog {
        val payload = DialogPayload.AlertDialog(
            title = LocaleString(R.string.error_general),
            message = LocaleString(R.string.engagement_queue_reconnection_failed),
            buttonVisible = true,
            buttonDescription = closeBtnAccessibility,
            buttonClickListener = buttonClickListener
        )
        return dialogService.showDialog(context, uiTheme, DialogType.AlertDialog(payload))
    }

    fun showMissingPermissionsDialog(context: Context, uiTheme: UiTheme, buttonClickListener: View.OnClickListener): AlertDialog {
        val payload = DialogPayload.AlertDialog(
            title = LocaleString(R.string.android_permissions_title),
            message = LocaleString(R.string.android_permissions_message),
            buttonVisible = true,
            buttonDescription = closeBtnAccessibility,
            buttonClickListener = buttonClickListener
        )
        return dialogService.showDialog(context, uiTheme, DialogType.AlertDialog(payload))
    }

    fun showOverlayPermissionsDialog(
        context: Context,
        uiTheme: UiTheme,
        positiveButtonClickListener: View.OnClickListener,
        negativeButtonClickListener: View.OnClickListener
    ): AlertDialog {
        val payload = DialogPayload.Option(
            title = LocaleString(R.string.android_overlay_permission_title),
            message = LocaleString(R.string.android_overlay_permission_message),
            positiveButtonText = ok,
            negativeButtonText = no,
            poweredByText = poweredByText,
            positiveButtonClickListener = positiveButtonClickListener,
            negativeButtonClickListener = negativeButtonClickListener
        )

        return dialogService.showDialog(context, uiTheme, DialogType.Option(payload))
    }

    fun showEndEngagementDialog(
        context: Context,
        uiTheme: UiTheme,
        positiveButtonClickListener: View.OnClickListener,
        negativeButtonClickListener: View.OnClickListener
    ): AlertDialog {
        val payload = DialogPayload.Option(
            title = LocaleString(R.string.engagement_end_confirmation_header),
            message = LocaleString(R.string.engagement_end_message),
            positiveButtonText = yes,
            negativeButtonText = no,
            poweredByText = poweredByText,
            positiveButtonClickListener = positiveButtonClickListener,
            negativeButtonClickListener = negativeButtonClickListener
        )

        return dialogService.showDialog(context, uiTheme, DialogType.ReversedOption(payload))
    }

    fun showExitQueueDialog(
        context: Context,
        uiTheme: UiTheme,
        positiveButtonClickListener: View.OnClickListener,
        negativeButtonClickListener: View.OnClickListener
    ): AlertDialog {
        val payload = DialogPayload.Option(
            title = LocaleString(R.string.engagement_queue_leave_header),
            message = LocaleString(R.string.engagement_queue_leave_message),
            positiveButtonText = yes,
            negativeButtonText = no,
            poweredByText = poweredByText,
            positiveButtonClickListener = positiveButtonClickListener,
            negativeButtonClickListener = negativeButtonClickListener

        )

        return dialogService.showDialog(context, uiTheme, DialogType.ReversedOption(payload))
    }

    fun showUnAuthenticatedDialog(context: Context, uiTheme: UiTheme, buttonClickListener: View.OnClickListener): AlertDialog {
        val payload = DialogPayload.AlertDialog(
            title = LocaleString(R.string.message_center_unavailable_title),
            message = LocaleString(R.string.message_center_not_authenticated_message),
            buttonVisible = true,
            buttonDescription = closeBtnAccessibility,
            buttonClickListener = buttonClickListener
        )
        return dialogService.showDialog(context, uiTheme, DialogType.AlertDialog(payload))
    }

    fun showEngagementConfirmationDialog(
        context: Context,
        theme: UiTheme,
        links: ConfirmationDialogLinks,
        linkClickListener: (Link) -> Unit,
        positiveButtonClickListener: View.OnClickListener,
        negativeButtonClickListener: View.OnClickListener
    ): AlertDialog {
        val payload = DialogPayload.Confirmation(
            title = LocaleString(R.string.engagement_confirm_title),
            message = LocaleString(R.string.engagement_confirm_message),
            link1 = links.link1,
            link2 = links.link2,
            positiveButtonText = allow,
            negativeButtonText = cancel,
            poweredByText = poweredByText,
            positiveButtonClickListener = positiveButtonClickListener,
            negativeButtonClickListener = negativeButtonClickListener,
            link1ClickListener = { linkClickListener(links.link1) },
            link2ClickListener = { linkClickListener(links.link2) }
        )

        return dialogService.showDialog(context, theme, DialogType.Confirmation(payload))
    }

    fun showOperatorEndedEngagementDialog(context: Context, theme: UiTheme, buttonClickListener: View.OnClickListener): AlertDialog {
        val payload = DialogPayload.OperatorEndedEngagement(
            title = LocaleString(R.string.engagement_ended_header),
            message = LocaleString(R.string.engagement_ended_message),
            buttonText = ok,
            buttonClickListener = buttonClickListener
        )

        return dialogService.showDialog(context, theme, DialogType.OperatorEndedEngagement(payload))
    }

    fun showUpgradeDialog(
        context: Context,
        theme: UiTheme,
        data: MediaUpgradeOfferData,
        onAcceptOfferClickListener: View.OnClickListener,
        onCloseClickListener: View.OnClickListener
    ): AlertDialog {

        val titleIconResPair = data.offer.run {
            when {
                isOneWayVideo -> R.string.media_upgrade_video_one_way_title to (theme.iconUpgradeVideoDialog
                    ?: R.drawable.ic_baseline_videocam)

                isTwoWayVideo -> R.string.media_upgrade_video_two_way_title to (theme.iconUpgradeVideoDialog
                    ?: R.drawable.ic_baseline_videocam)

                else -> R.string.media_upgrade_audio_title to (theme.iconUpgradeAudioDialog ?: R.drawable.ic_baseline_mic)
            }
        }

        val payload = DialogPayload.Upgrade(
            title = LocaleString(titleIconResPair.first, StringKeyPair(StringKey.OPERATOR_NAME, data.operatorName)),
            positiveButtonText = accept,
            negativeButtonText = decline,
            poweredByText = poweredByText,
            iconRes = titleIconResPair.second,
            positiveButtonClickListener = onAcceptOfferClickListener,
            negativeButtonClickListener = onCloseClickListener
        )

        return dialogService.showDialog(context, theme, DialogType.Upgrade(payload))
    }

    fun showScreenSharingDialog(
        context: Context,
        theme: UiTheme,
        operatorName: String?,
        positiveButtonClickListener: View.OnClickListener,
        negativeButtonClickListener: View.OnClickListener
    ): AlertDialog {
        val payload = DialogPayload.ScreenSharing(
            title = LocaleString(R.string.alert_screen_sharing_start_header),
            message = LocaleString(
                R.string.alert_screen_sharing_start_message,
                StringKeyPair(StringKey.OPERATOR_NAME, (operatorName ?: localeProvider.getString(backingOperatorName)))
            ),
            positiveButtonText = accept,
            negativeButtonText = decline,
            poweredByText = poweredByText,
            positiveButtonClickListener = positiveButtonClickListener,
            negativeButtonClickListener = negativeButtonClickListener
        )

        return dialogService.showDialog(context, theme, DialogType.ScreenSharing(payload))
    }

    fun showMessageCenterUnavailableDialog(
        context: Context,
        theme: UiTheme,
        onShow: ((AlertDialog) -> Unit)? = null
    ): AlertDialog {
        val payload = DialogPayload.AlertDialog(
            title = LocaleString(R.string.message_center_unavailable_title),
            message = LocaleString(R.string.message_center_unavailable_message)
        )

        return dialogService.showDialog(context, theme, DialogType.AlertDialog(payload), onShow = onShow) {
            window?.apply {
                setGravity(Gravity.BOTTOM)
                allowOutsideTouch()
            }
        }
    }

    fun showVisitorCodeDialog(
        context: Context
    ): AlertDialog {
        val view = Dependencies.useCaseFactory.visitorCodeViewBuilderUseCase(context, true)

        return MaterialAlertDialogBuilder(context)
            .setView(view)
            .setBackgroundInsetStart(0)
            .setBackgroundInsetEnd(0)
            .setCancelable(true)
            .setOnCancelListener { Dependencies.controllerFactory.callVisualizerController.dismissVisitorCodeDialog() }
            .show()
    }

    fun showLeaveCurrentConversationDialog(
        context: Context,
        theme: UiTheme,
        onStay: View.OnClickListener,
        onLeave: View.OnClickListener
    ): AlertDialog {
        val payload = DialogPayload.Option(
            title = LocaleString(R.string.secure_messaging_chat_leave_current_conversation_title),
            message = LocaleString(R.string.secure_messaging_chat_leave_current_conversation_message),
            positiveButtonText = LocaleString(R.string.secure_messaging_chat_leave_current_conversation_button_positive),
            negativeButtonText = LocaleString(R.string.secure_messaging_chat_leave_current_conversation_button_negative),
            poweredByText = poweredByText,
            positiveButtonClickListener = onStay,
            negativeButtonClickListener = onLeave
        )

        return dialogService.showDialog(context, theme, DialogType.OptionWithNegativeNeutral(payload))
    }

    fun showPushNotificationsPermissionDialog(
        context: Context,
        uiTheme: UiTheme,
        positiveButtonClickListener: View.OnClickListener,
        negativeButtonClickListener: View.OnClickListener
    ): AlertDialog {
        val payload = DialogPayload.Option(
            title = LocaleString(R.string.push_notifications_alert_title),
            message = LocaleString(R.string.push_notifications_alert_message),
            positiveButtonText = allow,
            negativeButtonText = cancel,
            poweredByText = poweredByText,
            positiveButtonClickListener = positiveButtonClickListener,
            negativeButtonClickListener = negativeButtonClickListener
        )

        return dialogService.showDialog(context, uiTheme, DialogType.Option(payload))
    }

    private fun Window.allowOutsideTouch() {
        setFlags(
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        )
        clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }
}
