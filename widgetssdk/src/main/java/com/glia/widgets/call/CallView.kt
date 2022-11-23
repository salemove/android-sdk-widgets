package com.glia.widgets.call

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.content.res.TypedArray
import android.media.AudioManager
import android.net.Uri
import android.provider.Settings
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.getSystemService
import androidx.core.content.withStyledAttributes
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.comms.MediaState
import com.glia.androidsdk.comms.VideoView
import com.glia.androidsdk.engagement.Survey
import com.glia.androidsdk.screensharing.ScreenSharing
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.UiTheme.UiThemeBuilder
import com.glia.widgets.core.configuration.GliaSdkConfiguration
import com.glia.widgets.core.dialog.Dialog
import com.glia.widgets.core.dialog.DialogController
import com.glia.widgets.core.dialog.model.DialogState.MediaUpgrade
import com.glia.widgets.core.dialog.model.DialogState.OperatorName
import com.glia.widgets.core.notification.device.NotificationManager
import com.glia.widgets.core.screensharing.ScreenSharingController
import com.glia.widgets.databinding.CallButtonsLayoutBinding
import com.glia.widgets.databinding.CallViewBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.Utils
import com.glia.widgets.view.Dialogs
import com.glia.widgets.view.OperatorStatusView
import com.glia.widgets.view.floatingvisitorvideoview.FloatingVisitorVideoContainer
import com.glia.widgets.view.head.controller.ServiceChatHeadController
import com.glia.widgets.view.header.AppBarView
import com.glia.widgets.view.unifiedui.exstensions.getColorCompat
import com.glia.widgets.view.unifiedui.exstensions.getColorStateListCompat
import com.glia.widgets.view.unifiedui.exstensions.getFontCompat
import com.glia.widgets.view.unifiedui.theme.call.CallTheme
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.theme.overlay.MaterialThemeOverlay
import com.google.android.material.transition.MaterialFade
import com.google.android.material.transition.SlideDistanceProvider
import kotlin.properties.Delegates

internal class CallView(
    context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int
) : ConstraintLayout(
    MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, defStyleRes),
    attrs,
    defStyleAttr,
    defStyleRes
), CallViewCallback {
    private val tag = CallView::class.java.simpleName

    private val callTheme: CallTheme? by lazy {
        Dependencies.getGliaThemeManager().theme?.callTheme
    }

    private val audioManager: AudioManager by lazy { context.getSystemService()!! }
    private val screenSharingViewCallback = ScreenSharingController.ViewCallback {
        showToast(it.debugMessage)
    }

    private val binding: CallViewBinding by lazy {
        CallViewBinding.inflate(LayoutInflater.from(this.context), this)
    }
    private val callButtonsLayoutBinding: CallButtonsLayoutBinding get() = binding.buttonsLayout

    private val appBar: AppBarView get() = binding.topAppBar
    private val operatorStatusView: OperatorStatusView get() = binding.operatorStatusView
    private val operatorNameView: TextView get() = binding.operatorNameView
    private val companyNameView: TextView get() = binding.companyNameView
    private val msrView: TextView get() = binding.msrView
    private val callTimerView: TextView get() = binding.callTimerView
    private val connectingView: TextView get() = binding.connectingView
    private val continueBrowsingView: TextView get() = binding.continueBrowsingView
    private val operatorVideoContainer: FrameLayout get() = binding.operatorVideoContainer
    private val chatButtonLabel: TextView get() = callButtonsLayoutBinding.chatButtonLabel
    private val videoButtonLabel: TextView get() = callButtonsLayoutBinding.videoButtonLabel
    private val muteButtonLabel: TextView get() = callButtonsLayoutBinding.muteButtonLabel
    private val speakerButtonLabel: TextView get() = callButtonsLayoutBinding.speakerButtonLabel
    private val minimizeButtonLabel: TextView get() = callButtonsLayoutBinding.minimizeButtonLabel
    private val videoButton: FloatingActionButton get() = callButtonsLayoutBinding.videoButton
    private val muteButton: FloatingActionButton get() = callButtonsLayoutBinding.muteButton
    private val speakerButton: FloatingActionButton get() = callButtonsLayoutBinding.speakerButton
    private val minimizeButton: FloatingActionButton get() = callButtonsLayoutBinding.minimizeButton
    private val chatButton: FloatingActionButton get() = callButtonsLayoutBinding.chatButton
    private val chatButtonBadgeView: TextView get() = callButtonsLayoutBinding.chatButtonBadge
    private val buttonsLayoutBackground: View get() = binding.buttonsLayoutBg
    private val buttonsLayout: View get() = callButtonsLayoutBinding.root
    private val onHoldTextView: TextView get() = binding.onHoldText
    private val floatingVisitorVideoContainer: FloatingVisitorVideoContainer get() = binding.floatingVisitorVideo

    private var theme: UiTheme by Delegates.notNull()

    private var callController: CallController? = null
    private var screenSharingController: ScreenSharingController? = null
    private var serviceChatHeadController: ServiceChatHeadController? = null
    private var dialogCallback: DialogController.Callback? = null
    private var dialogController: DialogController? = null

    private var onBackClickedListener: OnBackClickedListener? = null
    private var onEndListener: OnEndListener? = null
    private var onMinimizeListener: OnMinimizeListener? = null
    private var onNavigateToChatListener: OnNavigateToChatListener? = null
    private var onNavigateToSurveyListener: OnNavigateToSurveyListener? = null
    private var onTitleUpdatedListener: OnTitleUpdatedListener? = null
    private var defaultStatusbarColor: Int? = null

    private var operatorVideoView: VideoView? = null
    private var alertDialog: AlertDialog? = null

    @JvmOverloads
    constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.gliaChatStyle
    ) : this(context, attrs, defStyleAttr, R.style.Application_Glia_Chat)

    init {
        initConfigurations()
        readTypedArray(attrs, defStyleAttr, defStyleRes)
        setupViewAppearance()
        setupViewActions()
        setupControllers()
    }

    private fun setupViewActions() {
        appBar.setOnBackClickedListener {
            callController?.onBackClicked()
            onBackClickedListener?.onBackClicked()
        }
        appBar.setOnEndChatClickedListener { callController?.leaveChatClicked() }
        appBar.setOnXClickedListener { callController?.leaveChatQueueClicked() }
        chatButton.setOnClickListener { callController?.chatButtonClicked() }
        speakerButton.setOnClickListener { callController?.onSpeakerButtonPressed() }
        minimizeButton.setOnClickListener { callController?.minimizeButtonClicked() }
        muteButton.setOnClickListener { callController?.muteButtonClicked() }
        videoButton.setOnClickListener { callController?.videoButtonClicked() }
    }

    fun startCall(
        companyName: String?,
        queueId: String?,
        visitorContextAssetId: String?,
        useOverlays: Boolean,
        screenSharingMode: ScreenSharing.Mode?,
        mediaType: Engagement.MediaType?
    ) {
        Dependencies.getSdkConfigurationManager().isUseOverlay = useOverlays
        Dependencies.getSdkConfigurationManager().screenSharingMode = screenSharingMode
        callController?.initCall(companyName, queueId, visitorContextAssetId, mediaType)
        serviceChatHeadController?.init()
    }

    fun onDestroy(isFinishing: Boolean) {
        releaseOperatorVideoStream()
        dismissAlertDialog()
        onEndListener = null
        onBackClickedListener = null
        onNavigateToChatListener = null
        onNavigateToSurveyListener = null
        destroyControllers(isFinishing)
    }

    fun onResume() {
        floatingVisitorVideoContainer.onResume()
        callController?.onResume()
        operatorVideoView?.resumeRendering()
        screenSharingController?.setViewCallback(screenSharingViewCallback)
        screenSharingController?.onResume(this.context)
        serviceChatHeadController?.onResume(this)
        dialogController?.addCallback(dialogCallback)
    }

    fun onPause() {
        floatingVisitorVideoContainer.onPause()
        operatorVideoView?.pauseRendering()
        screenSharingController?.removeViewCallback(screenSharingViewCallback)
        dialogController?.removeCallback(dialogCallback)
        serviceChatHeadController?.onPause(this)
        callController?.onPause()
    }

    private fun destroyControllers(isFinishing: Boolean) {
        if (serviceChatHeadController != null && isFinishing) {
            serviceChatHeadController!!.onDestroy()
        }
        callController?.setViewCallback(null)
        callController = null
        screenSharingController = null
        dialogController = null
    }

    private fun setupControllers() {
        callController = Dependencies.getControllerFactory().getCallController(this)
        dialogCallback = DialogController.Callback {
            when (it.mode) {
                Dialog.MODE_NONE -> dismissAlertDialog()
                Dialog.MODE_UNEXPECTED_ERROR -> post { showUnexpectedErrorDialog() }
                Dialog.MODE_EXIT_QUEUE -> post { showExitQueueDialog() }
                Dialog.MODE_OVERLAY_PERMISSION -> post { showOverlayPermissionsDialog() }
                Dialog.MODE_END_ENGAGEMENT -> post { showEndEngagementDialog((it as OperatorName).operatorName) }
                Dialog.MODE_MEDIA_UPGRADE -> post { showUpgradeDialog(it as MediaUpgrade) }
                Dialog.MODE_NO_MORE_OPERATORS -> post { showNoMoreOperatorsAvailableDialog() }
                Dialog.MODE_ENGAGEMENT_ENDED -> post { showEngagementEndedDialog() }
                Dialog.MODE_START_SCREEN_SHARING -> post { showScreenSharingDialog() }
                Dialog.MODE_ENABLE_NOTIFICATION_CHANNEL -> post { showAllowNotificationsDialog() }
                Dialog.MODE_ENABLE_SCREEN_SHARING_NOTIFICATIONS_AND_START_SHARING -> post { showAllowScreenSharingNotificationsAndStartSharingDialog() }
            }
        }
        dialogController = Dependencies.getControllerFactory().dialogController
        screenSharingController = Dependencies.getControllerFactory().screenSharingController
        serviceChatHeadController = Dependencies.getControllerFactory().chatHeadController
    }

    private fun setTitle(title: String) {
        onTitleUpdatedListener?.onTitleUpdated(title)
        appBar.setTitle(title)
    }

    private fun handleCallTimerView(callState: CallState) {
        callTimerView.isVisible = callState.showCallTimerView()
        callState.callStatus.time?.also(callTimerView::setText)
    }

    private fun handleContinueBrowsingView(callState: CallState) {
        continueBrowsingView.isVisible =
            resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE &&
                    callState.showContinueBrowsingView()

        continueBrowsingView.text = resources.getString(
            if (callState.showOnHold()) R.string.glia_call_continue_browsing_on_hold else R.string.glia_call_continue_browsing
        )
    }

    private fun handleOperatorStatusViewState(state: CallState) {
        operatorStatusView.setShowRippleAnimation(state.showOperatorStatusViewRippleAnimation())
        operatorStatusView.setShowOnHold(state.showOnHold())
        if (state.isTransferring) {
            operatorStatusView.showTransferring()
            operatorNameView.setText(R.string.glia_chat_visitor_status_transferring)
        } else {
            handleOperatorStatusViewOperatorImage(state)
        }
        operatorStatusView.isVisible = state.showOperatorStatusView()
    }

    private fun handleOperatorStatusViewOperatorImage(state: CallState) {
        if (state.isCallOngoingAndOperatorConnected) {
            showOperatorProfileImageOnConnected(state)
        } else if (state.isCallOngoingAndOperatorIsConnecting) {
            showOperatorProfileImageOnConnecting(state)
        } else {
            operatorStatusView.showPlaceholder()
        }
    }

    private fun showOperatorProfileImageOnConnected(state: CallState) {
        if (state.callStatus.operatorProfileImageUrl != null) {
            operatorStatusView.showProfileImageOnConnect(state.callStatus.operatorProfileImageUrl)
        } else {
            operatorStatusView.showPlaceHolderWithIconPaddingOnConnect()
        }
    }

    private fun showOperatorProfileImageOnConnecting(state: CallState) {
        if (state.callStatus.operatorProfileImageUrl != null) {
            operatorStatusView.showProfileImage(state.callStatus.operatorProfileImageUrl)
        } else {
            operatorStatusView.showPlaceholder()
        }
    }

    private fun handleOperatorVideoState(state: CallState) {
        if (state.showOperatorVideo() && operatorVideoContainer.visibility == GONE) {
            operatorVideoContainer.visibility = VISIBLE
            showOperatorVideo(state.callStatus.operatorMediaState)
        } else if (!state.showOperatorVideo() && operatorVideoContainer.visibility == VISIBLE) {
            operatorVideoContainer.visibility = GONE
            hideOperatorVideo()
        }
    }

    private fun onIsSpeakerOnStateChanged(isSpeakerOn: Boolean) {
        if (isSpeakerOn != audioManager.isSpeakerphoneOn) {
            post { audioManager.isSpeakerphoneOn = isSpeakerOn }
        }
        setButtonActivated(
            speakerButton,
            theme.iconCallSpeakerOn,
            theme.iconCallSpeakerOff,
            R.string.glia_call_speaker_on_content_description,
            R.string.glia_call_speaker_off_content_description,
            isSpeakerOn
        )
    }

    private fun showExitQueueDialog() {
        alertDialog = Dialogs.showOptionsDialog(
            this.context,
            theme,
            resources.getString(R.string.glia_dialog_leave_queue_title),
            resources.getString(R.string.glia_dialog_leave_queue_message),
            resources.getString(R.string.glia_dialog_leave_queue_yes),
            resources.getString(R.string.glia_dialog_leave_queue_no),
            {
                dismissAlertDialog()
                callController?.endEngagementDialogYesClicked()
                onEndListener?.onEnd()
                callEnded()
            },
            {
                dismissAlertDialog()
                callController?.endEngagementDialogDismissed()
            },
            {
                it.dismiss()
                callController?.endEngagementDialogDismissed()
            },
            true
        )
    }

    private fun showAllowScreenSharingNotificationsAndStartSharingDialog() {
        if (alertDialog == null || !alertDialog!!.isShowing) {
            alertDialog = Dialogs.showOptionsDialog(
                this.context,
                theme,
                resources.getString(R.string.glia_dialog_screen_sharing_offer_enable_notifications_title),
                resources.getString(R.string.glia_dialog_screen_sharing_offer_enable_notifications_message),
                resources.getString(R.string.glia_dialog_screen_sharing_offer_enable_notifications_yes),
                resources.getString(R.string.glia_dialog_screen_sharing_offer_enable_notifications_no),
                {
                    dismissAlertDialog()
                    callController?.notificationsDialogDismissed()
                    NotificationManager.openNotificationChannelScreen(this.context)
                },
                {
                    dismissAlertDialog()
                    callController?.notificationsDialogDismissed()
                    screenSharingController?.onScreenSharingDeclined()
                }
            ) {
                it.dismiss()
                callController?.notificationsDialogDismissed()
                screenSharingController?.onScreenSharingDeclined()
            }
        }
    }

    private fun showAllowNotificationsDialog() {
        if (alertDialog == null || !alertDialog!!.isShowing) {
            alertDialog = Dialogs.showOptionsDialog(
                this.context,
                theme,
                resources.getString(R.string.glia_dialog_allow_notifications_title),
                resources.getString(R.string.glia_dialog_allow_notifications_message),
                resources.getString(R.string.glia_dialog_allow_notifications_yes),
                resources.getString(R.string.glia_dialog_allow_notifications_no),
                {
                    dismissAlertDialog()
                    callController?.notificationsDialogDismissed()
                    NotificationManager.openNotificationChannelScreen(this.context)
                },
                {
                    dismissAlertDialog()
                    callController?.notificationsDialogDismissed()
                }
            ) {
                it.dismiss()
                callController?.notificationsDialogDismissed()
            }
        }
    }

    private fun showScreenSharingDialog() {
        if (alertDialog == null || !alertDialog!!.isShowing) alertDialog =
            Dialogs.showScreenSharingDialog(
                this.context,
                theme,
                resources.getText(R.string.glia_dialog_screen_sharing_offer_title).toString(),
                resources.getText(R.string.glia_dialog_screen_sharing_offer_message).toString(),
                R.string.glia_dialog_screen_sharing_offer_accept,
                R.string.glia_dialog_screen_sharing_offer_decline,
                { screenSharingController!!.onScreenSharingAccepted(context) }
            ) { screenSharingController!!.onScreenSharingDeclined() }
    }

    private fun setButtonActivated(
        floatingActionButton: FloatingActionButton,
        activatedDrawableRes: Int?,
        notActivatedDrawableRes: Int?,
        @StringRes activatedContentDescription: Int,
        @StringRes notActivatedContentDescription: Int,
        isActivated: Boolean
    ) {
        val imageRes = if (isActivated) activatedDrawableRes else notActivatedDrawableRes
        val contentDescription =
            if (isActivated) activatedContentDescription else notActivatedContentDescription

        floatingActionButton.isActivated = isActivated
        floatingActionButton.contentDescription = resources.getString(contentDescription)
        floatingActionButton.setImageResource(imageRes ?: return)
    }

    private fun handleControlsVisibility(callState: CallState) {
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (shouldFadeInControls(callState)) {
                animateControls(fadeIn = true)
            } else if (shouldFadeOutControls(callState)) {
                animateControls(fadeIn = false)
            }
            appBar.isVisible = callState.landscapeLayoutControlsVisible
            buttonsLayoutBackground.isVisible =
                callState.landscapeLayoutControlsVisible && callState.isVideoCall

            buttonsLayout.isVisible = callState.landscapeLayoutControlsVisible
        } else {
            appBar.isVisible = true
            buttonsLayoutBackground.isVisible = false
            buttonsLayout.isVisible = true
        }
    }

    private fun animateControls(fadeIn: Boolean) {
        val transitionSet = TransitionSet()
        val appBarFade = MaterialFade()
        appBarFade.secondaryAnimatorProvider =
            SlideDistanceProvider(if (fadeIn) Gravity.TOP else Gravity.BOTTOM)
        transitionSet.addTransition(appBarFade.addTarget(appBar))
        val buttonsFade = MaterialFade()
        buttonsFade.secondaryAnimatorProvider =
            SlideDistanceProvider(if (fadeIn) Gravity.BOTTOM else Gravity.TOP)
        transitionSet.addTransition(buttonsFade.addTarget(buttonsLayoutBackground))
        transitionSet.addTransition(buttonsFade.addTarget(buttonsLayout))
        TransitionManager.beginDelayedTransition(this, transitionSet)
    }

    private fun shouldFadeInControls(callState: CallState) =
        callState.landscapeLayoutControlsVisible && appBar.isGone && buttonsLayout.isGone && buttonsLayoutBackground.isGone

    private fun shouldFadeOutControls(callState: CallState) =
        !callState.landscapeLayoutControlsVisible && appBar.isVisible && buttonsLayout.isVisible && buttonsLayoutBackground.isVisible

    private fun setupViewAppearance() {
        setAppBarTheme()
        // icons
        operatorStatusView.setTheme(theme)

        theme.iconCallChat?.also(chatButton::setImageResource)
        theme.iconCallVideoOn?.also(videoButton::setImageResource)
        theme.iconCallAudioOn?.also(muteButton::setImageResource)
        theme.iconCallSpeakerOn?.also(speakerButton::setImageResource)
        theme.iconCallMinimize?.also(minimizeButton::setImageResource)
        theme.brandPrimaryColor?.let(::getColorStateListCompat)
            ?.also(chatButtonBadgeView::setBackgroundTintList)

        // fonts
        theme.fontRes?.let(::getFontCompat)?.also {
            operatorNameView.typeface = it
            companyNameView.typeface = it
            msrView.typeface = it
            callTimerView.typeface = it
            connectingView.typeface = it
            continueBrowsingView.typeface = it
            chatButtonLabel.typeface = it
            videoButtonLabel.typeface = it
            muteButtonLabel.typeface = it
            speakerButtonLabel.typeface = it
            minimizeButtonLabel.typeface = it
        }

    }

    private fun setAppBarTheme() {
        val builder = UiThemeBuilder()
        builder.setTheme(theme)
        builder.setSystemNegativeColor(R.color.glia_system_negative_color)
        builder.setBaseLightColor(R.color.glia_base_light_color)
        builder.setBrandPrimaryColor(android.R.color.transparent)
        builder.setGliaChatHeaderTitleTintColor(android.R.color.white)
        builder.setGliaChatHeaderHomeButtonTintColor(android.R.color.white)
        builder.setGliaChatHeaderExitQueueButtonTintColor(android.R.color.white)
        builder.setFontRes(theme.fontRes)
        appBar.setTheme(builder.build())
        appBar.applyHeaderTheme(callTheme?.header)
    }

    private fun initConfigurations() {
        visibility = INVISIBLE
        setBackgroundColor(getColorCompat(R.color.glia_transparent_black_bg))
        // needed to overlap existing app bar in existing view with this view's app bar.
        elevation = 100f
    }

    private fun readTypedArray(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        context.withStyledAttributes(attrs, R.styleable.GliaView, defStyleAttr, defStyleRes) {
            setDefaultTheme(this)
        }
    }

    private fun setDefaultTheme(typedArray: TypedArray) {
        theme = Utils.getThemeFromTypedArray(typedArray, this.context)
    }

    fun setUiTheme(uiTheme: UiTheme?) {
        theme = Utils.getFullHybridTheme(uiTheme ?: return, theme)
        setupViewAppearance()
        if (isVisible) {
            handleStatusBarColor()
        }
    }

    fun setOnBackClickedListener(onBackClicked: OnBackClickedListener) {
        onBackClickedListener = onBackClicked
    }

    fun setOnEndListener(onEndListener: OnEndListener) {
        this.onEndListener = onEndListener
    }

    fun setOnMinimizeListener(onMinimizeListener: OnMinimizeListener) {
        this.onMinimizeListener = onMinimizeListener
    }

    fun setOnNavigateToChatListener(onNavigateToChatListener: OnNavigateToChatListener) {
        this.onNavigateToChatListener = onNavigateToChatListener
    }

    fun setOnNavigateToSurveyListener(onNavigateToSurveyListener: OnNavigateToSurveyListener) {
        this.onNavigateToSurveyListener = onNavigateToSurveyListener
    }

    fun setOnTitleUpdatedListener(onTitleUpdatedListener: OnTitleUpdatedListener) {
        this.onTitleUpdatedListener = onTitleUpdatedListener
    }

    private fun showUIOnCallOngoing() {
        visibility = VISIBLE
        handleStatusBarColor()
    }

    private fun hideUIOnCallEnd() {
        visibility = INVISIBLE
        val activity = Utils.getActivity(this.context)
        hideOperatorVideo()
        hideVisitorVideo()
        if (defaultStatusbarColor != null && activity != null) {
            activity.window.statusBarColor = defaultStatusbarColor!!
            defaultStatusbarColor = null
        }
        Utils.hideSoftKeyboard(this.context, windowToken)
    }

    private fun handleStatusBarColor() {
        val activity = Utils.getActivity(this.context)
        if (activity != null && defaultStatusbarColor == null) {
            defaultStatusbarColor = activity.window.statusBarColor
            activity.window.statusBarColor = getColorCompat(R.color.glia_transparent_black_bg)
        }
    }

    private fun showEndEngagementDialog(operatorName: String) {
        alertDialog = Dialogs.showOptionsDialog(
            this.context,
            theme,
            resources.getString(R.string.glia_dialog_end_engagement_title),
            resources.getString(R.string.glia_dialog_end_engagement_message, operatorName),
            resources.getString(R.string.glia_dialog_end_engagement_yes),
            resources.getString(R.string.glia_dialog_end_engagement_no),
            {
                dismissAlertDialog()
                callController?.endEngagementDialogYesClicked()
                alertDialog = null
            },
            {
                dismissAlertDialog()
                callController?.endEngagementDialogDismissed()
                alertDialog = null
            },
            {
                it.dismiss()
                callController?.endEngagementDialogDismissed()
            },
            true
        )
    }

    private fun showUpgradeDialog(mediaUpgrade: MediaUpgrade) {
        alertDialog = Dialogs.showUpgradeDialog(this.context, theme, mediaUpgrade, {
            callController?.acceptUpgradeOfferClicked(mediaUpgrade.mediaUpgradeOffer)
        }) {
            callController?.declineUpgradeOfferClicked(mediaUpgrade.mediaUpgradeOffer)
        }
    }

    private fun showOptionsDialog(
        title: String,
        message: String,
        positiveButtonText: String,
        neutralButtonText: String,
        positiveButtonClickListener: OnClickListener,
        neutralButtonClickListener: OnClickListener,
        cancelListener: DialogInterface.OnCancelListener
    ) {
        alertDialog = Dialogs.showOptionsDialog(
            this.context,
            theme,
            title,
            message,
            positiveButtonText,
            neutralButtonText,
            positiveButtonClickListener,
            neutralButtonClickListener,
            cancelListener
        )
    }

    private fun showAlertDialog(
        @StringRes title: Int, @StringRes message: Int, buttonClickListener: OnClickListener
    ) {
        dismissAlertDialog()
        alertDialog = Dialogs.showAlertDialog(
            this.context, theme, title, message, buttonClickListener
        )
    }

    private fun showEngagementEndedDialog() {
        dismissAlertDialog()
        alertDialog = Dialogs.showOperatorEndedEngagementDialog(this.context, theme) {
            dismissAlertDialog()
            callController?.noMoreOperatorsAvailableDismissed()
            onEndListener?.onEnd()
            callEnded()
        }
    }

    private fun showNoMoreOperatorsAvailableDialog() {
        showAlertDialog(
            R.string.glia_dialog_operators_unavailable_title,
            R.string.glia_dialog_operators_unavailable_message
        ) {
            dismissAlertDialog()
            callController?.noMoreOperatorsAvailableDismissed()
            onEndListener?.onEnd()
            callEnded()
            alertDialog = null
        }
    }

    private fun showUnexpectedErrorDialog() {
        showAlertDialog(
            R.string.glia_dialog_unexpected_error_title,
            R.string.glia_dialog_unexpected_error_message
        ) {
            dismissAlertDialog()
            callController?.unexpectedErrorDialogDismissed()
            onEndListener?.onEnd()
        }
    }

    fun showMissingPermissionsDialog() {
        showAlertDialog(
            R.string.glia_dialog_permission_error_title,
            R.string.glia_dialog_permission_error_message
        ) {
            dismissAlertDialog()
            callController?.unexpectedErrorDialogDismissed()
            onEndListener?.onEnd()
        }
    }

    private fun showOverlayPermissionsDialog() {
        showOptionsDialog(
            resources.getString(R.string.glia_dialog_overlay_permissions_title),
            resources.getString(R.string.glia_dialog_overlay_permissions_message),
            resources.getString(R.string.glia_dialog_overlay_permissions_ok),
            resources.getString(R.string.glia_dialog_overlay_permissions_no),
            {
                dismissAlertDialog()
                callController?.overlayPermissionsDialogDismissed()
                val overlayIntent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                )
                overlayIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                this.context.startActivity(overlayIntent)
            },
            {
                dismissAlertDialog()
                callController?.overlayPermissionsDialogDismissed()
            }
        ) {
            it.dismiss()
            callController?.overlayPermissionsDialogDismissed()
        }
    }

    private fun dismissAlertDialog() {
        alertDialog?.apply {
            dismiss()
            alertDialog = null
        }
    }

    private fun callEnded() {
        Dependencies.getControllerFactory().destroyControllers()
    }

    private fun showOperatorVideo(operatorMediaState: MediaState?) {
        releaseOperatorVideoStream()
        operatorMediaState?.video?.also {
            Logger.d(tag, "Starting video operator")
            operatorVideoView = it.createVideoView(Utils.getActivity(context))

            operatorVideoContainer.apply {
                removeAllViews()
                addView(operatorVideoView)
                invalidate()
            }
        }
    }

    private fun releaseOperatorVideoStream() {
        operatorVideoView?.apply {
            release()
            operatorVideoView = null
        }
    }

    private fun hideOperatorVideo() {
        operatorVideoContainer.removeAllViews()
        operatorVideoContainer.invalidate()
        releaseOperatorVideoStream()
    }

    private fun hideVisitorVideo() {
        floatingVisitorVideoContainer.hide()
    }

    fun onUserInteraction() {
        callController?.onUserInteraction()
    }

    fun setConfiguration(configuration: GliaSdkConfiguration?) {
        serviceChatHeadController?.setBuildTimeTheme(theme)
        serviceChatHeadController?.setSdkConfiguration(configuration)
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun interface OnBackClickedListener {
        fun onBackClicked()
    }

    fun interface OnEndListener {
        fun onEnd()
    }

    fun interface OnMinimizeListener {
        fun onMinimize()
    }

    fun interface OnNavigateToChatListener {
        fun call()
    }

    fun interface OnNavigateToSurveyListener {
        fun onSurvey(survey: Survey)
    }

    fun interface OnTitleUpdatedListener {
        fun onTitleUpdated(title: String?)
    }

    @Deprecated("", ReplaceWith("shouldShowMediaEngagementView(isUpgradeToCall)"))
    fun shouldShowMediaEngagementView() = shouldShowMediaEngagementView(false)

    fun shouldShowMediaEngagementView(isUpgradeToCall: Boolean) =
        callController?.shouldShowMediaEngagementView(isUpgradeToCall) ?: false

    override fun emitState(callState: CallState) {
        post {
            if (callState.isMediaEngagementStarted) {
                appBar.showEndButton()
            } else {
                appBar.showXButton()
            }
            if (callState.requestedMediaType == Engagement.MediaType.VIDEO) {
                setTitle(resources.getString(R.string.glia_call_video_app_bar_title))
            } else {
                setTitle(resources.getString(R.string.glia_call_audio_app_bar_title))
            }
            operatorNameView.text = callState.callStatus.formattedOperatorName
            connectingView.text = resources.getString(
                R.string.glia_call_connecting_with,
                callState.callStatus.formattedOperatorName,
                callState.callStatus.time
            )
            connectingView.contentDescription = resources.getString(
                R.string.glia_call_connecting_with,
                callState.callStatus.formattedOperatorName,
                ""
            )
            if (callState.companyName != null) {
                companyNameView.text = callState.companyName
                msrView.setText(R.string.glia_call_in_queue_message)
            }
            chatButtonBadgeView.text = callState.messagesNotSeen.toString()
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE &&
                callState.isVideoCall
            ) {
                appBar.backgroundTintList =
                    getColorStateListCompat(R.color.glia_transparent_black_bg)
            } else {
                appBar.backgroundTintList = getColorStateListCompat(android.R.color.transparent)
            }
            muteButton.isEnabled = callState.isMuteButtonEnabled
            speakerButton.isEnabled = callState.isSpeakerButtonEnabled
            videoButton.isEnabled = callState.isVideoButtonEnabled
            setButtonActivated(
                videoButton,
                theme.iconCallVideoOn,
                theme.iconCallVideoOff,
                R.string.glia_call_video_on_content_description,
                R.string.glia_call_video_off_content_description,
                callState.hasVideo
            )
            setButtonActivated(
                muteButton,
                theme.iconCallAudioOff,  // mute (eg. mic-off) button activated icon
                theme.iconCallAudioOn,  // mute (eg. mic-off) button deactivated icon
                R.string.glia_call_mute_content_description,
                R.string.glia_call_unmute_content_description,
                callState.isMuted
            )
            muteButtonLabel.setText(
                if (callState.isMuted) R.string.glia_call_mute_button_unmute else R.string.glia_call_mute_button_mute
            )
            chatButtonBadgeView.isVisible = callState.messagesNotSeen > 0
            videoButton.isVisible = callState.is2WayVideoCall
            videoButtonLabel.isVisible = callState.is2WayVideoCall
            operatorNameView.isVisible = callState.showOperatorNameView()
            companyNameView.isVisible = callState.showCompanyNameView()
            msrView.isVisible = callState.isCallNotOngoing
            connectingView.isVisible = callState.isCallOngoingAndOperatorIsConnecting
            onHoldTextView.isVisible = callState.showOnHold()
            handleCallTimerView(callState)
            handleContinueBrowsingView(callState)
            handleOperatorStatusViewState(callState)
            handleOperatorVideoState(callState)
            handleControlsVisibility(callState)
            onIsSpeakerOnStateChanged(callState.isSpeakerOn)
            if (callState.isVisible) {
                showUIOnCallOngoing()
            } else {
                hideUIOnCallEnd()
            }
            chatButton.isEnabled =
                callState.isAudioCall || callState.isVideoCall || callState.is2WayVideoCall
            chatButton.contentDescription =
                if (callState.messagesNotSeen == 0) resources.getString(R.string.glia_call_chat_zero_content_description) else resources.getQuantityString(
                    R.plurals.glia_call_chat_content_description,
                    callState.messagesNotSeen, callState.messagesNotSeen
                )
        }
    }

    override fun navigateToChat() {
        onNavigateToChatListener?.call()
    }

    override fun navigateToSurvey(survey: Survey) {
        onNavigateToSurveyListener?.onSurvey(survey)
    }

    override fun destroyView() {
        onEndListener?.onEnd()
    }

    override fun minimizeView() {
        onMinimizeListener?.onMinimize()
    }
}