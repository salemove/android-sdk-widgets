package com.glia.widgets.snapshotutils

import com.glia.androidsdk.chat.SingleChoiceOption
import com.glia.widgets.chat.model.OperatorMessageItem
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal interface SnapshotOperatorMessage : SnapshotStrings {

    fun operatorMessagePlainText(
        id: String = "operatorMessageId",
        timestamp: Long = 1706696391000,
        showChatHead: Boolean = false,
        operatorProfileImgUrl: String? = null,
        operatorId: String? = "operatorId",
        operatorName: String? = "Snap Shot",
        content: String?
    ) = OperatorMessageItem.PlainText(
        id, timestamp, showChatHead, operatorProfileImgUrl, operatorId, operatorName, content
    )

    fun operatorMessageResponseCard(
        id: String = "operatorMessageId",
        timestamp: Long = 1706696391000,
        showChatHead: Boolean = false,
        operatorProfileImgUrl: String? = null,
        operatorId: String? = "operatorId",
        operatorName: String? = "Snap Shot",
        content: String?,
        singleChoiceOptions: List<SingleChoiceOption> = shortLengthTexts().mapIndexed { i, s -> singleChoiceOption(s, i.toString()) },
        choiceCardImageUrl: String? = null
    ) = OperatorMessageItem.ResponseCard(
        id, timestamp, showChatHead, operatorProfileImgUrl, operatorId, operatorName, content, singleChoiceOptions, choiceCardImageUrl
    )

    fun singleChoiceOption(
        text: String,
        value: String
    ) = mock<SingleChoiceOption>().also {
        whenever(it.text).thenReturn(text)
        whenever(it.value).thenReturn(value)
    }
}
