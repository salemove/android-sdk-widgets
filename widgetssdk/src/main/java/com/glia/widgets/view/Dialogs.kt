package com.glia.widgets.view

import android.content.Context
import android.content.DialogInterface
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.glia.widgets.GliaWidgets
import com.glia.widgets.R
import com.glia.widgets.StringKey
import com.glia.widgets.StringKeyPair
import com.glia.widgets.StringProvider
import com.glia.widgets.UiTheme
import com.glia.widgets.callvisualizer.CallVisualizerSupportActivity
import com.glia.widgets.core.dialog.model.Link
import com.glia.widgets.core.dialog.model.ConfirmationDialogLinks
import com.glia.widgets.core.dialog.model.DialogState.MediaUpgrade
import com.glia.widgets.core.dialog.model.DialogState.OperatorName
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.asActivity
import com.glia.widgets.view.dialog.base.DialogPayload
import com.glia.widgets.view.dialog.base.DialogService
import com.glia.widgets.view.dialog.base.DialogType
import com.google.android.material.dialog.MaterialAlertDialogBuilder

internal object Dialogs {
    private val dialogService: DialogService by lazy { DialogService(Dependencies.getGliaThemeManager().theme) }

    private val stringProvider: StringProvider by lazy { Dependencies.getStringProvider() }

    private val poweredByText: String by lazy { stringProvider.getRemoteString(R.string.general_powered) }
    private val closeBtnAccessibility: String by lazy { stringProvider.getRemoteString(R.string.general_close_accessibility) }
    private val yes: String by lazy { stringProvider.getRemoteString(R.string.general_yes) }
    private val ok: String by lazy { stringProvider.getRemoteString(R.string.general_ok) }
    private val no: String by lazy { stringProvider.getRemoteString(R.string.general_no) }
    private val allow: String by lazy { stringProvider.getRemoteString(R.string.general_allow) }
    private val cancel: String by lazy { stringProvider.getRemoteString(R.string.general_cancel) }
    private val accept: String by lazy { stringProvider.getRemoteString(R.string.general_accept) }
    private val decline: String by lazy { stringProvider.getRemoteString(R.string.general_decline) }

    fun showNoMoreOperatorsAvailableDialog(context: Context, uiTheme: UiTheme, buttonClickListener: View.OnClickListener): AlertDialog {
        val payload = DialogPayload.AlertDialog(
            title = stringProvider.getRemoteString(R.string.engagement_queue_closed_header),
            message = stringProvider.getRemoteString(R.string.engagement_queue_closed_message),
            buttonVisible = true,
            buttonDescription = closeBtnAccessibility,
            buttonClickListener = buttonClickListener
        )

        return dialogService.showDialog(context, uiTheme, DialogType.AlertDialog(payload))
    }

    fun showUnexpectedErrorDialog(context: Context, uiTheme: UiTheme, buttonClickListener: View.OnClickListener): AlertDialog {
        val payload = DialogPayload.AlertDialog(
            title = stringProvider.getRemoteString(R.string.error_general),
            message = stringProvider.getRemoteString(R.string.engagement_queue_reconnection_failed),
            buttonVisible = true,
            buttonDescription = closeBtnAccessibility,
            buttonClickListener = buttonClickListener
        )
        return dialogService.showDialog(context, uiTheme, DialogType.AlertDialog(payload))
    }

    fun showMissingPermissionsDialog(context: Context, uiTheme: UiTheme, buttonClickListener: View.OnClickListener): AlertDialog {
        val payload = DialogPayload.AlertDialog(
            title = stringProvider.getRemoteString(R.string.android_permissions_title),
            message = stringProvider.getRemoteString(R.string.android_permissions_message),
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
        negativeButtonClickListener: View.OnClickListener,
        onCancelListener: DialogInterface.OnCancelListener
    ): AlertDialog {
        val payload = DialogPayload.Option(
            title = stringProvider.getRemoteString(R.string.android_overlay_permission_title),
            message = stringProvider.getRemoteString(R.string.android_overlay_permission_message),
            positiveButtonText = ok,
            negativeButtonText = no,
            poweredByText = poweredByText,
            positiveButtonClickListener = positiveButtonClickListener,
            negativeButtonClickListener = negativeButtonClickListener

        )

        return dialogService.showDialog(context, uiTheme, DialogType.Option(payload)) {
            setOnCancelListener(onCancelListener)
        }
    }

    fun showEndEngagementDialog(
        context: Context,
        uiTheme: UiTheme,
        positiveButtonClickListener: View.OnClickListener,
        negativeButtonClickListener: View.OnClickListener,
        onCancelListener: DialogInterface.OnCancelListener
    ): AlertDialog {
        val payload = DialogPayload.Option(
            title = stringProvider.getRemoteString(R.string.engagement_end_confirmation_header),
            message = stringProvider.getRemoteString(R.string.engagement_end_message),
            positiveButtonText = yes,
            negativeButtonText = no,
            poweredByText = poweredByText,
            positiveButtonClickListener = positiveButtonClickListener,
            negativeButtonClickListener = negativeButtonClickListener
        )

        return dialogService.showDialog(context, uiTheme, DialogType.ReversedOption(payload)) {
            setOnCancelListener(onCancelListener)
        }
    }

    fun showExitQueueDialog(
        context: Context,
        uiTheme: UiTheme,
        positiveButtonClickListener: View.OnClickListener,
        negativeButtonClickListener: View.OnClickListener,
        onCancelListener: DialogInterface.OnCancelListener
    ): AlertDialog {
        val payload = DialogPayload.Option(
            title = stringProvider.getRemoteString(R.string.engagement_queue_leave_header),
            message = stringProvider.getRemoteString(R.string.engagement_queue_leave_message),
            positiveButtonText = yes,
            negativeButtonText = no,
            poweredByText = poweredByText,
            positiveButtonClickListener = positiveButtonClickListener,
            negativeButtonClickListener = negativeButtonClickListener

        )

        return dialogService.showDialog(context, uiTheme, DialogType.ReversedOption(payload)) {
            setOnCancelListener(onCancelListener)
        }
    }

    fun showAllowScreenSharingNotificationsAndStartSharingDialog(
        context: Context,
        uiTheme: UiTheme,
        positiveButtonClickListener: View.OnClickListener,
        negativeButtonClickListener: View.OnClickListener,
        onCancelListener: DialogInterface.OnCancelListener
    ): AlertDialog {
        val payload = DialogPayload.Option(
            title = stringProvider.getRemoteString(R.string.android_screen_sharing_offer_with_notifications_title),
            message = stringProvider.getRemoteString(R.string.android_screen_sharing_offer_with_notifications_message),
            positiveButtonText = yes,
            negativeButtonText = no,
            poweredByText = poweredByText,
            positiveButtonClickListener = positiveButtonClickListener,
            negativeButtonClickListener = negativeButtonClickListener
        )

        return dialogService.showDialog(context, uiTheme, DialogType.Option(payload)) {
            setOnCancelListener(onCancelListener)
        }
    }

    fun showAllowNotificationsDialog(
        context: Context,
        uiTheme: UiTheme,
        positiveButtonClickListener: View.OnClickListener,
        negativeButtonClickListener: View.OnClickListener,
        onCancelListener: DialogInterface.OnCancelListener
    ): AlertDialog {
        val payload = DialogPayload.Option(
            title = stringProvider.getRemoteString(R.string.android_notification_allow_notifications_title),
            message = stringProvider.getRemoteString(R.string.android_notification_allow_notifications_message),
            positiveButtonText = yes,
            negativeButtonText = no,
            poweredByText = poweredByText,
            positiveButtonClickListener = positiveButtonClickListener,
            negativeButtonClickListener = negativeButtonClickListener

        )

        return dialogService.showDialog(context, uiTheme, DialogType.Option(payload)) {
            setOnCancelListener(onCancelListener)
        }
    }

    fun showUnAuthenticatedDialog(context: Context, uiTheme: UiTheme, buttonClickListener: View.OnClickListener): AlertDialog {
        val payload = DialogPayload.AlertDialog(
            title = stringProvider.getRemoteString(R.string.message_center_unavailable_title),
            message = stringProvider.getRemoteString(R.string.message_center_not_authenticated_message),
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
            title = stringProvider.getRemoteString(R.string.engagement_confirm_title),
            message = stringProvider.getRemoteString(R.string.engagement_confirm_message),
            link1Text = links.link1?.title,
            link2Text = links.link2?.title,
            positiveButtonText = allow,
            negativeButtonText = cancel,
            poweredByText = poweredByText,
            positiveButtonClickListener = positiveButtonClickListener,
            negativeButtonClickListener = negativeButtonClickListener,
            link1ClickListener = { links.link1?.let { linkClickListener(it) } },
            link2ClickListener = { links.link2?.let { linkClickListener(it) } }
        )

        return dialogService.showDialog(context, theme, DialogType.Confirmation(payload))
    }

    fun showOperatorEndedEngagementDialog(context: Context, theme: UiTheme, buttonClickListener: View.OnClickListener): AlertDialog {
        val payload = DialogPayload.OperatorEndedEngagement(
            title = stringProvider.getRemoteString(R.string.engagement_ended_header),
            message = stringProvider.getRemoteString(R.string.engagement_ended_message),
            buttonText = ok,
            buttonClickListener = buttonClickListener
        )

        return dialogService.showDialog(context, theme, DialogType.OperatorEndedEngagement(payload))
    }

    fun showUpgradeDialog(
        context: Context,
        theme: UiTheme,
        mediaUpgrade: MediaUpgrade,
        onAcceptOfferClickListener: View.OnClickListener,
        onCloseClickListener: View.OnClickListener
    ): AlertDialog {
        val titleIconResPair = when (mediaUpgrade.mediaUpgradeMode) {
            MediaUpgrade.MODE_AUDIO -> R.string.media_upgrade_audio_title to (theme.iconUpgradeAudioDialog ?: R.drawable.ic_baseline_mic)
            MediaUpgrade.MODE_VIDEO_ONE_WAY -> R.string.media_upgrade_video_one_way_title to (theme.iconUpgradeVideoDialog
                ?: R.drawable.ic_baseline_videocam)

            else -> R.string.media_upgrade_video_two_way_title to (theme.iconUpgradeVideoDialog ?: R.drawable.ic_baseline_videocam)
        }

        val payload = DialogPayload.Upgrade(
            title = stringProvider.getRemoteString(titleIconResPair.first, StringKeyPair(StringKey.OPERATOR_NAME, mediaUpgrade.operatorName)),
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
        dialogState: OperatorName,
        positiveButtonClickListener: View.OnClickListener,
        negativeButtonClickListener: View.OnClickListener
    ): AlertDialog {
        val payload = DialogPayload.ScreenSharing(
            title = stringProvider.getRemoteString(R.string.alert_screen_sharing_start_header),
            message = stringProvider.getRemoteString(R.string.alert_screen_sharing_start_message, StringKeyPair(StringKey.OPERATOR_NAME, dialogState.operatorName)),
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
            title = stringProvider.getRemoteString(R.string.message_center_unavailable_title),
            message = stringProvider.getRemoteString(R.string.message_center_unavailable_message)
        )

        return dialogService.showDialog(context, theme, DialogType.AlertDialog(payload), onShow = onShow) {
            window?.apply {
                setGravity(Gravity.BOTTOM)
                allowOutsideTouch()
            }
        }
    }

    fun showAlreadyInMessagingDialog(
        context: Context,
        theme: UiTheme,
        positiveButtonClickListener: View.OnClickListener,
        negativeButtonClickListener: View.OnClickListener
    ): AlertDialog {
        val payload = DialogPayload.Confirmation(  // TODO: change to dedicated DialogType
            title = stringProvider.getRemoteString(R.string.alert_already_in_messaging_header),
            message = stringProvider.getRemoteString(R.string.alert_already_in_messaging_message),
            positiveButtonText = stringProvider.getRemoteString(R.string.alert_already_in_messaging_append_existing),
            negativeButtonText = stringProvider.getRemoteString(R.string.alert_already_in_messaging_start_new),
            poweredByText = poweredByText,
            positiveButtonClickListener = positiveButtonClickListener,
            negativeButtonClickListener = negativeButtonClickListener
        )

        return dialogService.showDialog(context, theme, DialogType.Confirmation(payload)) // TODO: change to dedicated DialogType
    }

    fun showVisitorCodeDialog(
        context: Context
    ): AlertDialog {
        val view = GliaWidgets.getCallVisualizer().createVisitorCodeView(context).apply {
            setClosable(true)
        }

        return MaterialAlertDialogBuilder(context)
            .setView(view)
            .setBackgroundInsetStart(0)
            .setBackgroundInsetEnd(0)
            .setCancelable(true)
            .setOnCancelListener {
                Dependencies.getControllerFactory().dialogController.dismissVisitorCodeDialog()
                context.asActivity().takeIf { it is CallVisualizerSupportActivity }?.apply {
                    overridePendingTransition(0, 0)
                    finish()
                }
            }.show()
    }

    private fun Window.allowOutsideTouch() {
        setFlags(
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        )
        clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }
}
