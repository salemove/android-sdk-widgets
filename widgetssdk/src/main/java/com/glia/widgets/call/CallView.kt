package com.glia.widgets.call

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.content.res.TypedArray
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
import androidx.core.content.withStyledAttributes
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.comms.MediaState
import com.glia.androidsdk.comms.VideoView
import com.glia.androidsdk.engagement.Survey
import com.glia.androidsdk.screensharing.ScreenSharing
import com.glia.widgets.Constants
import com.glia.widgets.R
import com.glia.widgets.StringKey
import com.glia.widgets.StringKeyPair
import com.glia.widgets.UiTheme
import com.glia.widgets.UiTheme.UiThemeBuilder
import com.glia.widgets.call.CallState.ViewState
import com.glia.widgets.core.configuration.GliaSdkConfiguration
import com.glia.widgets.core.dialog.Dialog
import com.glia.widgets.core.dialog.DialogController
import com.glia.widgets.core.dialog.model.DialogState
import com.glia.widgets.core.dialog.model.DialogState.MediaUpgrade
import com.glia.widgets.core.notification.openNotificationChannelScreen
import com.glia.widgets.core.screensharing.ScreenSharingController
import com.glia.widgets.databinding.CallButtonsLayoutBinding
import com.glia.widgets.databinding.CallViewBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.Utils
import com.glia.widgets.helper.changeStatusBarColor
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.getColorStateListCompat
import com.glia.widgets.helper.getFontCompat
import com.glia.widgets.helper.getFullHybridTheme
import com.glia.widgets.helper.hideKeyboard
import com.glia.widgets.helper.insetsController
import com.glia.widgets.helper.requireActivity
import com.glia.widgets.helper.showToast
import com.glia.widgets.view.Dialogs
import com.glia.widgets.view.OperatorStatusView
import com.glia.widgets.view.floatingvisitorvideoview.FloatingVisitorVideoContainer
import com.glia.widgets.view.head.BadgeTextView
import com.glia.widgets.view.head.controller.ServiceChatHeadController
import com.glia.widgets.view.header.AppBarView
import com.glia.widgets.view.unifiedui.applyBarButtonStatesTheme
import com.glia.widgets.view.unifiedui.applyColorTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.call.CallTheme
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.theme.overlay.MaterialThemeOverlay
import com.google.android.material.transition.MaterialFade
import com.google.android.material.transition.SlideDistanceProvider
import kotlin.properties.Delegates

internal class CallView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : ConstraintLayout(
    MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, defStyleRes),
    attrs,
    defStyleAttr,
    defStyleRes
),
    CallViewCallback {

    private val callTheme: CallTheme? by lazy {
        Dependencies.getGliaThemeManager().theme?.callTheme
    }

    private val screenSharingViewCallback = object : ScreenSharingController.ViewCallback {
        override fun onScreenSharingRequestError(ex: GliaException) {
            showToast(ex.debugMessage)
        }

        override fun onScreenSharingRequestSuccess() {
            post { appBar.showEndScreenSharingButton() }
        }

        override fun onScreenSharingEnded() {
            post { appBar.hideEndScreenSharingButton() }
        }
    }

    private val binding: CallViewBinding by lazy {
        CallViewBinding.inflate(LayoutInflater.from(this.context), this)
    }

    //    -------- Buttons Bar widgets ----------
    private val callButtonsLayoutBinding: CallButtonsLayoutBinding get() = binding.buttonsLayout
    private val chatButtonLabel: CallButtonLabelView get() = callButtonsLayoutBinding.chatButtonLabel
    private val videoButtonLabel: CallButtonLabelView get() = callButtonsLayoutBinding.videoButtonLabel
    private val muteButtonLabel: CallButtonLabelView get() = callButtonsLayoutBinding.muteButtonLabel
    private val speakerButtonLabel: CallButtonLabelView get() = callButtonsLayoutBinding.speakerButtonLabel
    private val minimizeButtonLabel: CallButtonLabelView get() = callButtonsLayoutBinding.minimizeButtonLabel
    private val videoButton: FloatingActionButton get() = callButtonsLayoutBinding.videoButton
    private val muteButton: FloatingActionButton get() = callButtonsLayoutBinding.muteButton
    private val speakerButton: FloatingActionButton get() = callButtonsLayoutBinding.speakerButton
    private val minimizeButton: FloatingActionButton get() = callButtonsLayoutBinding.minimizeButton
    private val chatButton: FloatingActionButton get() = callButtonsLayoutBinding.chatButton
    private val chatButtonBadgeView: BadgeTextView get() = callButtonsLayoutBinding.chatButtonBadge
    private val buttonsLayoutBackground: View get() = binding.buttonsLayoutBg
    private val buttonsLayout: View get() = callButtonsLayoutBinding.root
    //    ---------------------------------------

    private val appBar: AppBarView get() = binding.topAppBar
    private val operatorStatusView: OperatorStatusView get() = binding.operatorStatusView
    private val operatorNameView: ThemedStateText get() = binding.operatorNameView
    private val companyNameView: TextView get() = binding.companyNameView
    private val msrView: TextView get() = binding.msrView
    private val callTimerView: ThemedStateText get() = binding.callTimerView
    private val connectingView: ThemedStateText get() = binding.connectingView
    private val continueBrowsingView: TextView get() = binding.continueBrowsingView
    private val operatorVideoContainer: FrameLayout get() = binding.operatorVideoContainer
    private val onHoldTextView: ThemedStateText get() = binding.onHoldText
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
    private var defaultStatusBarColor: Int? = null

    private var operatorVideoView: VideoView? = null
    private var alertDialog: AlertDialog? = null
    private var currentDialogState: DialogState? = null

    private val stringProvider = Dependencies.getStringProvider()

    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = R.attr.gliaChatStyle
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
        appBar.setOnEndCallButtonClickedListener { screenSharingController?.onForceStopScreenSharing() }
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
        isUpgradeToCall: Boolean,
        mediaType: Engagement.MediaType?
    ) {
        callController?.startCall(
            companyName,
            queueId,
            visitorContextAssetId,
            mediaType,
            useOverlays,
            screenSharingMode,
            isUpgradeToCall,
            serviceChatHeadController
        )
    }

    fun onDestroy() {
        releaseOperatorVideoStream()
        dismissAlertDialog()
        onEndListener = null
        onBackClickedListener = null
        onNavigateToChatListener = null
        onNavigateToSurveyListener = null
        destroyControllers()
    }

    fun onResume() {
        floatingVisitorVideoContainer.onResume()
        floatingVisitorVideoContainer.contentDescription = stringProvider.getRemoteString(R.string.call_visitor_video_accessibility_label)
        callController?.onResume()
        operatorVideoView?.resumeRendering()
        dialogController?.addCallback(dialogCallback)
        screenSharingController?.setViewCallback(screenSharingViewCallback)
        screenSharingController?.onResume(context.requireActivity())
        serviceChatHeadController?.onResume(this)
    }

    fun onPause() {
        floatingVisitorVideoContainer.onPause()
        operatorVideoView?.pauseRendering()
        screenSharingController?.removeViewCallback(screenSharingViewCallback)
        dialogController?.removeCallback(dialogCallback)
        serviceChatHeadController?.onPause(this)
        callController?.onPause()
    }

    private fun destroyControllers() {
        callController?.setViewCallback(null)
        callController = null
        screenSharingController = null
        dialogController = null
    }

    private fun setupControllers() {
        callController = Dependencies.getControllerFactory().getCallController(this)
        dialogCallback = DialogController.Callback {
            if (it.mode == currentDialogState?.mode) {
                return@Callback
            }
            currentDialogState = it
            when (it.mode) {
                Dialog.MODE_NONE -> dismissAlertDialog()
                Dialog.MODE_UNEXPECTED_ERROR -> post { showUnexpectedErrorDialog() }
                Dialog.MODE_EXIT_QUEUE -> post { showExitQueueDialog() }
                Dialog.MODE_OVERLAY_PERMISSION -> post { showOverlayPermissionsDialog() }
                Dialog.MODE_END_ENGAGEMENT -> post { showEndEngagementDialog() }
                Dialog.MODE_MEDIA_UPGRADE -> post { showUpgradeDialog(it as MediaUpgrade) }
                Dialog.MODE_NO_MORE_OPERATORS -> post { showNoMoreOperatorsAvailableDialog() }
                Dialog.MODE_ENGAGEMENT_ENDED -> post { showEngagementEndedDialog() }
                Dialog.MODE_START_SCREEN_SHARING -> post { showScreenSharingDialog() }
                Dialog.MODE_ENABLE_NOTIFICATION_CHANNEL -> post { showAllowNotificationsDialog() }
                Dialog.MODE_ENABLE_SCREEN_SHARING_NOTIFICATIONS_AND_START_SHARING -> post {
                    showAllowScreenSharingNotificationsAndStartSharingDialog()
                }

                Dialog.MODE_LIVE_OBSERVATION_OPT_IN -> post { callController?.onLiveObservationDialogRequested() }
                Dialog.MODE_VISITOR_CODE -> {
                    Logger.e(TAG, "DialogController callback in CallView with MODE_VISITOR_CODE")
                } // Should never happen inside CallView
                else -> Logger.e(TAG, "DialogController callback in CallView with ${it.mode}")
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
        callTimerView.contentDescription = stringProvider.getRemoteString(R.string.call_duration_accessibility_label)
    }

    private fun handleContinueBrowsingView(callState: CallState) {
        continueBrowsingView.isVisible =
            resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE &&
            callState.showContinueBrowsingView()

        continueBrowsingView.text = stringProvider.getRemoteString(
            if (callState.showOnHold()) R.string.call_on_hold_bottom_text else R.string.engagement_queue_wait_message
        )
    }

    private fun handleOperatorStatusViewState(state: CallState) {
        operatorStatusView.setShowRippleAnimation(state.showOperatorStatusViewRippleAnimation())
        operatorStatusView.setShowOnHold(state.showOnHold())
        if (state.isTransferring) {
            operatorStatusView.showTransferring()
            operatorNameView.text = (stringProvider.getRemoteString(R.string.engagement_queue_transferring))
        } else {
            operatorNameView.text = Dependencies.getSdkConfigurationManager().companyName
            operatorNameView.hint = stringProvider.getRemoteString(R.string.chat_operator_name_accessibility_label)
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
            connectingView.text = stringProvider.getRemoteString(R.string.android_call_queue_message)
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
        setButtonActivated(
            speakerButton,
            theme.iconCallSpeakerOn,
            theme.iconCallSpeakerOff,
            R.string.android_call_turn_speaker_off_button_accessibility,
            R.string.android_call_turn_speaker_on_button_accessibility,
            isSpeakerOn
        )
        speakerButtonLabel.isActivated = isSpeakerOn
    }

    private fun showExitQueueDialog() {
        alertDialog = Dialogs.showOptionsDialog(
            context = this.context,
            theme = theme,
            title = stringProvider.getRemoteString(R.string.engagement_queue_leave_header),
            message = stringProvider.getRemoteString(R.string.engagement_queue_leave_message),
            positiveButtonText = stringProvider.getRemoteString(R.string.general_yes),
            negativeButtonText = stringProvider.getRemoteString(R.string.general_no),
            positiveButtonClickListener = {
                dismissAlertDialog()
                callController?.endEngagementDialogYesClicked()
                onEndListener?.onEnd()
                callEnded()
            },
            negativeButtonClickListener = {
                dismissAlertDialog()
                callController?.endEngagementDialogDismissed()
            },
            cancelListener = {
                it.dismiss()
                callController?.endEngagementDialogDismissed()
            },
            isButtonsColorsReversed = true
        )
    }

    private fun showAllowScreenSharingNotificationsAndStartSharingDialog() {
        if (alertDialog == null || !alertDialog!!.isShowing) {
            alertDialog = Dialogs.showOptionsDialog(
                context = this.context,
                theme = theme,
                title = stringProvider.getRemoteString(R.string.android_screen_sharing_offer_with_notifications_title),
                message = stringProvider.getRemoteString(R.string.android_screen_sharing_offer_with_notifications_message),
                positiveButtonText = stringProvider.getRemoteString(R.string.general_yes),
                negativeButtonText = stringProvider.getRemoteString(R.string.general_no),
                positiveButtonClickListener = {
                    callController?.notificationsDialogDismissed()
                    this.context.openNotificationChannelScreen()
                },
                negativeButtonClickListener = {
                    callController?.notificationsDialogDismissed()
                    screenSharingController?.onScreenSharingDeclined()
                },
                cancelListener = {
                    callController?.notificationsDialogDismissed()
                    screenSharingController?.onScreenSharingDeclined()
                }
            )
        }
    }

    private fun showAllowNotificationsDialog() {
        if (alertDialog == null || !alertDialog!!.isShowing) {
            alertDialog = Dialogs.showOptionsDialog(
                context = this.context,
                theme = theme,
                title = stringProvider.getRemoteString(R.string.android_notification_allow_notifications_title),
                message = stringProvider.getRemoteString(R.string.android_notification_allow_notifications_message),
                positiveButtonText = stringProvider.getRemoteString(R.string.general_yes),
                negativeButtonText = stringProvider.getRemoteString(R.string.general_no),
                positiveButtonClickListener = {
                    dismissAlertDialog()
                    callController?.notificationsDialogDismissed()
                    this.context.openNotificationChannelScreen()
                },
                negativeButtonClickListener = {
                    dismissAlertDialog()
                    callController?.notificationsDialogDismissed()
                },
                cancelListener = {
                    it.dismiss()
                    callController?.notificationsDialogDismissed()
                }
            )
        }
    }

    private fun showScreenSharingDialog() {
        if (alertDialog == null || !alertDialog!!.isShowing) {
            alertDialog =
                Dialogs.showScreenSharingDialog(
                    this.context,
                    theme,
                    stringProvider.getRemoteString(R.string.screen_sharing_visitor_screen_disclaimer_title),
                    stringProvider.getRemoteString(R.string.screen_sharing_visitor_screen_disclaimer_info),
                    R.string.general_accept,
                    R.string.general_decline,
                    { screenSharingController!!.onScreenSharingAccepted(context.requireActivity()) }
                ) { screenSharingController!!.onScreenSharingDeclined() }
        }
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
        floatingActionButton.contentDescription = stringProvider.getRemoteString(contentDescription)
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
        operatorStatusView.applyOperatorTheme(callTheme?.connect?.operator)

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

        // ButtonBar Buttons
        chatButton.applyBarButtonStatesTheme(callTheme?.buttonBar?.chatButton)
        videoButton.applyBarButtonStatesTheme(callTheme?.buttonBar?.videoButton)
        muteButton.applyBarButtonStatesTheme(callTheme?.buttonBar?.muteButton)
        speakerButton.applyBarButtonStatesTheme(callTheme?.buttonBar?.speakerButton)
        minimizeButton.applyBarButtonStatesTheme(callTheme?.buttonBar?.minimizeButton)

        // ButtonBar Labels
        chatButtonLabel.setBarButtonStatesTheme(callTheme?.buttonBar?.chatButton)
        videoButtonLabel.setBarButtonStatesTheme(callTheme?.buttonBar?.videoButton)
        muteButtonLabel.setBarButtonStatesTheme(callTheme?.buttonBar?.muteButton)
        speakerButtonLabel.setBarButtonStatesTheme(callTheme?.buttonBar?.speakerButton)
        minimizeButtonLabel.setBarButtonStatesTheme(callTheme?.buttonBar?.minimizeButton)

        // Badge
        chatButtonBadgeView.applyBadgeTheme(callTheme?.buttonBar?.badge)

        // Texts
        appBar.setTitle(stringProvider.getRemoteString(R.string.engagement_audio_title))
        chatButtonLabel.text = stringProvider.getRemoteString(R.string.engagement_chat_title)
        speakerButtonLabel.text = stringProvider.getRemoteString(R.string.call_speaker_button)
        minimizeButtonLabel.text = stringProvider.getRemoteString(R.string.engagement_minimize_video_button)
        companyNameView.text = Dependencies.getSdkConfigurationManager().companyName
        videoButtonLabel.text = stringProvider.getRemoteString(R.string.engagement_video_title)
        callTheme?.topText.also(onHoldTextView::applyThemeAsDefault)
        callTheme?.duration.also(callTimerView::applyThemeAsDefault)
        callTheme?.operator.also(operatorNameView::applyThemeAsDefault)
        callTheme?.bottomText.also(continueBrowsingView::applyTextTheme)

        // Hints and content descriptions
        operatorVideoContainer.contentDescription = stringProvider.getRemoteString(R.string.call_operator_video_accessibility_label)
        operatorNameView.hint = stringProvider.getRemoteString(R.string.chat_operator_name_accessibility_label)
        binding.callTimerView.hint = stringProvider.getRemoteString(R.string.call_duration_accessibility_label)
        minimizeButton.contentDescription = stringProvider.getRemoteString(R.string.engagement_minimize_video_button)
        operatorVideoContainer.contentDescription = stringProvider.getRemoteString(R.string.call_operator_video_accessibility_label)

        // Background
        callTheme?.background?.fill.also(::applyColorTheme)
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
        elevation = Constants.WIDGETS_SDK_LAYER_ELEVATION
    }

    private fun readTypedArray(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        context.withStyledAttributes(attrs, R.styleable.GliaView, defStyleAttr, defStyleRes) {
            setDefaultTheme(this)
        }
    }

    private fun setDefaultTheme(typedArray: TypedArray) {
        theme = Utils.getThemeFromTypedArray(typedArray, this.context)
            .getFullHybridTheme(Dependencies.getSdkConfigurationManager().uiTheme)
    }

    fun setUiTheme(uiTheme: UiTheme?) {
        theme = theme.getFullHybridTheme(uiTheme ?: return)
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
        hideOperatorVideo()
        hideVisitorVideo()
        insetsController?.hideKeyboard()
    }

    private fun handleStatusBarColor() {
        val activity = context.requireActivity()
        if (defaultStatusBarColor == null) {
            defaultStatusBarColor = activity.window.statusBarColor
            changeStatusBarColor(getColorCompat(R.color.glia_transparent_black_bg))
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        defaultStatusBarColor?.also {
            changeStatusBarColor(it)
            defaultStatusBarColor = null
        }
    }

    private fun showEndEngagementDialog() {
        alertDialog = Dialogs.showOptionsDialog(
            context = this.context,
            theme = theme,
            title = stringProvider.getRemoteString(R.string.engagement_end_confirmation_header),
            message = stringProvider.getRemoteString(R.string.engagement_end_message),
            positiveButtonText = stringProvider.getRemoteString(R.string.general_yes),
            negativeButtonText = stringProvider.getRemoteString(R.string.general_no),
            positiveButtonClickListener = {
                dismissAlertDialog()
                callController?.endEngagementDialogYesClicked()
            },
            negativeButtonClickListener = {
                dismissAlertDialog()
                callController?.endEngagementDialogDismissed()
            },
            cancelListener = {
                it.dismiss()
                callController?.endEngagementDialogDismissed()
            },
            isButtonsColorsReversed = true
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
            context = this.context,
            theme = theme,
            title = title,
            message = message,
            positiveButtonText = positiveButtonText,
            negativeButtonText = neutralButtonText,
            positiveButtonClickListener = positiveButtonClickListener,
            negativeButtonClickListener = neutralButtonClickListener,
            cancelListener = cancelListener
        )
    }

    private fun showAlertDialog(
        @StringRes title: Int,
        @StringRes message: Int,
        buttonClickListener: OnClickListener
    ) {
        dismissAlertDialog()
        alertDialog = Dialogs.showAlertDialog(
            this.context,
            theme,
            title,
            message,
            buttonClickListener
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
            R.string.engagement_queue_closed_header,
            R.string.engagement_queue_closed_message
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
            R.string.error_general,
            R.string.engagement_queue_reconnection_failed
        ) {
            dismissAlertDialog()
            callController?.unexpectedErrorDialogDismissed()
            onEndListener?.onEnd()
        }
    }

    override fun showMissingPermissionsDialog() {
        showAlertDialog(
            R.string.android_permissions_title,
            R.string.android_permissions_message
        ) {
            dismissAlertDialog()
            callController?.unexpectedErrorDialogDismissed()
            onEndListener?.onEnd()
        }
    }

    override fun showEngagementConfirmationDialog(companyName: String) {
        dismissAlertDialog()
        alertDialog = Dialogs.showEngagementConfirmationDialog(
            context = context,
            theme = theme,
            companyName = companyName,
            positiveButtonClickListener = {
                dismissAlertDialog()
                callController?.onLiveObservationDialogAllowed()
            },
            negativeButtonClickListener = {
                dismissAlertDialog()
                callController?.onLiveObservationDialogRejected()
                onEndListener?.onEnd()
                callEnded()
            }
        )
    }

    private fun showOverlayPermissionsDialog() {
        showOptionsDialog(
            stringProvider.getRemoteString(R.string.android_overlay_permission_title),
            stringProvider.getRemoteString(R.string.android_overlay_permission_message),
            stringProvider.getRemoteString(R.string.general_ok),
            stringProvider.getRemoteString(R.string.general_no),
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
        currentDialogState = null
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
            Logger.d(TAG, "Starting video operator")
            operatorVideoView = it.createVideoView(context.requireActivity())

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
        context.showToast(message, Toast.LENGTH_SHORT)
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

    private fun applyTextThemeBasedOnCallState(callState: CallState) {
        when {
            callState.showOnHold() -> {
                // onHold operatorNameView, onHoldText
                callTheme?.connect?.onHold?.apply {
                    title?.also(operatorNameView::applyThemeOrDefault)
                    description?.also(onHoldTextView::applyThemeOrDefault)
                }
            }

            callState.isCallOngoingAndOperatorIsConnecting -> {
                // connecting connectingView, operatorNameView, callTimerView
                callTheme?.connect?.connecting?.apply {
                    title?.also(operatorNameView::applyThemeOrDefault)
                    description?.also(connectingView::applyThemeOrDefault)
                }
            }

            callState.isCallOngoingAndOperatorConnected -> {
                // connected operatorNameView, callTimerView
                invalidateOperatorNameViewTheme()
            }

            callState.isTransferring -> {
                // transferring operatorNameView, callTimerView
                invalidateOperatorNameViewTheme()
            }

            else -> {
                // queue companyNameView, msrView
                // this is the same as [callState.isCallNotOngoing] or queue state
                callTheme?.connect?.connecting?.apply {
                    title?.also(companyNameView::applyTextTheme)
                    description?.also(msrView::applyTextTheme)
                    operatorNameView.restoreDefaultTheme()
                    connectingView.restoreDefaultTheme()
                }
            }
        }
    }

    private fun invalidateOperatorNameViewTheme() {
        callTheme?.connect?.connected?.apply {
            title?.also(operatorNameView::applyThemeOrDefault)
        }
    }

    override fun emitState(callState: CallState) {
        post {
            connectingView.text = stringProvider.getRemoteString(R.string.android_call_queue_message)

            handleCallTimerView(callState)

            // No need to manage the remaining view's states if only time has changed
            if (callState.isOnlyTimeChanged) return@post

            setupEndButton(callState)

            if (screenSharingController?.isSharingScreen == true) {
                appBar.showEndScreenSharingButton()
            } else {
                appBar.hideEndScreenSharingButton()
            }
            if (callState.requestedMediaType == Engagement.MediaType.VIDEO) {
                setTitle(stringProvider.getRemoteString(R.string.engagement_video_title))
            } else {
                setTitle(stringProvider.getRemoteString(R.string.engagement_audio_title))
            }
            operatorNameView.text = callState.callStatus.formattedOperatorName
            connectingView.contentDescription = stringProvider.getRemoteString(
                R.string.engagement_connection_screen_connect_with,
                StringKeyPair(StringKey.OPERATOR_NAME, callState.callStatus.formattedOperatorName ?: ""),
                StringKeyPair(StringKey.BADGE_VALUE, "")
            )
            if (callState.companyName != null) {
                companyNameView.text = callState.companyName
                companyNameView.hint = callState.companyName
                msrView.text = stringProvider.getRemoteString(
                    R.string.android_call_queue_message
                )
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

            callState.isVideoButtonEnabled.also {
                videoButton.isEnabled = it
                videoButtonLabel.isEnabled = it
            }

            setButtonActivated(
                videoButton,
                theme.iconCallVideoOn,
                theme.iconCallVideoOff,
                R.string.android_call_turn_video_off_button_accessibility,
                R.string.android_call_turn_video_on_button_accessibility,
                callState.hasVideo
            )
            videoButton.isActivated = callState.hasVideo
            floatingVisitorVideoContainer.visibility = if (callState.hasVideo) VISIBLE else GONE

            setButtonActivated(
                muteButton,
                theme.iconCallAudioOff, // mute (eg. mic-off) button activated icon
                theme.iconCallAudioOn, // mute (eg. mic-off) button deactivated icon
                R.string.android_call_unmute_button_accessibility,
                R.string.android_call_mute_button_accessibility,
                callState.isMuted
            )
            muteButtonLabel.isActivated = callState.isMuted
            muteButtonLabel.text = stringProvider.getRemoteString(
                if (callState.isMuted) R.string.call_unmute_button else R.string.call_mute_button
            )
            onHoldTextView.text = stringProvider.getRemoteString(R.string.call_on_hold_icon)

            callState.chatButtonViewState.apply {
                if (this == ViewState.SHOW) {
                    chatButtonBadgeView.isVisible = callState.messagesNotSeen > 0
                }
                applyViewState(this, chatButton, chatButtonLabel)
            }
            applyViewState(callState.muteButtonViewState, muteButton, muteButtonLabel)
            applyViewState(callState.speakerButtonViewState, speakerButton, speakerButtonLabel)

            videoButton.isVisible = callState.is2WayVideoCall
            videoButtonLabel.isVisible = callState.is2WayVideoCall
            operatorNameView.isVisible = callState.showOperatorNameView()
            companyNameView.isVisible = callState.showCompanyNameView()
            msrView.isVisible = callState.isCallNotOngoing
            connectingView.isVisible = callState.isCallOngoingAndOperatorIsConnecting
            onHoldTextView.isVisible = callState.showOnHold()
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
            (callState.isAudioCall || callState.isVideoCall || callState.is2WayVideoCall).also {
                chatButton.isEnabled = it
                chatButtonLabel.isEnabled = it
            }

            chatButton.contentDescription =
                when (callState.messagesNotSeen) {
                    0 -> stringProvider.getRemoteString(R.string.engagement_chat_title)
                    1 -> stringProvider.getRemoteString(
                        R.string.call_buttons_chat_badge_value_single_item_accessibility_label,
                        StringKeyPair(StringKey.BADGE_VALUE, callState.messagesNotSeen.toString())
                    )
                    else -> stringProvider.getRemoteString(
                        R.string.call_buttons_chat_badge_value_multiple_items_accessibility_label,
                        StringKeyPair(StringKey.BADGE_VALUE, callState.messagesNotSeen.toString())
                    )
                }
            applyTextThemeBasedOnCallState(callState)
        }
    }

    private fun setupEndButton(callState: CallState) {
        if (callState.isCallVisualizer) {
            appBar.hideXAndEndButton()
        } else if (callState.isMediaEngagementStarted) {
            appBar.showEndButton()
        } else {
            appBar.showXButton()
        }
    }

    private fun applyViewState(state: ViewState, vararg views: View) {
        val isVisible = state != ViewState.HIDE
        val isEnabled = state == ViewState.SHOW
        views.forEach {
            it.isVisible = isVisible
            it.isEnabled = isEnabled
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
