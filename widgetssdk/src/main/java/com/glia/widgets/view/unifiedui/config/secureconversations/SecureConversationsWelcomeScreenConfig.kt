package com.glia.widgets.view.unifiedui.config.secureconversations

import com.glia.widgets.view.unifiedui.config.base.*
import com.glia.widgets.view.unifiedui.config.chat.AttachmentSourceListRemoteConfig
import com.glia.widgets.view.unifiedui.config.chat.FileUploadBarRemoteConfig
import com.glia.widgets.view.unifiedui.theme.secureconversations.SecureConversationsWelcomeScreenTheme
import com.google.gson.annotations.SerializedName

internal data class SecureConversationsWelcomeScreenConfig(
    @SerializedName("header")
    val headerRemoteConfig: HeaderRemoteConfig?,
    @SerializedName("welcomeTitle")
    val welcomeTitleRemoteConfig: TextRemoteConfig?,
    @SerializedName("titleImage")
    val titleImageRemoteConfig: ColorLayerRemoteConfig?,
    @SerializedName("welcomeSubtitle")
    val welcomeSubtitleRemoteConfig: TextRemoteConfig?,
    @SerializedName("checkMessagesButton")
    val checkMessagesButtonRemoteConfig: TextRemoteConfig?,
    @SerializedName("messageTitle")
    val messageTitleRemoteConfig: TextRemoteConfig?,
    @SerializedName("messageTextViewNormal")
    val messageTextViewNormalRemoteConfig: TextRemoteConfig?,
    @SerializedName("messageTextViewDisabled")
    val messageTextViewDisabledRemoteConfig: TextRemoteConfig?,
    @SerializedName("messageTextViewActive")
    val messageTextViewActiveRemoteConfig: TextRemoteConfig?,
    @SerializedName("messageTextViewLayer")
    val messageTextViewLayerRemoteConfig: LayerRemoteConfig?,
    @SerializedName("enabledSendButton")
    val enabledSendButtonRemoteConfig: ButtonRemoteConfig?,
    @SerializedName("disabledSendButton")
    val disabledSendButtonRemoteConfig: ButtonRemoteConfig?,
    @SerializedName("loadingSendButton")
    val loadingSendButtonRemoteConfig: ButtonRemoteConfig?,
    @SerializedName("activityIndicatorColor")
    val activityIndicatorColorLayerRemoteConfig: ColorLayerRemoteConfig?,
    @SerializedName("messageWarning")
    val messageWarningRemoteConfig: TextRemoteConfig?,
    @SerializedName("messageWarningIconColor")
    val messageWarningIconColorLayerRemoteConfig: ColorLayerRemoteConfig?,
    @SerializedName("filePickerButton")
    val filePickerButtonRemoteConfig: ColorLayerRemoteConfig?,
    @SerializedName("filePickerButtonDisabled")
    val filePickerButtonDisabledRemoteConfig: ColorLayerRemoteConfig?,
    @SerializedName("attachmentList")
    val attachmentSourceListRemoteConfig: FileUploadBarRemoteConfig?,
    @SerializedName("pickMedia")
    val pickMediaRemoteConfig: AttachmentSourceListRemoteConfig?,
    @SerializedName("background")
    val backgroundRemoteConfig: ColorLayerRemoteConfig?
) {
    fun toSecureConversationsWelcomeScreenTheme(): SecureConversationsWelcomeScreenTheme =
        SecureConversationsWelcomeScreenTheme(
            headerRemoteConfig?.toHeaderTheme(),
            welcomeTitleRemoteConfig?.toTextTheme(),
            titleImageRemoteConfig?.toColorTheme(),
            welcomeSubtitleRemoteConfig?.toTextTheme(),
            checkMessagesButtonRemoteConfig?.toTextTheme(),
            messageTitleRemoteConfig?.toTextTheme(),
            messageTextViewNormalRemoteConfig?.toTextTheme(),
            messageTextViewDisabledRemoteConfig?.toTextTheme(),
            messageTextViewActiveRemoteConfig?.toTextTheme(),
            messageTextViewLayerRemoteConfig?.toLayerTheme(),
            enabledSendButtonRemoteConfig?.toButtonTheme(),
            disabledSendButtonRemoteConfig?.toButtonTheme(),
            loadingSendButtonRemoteConfig?.toButtonTheme(),
            activityIndicatorColorLayerRemoteConfig?.toColorTheme(),
            messageWarningRemoteConfig?.toTextTheme(),
            messageWarningIconColorLayerRemoteConfig?.toColorTheme(),
            filePickerButtonRemoteConfig?.toColorTheme(),
            filePickerButtonDisabledRemoteConfig?.toColorTheme(),
            attachmentSourceListRemoteConfig?.toFileUploadBarTheme(),
            pickMediaRemoteConfig?.toAttachmentsPopupTheme(),
            backgroundRemoteConfig?.toColorTheme()
        )
}
