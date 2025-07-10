package com.glia.widgets.chat.model

internal data class ChatState(
    val isVisible: Boolean = false,
    val isChatInBottom: Boolean = true,
    val messagesNotSeen: Int = 0,
    val formattedOperatorName: String? = null,
    val operatorProfileImgUrl: String? = null,
    val isMediaUpgradeVide: Boolean? = null,
    val chatInputMode: ChatInputMode = ChatInputMode.ENABLED_NO_ENGAGEMENT,
    val lastTypedText: String = "",
    val engagementRequested: Boolean = false,
    val operatorStatusItem: OperatorStatusItem? = null,
    val isSendButtonVisible: Boolean = false,
    val isSendButtonEnabled: Boolean = false,
    val isSecureConversationsUnavailableLabelVisible: Boolean = false,
    val isSecureConversationsTopBannerVisible: Boolean = false,
    val isAttachmentButtonEnabled: Boolean = false,
    val isAttachmentButtonNeeded: Boolean = false,
    val isOperatorTyping: Boolean = false,
    val isAttachmentAllowed: Boolean = true,
    val isSecureMessaging: Boolean = false,
    val gvaQuickReplies: List<GvaButton> = emptyList(),
    val isInitialized: Boolean = false
) {
    val isOperatorOnline: Boolean get() = formattedOperatorName != null

    val isMediaUpgradeStarted: Boolean get() = isMediaUpgradeVide != null

    val isAudioCallStarted: Boolean
        get() = isMediaUpgradeVide != true

    val showMessagesUnseenIndicator: Boolean get() = !isChatInBottom && messagesNotSeen > 0

    val isAttachmentButtonVisible: Boolean get() = isAttachmentButtonNeeded && isAttachmentAllowed

    fun initChat(): ChatState = copy(
        isVisible = true,
        isSendButtonVisible = true,
        isSendButtonEnabled = false,
        isAttachmentAllowed = true,
        isAttachmentButtonNeeded = true,
        isAttachmentButtonEnabled = false,
        isInitialized = true
    )

    fun queueingStarted(): ChatState = copy(
        formattedOperatorName = null,
        operatorProfileImgUrl = null,
        chatInputMode = ChatInputMode.ENABLED,
        engagementRequested = true
    )

    fun setSecureMessagingState(): ChatState = copy(
        isSecureMessaging = true,
        chatInputMode = ChatInputMode.ENABLED,
        isAttachmentButtonNeeded = true,
        engagementRequested = false,
        operatorProfileImgUrl = null,
        formattedOperatorName = null
    )

    fun setLiveChatState(): ChatState = copy(
        isSendButtonVisible = true,
        isSendButtonEnabled = false,
        isSecureMessaging = false,
        chatInputMode = ChatInputMode.ENABLED_NO_ENGAGEMENT,
        isSecureConversationsUnavailableLabelVisible = false,
        isSecureConversationsTopBannerVisible = false
    )

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
        isSendButtonVisible = true,
        isSendButtonEnabled = false,
        isAttachmentButtonNeeded = true,
        isAttachmentButtonEnabled = false
    )

    fun operatorConnected(
        formattedOperatorName: String?,
        operatorProfileImgUrl: String?
    ): ChatState = copy(
        formattedOperatorName = formattedOperatorName,
        operatorProfileImgUrl = operatorProfileImgUrl,
        chatInputMode = ChatInputMode.ENABLED,
        isAttachmentButtonEnabled = true
    )

    fun liveChatHistoryLoaded(): ChatState = copy(
        chatInputMode = ChatInputMode.ENABLED_NO_ENGAGEMENT,
        isAttachmentButtonNeeded = true,
        isAttachmentButtonEnabled = false
    )

    fun upgradeMedia(isVideo: Boolean?): ChatState = copy(isMediaUpgradeVide = isVideo)

    fun changeVisibility(isVisible: Boolean): ChatState = copy(isVisible = isVisible)

    fun setLastTypedText(text: String): ChatState = copy(lastTypedText = text)

    fun isInBottomChanged(isChatInBottom: Boolean): ChatState = copy(isChatInBottom = isChatInBottom)

    fun messagesNotSeenChanged(messagesNotSeen: Int): ChatState = copy(messagesNotSeen = messagesNotSeen)

    fun setSendButtonEnabled(isShow: Boolean): ChatState = copy(isSendButtonEnabled = isShow)

    fun setSecureMessagingUnavailable(): ChatState = copy(
        isSecureConversationsUnavailableLabelVisible = true,
        isAttachmentButtonNeeded = true,
        isSendButtonVisible = true,
        isSendButtonEnabled = false,
        chatInputMode = ChatInputMode.DISABLED
    )

    fun setSecureMessagingAvailable(): ChatState = copy(
        isSecureConversationsUnavailableLabelVisible = false,
        isAttachmentButtonNeeded = true,
        isSendButtonVisible = true,
        isSendButtonEnabled = true,
        chatInputMode = ChatInputMode.ENABLED
    )

    fun setSecureConversationsTopBannerVisibility(isVisible: Boolean): ChatState = copy(
        isSecureConversationsTopBannerVisible = isVisible
    )

    fun setIsOperatorTyping(isOperatorTyping: Boolean): ChatState = copy(isOperatorTyping = isOperatorTyping)

    fun setIsAttachmentButtonEnabled(isAttachmentButtonEnabled: Boolean): ChatState = copy(isAttachmentButtonEnabled = isAttachmentButtonEnabled)

    fun chatUnavailableState(): ChatState = copy(
        formattedOperatorName = null,
        operatorProfileImgUrl = null,
        isVisible = false,
        isAttachmentButtonNeeded = false,
        isMediaUpgradeVide = null
    )
}
