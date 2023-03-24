package com.glia.widgets.callvisualizer.domain

import com.glia.widgets.call.CallActivity
import com.glia.widgets.chat.ChatActivity
import com.glia.widgets.filepreview.ui.FilePreviewActivity
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito.mock

internal class IsCallOrChatScreenActiveUseCaseTest {
    @Test
    fun isCallOrChatScreenActiveUseCase_returnsFalse_whenNotCallOrChatActivity() {
        val useCase = IsCallOrChatScreenActiveUseCase()
        val resumedActivity = mock(FilePreviewActivity::class.java)

        Assert.assertFalse(useCase(resumedActivity))
    }

    @Test
    fun isCallOrChatScreenActiveUseCase_returnsTrue_whenChatActivity() {
        val useCase = IsCallOrChatScreenActiveUseCase()
        val resumedActivity = mock(ChatActivity::class.java)

        Assert.assertTrue(useCase(resumedActivity))
    }

    @Test
    fun isCallOrChatScreenActiveUseCase_returnsTrue_whenCallActivity() {
        val useCase = IsCallOrChatScreenActiveUseCase()
        val resumedActivity = mock(CallActivity::class.java)

        Assert.assertTrue(useCase(resumedActivity))
    }

    @Test
    fun isCallOrChatScreenActiveUseCase_returnsFalse_whenActivityNull() {
        val useCase = IsCallOrChatScreenActiveUseCase()

        Assert.assertFalse(useCase(null))
    }
}