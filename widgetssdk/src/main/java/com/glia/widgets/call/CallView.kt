package com.glia.widgets.call

import android.content.Context
import android.content.res.Configuration
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.glia.androidsdk.comms.MediaState
import com.glia.androidsdk.comms.VideoView
import com.glia.telemetry_lib.GliaLogger
import com.glia.telemetry_lib.LogEvents
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.UiTheme.UiThemeBuilder
import com.glia.widgets.call.CallState.ViewState
import com.glia.widgets.databinding.CallButtonsLayoutBinding
import com.glia.widgets.databinding.CallViewBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.engagement.MediaType
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.SimpleWindowInsetsAndAnimationHandler
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.Utils
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.getColorStateListCompat
import com.glia.widgets.helper.getFontCompat
import com.glia.widgets.helper.hideKeyboard
import com.glia.widgets.helper.insetsController
import com.glia.widgets.helper.requireActivity
import com.glia.widgets.helper.setContentDescription
import com.glia.widgets.helper.setLocaleContentDescription
import com.glia.widgets.helper.setLocaleHint
import com.glia.widgets.helper.setLocaleText
import com.glia.widgets.helper.showToast
import com.glia.widgets.internal.dialog.DialogContract
import com.glia.widgets.internal.dialog.model.DialogState
import com.glia.widgets.launcher.ActivityLauncher
import com.glia.widgets.locale.LocaleString
import com.glia.widgets.locale.StringKey
import com.glia.widgets.locale.StringKeyPair
import com.glia.widgets.view.Dialogs
import com.glia.widgets.view.OperatorStatusView
import com.glia.widgets.view.dialog.base.DialogDelegate
import com.glia.widgets.view.dialog.base.DialogDelegateImpl
import com.glia.widgets.view.floatingvisitorvideoview.FloatingVisitorVideoContainer
import com.glia.widgets.view.head.BadgeTextView
import com.glia.widgets.view.head.ChatHeadContract
import com.glia.widgets.view.header.AppBarView
import com.glia.widgets.view.snackbar.SnackBarDelegate
import com.glia.widgets.view.snackbar.makeNoConnectionSnackBar
import com.glia.widgets.view.unifiedui.applyBarButtonStatesTheme
import com.glia.widgets.view.unifiedui.applyColorTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.call.CallTheme
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.theme.overlay.MaterialThemeOverlay
import com.google.android.material.transition.MaterialFade
import com.google.android.material.transition.SlideDistanceProvider
import java.util.concurrent.Executor
import kotlin.properties.Delegates

private const val CONTROLS_ALPHA_SEMI_TRANSPARENT = 0.9f
private const val CONTROLS_ALPHA = 1f

internal class CallView(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : ConstraintLayout(
    MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, defStyleRes),
    attrs,
    defStyleAttr,
    defStyleRes
), CallContract.View, DialogDelegate by DialogDelegateImpl() {
    private val activityLauncher: ActivityLauncher by lazy { Dependencies.activityLauncher }

    private val callTheme: CallTheme? by lazy {
        Dependencies.gliaThemeManager.theme?.callTheme
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
    private val poorConnectionView: ThemedStateText get() = binding.poorConnectionView

    private var theme: UiTheme by Delegates.notNull()

    private var callController: CallContract.Controller? = null
    private var serviceChatHeadController: ChatHeadContract.Controller? = null
    private var dialogCallback: DialogContract.Controller.Callback? = null
    private var dialogController: DialogContract.Controller? = null

    private var onBackClickedListener: OnBackClickedListener? = null
    private var onEndListener: OnEndListener? = null
    private var onMinimizeListener: OnMinimizeListener? = null
    private var onNavigateToChatListener: OnNavigateToChatListener? = null
    private var onNavigateToWebBrowserListener: OnNavigateToWebBrowserListener? = null
    private var onTitleUpdatedListener: OnTitleUpdatedListener? = null

    private var operatorVideoView: VideoView? = null

    private var snackBarDelegate: SnackBarDelegate? = null

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
        SimpleWindowInsetsAndAnimationHandler(this, appBarOrToolBar = appBar)
    }

    private fun setupViewActions() {
        appBar.setOnBackClickedListener {
            callController?.onBackClicked()
            onBackClickedListener?.onBackClicked()
        }
        appBar.setOnEndClickListener { callController?.endEngagementClicked() }
        appBar.setOnXClickedListener { callController?.exitQueueingClicked() }
        chatButton.setOnClickListener { callController?.chatButtonClicked() }
        speakerButton.setOnClickListener { callController?.onSpeakerButtonPressed() }
        minimizeButton.setOnClickListener { callController?.minimizeButtonClicked() }
        muteButton.setOnClickListener { callController?.muteButtonClicked() }
        videoButton.setOnClickListener { callController?.videoButtonClicked() }
        floatingVisitorVideoContainer.onFlipButtonClickListener = OnClickListener {
            callController?.flipVideoButtonClicked()
        }
    }

    fun startCall(isUpgradeToCall: Boolean, mediaType: MediaType?) {
        callController?.startCall(mediaType, isUpgradeToCall)
    }

    fun onDestroy() {
        releaseOperatorVideoStream()
        resetDialogStateAndDismiss()
        onEndListener = null
        onBackClickedListener = null
        onNavigateToChatListener = null
        onNavigateToWebBrowserListener = null
        destroyControllers()
    }

    fun onResume() {
        floatingVisitorVideoContainer.onResume()
        callController?.onResume()
        operatorVideoView?.resumeRendering()
        dialogCallback?.also { dialogController?.addCallback(it) }
        serviceChatHeadController?.onResume(this)
    }

    fun onPause() {
        floatingVisitorVideoContainer.onPause()
        operatorVideoView?.pauseRendering()
        dialogCallback?.also { dialogController?.removeCallback(it) }
        serviceChatHeadController?.onPause(this)
        callController?.onPause()
    }

    private fun destroyControllers() {
        callController = null
        dialogController = null
    }

    private fun setupControllers() {
        setController(Dependencies.controllerFactory.callController)
        dialogCallback = DialogContract.Controller.Callback {
            if (updateDialogState(it)) {
                when (it) {
                    DialogState.None -> resetDialogStateAndDismiss()
                    DialogState.ExitQueue -> post { showExitQueueDialog() }
                    DialogState.OverlayPermission -> post { showOverlayPermissionsDialog() }
                    DialogState.EndEngagement -> post { showEndEngagementDialog() }
                    DialogState.Confirmation -> post { callController?.onLiveObservationDialogRequested() }
                    else -> { /* noop */
                    }
                }
            }
        }
        dialogController = Dependencies.controllerFactory.dialogController
        serviceChatHeadController = Dependencies.controllerFactory.chatHeadController
    }

    override fun setController(controller: CallContract.Controller) {
        callController = controller
        controller.setView(this)
    }

    private fun setTitle(title: LocaleString) {
        onTitleUpdatedListener?.onTitleUpdated(title)
        appBar.setTitle(title)
    }

    private fun handleCallTimerView(callState: CallState) {
        callTimerView.isVisible = callState.showCallTimerView()
        callState.callStatus.time?.also(callTimerView::setText)
        callTimerView.setLocaleContentDescription(R.string.call_duration_accessibility_label)
    }

    private fun handleContinueBrowsingView(callState: CallState) {
        continueBrowsingView.isVisible =
            resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE &&
                callState.showContinueBrowsingView()

        continueBrowsingView.setLocaleText(
            if (callState.showOnHold()) R.string.call_on_hold_bottom_text else R.string.engagement_queue_wait_message
        )
    }

    private fun handleOperatorStatusViewState(state: CallState) {
        operatorStatusView.setShowRippleAnimation(state.showOperatorStatusViewRippleAnimation())
        operatorStatusView.setShowOnHold(state.showOnHold())
        if (state.isTransferring) {
            operatorStatusView.showTransferring()
            operatorNameView.setLocaleText(R.string.engagement_queue_transferring)
        } else {
            handleCompanyAndOperatorName(state)
            handleOperatorStatusViewOperatorImage(state)
        }
        operatorStatusView.isVisible = state.showOperatorStatusView()
    }

    private fun handleCompanyAndOperatorName(state: CallState) {
        // companyNameView is visible from queueing start till operator picks engagement
        companyNameView.setLocaleText(R.string.general_company_name)
        companyNameView.setLocaleHint(R.string.glia_call_company_name_hint)
        // operatorNameView is visible once operator picks engagement
        operatorNameView.text = state.callStatus.formattedOperatorName
        operatorNameView.setLocaleHint(R.string.chat_operator_name_accessibility_label)
    }

    private fun handleOperatorStatusViewOperatorImage(state: CallState) {
        if (state.isCallOngoingAndOperatorConnected) {
            showOperatorProfileImageOnConnected(state)
        } else if (state.isCallOngoingAndOperatorIsConnecting) {
            showOperatorProfileImageOnConnecting(state)
        } else {
            operatorStatusView.showPlaceholder()
            connectingView.setLocaleText(R.string.android_call_queue_message)
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

    private fun handleVisitorVideoState(state: CallState) {
        if (state.showVisitorVideo()) {
            showVisitorVideo(state)
        } else {
            hideVisitorVideo()
        }
        if (state.showOnHold()) {
            floatingVisitorVideoContainer.showOnHold()
        } else {
            floatingVisitorVideoContainer.hideOnHold()
        }
    }

    private fun handleOperatorVideoState(state: CallState) {
        if (state.showOperatorVideo() && operatorVideoContainer.isGone) {
            operatorVideoContainer.visibility = VISIBLE
            showOperatorVideo(state.callStatus.operatorMediaState)
        } else if (!state.showOperatorVideo() && operatorVideoContainer.isVisible) {
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

    private fun showExitQueueDialog() = showDialog {
        Dialogs.showExitQueueDialog(
            context = context,
            uiTheme = theme,
            positiveButtonClickListener = {
                resetDialogStateAndDismiss()
                callController?.endEngagementDialogYesClicked()
                onEndListener?.onEnd()
                callEnded()
            },
            negativeButtonClickListener = {
                resetDialogStateAndDismiss()
                callController?.endEngagementDialogDismissed()
            }
        )
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
        floatingActionButton.setLocaleContentDescription(contentDescription)
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
            buttonsLayoutBackground.isVisible = callState.landscapeLayoutControlsVisible && callState.isVideoCall
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
        appBarFade.secondaryAnimatorProvider = SlideDistanceProvider(if (fadeIn) Gravity.TOP else Gravity.BOTTOM)
        transitionSet.addTransition(appBarFade.addTarget(appBar))
        val buttonsFade = MaterialFade()
        buttonsFade.secondaryAnimatorProvider = SlideDistanceProvider(if (fadeIn) Gravity.BOTTOM else Gravity.TOP)
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
            poorConnectionView.typeface = it
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
        chatButtonLabel.setLocaleText(R.string.engagement_chat_title)
        speakerButtonLabel.setLocaleText(R.string.call_speaker_button)
        minimizeButtonLabel.setLocaleText(R.string.engagement_minimize_video_button)
        companyNameView.setLocaleText(R.string.general_company_name)
        videoButtonLabel.setLocaleText(R.string.engagement_video_title)
        callTheme?.topText.also(onHoldTextView::applyThemeAsDefault)
        callTheme?.duration.also(callTimerView::applyThemeAsDefault)
        callTheme?.operator.also(operatorNameView::applyThemeAsDefault)
        callTheme?.bottomText.also(continueBrowsingView::applyTextTheme)
        callTheme?.mediaQualityIndicator.also { poorConnectionView.applyTextTheme(it, withBackground = true) }

        // Hints and content descriptions
        operatorVideoContainer.setLocaleContentDescription(R.string.call_operator_video_accessibility_label)
        operatorNameView.setLocaleHint(R.string.chat_operator_name_accessibility_label)
        binding.callTimerView.setLocaleHint(R.string.call_duration_accessibility_label)
        minimizeButton.setLocaleContentDescription(R.string.engagement_minimize_video_button)
        operatorVideoContainer.setLocaleContentDescription(R.string.call_operator_video_accessibility_label)

        // Background
        callTheme?.background?.fill.also(::applyColorTheme)

        // Video
        floatingVisitorVideoContainer.setTheme(callTheme?.visitorVideo)
    }

    private fun setAppBarTheme() {
        val builder = UiThemeBuilder()
        builder.setTheme(theme)
        builder.setSystemNegativeColor(R.color.glia_negative_color)
        builder.setBaseLightColor(R.color.glia_light_color)
        builder.setBrandPrimaryColor(R.color.glia_call_view_background_color)
        builder.setGliaChatHeaderTitleTintColor(android.R.color.white)
        builder.setGliaChatHeaderHomeButtonTintColor(android.R.color.white)
        builder.setGliaChatHeaderExitQueueButtonTintColor(android.R.color.white)
        builder.setFontRes(theme.fontRes)
        appBar.setTheme(builder.build())
        appBar.applyHeaderTheme(callTheme?.header)
    }

    private fun initConfigurations() {
        visibility = INVISIBLE
        setBackgroundColor(getColorCompat(R.color.glia_call_view_background_color))
    }

    private fun readTypedArray(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        context.withStyledAttributes(attrs, R.styleable.GliaView, defStyleAttr, defStyleRes) {
            setDefaultTheme(this)
        }
    }

    private fun setDefaultTheme(typedArray: TypedArray) {
        theme = Utils.getThemeFromTypedArray(typedArray, this.context)
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

    fun setOnNavigateToWebBrowserListener(onNavigateToWebBrowserListener: OnNavigateToWebBrowserListener) {
        this.onNavigateToWebBrowserListener = onNavigateToWebBrowserListener
    }

    fun setOnTitleUpdatedListener(onTitleUpdatedListener: OnTitleUpdatedListener) {
        this.onTitleUpdatedListener = onTitleUpdatedListener
    }

    private fun showUIOnCallOngoing() {
        visibility = VISIBLE
    }

    private fun hideUIOnCallEnd() {
        visibility = INVISIBLE
        hideOperatorVideo()
        hideVisitorVideo()
        insetsController?.hideKeyboard()
    }

    private fun showEndEngagementDialog() = showDialog {
        Dialogs.showEndEngagementDialog(
            context = context,
            uiTheme = theme,
            positiveButtonClickListener = {
                resetDialogStateAndDismiss()
                callController?.endEngagementDialogYesClicked()
            },
            negativeButtonClickListener = {
                resetDialogStateAndDismiss()
                callController?.endEngagementDialogDismissed()
            }
        )
    }

    override fun showMissingPermissionsDialog() = showDialog {
        Dialogs.showMissingPermissionsDialog(context, theme) {
            resetDialogStateAndDismiss()
            callController?.unexpectedErrorDialogDismissed()
            onEndListener?.onEnd()
        }
    }

    override fun showEngagementConfirmationDialog() {
        callController?.confirmationDialogLinks?.let { links ->
            showDialog {
                Dialogs.showEngagementConfirmationDialog(
                    context = context,
                    theme = theme,
                    links = links,
                    positiveButtonClickListener = {
                        resetDialogStateAndDismiss()
                        callController?.onLiveObservationDialogAllowed()
                    },
                    negativeButtonClickListener = {
                        resetDialogStateAndDismiss()
                        callController?.onLiveObservationDialogRejected()
                        onEndListener?.onEnd()
                        callEnded()
                    },
                    linkClickListener = { callController?.onLinkClicked(it) }
                )
            }
        }
    }

    private fun showOverlayPermissionsDialog() = showDialog {
        Dialogs.showOverlayPermissionsDialog(
            context = context,
            uiTheme = theme,
            positiveButtonClickListener = {
                resetDialogStateAndDismiss()
                callController?.overlayPermissionsDialogDismissed()
                activityLauncher.launchOverlayPermission(context)
            },
            negativeButtonClickListener = {
                resetDialogStateAndDismiss()
                callController?.overlayPermissionsDialogDismissed()
            }
        )
    }

    private fun callEnded() {
        Dependencies.destroyControllers()
    }

    private fun showVisitorVideo(state: CallState) {
        floatingVisitorVideoContainer.show(state.callStatus.visitorMediaState)
        floatingVisitorVideoContainer.showFlipCameraButton(state.flipButtonState)
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
                GliaLogger.i(LogEvents.CALL_SCREEN_OPERATOR_VIDEO_SHOWN)
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

    override fun showToast(message: String) {
        post { context.showToast(message, Toast.LENGTH_SHORT) }
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

    fun interface OnNavigateToWebBrowserListener {
        fun openLink(title: LocaleString, url: String)
    }

    fun interface OnTitleUpdatedListener {
        fun onTitleUpdated(title: LocaleString?)
    }

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
            connectingView.setLocaleText(R.string.android_call_queue_message)

            handleCallTimerView(callState)

            // No need to manage the remaining view's states if only time has changed
            if (callState.isOnlyTimeChanged) return@post

            setupEndButton(callState)

            callState.isCurrentCallVideo?.also {
                if (it) {
                    setTitle(LocaleString(R.string.engagement_video_title))
                } else {
                    setTitle(LocaleString(R.string.engagement_audio_title))
                }
            }

            operatorNameView.text = callState.callStatus.formattedOperatorName
            connectingView.setLocaleContentDescription(
                R.string.engagement_connection_screen_connect_with,
                StringKeyPair(StringKey.OPERATOR_NAME, callState.callStatus.formattedOperatorName ?: ""),
                StringKeyPair(StringKey.BADGE_VALUE, "")
            )
            companyNameView.setLocaleText(R.string.general_company_name)
            companyNameView.setLocaleHint(R.string.general_company_name)
            msrView.setLocaleText(R.string.android_call_queue_message)
            chatButtonBadgeView.text = callState.messagesNotSeen.toString()

            //according to design, in Landscape mode + video call, the controls should be semi-transparent
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE && callState.isVideoCall) {
                buttonsLayoutBackground.alpha = CONTROLS_ALPHA_SEMI_TRANSPARENT
                appBar.alpha = CONTROLS_ALPHA_SEMI_TRANSPARENT
            } else {
                buttonsLayoutBackground.alpha = CONTROLS_ALPHA
                appBar.alpha = CONTROLS_ALPHA
            }

            callState.isVideoButtonEnabled.also {
                videoButton.isEnabled = it
                videoButtonLabel.isEnabled = it
            }

            // This should be before setButtonActivated, because FAB is unable to update the drawable when the state is changed
            // So the next step(setButtonActivated) sets/re-sets the correct drawable that solves the issue.
            applyViewState(callState.speakerButtonViewState, speakerButton, speakerButtonLabel)
            setButtonActivated(
                videoButton,
                theme.iconCallVideoOn,
                theme.iconCallVideoOff,
                R.string.android_call_turn_video_off_button_accessibility,
                R.string.android_call_turn_video_on_button_accessibility,
                callState.hasVideo
            )
            videoButtonLabel.isActivated = callState.hasVideo

            // This should be before setButtonActivated, because FAB is unable to update the drawable when the state is changed
            // So the next step(setButtonActivated) sets/re-sets the correct drawable that solves the issue.
            applyViewState(callState.muteButtonViewState, muteButton, muteButtonLabel)
            setButtonActivated(
                muteButton,
                theme.iconCallAudioOff, // mute (e.g., mic-off) button activated icon
                theme.iconCallAudioOn, // mute (e.g., mic-off) button deactivated icon
                R.string.android_call_unmute_button_accessibility,
                R.string.android_call_mute_button_accessibility,
                callState.isMuted
            )
            muteButtonLabel.isActivated = callState.isMuted
            muteButtonLabel.setLocaleText(
                if (callState.isMuted) R.string.call_unmute_button else R.string.call_mute_button
            )
            onHoldTextView.setLocaleText(R.string.call_on_hold_icon)

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
            handleVisitorVideoState(callState)
            handleControlsVisibility(callState)
            onIsSpeakerOnStateChanged(callState.isSpeakerOn)
            if (callState.isVisible) {
                showUIOnCallOngoing()
            } else {
                hideUIOnCallEnd()
            }

            callState.chatButtonViewState.apply {
                if (this != ViewState.HIDE) {
                    chatButtonBadgeView.isVisible = callState.messagesNotSeen > 0
                }
                applyViewState(this, chatButton, chatButtonLabel)
                // Re-set the drawable to force FAB to refresh icon
                theme.iconCallChat?.apply(chatButton::setImageResource)
            }

            chatButton.setContentDescription(
                when (callState.messagesNotSeen) {
                    0 -> LocaleString(R.string.engagement_chat_title)
                    1 -> LocaleString(
                        R.string.call_buttons_chat_badge_value_single_item_accessibility_label,
                        StringKeyPair(StringKey.BADGE_VALUE, callState.messagesNotSeen.toString())
                    )

                    else -> LocaleString(
                        R.string.call_buttons_chat_badge_value_multiple_items_accessibility_label,
                        StringKeyPair(StringKey.BADGE_VALUE, callState.messagesNotSeen.toString())
                    )
                }
            )
            applyTextThemeBasedOnCallState(callState)
            poorConnectionView.isVisible = callState.isMediaQualityPoor
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

    override fun navigateToWebBrowserActivity(title: LocaleString, url: String) {
        onNavigateToWebBrowserListener?.openLink(title, url)
    }

    override fun destroyView() {
        onEndListener?.onEnd()
    }

    override fun minimizeView() {
        onMinimizeListener?.onMinimize()
    }

    @VisibleForTesting
    internal var executor: Executor? = null

    override fun post(action: Runnable?): Boolean {
        return executor?.execute(action)?.let { true } ?: super.post(action)
    }

    override fun showConnectionSnackBar() {
        snackBarDelegate = makeNoConnectionSnackBar(context.requireActivity()).apply {
            show()
        }
    }

    override fun dismissConnectionSnackBar() {
        snackBarDelegate?.dismiss()
        snackBarDelegate = null
    }
}
