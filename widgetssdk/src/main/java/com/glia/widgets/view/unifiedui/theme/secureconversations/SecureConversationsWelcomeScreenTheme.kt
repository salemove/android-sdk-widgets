package com.glia.widgets.view.unifiedui.theme.secureconversations

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
)
