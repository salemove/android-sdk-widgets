package com.glia.widgets.chat.model

internal data class ChatState(
    val integratorChatStarted: Boolean = false,
    val isVisible: Boolean = false,
    val isChatInBottom: Boolean = true,
    val messagesNotSeen: Int = 0,
    val formattedOperatorName: String? = null,
    val operatorProfileImgUrl: String? = null,
    val companyName: String? = null,
    val queueId: String? = null,
    val visitorContextAssetId: String? = null,
    val mediaUpgradeStartedTimerItem: MediaUpgradeStartedTimerItem? = null,
    val chatItems: List<ChatItem> = emptyList(),
    val chatInputMode: ChatInputMode = ChatInputMode.ENABLED_NO_ENGAGEMENT,
    val lastTypedText: String = "",
    val engagementRequested: Boolean = false,
    val pendingNavigationType: String? = null,
    val unsentMessages: List<VisitorMessageItem.Unsent> = emptyList(),
    val operatorStatusItem: OperatorStatusItem? = null,
    val showSendButton: Boolean = false,
    val isAttachmentButtonEnabled: Boolean = false,
    val isAttachmentButtonNeeded: Boolean = false,
    val isOperatorTyping: Boolean = false,
    val isAttachmentAllowed: Boolean = true,
    val isSecureMessaging: Boolean = false,
    val gvaQuickReplies: List<GvaButton> = emptyList()
) {

    val isOperatorOnline: Boolean get() = formattedOperatorName != null

    val isMediaUpgradeStarted: Boolean get() = mediaUpgradeStartedTimerItem != null

    val isAudioCallStarted: Boolean
        get() = isMediaUpgradeStarted && mediaUpgradeStartedTimerItem is MediaUpgradeStartedTimerItem.Audio

    val showMessagesUnseenIndicator: Boolean get() = !isChatInBottom && messagesNotSeen > 0

    val isAttachmentButtonVisible: Boolean get() = isAttachmentButtonNeeded && isAttachmentAllowed

    fun initChat(companyName: String?, queueId: String?, visitorContextAssetId: String?): ChatState = copy(
        integratorChatStarted = true,
        companyName = companyName,
        queueId = queueId,
        visitorContextAssetId = visitorContextAssetId,
        isVisible = true,
        showSendButton = false,
        isAttachmentButtonEnabled = true,
        isAttachmentAllowed = true
    )

    fun queueingStarted(operatorStatusItem: OperatorStatusItem?): ChatState = copy(
        formattedOperatorName = null,
        operatorProfileImgUrl = null,
        chatInputMode = ChatInputMode.ENABLED,
        engagementRequested = true,
        operatorStatusItem = operatorStatusItem
    )

    fun setSecureMessagingState(): ChatState = copy(
        isSecureMessaging = true,
        chatInputMode = ChatInputMode.ENABLED,
        isAttachmentButtonNeeded = true
    )

    fun setLiveChatState(): ChatState = copy(isSecureMessaging = false)

    fun allowSendAttachmentStateChanged(isAttachmentAllowed: Boolean): ChatState = copy(isAttachmentAllowed = isAttachmentAllowed)

    fun engagementStarted(): ChatState = copy(
        chatInputMode = ChatInputMode.ENABLED,
        isAttachmentButtonNeeded = true,
        engagementRequested = true
    )

    fun transferring(): ChatState = copy(
        formattedOperatorName = null,
        operatorProfileImgUrl = null,
        engagementRequested = false,
        operatorStatusItem = OperatorStatusItem.Transferring,
        chatInputMode = ChatInputMode.DISABLED,
        showSendButton = false,
        isAttachmentButtonNeeded = false
    )

    fun operatorConnected(
        formattedOperatorName: String?,
        operatorProfileImgUrl: String?
    ): ChatState = copy(
        formattedOperatorName = formattedOperatorName,
        operatorProfileImgUrl = operatorProfileImgUrl
    )

    fun historyLoaded(chatItems: List<ChatItem>): ChatState = copy(
        chatInputMode = ChatInputMode.ENABLED_NO_ENGAGEMENT,
        isAttachmentButtonNeeded = false,
        chatItems = chatItems
    )

    fun changeItems(newItems: List<ChatItem>): ChatState = copy(chatItems = newItems)

    fun changeTimerItem(
        newItems: List<ChatItem>,
        mediaUpgradeStartedTimerItem: MediaUpgradeStartedTimerItem?
    ): ChatState = copy(
        chatItems = newItems,
        mediaUpgradeStartedTimerItem = mediaUpgradeStartedTimerItem
    )

    fun changeVisibility(isVisible: Boolean): ChatState = copy(isVisible = isVisible)

    fun setLastTypedText(text: String): ChatState = copy(lastTypedText = text)

    fun chatInputModeChanged(chatInputMode: ChatInputMode): ChatState = copy(
        chatInputMode = chatInputMode,
        isAttachmentButtonNeeded = chatInputMode == ChatInputMode.ENABLED
    )

    fun isInBottomChanged(isChatInBottom: Boolean): ChatState = copy(isChatInBottom = isChatInBottom)

    fun messagesNotSeenChanged(messagesNotSeen: Int): ChatState = copy(messagesNotSeen = messagesNotSeen)

    fun setPendingNavigationType(pendingNavigationType: String?): ChatState = copy(pendingNavigationType = pendingNavigationType)

    fun changeUnsentMessages(unsentMessages: List<VisitorMessageItem.Unsent>): ChatState = copy(unsentMessages = unsentMessages)

    fun setShowSendButton(isShow: Boolean): ChatState = copy(showSendButton = isShow)

    fun setIsOperatorTyping(isOperatorTyping: Boolean): ChatState = copy(isOperatorTyping = isOperatorTyping)

    fun setIsAttachmentButtonEnabled(isAttachmentButtonEnabled: Boolean): ChatState = copy(isAttachmentButtonEnabled = isAttachmentButtonEnabled)

    fun stop(): ChatState = copy(
        formattedOperatorName = null,
        operatorProfileImgUrl = null,
        isVisible = false,
        integratorChatStarted = false,
        isAttachmentButtonNeeded = false
    )
}
