package com.glia.widgets.chat

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.TypedArray
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.withStyledAttributes
import androidx.core.view.ViewCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.chat.AttachmentFile
import com.glia.androidsdk.engagement.Survey
import com.glia.androidsdk.screensharing.ScreenSharing
import com.glia.widgets.GliaWidgets
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.adapter.ChatAdapter
import com.glia.widgets.chat.adapter.ChatAdapter.*
import com.glia.widgets.chat.adapter.UploadAttachmentAdapter
import com.glia.widgets.chat.adapter.holder.WebViewViewHolder
import com.glia.widgets.chat.controller.ChatController
import com.glia.widgets.chat.helper.FileHelper
import com.glia.widgets.chat.model.ChatInputMode
import com.glia.widgets.chat.model.ChatState
import com.glia.widgets.chat.model.history.ChatItem
import com.glia.widgets.chat.model.history.OperatorAttachmentItem
import com.glia.widgets.chat.model.history.VisitorAttachmentItem
import com.glia.widgets.core.configuration.GliaSdkConfiguration
import com.glia.widgets.core.dialog.Dialog
import com.glia.widgets.core.dialog.DialogController
import com.glia.widgets.core.dialog.model.DialogState.MediaUpgrade
import com.glia.widgets.core.dialog.model.DialogState.OperatorName
import com.glia.widgets.core.fileupload.model.FileAttachment
import com.glia.widgets.core.notification.device.NotificationManager
import com.glia.widgets.core.screensharing.ScreenSharingController
import com.glia.widgets.databinding.ChatViewBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.filepreview.ui.FilePreviewActivity
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.SimpleTextWatcher
import com.glia.widgets.helper.Utils
import com.glia.widgets.view.Dialogs
import com.glia.widgets.view.SingleChoiceCardView.OnOptionClickedListener
import com.glia.widgets.view.head.controller.ServiceChatHeadController
import com.glia.widgets.view.unifiedui.exstensions.*
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.glia.widgets.view.unifiedui.theme.base.HeaderTheme
import com.glia.widgets.view.unifiedui.theme.chat.InputTheme
import com.glia.widgets.view.unifiedui.theme.chat.UnreadIndicatorTheme
import com.google.android.material.shape.MarkerEdgeTreatment
import com.google.android.material.theme.overlay.MaterialThemeOverlay
import java.io.File
import java.io.IOException
import kotlin.properties.Delegates

class ChatView(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
    ConstraintLayout(
        MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, defStyleRes),
        attrs,
        defStyleAttr,
        defStyleRes
    ), OnFileItemClickListener, OnImageItemClickListener {

    @JvmOverloads
    constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.gliaChatStyle
    ) : this(context, attrs, defStyleAttr, R.style.Application_Glia_Chat)

    private var alertDialog: AlertDialog? = null
    private var callback: ChatViewCallback? = null
    private var controller: ChatController? = null
    private var dialogCallback: DialogController.Callback? = null
    private var dialogController: DialogController? = null
    private val screenSharingViewCallback = object: ScreenSharingController.ViewCallback {
        override fun onScreenSharingRequestError(ex: GliaException?) {
            ex?.run { showToast(this.debugMessage) }
        }

        override fun onScreenSharingStarted() {
            Handler(Looper.getMainLooper()).post { binding.appBarView.showEndScreenSharingButton() }
        }
    }
    private var screenSharingController: ScreenSharingController? = null
    private var serviceChatHeadController: ServiceChatHeadController? = null

    private var uploadAttachmentAdapter by Delegates.notNull<UploadAttachmentAdapter>()
    private var adapter by Delegates.notNull<ChatAdapter>()

    private var isInBottom = true
    private var downloadFileHolder: AttachmentFile? = null
    private var theme: UiTheme by Delegates.notNull()

    // needed for setting status bar color back when view is gone
    private var defaultStatusBarColor: Int? = null
    private var statusBarColor: Int by Delegates.notNull()
    private var onTitleUpdatedListener: OnTitleUpdatedListener? = null
    private var onBackClickedListener: OnBackClickedListener? = null
    private var onEndListener: OnEndListener? = null
    private var onMinimizeListener: OnMinimizeListener? = null
    private var onNavigateToCallListener: OnNavigateToCallListener? = null
    private var onNavigateToSurveyListener: OnNavigateToSurveyListener? = null
    private var onBackToCallListener: OnBackToCallListener? = null
    private val onOptionClickedListener = OnOptionClickedListener { id, indexInList, optionIndex ->
        Logger.d(TAG, "singleChoiceCardClicked")
        controller?.singleChoiceOptionClicked(id, indexInList, optionIndex)
    }
    private val onScrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                controller?.onRecyclerviewPositionChanged(!recyclerView.canScrollVertically(1))
            }
        }
    private val onCustomCardResponse =
        OnCustomCardResponse { messageId: String, text: String?, value: String? ->
            controller?.sendCustomCardResponse(messageId, text, value)
        }
    private val dataObserver: AdapterDataObserver = object : AdapterDataObserver() {
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            super.onItemRangeInserted(positionStart, itemCount)
            val totalItemCount = adapter.itemCount
            val lastIndex = totalItemCount - 1
            if (isInBottom) {
                val holder = binding.chatRecyclerView.findViewHolderForAdapterPosition(lastIndex)
                if (holder is WebViewViewHolder) {
                    // WebView needs time for calculating the height.
                    // So to scroll to the bottom we need to do it with delay.
                    postDelayed(
                        { binding.chatRecyclerView.scrollToPosition(lastIndex) },
                        WEB_VIEW_INITIALIZATION_DELAY.toLong()
                    )
                } else {
                    binding.chatRecyclerView.scrollToPosition(lastIndex)
                }
            }
        }
    }
    private val textWatcher: TextWatcher = object : SimpleTextWatcher() {
        override fun afterTextChanged(editable: Editable) {
            controller?.onMessageTextChanged(editable.toString().trim { it <= ' ' })
        }
    }

    private var binding by Delegates.notNull<ChatViewBinding>()
    private val attachmentPopup by lazy { AttachmentPopup(binding.chatMessageLayout) }

    init {
        initConfigurations()
        bindViews()
        readTypedArray(attrs, defStyleAttr, defStyleRes)
        setupViewAppearance()
        setupViewActions()
        setupControllers()
    }

    /**
     * @param uiTheme sets this view's appearance using the parameters provided in the
     * [com.glia.widgets.UiTheme]
     */
    fun setUiTheme(uiTheme: UiTheme?) {
        if (uiTheme == null) return
        theme = Utils.getFullHybridTheme(uiTheme, theme)
        theme.brandPrimaryColor?.let(::getColorCompat)?.also { statusBarColor = it }
        setupViewAppearance()
        if (isVisible) {
            handleStatusBarColor()
        }
    }

    /**
     * Used to start the chat functionality.
     *
     * @param companyName Text shown in the chat while waiting in a queue.
     * @param queueId     The queue id to which you would like to queue to and speak to operators from.
     * @param visitorContextAssetId  Provide some context asset ID as to from where are you initiating the chat from.
     * @param useOverlays Used to set if the user opted to use overlays or not.
     * @see [com.glia.widgets.GliaWidgets].USE_OVERLAY to see its full usage description.
     * Important! This parameter is ignored if the view is not used in the sdk's [ChatActivity]
     */
    @JvmOverloads
    fun startChat(
        companyName: String?,
        queueId: String?,
        visitorContextAssetId: String?,
        useOverlays: Boolean = false,
        screenSharingMode: ScreenSharing.Mode? = null,
        chatType: ChatType = ChatType.LIVE_CHAT
    ) {
        Dependencies.getSdkConfigurationManager()?.isUseOverlay = useOverlays
        Dependencies.getSdkConfigurationManager()?.screenSharingMode = screenSharingMode
        controller?.initChat(companyName, queueId, visitorContextAssetId, chatType)
        serviceChatHeadController?.init()
    }

    /**
     * Used to force the view to be visible. Will show the current state of the view.
     */
    fun show() {
        controller?.show()
    }

    /**
     * Used to tell the view that the user has pressed the back button so that the view can
     * set its state accordingly.
     */
    fun backPressed(): Boolean {
        controller?.onBackArrowClicked()
        return true
    }

    fun setOnTitleUpdatedListener(onTitleUpdatedListener: OnTitleUpdatedListener?) {
        this.onTitleUpdatedListener = onTitleUpdatedListener
    }

    /**
     * Add a listener here if you wish to be notified when the user clicks the up button on the
     * appbar.
     *
     * @param onBackClicked The callback which is fired when the button is clicked.
     */
    fun setOnBackClickedListener(onBackClicked: OnBackClickedListener?) {
        onBackClickedListener = onBackClicked
    }

    /**
     * Add a listener here to be notified if for any reason the chat should end.
     *
     * @param onEndListener The callback which is fired when the chat ends.
     */
    fun setOnEndListener(onEndListener: OnEndListener?) {
        this.onEndListener = onEndListener
    }

    fun setOnMinimizeListener(onMinimizeListener: OnMinimizeListener?) {
        this.onMinimizeListener = onMinimizeListener
    }

    /**
     * Add a listener here for when the user has accepted an audio or video call and should navigate
     * to a call.
     * Important! Should be used together with [.navigateToCallSuccess] to notify the view
     * of a completed navigation.
     *
     * @param onNavigateToCallListener The callback which is fired when the user accepts a media
     * upgrade offer.
     */
    fun setOnNavigateToCallListener(onNavigateToCallListener: OnNavigateToCallListener?) {
        this.onNavigateToCallListener = onNavigateToCallListener
    }

    fun setOnNavigateToSurveyListener(onNavigateToSurveyListener: OnNavigateToSurveyListener?) {
        this.onNavigateToSurveyListener = onNavigateToSurveyListener
    }

    fun setOnBackToCallListener(onBackToCallListener: OnBackToCallListener?) {
        this.onBackToCallListener = onBackToCallListener
    }

    /**
     * Use this method to notify the view when your activity or fragment is back in its resumed
     * state.
     */
    fun onResume() {
        controller?.onResume()
        screenSharingController?.setViewCallback(screenSharingViewCallback)
        screenSharingController?.onResume(this.context)
        dialogController?.addCallback(dialogCallback)
        serviceChatHeadController?.onResume(this)
    }

    fun onPause() {
        controller?.onPause()
        screenSharingController?.removeViewCallback(screenSharingViewCallback)
        dialogController?.removeCallback(dialogCallback)
        serviceChatHeadController?.onPause(this)
    }

    /**
     * Use this method to notify the view that your activity or fragment's view is being destroyed.
     * Used to dispose of any loose resources.
     *
     * @param isFinishing - indicates if activity is being recreated - "isFinishing = false" or is being completely destroyed - "isFinishing = true"
     */
    fun onDestroyView(isFinishing: Boolean) {
        alertDialog?.dismiss()
        alertDialog = null

        onEndListener = null
        onBackClickedListener = null
        onNavigateToCallListener = null
        onNavigateToSurveyListener = null
        destroyController()
        callback = null
        adapter.unregisterAdapterDataObserver(dataObserver)
        binding.chatRecyclerView.adapter = null
        binding.chatRecyclerView.removeOnScrollListener(onScrollListener)
        binding.addAttachmentQueue.adapter = null
        if (isFinishing) serviceChatHeadController?.onDestroy()
        dialogController = null
    }

    /**
     * Use this method together with [.setOnNavigateToCallListener]
     * to notify the view that you have finished navigating.
     */
    fun navigateToCallSuccess() {
        controller?.navigateToCallSuccess()
    }

    private fun setupControllers() {
        setupChatStateCallback()
        controller = Dependencies.getControllerFactory().getChatController(callback)
        setupDialogCallback()
        dialogController = Dependencies.getControllerFactory().dialogController
        screenSharingController = Dependencies.getControllerFactory().screenSharingController
        serviceChatHeadController = Dependencies.getControllerFactory().chatHeadController
    }

    private fun setupChatStateCallback() {
        callback = object : ChatViewCallback {
            override fun emitUploadAttachments(attachments: List<FileAttachment>) {
                post { uploadAttachmentAdapter.submitList(attachments) }
            }

            override fun emitState(chatState: ChatState) {
                // some state updates on core-sdk are coming from the computation thread
                // need to update state on uiThread
                post {
                    updateShowSendButton(chatState)
                    updateChatEditText(chatState)
                    updateAppBar(chatState)
                    binding.newMessagesIndicatorLayout.isVisible =
                        chatState.showMessagesUnseenIndicator()
                    updateNewMessageOperatorStatusView(chatState.operatorProfileImgUrl)
                    isInBottom = chatState.isChatInBottom
                    binding.chatRecyclerView.setInBottom(isInBottom)
                    binding.newMessagesBadgeView.text = chatState.messagesNotSeen.toString()
                    if (chatState.isVisible) {
                        showChat()
                    } else {
                        hideChat()
                    }

                    binding.operatorTypingAnimationView.isVisible = chatState.isOperatorTyping
                    updateAttachmentButton(chatState)
                }
            }

            override fun emitItems(items: List<ChatItem>) {
                val updatedItems = items
                    .asSequence()
                    .map { chatItem: ChatItem -> updateIsFileDownloaded(chatItem) }
                    .toList()
                post { adapter.submitList(updatedItems) }
            }

            override fun navigateToCall(mediaType: String) {
                onNavigateToCallListener?.call(theme, mediaType)
            }

            override fun backToCall() {
                onBackToCallListener?.onBackToCall()
            }

            override fun navigateToSurvey(survey: Survey) {
                onNavigateToSurveyListener?.onSurvey(theme, survey)
            }

            override fun destroyView() {
                onEndListener?.onEnd()
            }

            override fun minimizeView() {
                onMinimizeListener?.onMinimize()
            }

            override fun smoothScrollToBottom() {
                post { binding.chatRecyclerView.smoothScrollToPosition(adapter.itemCount - 1) }
            }

            override fun scrollToBottomImmediate() {
                post { binding.chatRecyclerView.scrollToPosition(adapter.itemCount - 1) }
            }

            override fun fileDownloadError(attachmentFile: AttachmentFile, error: Throwable) {
                fileDownloadFailed(attachmentFile)
            }

            override fun fileDownloadSuccess(attachmentFile: AttachmentFile) {
                fileDownloadCompleted(attachmentFile)
            }

            override fun clearMessageInput() {
                post {
                    binding.chatEditText.apply {
                        removeTextChangedListener(textWatcher)
                        text.clear()
                        addTextChangedListener(textWatcher)
                    }
                }
            }
        }
    }

    private fun updateNewMessageOperatorStatusView(operatorProfileImgUrl: String?) {
        binding.newMessagesIndicatorImage.apply {
            operatorProfileImgUrl?.also(::showProfileImage) ?: showPlaceholder()
        }
    }

    private fun setupDialogCallback() {
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
                Dialog.MODE_VISITOR_CODE -> { Logger.e(TAG, "DialogController callback in ChatView with MODE_VISITOR_CODE")} // Should never happen inside ChatView
            }
        }
    }

    private fun updateAttachmentButton(chatState: ChatState) {
        binding.addAttachmentButton.isEnabled = chatState.isAttachmentButtonEnabled
        binding.addAttachmentButton.isVisible = chatState.isAttachmentButtonVisible
    }

    private fun updateIsFileDownloaded(item: ChatItem): ChatItem {
        return when (item) {
            is OperatorAttachmentItem -> {
                val isFileDownloaded = FileHelper.isFileDownloaded(item.attachmentFile)
                OperatorAttachmentItem(
                    item.id,
                    item.viewType,
                    item.showChatHead,
                    item.attachmentFile,
                    item.operatorProfileImgUrl,
                    isFileDownloaded,
                    item.isDownloading,
                    item.operatorId,
                    item.messageId,
                    item.timestamp
                )
            }
            is VisitorAttachmentItem -> {
                val isFileDownloaded = FileHelper.isFileDownloaded(item.attachmentFile)
                VisitorAttachmentItem.editDownloadedStatus(item, isFileDownloaded)
            }
            else -> {
                item
            }
        }
    }

    private fun updateShowSendButton(chatState: ChatState) {
        if (chatState.showSendButton == binding.sendButton.isVisible) return

        binding.sendButton.isVisible = chatState.showSendButton
    }

    private fun updateChatEditText(chatState: ChatState) {
        when (chatState.chatInputMode) {
            ChatInputMode.SINGLE_CHOICE_CARD -> binding.chatEditText.setHint(R.string.glia_chat_single_choice_card_hint)
            ChatInputMode.ENABLED_NO_ENGAGEMENT -> binding.chatEditText.setHint(R.string.glia_chat_not_started_hint)
            else -> binding.chatEditText.setHint(R.string.glia_chat_enter_message)
        }
        binding.chatEditText.isEnabled = chatState.chatInputMode.isEnabled
    }

    private fun updateAppBar(chatState: ChatState) {
        if (chatState.isOperatorOnline) {
            binding.appBarView.showEndButton()
        } else if (chatState.engagementRequested) {
            binding.appBarView.showXButton()
        } else {
            binding.appBarView.hideLeaveButtons()
        }
        if (screenSharingController?.isSharingScreen == true) {
            binding.appBarView.showEndScreenSharingButton()
        } else {
            binding.appBarView.hideEndScreenSharingButton()
        }
        if (chatState.isSecureMessaging) {
            showToolbar(resources.getString(R.string.glia_messaging_title))
        } else {
            showToolbar(theme.appBarTitle)
        }
    }

    private fun showAllowScreenSharingNotificationsAndStartSharingDialog() {
        if (alertDialog == null || !alertDialog!!.isShowing) {
            alertDialog = Dialogs.showOptionsDialog(
                context = this.context,
                theme = theme,
                title = resources.getString(R.string.glia_dialog_screen_sharing_offer_enable_notifications_title),
                message = resources.getString(R.string.glia_dialog_screen_sharing_offer_enable_notifications_message),
                positiveButtonText = resources.getString(R.string.glia_dialog_screen_sharing_offer_enable_notifications_yes),
                negativeButtonText = resources.getString(R.string.glia_dialog_screen_sharing_offer_enable_notifications_no),
                positiveButtonClickListener = {
                    dismissAlertDialog()
                    NotificationManager.openNotificationChannelScreen(this.context)
                },
                negativeButtonClickListener = {
                    dismissAlertDialog()
                    controller?.notificationsDialogDismissed()
                    screenSharingController?.onScreenSharingDeclined()
                },
                cancelListener = {
                    it.dismiss()
                    controller?.notificationsDialogDismissed()
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
                title = resources.getString(R.string.glia_dialog_allow_notifications_title),
                message = resources.getString(R.string.glia_dialog_allow_notifications_message),
                positiveButtonText = resources.getString(R.string.glia_dialog_allow_notifications_yes),
                negativeButtonText = resources.getString(R.string.glia_dialog_allow_notifications_no),
                positiveButtonClickListener = {
                    dismissAlertDialog()
                    controller?.notificationsDialogDismissed()
                    NotificationManager.openNotificationChannelScreen(this.context)
                },
                negativeButtonClickListener = {
                    dismissAlertDialog()
                    controller?.notificationsDialogDismissed()
                },
                cancelListener = {
                    it.dismiss()
                    controller?.notificationsDialogDismissed()
                }
            )
        }
    }

    private fun showScreenSharingDialog() {
        if (alertDialog == null || !alertDialog!!.isShowing) {
            alertDialog = Dialogs.showScreenSharingDialog(
                context,
                theme,
                resources.getText(R.string.glia_dialog_screen_sharing_offer_title).toString(),
                resources.getText(R.string.glia_dialog_screen_sharing_offer_message).toString(),
                R.string.glia_dialog_screen_sharing_offer_accept,
                R.string.glia_dialog_screen_sharing_offer_decline,
                { screenSharingController?.onScreenSharingAccepted(context) }
            ) { screenSharingController?.onScreenSharingDeclined() }
        }
    }

    private fun showChat() {
        visibility = VISIBLE
        handleStatusBarColor()
    }

    private fun hideChat() {
        visibility = INVISIBLE
        val activity = Utils.getActivity(this.context)
        if (defaultStatusBarColor != null && activity != null) {
            activity.window.statusBarColor = defaultStatusBarColor!!
            defaultStatusBarColor = null
        }
        Utils.hideSoftKeyboard(this.context, windowToken)
    }

    private fun showToolbar(title: String?) {
        onTitleUpdatedListener?.onTitleUpdated(title)

        binding.appBarView.setTitle(title)
        binding.appBarView.setVisibility(title != null)
    }

    private fun destroyController() {
        controller?.onDestroy(Utils.getActivity(this.context) is ChatActivity)
        controller = null
    }

    private fun readTypedArray(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        context.withStyledAttributes(attrs, R.styleable.GliaView, defStyleAttr, defStyleRes) {
            setDefaultTheme(this)
        }
    }

    private fun setDefaultTheme(typedArray: TypedArray) {
        theme = Utils.getThemeFromTypedArray(typedArray, this.context)
        theme = Utils.getFullHybridTheme(Dependencies.getSdkConfigurationManager()?.uiTheme, theme)
        theme.brandPrimaryColor?.let(::getColorCompat)?.also { statusBarColor = it }
    }

    private fun initConfigurations() {
        isInvisible = true
        // needed to overlap existing app bar in existing view with this view's app bar.
        ViewCompat.setElevation(this, 100.0f)
    }

    private fun bindViews() {
        binding = ChatViewBinding.inflate(layoutInflater, this)
    }

    private fun setupViewAppearance() {
        adapter = ChatAdapter(
            theme,
            onOptionClickedListener,
            this,
            this,
            onCustomCardResponse,
            GliaWidgets.getCustomCardAdapter(),
            Dependencies.getUseCaseFactory().createGetImageFileFromCacheUseCase(),
            Dependencies.getUseCaseFactory().createGetImageFileFromDownloadsUseCase(),
            Dependencies.getUseCaseFactory().createGetImageFileFromNetworkUseCase()
        )
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this.context)
        adapter.registerAdapterDataObserver(dataObserver)
        binding.chatRecyclerView.adapter = adapter
        binding.chatRecyclerView.addOnScrollListener(onScrollListener)
        uploadAttachmentAdapter = UploadAttachmentAdapter()
        uploadAttachmentAdapter.setItemCallback { controller?.onRemoveAttachment(it) }
        uploadAttachmentAdapter.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                binding.addAttachmentQueue.smoothScrollToPosition(uploadAttachmentAdapter.itemCount)
            }
        })
        binding.addAttachmentQueue.layoutManager = LinearLayoutManager(this.context)
        binding.addAttachmentQueue.adapter = uploadAttachmentAdapter
        binding.appBarView.setTheme(theme)


        //icons
        theme.iconSendMessage?.also(binding.sendButton::setImageResource)

        // new messages indicator shape
        val shapeAppearanceModel = binding.newMessagesIndicatorCard.shapeAppearanceModel
            .toBuilder()
            .setBottomEdge(MarkerEdgeTreatment(resources.getDimension(R.dimen.glia_chat_new_messages_bottom_edge_radius)))
            .build()

        binding.newMessagesIndicatorImage.setShowRippleAnimation(false)
        binding.newMessagesIndicatorImage.setTheme(theme)
        binding.newMessagesIndicatorCard.shapeAppearanceModel = shapeAppearanceModel
        theme.brandPrimaryColor?.let(::getColorStateListCompat)?.also {
            binding.newMessagesBadgeView.backgroundTintList = it
        }
        theme.baseLightColor?.let(::getColorCompat)
            ?.also(binding.newMessagesBadgeView::setTextColor)

        // colors
        theme.baseShadeColor?.let(::getColorCompat)?.also(binding.dividerView::setBackgroundColor)

        theme.brandPrimaryColor?.let(::getColorStateListCompat)?.also {
            binding.newMessagesBadgeView.backgroundTintList = it
        }
        binding.sendButton.imageTintList =
            theme.sendMessageButtonTintColor?.let(::getColorStateListCompat)

        theme.baseDarkColor?.let(::getColorCompat)?.also(binding.chatEditText::setTextColor)
        theme.baseNormalColor?.let(::getColorCompat)?.also(binding.chatEditText::setHintTextColor)

        theme.gliaChatBackgroundColor?.let(::getColorCompat)?.also(::setBackgroundColor)

        // fonts
        theme.fontRes?.also { binding.chatEditText.typeface = getFontCompat(it) }

        theme.brandPrimaryColor?.also {
            binding.operatorTypingAnimationView.addColorFilter(color = it)
        }

        applyTheme(Dependencies.getGliaThemeManager().theme)
    }

    private fun handleStatusBarColor() {
        val activity = Utils.getActivity(this.context)
        if (activity != null && defaultStatusBarColor == null) {
            defaultStatusBarColor = activity.window.statusBarColor
            if (controller != null && controller!!.isChatVisible) {
                activity.window.statusBarColor = statusBarColor
            }
        }
    }

    private fun setupViewActions() {
        binding.chatEditText.addTextChangedListener(textWatcher)
        binding.sendButton.setOnClickListener {
            val message = binding.chatEditText.text.toString().trim { it <= ' ' }
            controller?.sendMessage(message)
        }
        setupAddAttachmentButton()
        binding.appBarView.setOnBackClickedListener { controller?.onBackArrowClicked(onBackClickedListener) }
        binding.appBarView.setOnEndChatClickedListener { controller?.leaveChatClicked() }
        binding.appBarView.setOnEndCallButtonClickedListener {
            screenSharingController?.onForceStopScreenSharing()
            binding.appBarView.hideEndScreenSharingButton()
        }
        binding.appBarView.setOnXClickedListener { controller?.leaveChatQueueClicked() }
        binding.newMessagesIndicatorCard.setOnClickListener { controller?.newMessagesIndicatorClicked() }
    }

    private fun setupAddAttachmentButton() {
        binding.addAttachmentButton.setOnClickListener {
            attachmentPopup.show(binding.chatMessageLayout, {
                val intent = Intent()
                intent.type = INTENT_TYPE_IMAGES
                intent.action = Intent.ACTION_OPEN_DOCUMENT
                Utils.getActivity(context)?.startActivityForResult(
                    Intent.createChooser(
                        intent,
                        resources.getString(R.string.glia_chat_select_picture_title)
                    ),
                    OPEN_DOCUMENT_ACTION_REQUEST
                )
            }, {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    dispatchImageCapture()
                } else {
                    Utils.getActivity(context)?.requestPermissions(
                        arrayOf(Manifest.permission.CAMERA),
                        CAMERA_PERMISSION_REQUEST
                    )
                }
            }, {
                val intent = Intent()
                intent.type = INTENT_TYPE_ALL
                intent.action = Intent.ACTION_OPEN_DOCUMENT
                Utils.getActivity(context)?.startActivityForResult(
                    Intent.createChooser(
                        intent,
                        resources.getString(R.string.glia_chat_select_file_title)
                    ),
                    OPEN_DOCUMENT_ACTION_REQUEST
                )
            })
        }
    }

    private fun dispatchImageCapture() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        var photoFile: File? = null
        try {
            photoFile = Utils.createTempPhotoFile(context)
        } catch (exception: IOException) {
            Logger.e(TAG, "Create photo file failed: " + exception.message)
        }
        if (photoFile != null) {
            controller?.photoCaptureFileUri = FileProvider.getUriForFile(
                context,
                FileHelper.getFileProviderAuthority(context),
                photoFile
            )
            if (controller?.photoCaptureFileUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, controller!!.photoCaptureFileUri)
                Utils.getActivity(context)?.startActivityForResult(
                    intent,
                    CAPTURE_IMAGE_ACTION_REQUEST
                )
            }
        }
    }

    private fun showExitQueueDialog() {
        alertDialog = Dialogs.showOptionsDialog(
            context = context,
            theme = theme,
            title = resources.getString(R.string.glia_dialog_leave_queue_title),
            message = resources.getString(R.string.glia_dialog_leave_queue_message),
            positiveButtonText = resources.getString(R.string.glia_dialog_leave_queue_yes),
            negativeButtonText = resources.getString(R.string.glia_dialog_leave_queue_no),
            positiveButtonClickListener = {
                dismissAlertDialog()
                controller?.endEngagementDialogYesClicked()
                onEndListener?.onEnd()
                chatEnded()
            },
            negativeButtonClickListener = {
                dismissAlertDialog()
                controller?.endEngagementDialogDismissed()
            },
            cancelListener = {
                it.dismiss()
                controller?.endEngagementDialogDismissed()
            },
            isButtonsColorsReversed = true
        )
    }

    private fun showEndEngagementDialog(operatorName: String) {
        alertDialog = Dialogs.showOptionsDialog(
            context = context,
            theme = theme,
            title = resources.getString(R.string.glia_dialog_end_engagement_title),
            message = resources.getString(
                R.string.glia_dialog_end_engagement_message,
                operatorName
            ),
            positiveButtonText = resources.getString(R.string.glia_dialog_end_engagement_yes),
            negativeButtonText = resources.getString(R.string.glia_dialog_end_engagement_no),
            positiveButtonClickListener = {
                dismissAlertDialog()
                controller?.endEngagementDialogYesClicked()
            },
            negativeButtonClickListener = {
                dismissAlertDialog()
                controller?.endEngagementDialogDismissed()
            },
            cancelListener = {
                controller?.endEngagementDialogDismissed()
                it.dismiss()
            },
            isButtonsColorsReversed = true
        )
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
        @StringRes title: Int, @StringRes message: Int, buttonClickListener: OnClickListener
    ) {
        alertDialog?.dismiss()
        alertDialog = null
        alertDialog = Dialogs.showAlertDialog(context, theme, title, message, buttonClickListener)
    }

    private fun showEngagementEndedDialog() {
        alertDialog?.dismiss()
        alertDialog = null
        alertDialog = Dialogs.showOperatorEndedEngagementDialog(context, theme) {
            dismissAlertDialog()
            controller?.noMoreOperatorsAvailableDismissed()
            onEndListener?.onEnd()
            chatEnded()
        }
    }

    private fun showNoMoreOperatorsAvailableDialog() {
        showAlertDialog(
            R.string.glia_dialog_operators_unavailable_title,
            R.string.glia_dialog_operators_unavailable_message
        ) {
            dismissAlertDialog()
            controller?.noMoreOperatorsAvailableDismissed()
            onEndListener?.onEnd()
            chatEnded()
        }
    }

    private fun showUpgradeDialog(mediaUpgrade: MediaUpgrade) {
        alertDialog = Dialogs.showUpgradeDialog(context, theme, mediaUpgrade, {
            controller?.acceptUpgradeOfferClicked(mediaUpgrade.mediaUpgradeOffer)
        }) {
            controller?.declineUpgradeOfferClicked(mediaUpgrade.mediaUpgradeOffer)
        }
    }

    private fun showUnexpectedErrorDialog() {
        showAlertDialog(
            R.string.glia_dialog_unexpected_error_title,
            R.string.glia_dialog_unexpected_error_message
        ) {
            dismissAlertDialog()
            controller?.unexpectedErrorDialogDismissed()
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
                controller?.overlayPermissionsDialogDismissed()
                dismissAlertDialog()
                val overlayIntent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + this.context.packageName)
                )
                overlayIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                this.context.startActivity(overlayIntent)
            },
            {
                controller?.overlayPermissionsDialogDismissed()
                dismissAlertDialog()
            }
        ) {
            controller?.overlayPermissionsDialogDismissed()
            dismissAlertDialog()
        }
    }

    private fun chatEnded() {
        Dependencies.getControllerFactory().destroyControllers()
    }

    private fun dismissAlertDialog() {
        alertDialog?.dismiss()
        alertDialog = null
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            OPEN_DOCUMENT_ACTION_REQUEST, CAPTURE_VIDEO_ACTION_REQUEST -> handleDocumentOrVideoActionResult(
                intent
            )
            CAPTURE_IMAGE_ACTION_REQUEST -> handleCaptureImageActionResult()
        }
    }

    private fun handleDocumentOrVideoActionResult(intent: Intent?) {
        intent?.data?.also {
            controller?.onAttachmentReceived(
                Utils.mapUriToFileAttachment(context.contentResolver, it)
            )
        }
    }

    private fun handleCaptureImageActionResult() {
        controller?.apply {
            photoCaptureFileUri?.also {
                FileHelper.fixCapturedPhotoRotation(context, it)
                onAttachmentReceived(Utils.mapUriToFileAttachment(context.contentResolver, it))
            }
        }
    }

    override fun onFileDownloadClick(file: AttachmentFile) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            onFileDownload(file)
        } else {
            Utils.getActivity(context)?.requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), WRITE_PERMISSION_REQUEST
            )
            downloadFileHolder = file
        }
    }

    private fun onFileDownload(attachmentFile: AttachmentFile) {
        submitUpdatedItems(attachmentFile, isDownloading = true, isFileExists = false)
        controller?.onFileDownloadClicked(attachmentFile)
    }

    fun fileDownloadFailed(file: AttachmentFile) {
        submitUpdatedItems(file, isDownloading = false, isFileExists = false)
        showToast(context.getString(R.string.glia_chat_file_download_failed_msg))
    }

    fun fileDownloadCompleted(file: AttachmentFile) {
        submitUpdatedItems(file, isDownloading = false, isFileExists = true)
        showToast(
            context.getString(R.string.glia_chat_file_download_success_message), Toast.LENGTH_LONG
        )
    }

    private fun submitUpdatedItems(
        attachmentFile: AttachmentFile, isDownloading: Boolean, isFileExists: Boolean
    ) {
        val updatedItems = adapter.currentList
            .asSequence()
            .map { updatedDownloadingItemState(attachmentFile, it, isDownloading, isFileExists) }
            .toList()

        adapter.submitList(updatedItems)
    }

    private fun updatedDownloadingItemState(
        attachmentFile: AttachmentFile,
        currentChatItem: ChatItem,
        isDownloading: Boolean,
        isFileExists: Boolean
    ): ChatItem {
        if (currentChatItem is VisitorAttachmentItem) {
            if (currentChatItem.attachmentFile.id == attachmentFile.id) {
                return VisitorAttachmentItem.editFileStatuses(currentChatItem, isFileExists, isDownloading)
            }
        } else if (currentChatItem is OperatorAttachmentItem) {
            if (currentChatItem.attachmentFile.id == attachmentFile.id) {
                return OperatorAttachmentItem(
                    currentChatItem.id,
                    currentChatItem.viewType,
                    currentChatItem.showChatHead,
                    currentChatItem.attachmentFile,
                    currentChatItem.operatorProfileImgUrl,
                    isFileExists,
                    isDownloading,
                    currentChatItem.operatorId,
                    currentChatItem.messageId,
                    currentChatItem.timestamp
                )
            }
        }
        return currentChatItem
    }

    override fun onFileOpenClick(file: AttachmentFile) {
        val contentUri = FileHelper.getContentUri(file, context)
        val openIntent = Intent(Intent.ACTION_VIEW)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            .setDataAndType(contentUri, file.contentType)
        openIntent.clipData = ClipData.newRawUri("", contentUri)
        context.startActivity(openIntent)
    }

    override fun onImageItemClick(item: AttachmentFile) {
        context.startActivity(FilePreviewActivity.intent(this.context, item.id, item.name, theme))
    }

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                CAMERA_PERMISSION_REQUEST -> dispatchImageCapture()
                WRITE_PERMISSION_REQUEST -> downloadFileHolder?.also(::onFileDownload)
            }
        }
    }

    fun setConfiguration(configuration: GliaSdkConfiguration?) {
        serviceChatHeadController?.setBuildTimeTheme(theme)
        serviceChatHeadController?.setSdkConfiguration(configuration)
    }

    private fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, message, duration).show()
    }

    interface OnBackClickedListener {
        /**
         * Callback which is used to notify the enclosing activity or fragment when the user
         * clicks on the view's top app bar's up button.
         */
        fun onBackClicked()
    }

    interface OnEndListener {
        /**
         * Callback which is fired when the chat is ended. End can happen due to the user clicking
         * on the end engagement button or the leave queue button.
         */
        fun onEnd()
    }

    interface OnMinimizeListener {
        fun onMinimize()
    }

    interface OnNavigateToCallListener {
        /**
         * Callback which is fired when the user has accepted a media upgrade offer and should be
         * navigated to a view where they can visually see data about their media upgrade.
         *
         * @param theme Used to pass the finalized [UiTheme]
         * to the activity which is being navigated to.
         */
        fun call(theme: UiTheme?, mediaType: String?)
    }

    interface OnBackToCallListener {
        fun onBackToCall()
    }

    interface OnNavigateToSurveyListener {
        fun onSurvey(theme: UiTheme?, survey: Survey)
    }

    interface OnTitleUpdatedListener {
        fun onTitleUpdated(title: String?)
    }

    companion object {
        private const val TAG = "ChatView"
        private const val OPEN_DOCUMENT_ACTION_REQUEST = 100
        private const val CAPTURE_IMAGE_ACTION_REQUEST = 101
        private const val CAPTURE_VIDEO_ACTION_REQUEST = 102
        private const val CAMERA_PERMISSION_REQUEST = 1010
        private const val WRITE_PERMISSION_REQUEST = 1001001
        private const val WEB_VIEW_INITIALIZATION_DELAY = 100

        private const val INTENT_TYPE_IMAGES = "image/*"
        private const val INTENT_TYPE_ALL = "*/*"
    }

    private fun applyTheme(unifiedTheme: UnifiedTheme?) {
        val chatTheme = unifiedTheme?.chatTheme ?: return

        chatTheme.background?.also { applyColorTheme(it.fill) }

        chatTheme.header?.also(::applyHeaderTheme)

        chatTheme.input?.also(::applyInputTheme)

        chatTheme.typingIndicator?.primaryColor?.also(binding.operatorTypingAnimationView::addColorFilter)

        chatTheme.unreadIndicator?.also(::applyUnreadMessagesTheme)

    }

    private fun applyHeaderTheme(headerTheme: HeaderTheme) {
        binding.appBarView.applyHeaderTheme(headerTheme)
        headerTheme.background?.fill?.primaryColor?.also { color ->
            statusBarColor = color
        }
    }

    private fun applyInputTheme(inputTheme: InputTheme) {
        binding.sendButton.applyButtonTheme(inputTheme.sendButton)
        binding.addAttachmentButton.applyButtonTheme(inputTheme.mediaButton)
        binding.dividerView.applyColorTheme(inputTheme.divider)
        binding.chatMessageLayout.applyLayerTheme(inputTheme.background)
        binding.chatEditText.applyTextTheme(textTheme = inputTheme.text, withAlignment = false)
        inputTheme.placeholder?.textColor?.primaryColor?.also(binding.chatEditText::setHintTextColor)
    }

    private fun applyUnreadMessagesTheme(unreadIndicatorTheme: UnreadIndicatorTheme) {
        unreadIndicatorTheme.bubble?.badge?.also(binding.newMessagesBadgeView::applyBadgeTheme)
        unreadIndicatorTheme.bubble?.userImage?.also(binding.newMessagesIndicatorImage::applyUserImageTheme)
        unreadIndicatorTheme.background?.primaryColor?.also(binding.newMessagesIndicatorCard::setCardBackgroundColor)
    }

}
