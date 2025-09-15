package com.glia.widgets.chat

import android.content.Context
import android.content.res.TypedArray
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.VisibleForTesting
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.withStyledAttributes
import androidx.core.view.ViewCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import androidx.transition.AutoTransition
import androidx.transition.ChangeBounds
import androidx.transition.ChangeTransform
import androidx.transition.Fade
import androidx.transition.Transition
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.glia.androidsdk.Engagement.MediaType
import com.glia.androidsdk.chat.AttachmentFile
import com.glia.widgets.Constants
import com.glia.widgets.GliaWidgets
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.chat.adapter.ChatAdapter
import com.glia.widgets.chat.adapter.ChatAdapter.OnCustomCardResponse
import com.glia.widgets.chat.adapter.ChatAdapter.OnFileItemClickListener
import com.glia.widgets.chat.adapter.ChatAdapter.OnImageItemClickListener
import com.glia.widgets.chat.adapter.ChatItemHeightManager
import com.glia.widgets.chat.adapter.UploadAttachmentAdapter
import com.glia.widgets.chat.model.ChatInputMode
import com.glia.widgets.chat.model.ChatItem
import com.glia.widgets.chat.model.ChatState
import com.glia.widgets.chat.model.CustomCardChatItem
import com.glia.widgets.databinding.ChatViewBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.entrywidget.EntryWidgetContract
import com.glia.widgets.entrywidget.adapter.EntryWidgetAdapter
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.SimpleTextWatcher
import com.glia.widgets.helper.SimpleWindowInsetsAndAnimationHandler
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.Utils
import com.glia.widgets.helper.addColorFilter
import com.glia.widgets.helper.asActivity
import com.glia.widgets.helper.fileName
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.getColorStateListCompat
import com.glia.widgets.helper.getContentUriCompat
import com.glia.widgets.helper.getFontCompat
import com.glia.widgets.helper.hideKeyboard
import com.glia.widgets.helper.insetsController
import com.glia.widgets.helper.layoutInflater
import com.glia.widgets.helper.requireActivity
import com.glia.widgets.helper.setLocaleContentDescription
import com.glia.widgets.helper.setLocaleHint
import com.glia.widgets.helper.setLocaleText
import com.glia.widgets.internal.dialog.DialogContract
import com.glia.widgets.internal.dialog.model.DialogState
import com.glia.widgets.internal.dialog.model.LeaveDialogAction
import com.glia.widgets.internal.fileupload.model.LocalAttachment
import com.glia.widgets.launcher.ActivityLauncher
import com.glia.widgets.locale.LocaleString
import com.glia.widgets.view.Dialogs
import com.glia.widgets.view.SingleChoiceCardView.OnOptionClickedListener
import com.glia.widgets.view.animation.SimpleTransitionListener
import com.glia.widgets.view.dialog.base.DialogDelegate
import com.glia.widgets.view.dialog.base.DialogDelegateImpl
import com.glia.widgets.view.head.ChatHeadContract
import com.glia.widgets.view.unifiedui.applyButtonTheme
import com.glia.widgets.view.unifiedui.applyColorTheme
import com.glia.widgets.view.unifiedui.applyHintColor
import com.glia.widgets.view.unifiedui.applyHintTheme
import com.glia.widgets.view.unifiedui.applyImageColor
import com.glia.widgets.view.unifiedui.applyImageColorTheme
import com.glia.widgets.view.unifiedui.applyLayerTheme
import com.glia.widgets.view.unifiedui.applyTextColor
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.glia.widgets.view.unifiedui.theme.base.HeaderTheme
import com.glia.widgets.view.unifiedui.theme.chat.InputTheme
import com.glia.widgets.view.unifiedui.theme.chat.UnreadIndicatorTheme
import com.glia.widgets.view.unifiedui.theme.securemessaging.SecureMessagingTheme
import com.google.android.material.shape.MarkerEdgeTreatment
import com.google.android.material.theme.overlay.MaterialThemeOverlay
import java.util.concurrent.Executor
import kotlin.properties.Delegates

internal class ChatView(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : ConstraintLayout(
    MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, defStyleRes), attrs, defStyleAttr, defStyleRes
), OnFileItemClickListener, OnImageItemClickListener, ChatContract.View, DialogDelegate by DialogDelegateImpl() {
    private val activityLauncher: ActivityLauncher by lazy { Dependencies.activityLauncher }

    private var controller: ChatContract.Controller? = null
    private var dialogCallback: DialogContract.Controller.Callback? = null
    private var dialogController: DialogContract.Controller? = null

    private var serviceChatHeadController: ChatHeadContract.Controller? = null

    private var uploadAttachmentAdapter by Delegates.notNull<UploadAttachmentAdapter>()
    private var adapter by Delegates.notNull<ChatAdapter>()

    private var localeProvider = Dependencies.localeProvider
    private var isInBottom = true
    private var theme: UiTheme by Delegates.notNull()

    private var onTitleUpdatedListener: OnTitleUpdatedListener? = null
    private var onEndListener: OnEndListener? = null
    private var onMinimizeListener: OnMinimizeListener? = null
    private var onBackToCallListener: OnBackToCallListener? = null
    private val onRetryClickListener = ChatAdapter.OnRetryClickListener {
        controller?.onRetryClicked(it)
    }
    private val onOptionClickedListener = OnOptionClickedListener { item, selectedOption ->
        Logger.d(TAG, "singleChoiceCardClicked")
        controller?.singleChoiceOptionClicked(item, selectedOption)
    }
    private val onScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            controller?.onRecyclerviewPositionChanged(!recyclerView.canScrollVertically(1))
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            // hide the keyboard on chat scroll
            if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                insetsController?.hideKeyboard()
            }
        }
    }
    private val onCustomCardResponse = OnCustomCardResponse { customCard: CustomCardChatItem, text: String, value: String ->
        controller?.sendCustomCardResponse(customCard, text, value)
    }
    private val dataObserver: AdapterDataObserver = object : AdapterDataObserver() {
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            super.onItemRangeInserted(positionStart, itemCount)

            val totalItemCount = adapter.itemCount
            val lastIndex = totalItemCount - 1
            if (isInBottom && lastIndex != -1) {
                val itemViewType = adapter.getItemViewType(lastIndex)
                if (itemViewType == ChatAdapter.CUSTOM_CARD_TYPE) {
                    // WebView needs time for calculating the height.
                    // So to scroll to the bottom, we need to do it with delay.
                    postDelayed(
                        { binding.chatRecyclerView.scrollToPosition(lastIndex) }, Constants.WEB_VIEW_INITIALIZATION_DELAY
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
    private val rootConstraintLayout
        get() = binding.root as ConstraintLayout
    private val attachmentPopup by lazy {
        AttachmentPopup(
            context, Dependencies.gliaThemeManager.theme?.chatTheme?.attachmentsPopup
        )
    }

    private val onGvaButtonsClickListener = ChatAdapter.OnGvaButtonsClickListener {
        controller?.onGvaButtonClicked(it)
    }

    private val chatActivity: ChatActivity? = context.asActivity() as? ChatActivity

    private val takePictureLauncher = chatActivity?.registerForActivityResult(ActivityResultContracts.TakePicture()) {
        controller?.onImageCaptured(it)
    }

    private val getContentLauncher = chatActivity?.registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        it?.apply { controller?.onContentChosen(this) }
    }

    //This will allow us to view picked files with Uri
    private val openDocumentLauncher = chatActivity?.registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        it?.apply { controller?.onContentChosen(this) }
    }

    init {
        initConfigurations()
        bindViews()
        readTypedArray(attrs, defStyleAttr, defStyleRes)
        setupViewAppearance()
        setupViewActions()
        setupControllers()
        SimpleWindowInsetsAndAnimationHandler(this, appBarOrToolBar = binding.appBarView)
    }

    @JvmOverloads
    constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.gliaChatStyle
    ) : this(context, attrs, defStyleAttr, R.style.Application_Glia_Chat)


    fun startChat(intention: Intention) {
        dialogCallback?.also { dialogController?.addCallback(it) }
        controller?.initChat(intention)
    }

    fun restoreChat() {
        dialogCallback?.also { dialogController?.addCallback(it) }
        controller?.restoreChat()
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
    fun onBackPressed() {
        controller?.onBackArrowClicked()
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
        controller?.setOnBackClickedListener(onBackClicked)
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

    fun setOnBackToCallListener(onBackToCallListener: OnBackToCallListener?) {
        this.onBackToCallListener = onBackToCallListener
    }

    /**
     * Use this method to notify the view when your activity or fragment is back in its resumed
     * state.
     */
    fun onResume() {
        controller?.onResume()
        dialogCallback?.also { dialogController?.addCallback(it) }
    }

    fun onPause() {
        controller?.onPause()
        dialogCallback?.also { dialogController?.removeCallback(it) }
    }

    /**
     * Use this method to notify the view that your activity or fragment's view is being destroyed.
     * Used to dispose of any loose resources.
     */
    fun onDestroyView() {
        resetDialogStateAndDismiss()

        onEndListener = null
        destroyController()
        adapter.unregisterAdapterDataObserver(dataObserver)
        binding.chatRecyclerView.adapter = null
        binding.chatRecyclerView.removeOnScrollListener(onScrollListener)
        binding.addAttachmentQueue.adapter = null
        dialogController = null
    }

    fun shouldShow(): Boolean = controller?.let { !it.isCallVisualizerOngoing() } ?: run {
        Logger.e(TAG, "ChatController is unexpectedly null")
        false
    }

    private fun setupControllers() {
        setController(Dependencies.controllerFactory.chatController)
        setupDialogCallback()
        dialogController = Dependencies.controllerFactory.dialogController
        serviceChatHeadController = Dependencies.controllerFactory.chatHeadController
    }

    override fun setController(controller: ChatContract.Controller) {
        this.controller = controller
        controller.setView(this)
    }

    override fun emitUploadAttachments(attachments: List<LocalAttachment>) {
        post { uploadAttachmentAdapter.submitList(attachments) }
    }

    override fun emitState(chatState: ChatState) {
        // some state updates on core-sdk are coming from the computation thread
        // need to update state on uiThread
        post {
            updateSendButtonState(chatState)
            updateChatEditText(chatState)
            updateAppBar(chatState)
            binding.newMessagesIndicatorLayout.isVisible = chatState.showMessagesUnseenIndicator
            updateNewMessageOperatorStatusView(chatState.operatorProfileImgUrl)
            isInBottom = chatState.isChatInBottom
            binding.chatRecyclerView.setInBottom(isInBottom)
            binding.newMessagesBadgeView.text = "${chatState.messagesNotSeen}"
            if (chatState.isVisible) {
                showChat()
            } else {
                hideChat()
            }

            binding.operatorTypingAnimationView.isVisible = chatState.isOperatorTyping
            updateAttachmentButton(chatState)
            updateQuickRepliesState(chatState)
            updateSecureMessagingState(chatState)
        }
    }

    override fun emitItems(items: List<ChatItem>) {
        adapter.submitList(items)
    }

    override fun backToCall() {
        onBackToCallListener?.onBackToCall()
    }

    override fun minimizeView() {
        onMinimizeListener?.onMinimize()
    }

    override fun smoothScrollToBottom() {
        if (adapter.itemCount < 1) return
        post { binding.chatRecyclerView.smoothScrollToPosition(adapter.itemCount - 1) }
    }

    override fun scrollToBottomImmediate() {
        if (adapter.itemCount < 1) return
        post { binding.chatRecyclerView.scrollToPosition(adapter.itemCount - 1) }
    }

    override fun clearMessageInput() {
        post {
            binding.chatEditText.apply {
                removeTextChangedListener(textWatcher)
                text?.clear()
                addTextChangedListener(textWatcher)
            }
        }
    }

    override fun navigateToImagePreview(attachmentFile: AttachmentFile, view: View) {
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            context.requireActivity(), view, context.getString(R.string.glia_file_preview_transition_name) // Not translatable
        )

        activityLauncher.launchImagePreview(context, attachmentFile, options.toBundle())
        insetsController?.hideKeyboard()
    }

    override fun navigateToImagePreview(attachmentFile: LocalAttachment, view: View) {
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            context.requireActivity(), view, context.getString(R.string.glia_file_preview_transition_name) // Not translatable
        )

        activityLauncher.launchImagePreview(context, attachmentFile, options.toBundle())
        insetsController?.hideKeyboard()
    }

    override fun launchCall(mediaType: MediaType) {
        activityLauncher.launchCall(context, mediaType, false)
        context.asActivity()?.finish()
    }

    override fun fileIsNotReadyForPreview() {
        showToast(localeProvider.getString(R.string.android_file_not_ready_for_preview))
    }

    override fun showBroadcastNotSupportedToast() {
        showToast(localeProvider.getString(R.string.gva_unsupported_action_error))
    }

    override fun navigateToWebBrowserActivity(title: LocaleString, url: String) {
        activityLauncher.launchWebBrowser(context, title, url)
    }

    override fun showEngagementConfirmationDialog() {
        controller?.getConfirmationDialogLinks()?.let { links ->
            showDialog {
                Dialogs.showEngagementConfirmationDialog(
                    context = context,
                    theme = theme,
                    links = links,
                    positiveButtonClickListener = { onEngagementConfirmationDialogAllowed() },
                    negativeButtonClickListener = { onEngagementConfirmationDialogDismissed() },
                    linkClickListener = { controller?.onLinkClicked(it) })
            }
        }
    }

    private fun onEngagementConfirmationDialogAllowed() {
        resetDialogStateAndDismiss()
        controller?.onLiveObservationDialogAllowed()
    }

    private fun onEngagementConfirmationDialogDismissed() {
        resetDialogStateAndDismiss()
        controller?.onLiveObservationDialogRejected()
        onEndListener?.onEnd()
        chatEnded()
    }

    override fun requestOpenEmailClient(uri: Uri) {
        activityLauncher.launchEmailClient(context, uri) {
            Logger.e(TAG, "No email client, uri - $uri")
            showToast(localeProvider.getString(R.string.error_general))
        }
    }

    override fun requestOpenDialer(uri: Uri) {
        activityLauncher.launchDialer(context, uri) {
            Logger.e(TAG, "No dialer uri - $uri")
            showToast(localeProvider.getString(R.string.error_general))
        }
    }

    override fun requestOpenUri(uri: Uri) {
        activityLauncher.launchUri(context, uri) {
            Logger.e(TAG, "No app to open url - $uri")
            showToast(localeProvider.getString(R.string.error_general))
        }
    }

    private fun updateQuickRepliesState(chatState: ChatState) {
        binding.gvaQuickRepliesLayout.setButtons(chatState.gvaQuickReplies)
    }

    private fun updateNewMessageOperatorStatusView(operatorProfileImgUrl: String?) {
        binding.newMessagesIndicatorImage.apply {
            operatorProfileImgUrl?.also(::showProfileImage) ?: showPlaceholder()
        }
    }

    private fun setupDialogCallback() {
        dialogCallback = DialogContract.Controller.Callback {
            if (updateDialogState(it)) {
                when (it) {
                    DialogState.None -> resetDialogStateAndDismiss()
                    DialogState.UnexpectedError -> post { showUnexpectedErrorDialog() }
                    DialogState.ExitQueue -> post { showExitQueueDialog() }
                    DialogState.OverlayPermission -> post { showOverlayPermissionsDialog() }
                    DialogState.EndEngagement -> post { showEndEngagementDialog() }
                    DialogState.Confirmation -> post { controller?.onEngagementConfirmationDialogRequested() }
                    is DialogState.LeaveCurrentConversation -> post { showLeaveCurrentConversationDialog(it.action) }

                    DialogState.VisitorCode -> {
                        Logger.e(TAG, "DialogController callback in ChatView with MODE_VISITOR_CODE")
                    } // Should never happen inside ChatView
                    else -> { /* noop */
                    }
                }
            }
        }
    }

    private fun updateAttachmentButton(chatState: ChatState) {
        binding.addAttachmentButton.isEnabled = chatState.isAttachmentButtonEnabled
        binding.addAttachmentButton.isVisible = chatState.isAttachmentButtonVisible
    }

    private fun updateSendButtonState(chatState: ChatState) {
        binding.sendButton.apply {
            isVisible = chatState.isSendButtonVisible
            isEnabled = chatState.isSendButtonEnabled
        }
    }

    private fun updateChatEditText(chatState: ChatState) {
        when (chatState.chatInputMode) {
            ChatInputMode.ENABLED_NO_ENGAGEMENT -> binding.chatEditText.setLocaleHint(
                R.string.chat_message_start_engagement_placeholder
            )

            else -> binding.chatEditText.setLocaleHint(R.string.chat_input_placeholder)
        }
        binding.chatEditText.isEnabled = chatState.chatInputMode.isEnabled
        binding.messageInputBackground.isEnabled = chatState.chatInputMode.isEnabled
        Dependencies.gliaThemeManager.theme?.chatTheme?.let {
            binding.chatEditText.apply {
                val inputTheme = if (isEnabled) it.input else it.inputDisabled
                applyTextTheme(inputTheme?.text, withAlignment = false)
            }
        }
    }

    private fun updateAppBar(chatState: ChatState) {
        if (chatState.isOperatorOnline) {
            binding.appBarView.showEndButton()
        } else if (chatState.engagementRequested) {
            binding.appBarView.showXButton()
        } else {
            binding.appBarView.hideLeaveButtons()
        }

        if (chatState.isSecureMessaging) {
            showToolbar(LocaleString(R.string.message_center_header))
            binding.appBarView.hideBackButton()
            binding.appBarView.showXButton()
            binding.secureConversationsBottomBanner.visibility = VISIBLE
        } else {
            showToolbar(LocaleString(R.string.engagement_chat_title))
            binding.appBarView.showBackButton()
            binding.secureConversationsBottomBanner.visibility = GONE
        }
    }

    private fun showChat() {
        visibility = VISIBLE
    }

    private fun hideChat() {
        visibility = INVISIBLE
        insetsController?.hideKeyboard()
    }

    private fun showToolbar(title: LocaleString?) {
        onTitleUpdatedListener?.onTitleUpdated(title)

        binding.appBarView.setTitle(title)
        binding.appBarView.setVisibility(title != null)
    }

    private fun destroyController() {
        controller?.let {
            if (it.getView() == this) {
                it.onDestroy(context.asActivity() is ChatActivity)
            }
        }
        controller = null
    }

    private fun readTypedArray(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        context.withStyledAttributes(attrs, R.styleable.GliaView, defStyleAttr, defStyleRes) {
            setDefaultTheme(this)
        }
    }

    private fun setDefaultTheme(typedArray: TypedArray) {
        theme = Utils.getThemeFromTypedArray(typedArray, this.context)
    }

    private fun initConfigurations() {
        isInvisible = true
        // needed to overlap existing app bar in existing view with this view's app bar.
        ViewCompat.setElevation(this, Constants.WIDGETS_SDK_LAYER_ELEVATION)
    }

    private fun bindViews() {
        binding = ChatViewBinding.inflate(layoutInflater, this)
    }

    private fun setupViewAppearance() {
        adapter = ChatAdapter(
            theme,
            onRetryClickListener,
            onOptionClickedListener,
            this,
            this,
            onCustomCardResponse,
            onGvaButtonsClickListener,
            ChatItemHeightManager(theme, layoutInflater, resources),
            GliaWidgets.getCustomCardAdapter(),
            Dependencies.useCaseFactory.createGetImageFileFromCacheUseCase(),
            Dependencies.useCaseFactory.createGetImageFileFromDownloadsUseCase(),
            Dependencies.useCaseFactory.createGetImageFileFromNetworkUseCase()
        )
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this.context)
        adapter.registerAdapterDataObserver(dataObserver)
        binding.chatRecyclerView.adapter = adapter
        binding.chatRecyclerView.itemAnimator = null
        binding.chatRecyclerView.addOnScrollListener(onScrollListener)
        uploadAttachmentAdapter = UploadAttachmentAdapter()
        uploadAttachmentAdapter.setItemCallback { controller?.onRemoveAttachment(it) }
        uploadAttachmentAdapter.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                binding.addAttachmentQueue.smoothScrollToPosition(uploadAttachmentAdapter.itemCount)
            }
        })
        binding.addAttachmentButton.setLocaleContentDescription(R.string.chat_attach_files)
        binding.addAttachmentQueue.layoutManager = LinearLayoutManager(this.context)
        binding.addAttachmentQueue.adapter = uploadAttachmentAdapter
        binding.appBarView.setTheme(theme)
        binding.appBarView.setTitle(LocaleString(R.string.engagement_chat_title))
        // icons
        theme.iconSendMessage?.also(binding.sendButton::setImageResource)

        // new messages indicator shape
        val shapeAppearanceModel = binding.newMessagesIndicatorCard.shapeAppearanceModel.toBuilder()
            .setBottomEdge(MarkerEdgeTreatment(resources.getDimension(R.dimen.glia_chat_new_messages_bottom_edge_radius))).build()

        binding.newMessagesIndicatorImage.setShowRippleAnimation(false)
        binding.newMessagesIndicatorImage.setTheme(theme)
        binding.newMessagesIndicatorCard.shapeAppearanceModel = shapeAppearanceModel

        // global colors
        theme.brandPrimaryColor
            ?.also(binding.operatorTypingAnimationView::addColorFilter)
            ?.let(::getColorStateListCompat)
            ?.also(binding.newMessagesBadgeView::setBackgroundTintList)

        theme.baseLightColor?.let(::getColorCompat)
            ?.also(binding.newMessagesBadgeView::setTextColor)
            ?.also(binding.scErrorLabel::setTextColor)
        theme.baseLightColor?.let(::getColorStateListCompat)
            ?.also(binding.scErrorLabel::setCompoundDrawableTintList)

        theme.systemNegativeColor?.let(::getColorCompat)
            ?.also(binding.scErrorLabel::setBackgroundColor)

        val disabledInputColor = theme.baseShadeColor?.let(::getColorCompat)
            ?.also(binding.chatDivider::setBackgroundColor)
            ?.also(binding.scBottomBannerDivider::setBackgroundColor)

        theme.baseDarkColor?.let(::getColorCompat)
            ?.also { binding.chatEditText.applyTextColor(it, disabledInputColor) }
        theme.baseNormalColor?.let(::getColorCompat)
            ?.also { binding.chatEditText.applyHintColor(it, disabledInputColor) }
            ?.also { binding.addAttachmentButton.applyImageColor(it, disabledInputColor) }
            ?.also(binding.scBottomBannerLabel::setTextColor)
        theme.systemAgentBubbleColor?.let(::getColorCompat)?.also(binding.scBottomBannerLabel::setBackgroundColor)

        // other
        theme.sendMessageButtonTintColor?.let(::getColorCompat)
            ?.also { binding.sendButton.applyImageColor(it, disabledInputColor) }
        theme.gliaChatBackgroundColor?.let(::getColorCompat)?.also(::setBackgroundColor)
        binding.gvaQuickRepliesLayout.updateTheme(theme)

        // fonts
        theme.fontRes?.also { binding.chatEditText.typeface = getFontCompat(it) }

        // remote locales
        binding.scErrorLabel.setLocaleText(R.string.secure_messaging_chat_unavailable_message)
        binding.scBottomBannerLabel.setLocaleText(R.string.secure_messaging_chat_banner_bottom)
        binding.sendButton.setLocaleContentDescription(R.string.general_send)
        binding.scTopBannerTitle.setLocaleText(R.string.secure_messaging_chat_banner_top)

        binding.scTopBannerOptions.apply {
            val entryWidgetTheme = Dependencies.gliaThemeManager.theme?.chatTheme?.secureMessaging
            val entryWidgetsAdapter = EntryWidgetAdapter(
                EntryWidgetContract.ViewType.MESSAGING_LIVE_SUPPORT,
                entryWidgetTheme
            )
            entryWidgetsAdapter.onItemClickListener = {
                onNeedSupportButtonClicked(null)
                controller?.onScTopBannerItemClicked(it)
            }
            setAdapter(entryWidgetsAdapter)
        }

        applyTheme(Dependencies.gliaThemeManager.theme)
    }

    private fun setupViewActions() {
        binding.chatEditText.addTextChangedListener(textWatcher)
        binding.sendButton.setOnClickListener {
            val message = binding.chatEditText.text.toString().trim { it <= ' ' }
            controller?.sendMessage(message)
        }
        setupAddAttachmentButton()
        binding.appBarView.setOnBackClickedListener { controller?.onBackArrowClicked() }
        binding.appBarView.setOnEndChatClickedListener { controller?.leaveChatClicked() }
        binding.appBarView.setOnXClickedListener { controller?.onXButtonClicked() }
        binding.newMessagesIndicatorCard.setOnClickListener { controller?.newMessagesIndicatorClicked() }
        binding.gvaQuickRepliesLayout.onItemClickedListener = GvaChipGroup.OnItemClickedListener {
            controller?.onGvaButtonClicked(it)

            // move the focus back to the chat list
            binding.chatRecyclerView.adapter?.itemCount?.let { size ->
                val viewHolder = binding.chatRecyclerView.findViewHolderForAdapterPosition(size - 1)
                viewHolder?.itemView?.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
            }
        }

        binding.scTopBannerTitle.setOnClickListener(::onNeedSupportButtonClicked)
        binding.scTopBannerIcon.setOnClickListener(::onNeedSupportButtonClicked)
        binding.blockingCurtain.setOnClickListener(::onNeedSupportButtonClicked)
    }

    private fun setupAddAttachmentButton() {
        binding.addAttachmentButton.setOnClickListener {
            attachmentPopup.show(binding.addAttachmentButton, {
                getContentLauncher?.launch(arrayOf(Constants.MIME_TYPE_IMAGES))
            }, {
                controller?.onTakePhotoClicked()
            }, {
                openDocumentLauncher?.launch(arrayOf(Constants.MIME_TYPE_ALL))
            })
        }
    }

    override fun dispatchImageCapture(uri: Uri) {
        takePictureLauncher?.launch(uri)
    }

    private fun showExitQueueDialog() = showDialog {
        Dialogs.showExitQueueDialog(context = context, uiTheme = theme, positiveButtonClickListener = {
            resetDialogStateAndDismiss()
            controller?.endEngagementDialogYesClicked()
            onEndListener?.onEnd()
            chatEnded()
        }, negativeButtonClickListener = {
            resetDialogStateAndDismiss()
            controller?.endEngagementDialogDismissed()
        })
    }

    private fun showEndEngagementDialog() = showDialog {
        Dialogs.showEndEngagementDialog(context = context, uiTheme = theme, positiveButtonClickListener = {
            resetDialogStateAndDismiss()
            controller?.endEngagementDialogYesClicked()
        }, negativeButtonClickListener = {
            resetDialogStateAndDismiss()
            controller?.endEngagementDialogDismissed()
        })
    }

    private fun showUnexpectedErrorDialog() = showDialog {
        Dialogs.showUnexpectedErrorDialog(context, theme) {
            resetDialogStateAndDismiss()
            controller?.unexpectedErrorDialogDismissed()
            onEndListener?.onEnd()
        }
    }

    private fun showOverlayPermissionsDialog() = showDialog {
        Dialogs.showOverlayPermissionsDialog(context = context, uiTheme = theme, positiveButtonClickListener = {
            controller?.overlayPermissionsDialogDismissed()
            resetDialogStateAndDismiss()
            activityLauncher.launchOverlayPermission(context)
        }, negativeButtonClickListener = {
            controller?.overlayPermissionsDialogDismissed()
            resetDialogStateAndDismiss()
        })
    }

    private fun showLeaveCurrentConversationDialog(action: LeaveDialogAction) = showDialog {
        Dialogs.showLeaveCurrentConversationDialog(
            context,
            theme,
            onStay = {
                resetDialogStateAndDismiss()
                controller?.leaveCurrentConversationDialogStayClicked()
            },
            onLeave = {
                resetDialogStateAndDismiss()
                controller?.leaveCurrentConversationDialogLeaveClicked(action)
            }
        )
    }

    private fun chatEnded() {
        Dependencies.destroyControllers()
    }

    override fun onFileDownloadClick(file: AttachmentFile) {
        controller?.onFileDownloadClicked(file)
    }

    override fun fileDownloadError() {
        showToast(localeProvider.getString(R.string.chat_download_failed))
    }

    override fun fileDownloadSuccess() {
        showToast(localeProvider.getString(R.string.android_chat_download_complete_message), Toast.LENGTH_LONG)
    }

    override fun onFileOpenClick(file: AttachmentFile) {
        activityLauncher.launchFileReader(context, getContentUriCompat(file.fileName, context), file.contentType) {
            showToast(message = localeProvider.getString(R.string.android_file_view_error))
        }
    }

    override fun onLocalFileOpenClick(attachment: LocalAttachment) {
        activityLauncher.launchFileReader(context, attachment.uri, attachment.mimeType.orEmpty()) {
            showToast(message = localeProvider.getString(R.string.android_file_view_error))
        }
    }

    override fun onImageItemClick(item: AttachmentFile, view: View) {
        controller?.onImageItemClick(item, view)
    }

    override fun onLocalImageItemClick(attachment: LocalAttachment, view: View) {
        controller?.onLocalImageItemClick(attachment, view)
    }

    override fun showToast(message: String, duration: Int) {
        Toast.makeText(context, message, duration).show()
    }

    private fun applyTheme(unifiedTheme: UnifiedTheme?) {
        val chatTheme = unifiedTheme?.chatTheme ?: return

        chatTheme.background?.also { applyColorTheme(it.fill) }

        chatTheme.header?.also(::applyHeaderTheme)

        applyInputTheme(chatTheme.input, chatTheme.inputDisabled)

        chatTheme.typingIndicator?.primaryColor?.also(binding.operatorTypingAnimationView::addColorFilter)

        chatTheme.unreadIndicator?.also(::applyUnreadMessagesTheme)

        chatTheme.secureMessaging?.also(::applySecureMessagingTheme)
    }

    private fun applyHeaderTheme(headerTheme: HeaderTheme) {
        binding.appBarView.applyHeaderTheme(headerTheme)
    }

    private fun applyInputTheme(defaultTheme: InputTheme?, disabledTheme: InputTheme?) {
        binding.sendButton.applyButtonTheme(defaultTheme?.sendButton, disabledTheme?.sendButton)
        binding.addAttachmentButton.applyButtonTheme(defaultTheme?.mediaButton, disabledTheme?.mediaButton)
        binding.chatDivider.applyColorTheme(defaultTheme?.divider, disabledTheme?.divider)
        binding.messageInputBackground.applyThemes(defaultTheme?.background, disabledTheme?.background)
        // It is impossible to support all possible change to text view related to disabled state
        // So it is achived manually in [updateChatEditText] method
        binding.chatEditText.applyTextTheme(textTheme = defaultTheme?.text, withAlignment = false)
        binding.chatEditText.applyHintTheme(defaultTheme?.placeholder, disabledTheme?.placeholder)
    }

    private fun applyUnreadMessagesTheme(unreadIndicatorTheme: UnreadIndicatorTheme) {
        unreadIndicatorTheme.bubble?.badge?.also(binding.newMessagesBadgeView::applyBadgeTheme)
        unreadIndicatorTheme.bubble?.userImage?.also(binding.newMessagesIndicatorImage::applyUserImageTheme)
        unreadIndicatorTheme.background?.primaryColor?.also(binding.newMessagesIndicatorCard::setCardBackgroundColor)
    }

    private fun applySecureMessagingTheme(secureMessagingTheme: SecureMessagingTheme) {
        binding.scBottomBannerLabel.applyTextTheme(secureMessagingTheme.bottomBannerText)
        binding.scBottomBannerLabel.applyLayerTheme(secureMessagingTheme.bottomBannerBackground)
        binding.scBottomBannerDivider.applyColorTheme(secureMessagingTheme.bottomBannerDividerColor)
        binding.scErrorLabel.applyLayerTheme(secureMessagingTheme.unavailableStatusBackground)
        binding.scErrorLabel.applyTextTheme(secureMessagingTheme.unavailableStatusText)
        binding.scTopBannerBackground.applyLayerTheme(secureMessagingTheme.topBannerBackground)
        binding.scTopBannerTitle.applyTextTheme(secureMessagingTheme.topBannerText)
        binding.scTopBannerIcon.applyImageColorTheme(secureMessagingTheme.topBannerDropDownIconColor)
        binding.scTopBannerDivider.applyColorTheme(secureMessagingTheme.topBannerDividerColor)
        binding.scTopBannerOptions.setSecureMessagingTheme(secureMessagingTheme)
    }

    private fun updateSecureMessagingState(state: ChatState) {
        TransitionManager.beginDelayedTransition(
            rootConstraintLayout, AutoTransition()
                .addTarget(binding.scErrorLabel)
                .addTarget(binding.scTopBannerTitle)
                .addTarget(binding.scTopBannerIcon)
                .addTarget(binding.scTopBannerBackground)
                .addTarget(binding.scTopBannerDivider)
        )
        binding.scErrorLabel.isVisible = state.isSecureConversationsUnavailableLabelVisible
        if (state.isSecureConversationsTopBannerVisible.not() && isNeedSupportDropDownShown()) {
            onNeedSupportButtonClicked(null)
        }
        binding.scTopBannerGroup.isVisible = state.isSecureConversationsTopBannerVisible
        binding.scTopBannerDivider.isVisible = state.isSecureConversationsTopBannerVisible
    }

    @VisibleForTesting
    internal var executor: Executor? = null

    override fun post(action: Runnable?): Boolean {
        return executor?.execute(action)?.let { true } ?: super.post(action)
    }

    fun interface OnBackClickedListener {
        /**
         * Callback which is used to notify the enclosing activity or fragment when the user
         * clicks on the view's top app bar's up button.
         */
        fun onBackClicked()
    }

    fun interface OnEndListener {
        /**
         * Callback which is fired when the chat is ended. End can happen due to the user clicking
         * on the end engagement button or the leave queue button.
         */
        fun onEnd()
    }

    fun interface OnMinimizeListener {
        fun onMinimize()
    }

    fun interface OnBackToCallListener {
        fun onBackToCall()
    }

    fun interface OnTitleUpdatedListener {
        fun onTitleUpdated(title: LocaleString?)
    }

    private fun onNeedSupportButtonClicked(ignored: View?) {
        val animationRules = createSecureConversationAnimationRules()

        TransitionManager.beginDelayedTransition(rootConstraintLayout, animationRules)
        updateLayoutToNewNeedSupportBannerState(!isNeedSupportDropDownShown())
    }

    private fun isNeedSupportDropDownShown(): Boolean {
        return binding.blockingCurtain.isVisible
    }

    private fun createSecureConversationAnimationRules(): TransitionSet {
        return TransitionSet()
            .addTransition(
                ChangeTransform()
                    .addTarget(binding.scTopBannerIcon)
            )
            .addTransition(
                Fade()
                    .addTarget(binding.blockingCurtain)
                    .addTarget(binding.scTopBannerDivider)
            )
            .addTransition(
                ChangeBounds()
                    .addTarget(binding.blockingCurtain)
                    .addTarget(binding.scTopBannerOptions)
            )
            .setDuration(500)
            .addListener(object : SimpleTransitionListener() {
                override fun onTransitionEnd(transition: Transition) {
                    binding.blockingCurtain.apply {
                        if (isInvisible) {
                            visibility = GONE
                        }
                    }
                }
            })
    }

    private fun updateLayoutToNewNeedSupportBannerState(shouldShowDropDown: Boolean) {
        ConstraintSet().apply {
            clone(rootConstraintLayout)
            if (shouldShowDropDown) {
                clear(R.id.sc_top_banner_options, ConstraintSet.BOTTOM)
                setVisibility(R.id.blocking_curtain, VISIBLE)
                setVisibility(R.id.sc_top_banner_divider, INVISIBLE)
                setRotation(R.id.sc_top_banner_icon, 180f)
            } else {
                connect(R.id.sc_top_banner_options, ConstraintSet.BOTTOM, R.id.header_barrier, ConstraintSet.BOTTOM)
                setVisibility(R.id.blocking_curtain, INVISIBLE)
                setVisibility(R.id.sc_top_banner_divider, VISIBLE)
                setRotation(R.id.sc_top_banner_icon, 0f)
            }
        }.applyTo(rootConstraintLayout)
    }
}
