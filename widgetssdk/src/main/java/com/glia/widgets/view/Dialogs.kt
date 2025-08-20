package com.glia.widgets.view

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.glia.telemetry_lib.Attributes
import com.glia.telemetry_lib.ButtonNames
import com.glia.telemetry_lib.DialogNames
import com.glia.telemetry_lib.GliaLogger
import com.glia.telemetry_lib.LogEvents
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.di.Dependencies
import com.glia.widgets.engagement.domain.MediaUpgradeOfferData
import com.glia.widgets.helper.isOneWayVideo
import com.glia.widgets.helper.isTwoWayVideo
import com.glia.widgets.internal.dialog.model.ConfirmationDialogLinks
import com.glia.widgets.internal.dialog.model.Link
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
            buttonClickListener = handleDialogButtonClick(
                dialogName = DialogNames.QUEUE_IS_CLOSED,
                buttonName = ButtonNames.CLOSE,
                clickListener = buttonClickListener
            )
        )

        return dialogService.showDialog(context, uiTheme, DialogType.AlertDialog(payload), onShow = { dialog ->
            GliaLogger.i(LogEvents.DIALOG_SHOWN, null) {
                put(Attributes.DIALOG_NAME, DialogNames.QUEUE_IS_CLOSED)
            }
        }).apply {
            setOnDismissListener {
                GliaLogger.i(LogEvents.DIALOG_CLOSED, null) {
                    put(Attributes.DIALOG_NAME, DialogNames.QUEUE_IS_CLOSED)
                }
            }
        }
    }

    fun showUnexpectedErrorDialog(context: Context, uiTheme: UiTheme, buttonClickListener: View.OnClickListener): AlertDialog {
        val payload = DialogPayload.AlertDialog(
            title = LocaleString(R.string.error_general),
            message = LocaleString(R.string.engagement_queue_reconnection_failed),
            buttonVisible = true,
            buttonDescription = closeBtnAccessibility,
            buttonClickListener = handleDialogButtonClick(
                dialogName = DialogNames.UNEXPECTED_ERROR,
                buttonName = ButtonNames.CLOSE,
                clickListener = buttonClickListener
            )
        )
        return dialogService.showDialog(context, uiTheme, DialogType.AlertDialog(payload), onShow = { dialog ->
            GliaLogger.i(LogEvents.DIALOG_SHOWN, null) {
                put(Attributes.DIALOG_NAME, DialogNames.UNEXPECTED_ERROR)
            }
        }).apply {
            setOnDismissListener {
                GliaLogger.i(LogEvents.DIALOG_CLOSED, null) {
                    put(Attributes.DIALOG_NAME, DialogNames.UNEXPECTED_ERROR)
                }
            }
        }
    }

    fun showMissingPermissionsDialog(context: Context, uiTheme: UiTheme, buttonClickListener: View.OnClickListener): AlertDialog {
        val payload = DialogPayload.AlertDialog(
            title = LocaleString(R.string.android_permissions_title),
            message = LocaleString(R.string.android_permissions_message),
            buttonVisible = true,
            buttonDescription = closeBtnAccessibility,
            buttonClickListener = handleDialogButtonClick(
                dialogName = DialogNames.ADDITIONAL_PERMISSIONS_REQUEST,
                buttonName = ButtonNames.CLOSE,
                clickListener = buttonClickListener
            )
        )
        return dialogService.showDialog(context, uiTheme, DialogType.AlertDialog(payload), onShow = { dialog ->
            GliaLogger.i(LogEvents.DIALOG_SHOWN, null) {
                put(Attributes.DIALOG_NAME, DialogNames.ADDITIONAL_PERMISSIONS_REQUEST)
            }
        }).apply {
            setOnDismissListener {
                GliaLogger.i(LogEvents.DIALOG_CLOSED, null) {
                    put(Attributes.DIALOG_NAME, DialogNames.ADDITIONAL_PERMISSIONS_REQUEST)
                }
            }
        }
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
            positiveButtonClickListener = handleDialogButtonClick(
                dialogName = DialogNames.SCREEN_OVERLAY_PERMISSION_REQUEST,
                buttonName = ButtonNames.POSITIVE,
                clickListener = positiveButtonClickListener
            ),
            negativeButtonClickListener = handleDialogButtonClick(
                dialogName = DialogNames.SCREEN_OVERLAY_PERMISSION_REQUEST,
                buttonName = ButtonNames.NEGATIVE,
                clickListener = negativeButtonClickListener
            )
        )

        return dialogService.showDialog(context, uiTheme, DialogType.Option(payload), onShow = { dialog ->
            GliaLogger.i(LogEvents.DIALOG_SHOWN, null) {
                put(Attributes.DIALOG_NAME, DialogNames.SCREEN_OVERLAY_PERMISSION_REQUEST)
            }
        }).apply {
            setOnDismissListener {
                GliaLogger.i(LogEvents.DIALOG_CLOSED, null) {
                    put(Attributes.DIALOG_NAME, DialogNames.SCREEN_OVERLAY_PERMISSION_REQUEST)
                }
            }
        }
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
            positiveButtonClickListener = handleDialogButtonClick(
                dialogName = DialogNames.LEAVE_ENGAGEMENT_CONFIRMATION,
                buttonName = ButtonNames.POSITIVE,
                clickListener = positiveButtonClickListener
            ),
            negativeButtonClickListener = handleDialogButtonClick(
                dialogName = DialogNames.LEAVE_ENGAGEMENT_CONFIRMATION,
                buttonName = ButtonNames.NEGATIVE,
                clickListener = negativeButtonClickListener
            )
        )

        return dialogService.showDialog(context, uiTheme, DialogType.ReversedOption(payload), onShow = { dialog ->
            GliaLogger.i(LogEvents.DIALOG_SHOWN, null) {
                put(Attributes.DIALOG_NAME, DialogNames.LEAVE_ENGAGEMENT_CONFIRMATION)
            }
        }).apply {
            setOnDismissListener {
                GliaLogger.i(LogEvents.DIALOG_CLOSED, null) {
                    put(Attributes.DIALOG_NAME, DialogNames.LEAVE_ENGAGEMENT_CONFIRMATION)
                }
            }
        }
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
            positiveButtonClickListener = handleDialogButtonClick(
                dialogName = DialogNames.LEAVE_QUEUE_CONFIRMATION,
                buttonName = ButtonNames.POSITIVE,
                clickListener = positiveButtonClickListener
            ),
            negativeButtonClickListener = handleDialogButtonClick(
                dialogName = DialogNames.LEAVE_QUEUE_CONFIRMATION,
                buttonName = ButtonNames.NEGATIVE,
                clickListener = negativeButtonClickListener
            )

        )

        return dialogService.showDialog(context, uiTheme, DialogType.ReversedOption(payload), onShow = { dialog ->
            GliaLogger.i(LogEvents.DIALOG_SHOWN, null) {
                put(Attributes.DIALOG_NAME, DialogNames.LEAVE_QUEUE_CONFIRMATION)
            }
        }).apply {
            setOnDismissListener {
                GliaLogger.i(LogEvents.DIALOG_CLOSED, null) {
                    put(Attributes.DIALOG_NAME, DialogNames.LEAVE_QUEUE_CONFIRMATION)
                }
            }
        }
    }

    fun showUnAuthenticatedDialog(context: Context, uiTheme: UiTheme, buttonClickListener: View.OnClickListener): AlertDialog {
        val payload = DialogPayload.AlertDialog(
            title = LocaleString(R.string.message_center_unavailable_title),
            message = LocaleString(R.string.message_center_not_authenticated_message),
            buttonVisible = true,
            buttonDescription = closeBtnAccessibility,
            buttonClickListener = handleDialogButtonClick(
                dialogName = DialogNames.UNAUTHENTICATED_ERROR,
                buttonName = ButtonNames.CLOSE,
                clickListener = buttonClickListener
            )
        )
        return dialogService.showDialog(context, uiTheme, DialogType.AlertDialog(payload), onShow = { dialog ->
            GliaLogger.i(LogEvents.DIALOG_SHOWN, null) {
                put(Attributes.DIALOG_NAME, DialogNames.UNAUTHENTICATED_ERROR)
            }
        }).apply {
            setOnDismissListener {
                GliaLogger.i(LogEvents.DIALOG_CLOSED, null) {
                    put(Attributes.DIALOG_NAME, DialogNames.UNAUTHENTICATED_ERROR)
                }
            }
        }
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
            positiveButtonClickListener = handleDialogButtonClick(
                dialogName = DialogNames.LIVE_OBSERVATION_CONFIRMATION,
                buttonName = ButtonNames.POSITIVE,
                clickListener = positiveButtonClickListener
            ),
            negativeButtonClickListener = handleDialogButtonClick(
                dialogName = DialogNames.LIVE_OBSERVATION_CONFIRMATION,
                buttonName = ButtonNames.NEGATIVE,
                clickListener = negativeButtonClickListener
            ),
            link1ClickListener = handleDialogButtonClick(
                dialogName = DialogNames.LIVE_OBSERVATION_CONFIRMATION,
                buttonName = ButtonNames.LIVE_OBSERVATION_LINK_1,
                clickListener = { linkClickListener(links.link1) }
            ),
            link2ClickListener = handleDialogButtonClick(
                dialogName = DialogNames.LIVE_OBSERVATION_CONFIRMATION,
                buttonName = ButtonNames.LIVE_OBSERVATION_LINK_2,
                clickListener = { linkClickListener(links.link2) }
            )
        )

        return dialogService.showDialog(context, theme, DialogType.Confirmation(payload), onShow = { dialog ->
            GliaLogger.i(LogEvents.DIALOG_SHOWN, null) {
                put(Attributes.DIALOG_NAME, DialogNames.LIVE_OBSERVATION_CONFIRMATION)
            }
        }).apply {
            setOnDismissListener {
                GliaLogger.i(LogEvents.DIALOG_CLOSED, null) {
                    put(Attributes.DIALOG_NAME, DialogNames.LIVE_OBSERVATION_CONFIRMATION)
                }
            }
        }
    }

    fun showOperatorEndedEngagementDialog(context: Context, theme: UiTheme, buttonClickListener: View.OnClickListener): AlertDialog {
        val payload = DialogPayload.OperatorEndedEngagement(
            title = LocaleString(R.string.engagement_ended_header),
            message = LocaleString(R.string.engagement_ended_message),
            buttonText = ok,
            buttonClickListener = handleDialogButtonClick(
                dialogName = DialogNames.ENGAGEMENT_ENDED,
                buttonName = ButtonNames.OK,
                clickListener = buttonClickListener
            )
        )

        return dialogService.showDialog(context, theme, DialogType.OperatorEndedEngagement(payload), onShow = { dialog ->
            GliaLogger.i(LogEvents.DIALOG_SHOWN, null) {
                put(Attributes.DIALOG_NAME, DialogNames.ENGAGEMENT_ENDED)
            }
        }).apply {
            setOnDismissListener {
                GliaLogger.i(LogEvents.DIALOG_CLOSED, null) {
                    put(Attributes.DIALOG_NAME, DialogNames.ENGAGEMENT_ENDED)
                }
            }
        }
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
            positiveButtonClickListener = handleDialogButtonClick(
                dialogName = DialogNames.MEDIA_UPGRADE_CONFIRMATION,
                buttonName = ButtonNames.POSITIVE,
                clickListener = onAcceptOfferClickListener
            ),
            negativeButtonClickListener = handleDialogButtonClick(
                dialogName = DialogNames.MEDIA_UPGRADE_CONFIRMATION,
                buttonName = ButtonNames.NEGATIVE,
                clickListener = onCloseClickListener
            )
        )

        return dialogService.showDialog(context, theme, DialogType.Upgrade(payload), onShow = { dialog ->
            GliaLogger.i(LogEvents.DIALOG_SHOWN, null) {
                put(Attributes.DIALOG_NAME, DialogNames.MEDIA_UPGRADE_CONFIRMATION)
                data.offer.run {
                    val offer = when {
                        isOneWayVideo -> "ONE_WAY_VIDEO"
                        isTwoWayVideo -> "TWO_WAY_VIDEO"
                        else -> "AUDIO"
                    }
                    put(Attributes.MEDIA_UPGRADE_OFFER, offer)
                }
            }
        }).apply {
            setOnDismissListener {
                GliaLogger.i(LogEvents.DIALOG_CLOSED, null) {
                    put(Attributes.DIALOG_NAME, DialogNames.MEDIA_UPGRADE_CONFIRMATION)
                }
            }
        }
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

        return dialogService.showDialog(context, theme, DialogType.AlertDialog(payload), onShow = { dialog ->
            GliaLogger.i(LogEvents.DIALOG_SHOWN, null) {
                put(Attributes.DIALOG_NAME, DialogNames.SECURE_CONVERSATION_UNAVAILABLE_ERROR)
            }
        }) {
            window?.apply {
                setGravity(Gravity.BOTTOM)
                allowOutsideTouch()
            }
        }.apply {
            setOnDismissListener {
                GliaLogger.i(LogEvents.DIALOG_CLOSED, null) {
                    put(Attributes.DIALOG_NAME, DialogNames.SECURE_CONVERSATION_UNAVAILABLE_ERROR)
                }
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
            .setOnDismissListener {
                GliaLogger.i(LogEvents.DIALOG_CLOSED, null) {
                    put(Attributes.DIALOG_NAME, DialogNames.VISITOR_CODE)
                }
            }
            .show()
            .also {
                GliaLogger.i(LogEvents.DIALOG_CLOSED, null) {
                    put(Attributes.DIALOG_NAME, DialogNames.VISITOR_CODE)
                }
            }
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
            positiveButtonClickListener = handleDialogButtonClick(
                dialogName = DialogNames.LEAVE_SECURE_CONVERSATIONS_CONFIRMATION,
                buttonName = ButtonNames.POSITIVE,
                clickListener = onStay
            ),
            negativeButtonClickListener = handleDialogButtonClick(
                dialogName = DialogNames.LEAVE_SECURE_CONVERSATIONS_CONFIRMATION,
                buttonName = ButtonNames.NEGATIVE,
                clickListener = onLeave
            )
        )

        return dialogService.showDialog(context, theme, DialogType.OptionWithNegativeNeutral(payload), onShow = { dialog ->
            GliaLogger.i(LogEvents.DIALOG_SHOWN, null) {
                put(Attributes.DIALOG_NAME, DialogNames.LEAVE_SECURE_CONVERSATIONS_CONFIRMATION)
            }
        }).apply {
            setOnDismissListener {
                GliaLogger.i(LogEvents.DIALOG_CLOSED, null) {
                    put(Attributes.DIALOG_NAME, DialogNames.LEAVE_SECURE_CONVERSATIONS_CONFIRMATION)
                }
            }
        }
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
            positiveButtonText = LocaleString(R.string.push_notifications_alert_button_positive),
            negativeButtonText = LocaleString(R.string.push_notifications_alert_button_negative),
            poweredByText = poweredByText,
            positiveButtonClickListener = handleDialogButtonClick(
                dialogName = DialogNames.ALLOW_PUSH_NOTIFICATION,
                buttonName = ButtonNames.POSITIVE,
                clickListener = positiveButtonClickListener
            ),
            negativeButtonClickListener = handleDialogButtonClick(
                dialogName = DialogNames.ALLOW_PUSH_NOTIFICATION,
                buttonName = ButtonNames.NEGATIVE,
                clickListener = negativeButtonClickListener
            )
        )

        return dialogService.showDialog(context, uiTheme, DialogType.Option(payload), onShow = { dialog ->
            GliaLogger.i(LogEvents.DIALOG_SHOWN, null) {
                put(Attributes.DIALOG_NAME, DialogNames.ALLOW_PUSH_NOTIFICATION)
            }
        }).apply {
            setOnDismissListener {
                GliaLogger.i(LogEvents.DIALOG_CLOSED, null) {
                    put(Attributes.DIALOG_NAME, DialogNames.ALLOW_PUSH_NOTIFICATION)
                }
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

    private fun handleDialogButtonClick(
        dialogName: String,
        buttonName: String,
        clickListener: View.OnClickListener
    ): View.OnClickListener {
        return View.OnClickListener {
            GliaLogger.i(LogEvents.DIALOG_BUTTON_CLICKED, buttonName) {
                put(Attributes.DIALOG_NAME, dialogName)
                put(Attributes.BUTTON_NAME, buttonName)
            }
            clickListener.onClick(it)
        }
    }
}
