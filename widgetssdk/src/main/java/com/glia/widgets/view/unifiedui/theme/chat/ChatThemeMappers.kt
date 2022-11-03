package com.glia.widgets.view.unifiedui.theme.chat

import com.glia.widgets.view.unifiedui.config.chat.*
import com.glia.widgets.view.unifiedui.theme.base.updateFrom

internal fun ThemeAttachmentsItem?.updateFrom(attachmentSourceRemoteConfig: AttachmentSourceRemoteConfig?): ThemeAttachmentsItem? =
    attachmentSourceRemoteConfig?.let {
        ThemeAttachmentsItem(
            text = this?.text.updateFrom(it.textRemoteConfig),
            iconColor = this?.iconColor.updateFrom(it.tintColor)
        )
    } ?: this

internal fun ThemeAttachmentsPopup?.updateFrom(attachmentSourceListRemoteConfig: AttachmentSourceListRemoteConfig?): ThemeAttachmentsPopup? =
    attachmentSourceListRemoteConfig?.let {
        ThemeAttachmentsPopup(
            dividerColor = this?.dividerColor.updateFrom(it.separator),
            background = this?.background.updateFrom(it.background),
            photoLibrary = this?.photoLibrary?.updateFrom(it.photoLibrary),
            takePhoto = this?.takePhoto?.updateFrom(it.takePhoto),
            browse = this?.browse?.updateFrom(it.browse)
        )
    } ?: this

internal fun ThemeUserImage?.updateFrom(userImageRemoteConfig: UserImageRemoteConfig?): ThemeUserImage? =
    userImageRemoteConfig?.let {
        ThemeUserImage(
            placeholderColor = this?.placeholderColor.updateFrom(it.placeholderColor),
            placeholderBackgroundColor = this?.placeholderBackgroundColor.updateFrom(it.placeholderBackgroundColor),
            imageBackgroundColor = this?.imageBackgroundColor.updateFrom(it.imageBackgroundColor)
        )
    } ?: this

internal fun ThemeBubble?.updateFrom(bubbleRemoteConfig: BubbleRemoteConfig?): ThemeBubble? = bubbleRemoteConfig?.let {
    ThemeBubble(
        userImage = this?.userImage.updateFrom(it.userImage),
        badge = this?.badge.updateFrom(it.badgeRemoteConfig),
        onHoldOverlay = this?.onHoldOverlay.updateFrom(it.onHoldOverlay?.color)
    )
} ?: this

internal fun ThemeEngagementState?.updateFrom(engagementStateRemoteConfig: EngagementStateRemoteConfig?): ThemeEngagementState? =
    engagementStateRemoteConfig?.let {
        ThemeEngagementState(
            title = this?.title.updateFrom(it.title),
            description = this?.description.updateFrom(it.description),
            tintColor = this?.tintColor.updateFrom(it.tintColor)
        )
    } ?: this

internal fun ThemeOperator?.updateFrom(operatorRemoteConfig: OperatorRemoteConfig?): ThemeOperator? = operatorRemoteConfig?.let {
    ThemeOperator(
        image = this?.image.updateFrom(it.image),
        animationColor = this?.animationColor.updateFrom(it.animationColor),
        overlayColor = this?.overlayColor.updateFrom(it.overlayColor)
    )
} ?: this

internal fun ThemeEngagementStates?.updateFrom(engagementStatesRemoteConfig: EngagementStatesRemoteConfig?): ThemeEngagementStates? =
    engagementStatesRemoteConfig?.let {
        ThemeEngagementStates(
            operator = this?.operator.updateFrom(it.operatorRemoteConfig),
            queue = this?.queue.updateFrom(it.queue),
            connecting = this?.connecting.updateFrom(it.connecting),
            connected = this?.connected.updateFrom(it.connected),
            transferring = this?.transferring.updateFrom(it.transferring),
            onHold = this?.onHold.updateFrom(it.onHold)
        )
    } ?: this

internal fun ThemeMessageBalloon?.updateFrom(messageBalloonRemoteConfig: MessageBalloonRemoteConfig?): ThemeMessageBalloon? =
    messageBalloonRemoteConfig?.let {
        ThemeMessageBalloon(
            background = this?.background.updateFrom(it.background),
            text = this?.text.updateFrom(it.textRemoteConfig),
            status = this?.status.updateFrom(it.status),
            alignment = it.alignmentTypeRemoteConfig?.nativeAlignment ?: this?.alignment
        )
    } ?: this

internal fun ThemeFileUpload?.updateFrom(fileUploadRemoteConfig: FileUploadRemoteConfig?): ThemeFileUpload? = fileUploadRemoteConfig?.let {
    ThemeFileUpload(
        text = this?.text.updateFrom(it.textRemoteConfig),
        info = this?.info.updateFrom(it.info)
    )
} ?: this

internal fun ThemeFilePreview?.updateFrom(filePreviewRemoteConfig: FilePreviewRemoteConfig?): ThemeFilePreview? = filePreviewRemoteConfig?.let {
    ThemeFilePreview(
        text = this?.text.updateFrom(it.textRemoteConfig),
        errorIcon = this?.errorIcon.updateFrom(it.errorIcon),
        background = this?.background.updateFrom(it.background),
        errorBackground = this?.errorBackground.updateFrom(it.errorBackground)
    )
} ?: this

internal fun ThemeFileUploadBar?.updateFrom(fileUploadBarRemoteConfig: FileUploadBarRemoteConfig?): ThemeFileUploadBar? =
    fileUploadBarRemoteConfig?.let {
        ThemeFileUploadBar(
            filePreview = this?.filePreview.updateFrom(it.filePreviewRemoteConfig),
            uploading = this?.uploading.updateFrom(it.uploading),
            uploaded = this?.uploaded.updateFrom(it.uploaded),
            error = this?.error.updateFrom(it.error),
            progress = this?.progress.updateFrom(it.progress),
            errorProgress = this?.errorProgress.updateFrom(it.errorProgress),
            progressBackground = this?.progressBackground.updateFrom(it.progressBackground),
            removeButton = this?.removeButton.updateFrom(it.removeButton)
        )
    } ?: this

internal fun ThemeInput?.updateFrom(inputRemoteConfig: InputRemoteConfig?): ThemeInput? = inputRemoteConfig?.let {
    ThemeInput(
        text = this?.text.updateFrom(it.textRemoteConfig),
        placeholder = this?.placeholder.updateFrom(it.placeholder),
        divider = this?.divider.updateFrom(it.separator),
        sendButton = this?.sendButton.updateFrom(it.sendButtonRemoteConfig),
        mediaButton = this?.mediaButton.updateFrom(it.mediaButtonRemoteConfig),
        background = this?.background.updateFrom(it.background),
        fileUploadBar = this?.fileUploadBar.updateFrom(it.fileUploadBarRemoteConfig)
    )
} ?: this

internal fun ThemeResponseCardOption?.updateFrom(responseCardOptionRemoteConfig: ResponseCardOptionRemoteConfig?): ThemeResponseCardOption? =
    responseCardOptionRemoteConfig?.let {
        ThemeResponseCardOption(
            normal = this?.normal.updateFrom(it.normal),
            disabled = this?.disabled.updateFrom(it.disabled),
            selected = this?.selected.updateFrom(it.selected),
        )
    } ?: this

internal fun ThemeResponseCard?.updateFrom(responseCardRemoteConfig: ResponseCardRemoteConfig?): ThemeResponseCard? =
    responseCardRemoteConfig?.let {
        ThemeResponseCard(
            background = this?.background.updateFrom(it.background),
            option = this?.option.updateFrom(it.option),
            message = this?.message.updateFrom(it.message)
        )
    } ?: this

internal fun ThemeUpgrade?.updateFrom(upgradeRemoteConfig: UpgradeRemoteConfig?): ThemeUpgrade? = upgradeRemoteConfig?.let {
    ThemeUpgrade(
        text = this?.text.updateFrom(it.textRemoteConfig),
        description = this?.description.updateFrom(it.description),
        iconColor = this?.iconColor.updateFrom(it.iconColor),
        background = this?.background.updateFrom(it.background)
    )
} ?: this

internal fun ChatTheme?.updateFrom(chatRemoteConfig: ChatRemoteConfig?): ChatTheme? = chatRemoteConfig?.let {
    ChatTheme(
        background = this?.background.updateFrom(it.background),
        header = this?.header.updateFrom(it.headerRemoteConfig),
        operatorMessage = this?.operatorMessage.updateFrom(it.operatorMessage),
        visitorMessage = this?.visitorMessage.updateFrom(it.visitorMessage),
        connect = this?.connect.updateFrom(it.connect),
        input = this?.input.updateFrom(it.inputRemoteConfig),
        responseCard = this?.responseCard.updateFrom(it.responseCardRemoteConfig),
        audioUpgrade = this?.audioUpgrade.updateFrom(it.audioUpgradeRemoteConfig),
        videoUpgrade = this?.videoUpgrade.updateFrom(it.videoUpgradeRemoteConfig),
        bubble = this?.bubble.updateFrom(it.bubbleRemoteConfig),
        attachmentsPopup = this?.attachmentsPopup.updateFrom(it.attachmentSourceListRemoteConfig),
        unreadIndicator = this?.unreadIndicator.updateFrom(it.unreadIndicator),
        typingIndicator = this?.typingIndicator.updateFrom(it.typingIndicator)
    )
} ?: this
