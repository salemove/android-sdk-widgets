package com.glia.widgets.view.unifiedui.theme.secureconversations

import com.glia.widgets.view.unifiedui.theme.base.ButtonTheme
import com.glia.widgets.view.unifiedui.theme.base.ColorTheme
import com.glia.widgets.view.unifiedui.theme.base.HeaderTheme
import com.glia.widgets.view.unifiedui.theme.base.LayerTheme
import com.glia.widgets.view.unifiedui.theme.base.TextTheme
import com.glia.widgets.view.unifiedui.theme.chat.AttachmentsPopupTheme
import com.glia.widgets.view.unifiedui.theme.chat.FileUploadBarTheme

internal data class SecureConversationsWelcomeScreenTheme(
    val headerTheme: HeaderTheme?,
    val welcomeTitleTheme: TextTheme?,
    val titleImageTheme: ColorTheme?,
    val welcomeSubtitleTheme: TextTheme?,
    val checkMessagesButtonTheme: TextTheme?,
    val messageTitleTheme: TextTheme?,
    val messageTextViewNormalTheme: TextTheme?,
    val messageTextViewDisabledTheme: TextTheme?,
    val messageTextViewActiveTheme: TextTheme?,
    val messageTextViewLayerTheme: LayerTheme?,
    val enabledSendButtonTheme: ButtonTheme?,
    val disabledSendButtonTheme: ButtonTheme?,
    val loadingSendButtonTheme: ButtonTheme?,
    val activityIndicatorColorTheme: ColorTheme?,
    val messageWarningTheme: TextTheme?,
    val messageWarningIconColorTheme: ColorTheme?,
    val filePickerButtonTheme: ColorTheme?,
    val filePickerButtonDisabledTheme: ColorTheme?,
    val attachmentListTheme: FileUploadBarTheme?,
    val pickMediaTheme: AttachmentsPopupTheme?,
    val backgroundTheme: ColorTheme?
)
