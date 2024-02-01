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
    val isMediaUpgradeVide: Boolean? = null,
    val chatInputMode: ChatInputMode = ChatInputMode.ENABLED_NO_ENGAGEMENT,
    val lastTypedText: String = "",
    val engagementRequested: Boolean = false,
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

    val isMediaUpgradeStarted: Boolean get() = isMediaUpgradeVide != null

    val isAudioCallStarted: Boolean
        get() = isMediaUpgradeVide != true

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

    fun queueingStarted(): ChatState = copy(
        formattedOperatorName = null,
        operatorProfileImgUrl = null,
        chatInputMode = ChatInputMode.ENABLED,
        engagementRequested = true
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
        operatorProfileImgUrl = operatorProfileImgUrl,
        chatInputMode = ChatInputMode.ENABLED,
        isAttachmentButtonNeeded = true
    )

    fun historyLoaded(): ChatState = copy(
        chatInputMode = ChatInputMode.ENABLED_NO_ENGAGEMENT,
        isAttachmentButtonNeeded = false
    )

    fun upgradeMedia(isVideo: Boolean?): ChatState = copy(isMediaUpgradeVide = isVideo)

    fun changeVisibility(isVisible: Boolean): ChatState = copy(isVisible = isVisible)

    fun setLastTypedText(text: String): ChatState = copy(lastTypedText = text)

    fun isInBottomChanged(isChatInBottom: Boolean): ChatState = copy(isChatInBottom = isChatInBottom)

    fun messagesNotSeenChanged(messagesNotSeen: Int): ChatState = copy(messagesNotSeen = messagesNotSeen)

    fun setShowSendButton(isShow: Boolean): ChatState = copy(showSendButton = isShow)

    fun setIsOperatorTyping(isOperatorTyping: Boolean): ChatState = copy(isOperatorTyping = isOperatorTyping)

    fun setIsAttachmentButtonEnabled(isAttachmentButtonEnabled: Boolean): ChatState = copy(isAttachmentButtonEnabled = isAttachmentButtonEnabled)

    fun stop(): ChatState = copy(
        formattedOperatorName = null,
        operatorProfileImgUrl = null,
        isVisible = false,
        integratorChatStarted = false,
        isAttachmentButtonNeeded = false,
        isMediaUpgradeVide = null
    )
}
