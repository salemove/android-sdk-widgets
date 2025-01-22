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
    val isSharingScreen: Boolean = false,
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
        isSendButtonVisible = false,
        isSendButtonEnabled = true,
        isAttachmentAllowed = true,
        isAttachmentButtonEnabled = true,
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
        isSecureMessaging = false,
        isSendButtonEnabled = true,
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
        isSendButtonVisible = false,
        isAttachmentButtonNeeded = false
    )

    fun operatorConnected(
        formattedOperatorName: String?,
        operatorProfileImgUrl: String?
    ): ChatState = copy(
        formattedOperatorName = formattedOperatorName,
        operatorProfileImgUrl = operatorProfileImgUrl,
        chatInputMode = ChatInputMode.ENABLED,
        isAttachmentButtonNeeded = true
    )

    fun liveChatHistoryLoaded(): ChatState = copy(
        chatInputMode = ChatInputMode.ENABLED_NO_ENGAGEMENT,
        isAttachmentButtonNeeded = false
    )

    fun upgradeMedia(isVideo: Boolean?): ChatState = copy(isMediaUpgradeVide = isVideo)

    fun changeVisibility(isVisible: Boolean): ChatState = copy(isVisible = isVisible)

    fun setLastTypedText(text: String): ChatState = copy(lastTypedText = text)

    fun isInBottomChanged(isChatInBottom: Boolean): ChatState = copy(isChatInBottom = isChatInBottom)

    fun messagesNotSeenChanged(messagesNotSeen: Int): ChatState = copy(messagesNotSeen = messagesNotSeen)

    fun setShowSendButton(isShow: Boolean): ChatState = copy(isSendButtonVisible = isShow)

    fun setSecureMessagingUnavailable(): ChatState = copy(
        isSecureConversationsUnavailableLabelVisible = true,
        isAttachmentButtonNeeded = true,
        isAttachmentButtonEnabled = false,
        isSendButtonVisible = true,
        isSendButtonEnabled = false,
        chatInputMode = ChatInputMode.DISABLED
    )

    fun setSecureMessagingAvailable(): ChatState = copy(
        isSecureConversationsUnavailableLabelVisible = false,
        isAttachmentButtonNeeded = true,
        isAttachmentButtonEnabled = true,
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

    fun startScreenSharing(): ChatState = copy(isSharingScreen = true)
    fun endScreenSharing(): ChatState = copy(isSharingScreen = false)
}
