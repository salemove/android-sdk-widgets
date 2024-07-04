package com.glia.widgets.view.unifiedui.theme.secureconversations

import com.glia.widgets.view.unifiedui.Mergeable
import com.glia.widgets.view.unifiedui.merge
import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.HeaderTheme
import com.glia.widgets.view.unifiedui.theme.base.TextInputTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme
import com.glia.widgets.view.unifiedui.theme.chat.AttachmentsPopupTheme
import com.glia.widgets.view.unifiedui.theme.chat.FileUploadBarTheme

internal data class SecureConversationsWelcomeScreenTheme(
    val headerTheme: HeaderTheme? = null,
    val welcomeTitleTheme: TextTheme? = null,
    val titleImageTheme: ColorTheme? = null,
    val welcomeSubtitleTheme: TextTheme? = null,
    val checkMessagesButtonTheme: TextTheme? = null,
    val messageTitleTheme: TextTheme? = null,
    val messageInputNormalTheme: TextInputTheme? = null,
    val messageInputActiveTheme: TextInputTheme? = null,
    val messageInputDisabledTheme: TextInputTheme? = null,
    val messageInputErrorTheme: TextInputTheme? = null,
    val messageInputHintTheme: TextTheme? = null,
    val enabledSendButtonTheme: ButtonTheme? = null,
    val disabledSendButtonTheme: ButtonTheme? = null,
    val loadingSendButtonTheme: ButtonTheme? = null,
    val activityIndicatorColorTheme: ColorTheme? = null,
    val messageWarningTheme: TextTheme? = null,
    val messageWarningIconColorTheme: ColorTheme? = null,
    val filePickerButtonTheme: ColorTheme? = null,
    val filePickerButtonDisabledTheme: ColorTheme? = null,
    val attachmentListTheme: FileUploadBarTheme? = null,
    val pickMediaTheme: AttachmentsPopupTheme? = null,
    val backgroundTheme: ColorTheme? = null
) : Mergeable<SecureConversationsWelcomeScreenTheme> {
    override fun merge(other: SecureConversationsWelcomeScreenTheme): SecureConversationsWelcomeScreenTheme =
        SecureConversationsWelcomeScreenTheme(
            headerTheme = headerTheme merge other.headerTheme,
            welcomeTitleTheme = welcomeTitleTheme merge other.welcomeTitleTheme,
            titleImageTheme = titleImageTheme merge other.titleImageTheme,
            welcomeSubtitleTheme = welcomeSubtitleTheme merge other.welcomeSubtitleTheme,
            checkMessagesButtonTheme = checkMessagesButtonTheme merge other.checkMessagesButtonTheme,
            messageTitleTheme = messageTitleTheme merge other.messageTitleTheme,
            messageInputNormalTheme = messageInputNormalTheme merge other.messageInputNormalTheme,
            messageInputActiveTheme = messageInputActiveTheme merge other.messageInputActiveTheme,
            messageInputDisabledTheme = messageInputDisabledTheme merge other.messageInputDisabledTheme,
            messageInputErrorTheme = messageInputErrorTheme merge other.messageInputErrorTheme,
            messageInputHintTheme = messageInputHintTheme merge other.messageInputHintTheme,
            enabledSendButtonTheme = enabledSendButtonTheme merge other.enabledSendButtonTheme,
            disabledSendButtonTheme = disabledSendButtonTheme merge other.disabledSendButtonTheme,
            loadingSendButtonTheme = loadingSendButtonTheme merge other.loadingSendButtonTheme,
            activityIndicatorColorTheme = activityIndicatorColorTheme merge other.activityIndicatorColorTheme,
            messageWarningTheme = messageWarningTheme merge other.messageWarningTheme,
            messageWarningIconColorTheme = messageWarningIconColorTheme merge other.messageWarningIconColorTheme,
            filePickerButtonTheme = filePickerButtonTheme merge other.filePickerButtonTheme,
            filePickerButtonDisabledTheme = filePickerButtonDisabledTheme merge other.filePickerButtonDisabledTheme,
            attachmentListTheme = attachmentListTheme merge other.attachmentListTheme,
            pickMediaTheme = pickMediaTheme merge other.pickMediaTheme,
            backgroundTheme = backgroundTheme merge other.backgroundTheme
        )
}
