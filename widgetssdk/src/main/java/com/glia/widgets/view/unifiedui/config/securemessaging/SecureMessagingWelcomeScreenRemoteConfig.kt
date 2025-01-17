package com.glia.widgets.view.unifiedui.config.securemessaging

import com.glia.widgets.view.unifiedui.config.base.ButtonRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.ColorLayerRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.HeaderRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.TextInputRemoteConfig
import com.glia.widgets.view.unifiedui.config.base.TextRemoteConfig
import com.glia.widgets.view.unifiedui.config.chat.AttachmentSourceListRemoteConfig
import com.glia.widgets.view.unifiedui.config.chat.FileUploadBarRemoteConfig
import com.glia.widgets.view.unifiedui.theme.securemessaging.SecureMessagingWelcomeScreenTheme
import com.google.gson.annotations.SerializedName

internal data class SecureMessagingWelcomeScreenRemoteConfig(
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
    @SerializedName("messageInputNormal")
    val messageInputNormalRemoteConfig: TextInputRemoteConfig?,
    @SerializedName("messageInputActive")
    val messageInputActiveRemoteConfig: TextInputRemoteConfig?,
    @SerializedName("messageInputDisabled")
    val messageInputDisabledRemoteConfig: TextInputRemoteConfig?,
    @SerializedName("messageInputError")
    val messageInputErrorRemoteConfig: TextInputRemoteConfig?,
    @SerializedName("messageInputHint")
    val messageInputHintRemoteConfig: TextRemoteConfig?,
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
    fun toSecureMessagingWelcomeScreenTheme(): SecureMessagingWelcomeScreenTheme =
        SecureMessagingWelcomeScreenTheme(
            headerTheme = headerRemoteConfig?.toHeaderTheme(),
            welcomeTitleTheme = welcomeTitleRemoteConfig?.toTextTheme(),
            titleImageTheme = titleImageRemoteConfig?.toColorTheme(),
            welcomeSubtitleTheme = welcomeSubtitleRemoteConfig?.toTextTheme(),
            checkMessagesButtonTheme = checkMessagesButtonRemoteConfig?.toTextTheme(),
            messageTitleTheme = messageTitleRemoteConfig?.toTextTheme(),
            messageInputNormalTheme = messageInputNormalRemoteConfig?.toTextInputTheme(),
            messageInputActiveTheme = messageInputActiveRemoteConfig?.toTextInputTheme(),
            messageInputDisabledTheme = messageInputDisabledRemoteConfig?.toTextInputTheme(),
            messageInputErrorTheme = messageInputErrorRemoteConfig?.toTextInputTheme(),
            messageInputHintTheme = messageInputHintRemoteConfig?.toTextTheme(),
            enabledSendButtonTheme = enabledSendButtonRemoteConfig?.toButtonTheme(),
            disabledSendButtonTheme = disabledSendButtonRemoteConfig?.toButtonTheme(),
            loadingSendButtonTheme = loadingSendButtonRemoteConfig?.toButtonTheme(),
            activityIndicatorColorTheme = activityIndicatorColorLayerRemoteConfig?.toColorTheme(),
            messageWarningTheme = messageWarningRemoteConfig?.toTextTheme(),
            messageWarningIconColorTheme = messageWarningIconColorLayerRemoteConfig?.toColorTheme(),
            filePickerButtonTheme = filePickerButtonRemoteConfig?.toColorTheme(),
            filePickerButtonDisabledTheme = filePickerButtonDisabledRemoteConfig?.toColorTheme(),
            attachmentListTheme = attachmentSourceListRemoteConfig?.toFileUploadBarTheme(),
            pickMediaTheme = pickMediaRemoteConfig?.toAttachmentsPopupTheme(),
            backgroundTheme = backgroundRemoteConfig?.toColorTheme()
        )
}
