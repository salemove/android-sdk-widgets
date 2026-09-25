package com.glia.widgets.internal.chathead

import android.app.Activity
import com.glia.widgets.call.CallActivity
import com.glia.widgets.chat.ChatActivity
import com.glia.widgets.filepreview.ui.ImagePreviewActivity
import com.glia.widgets.helper.DialogHolderActivity
import com.glia.widgets.messagecenter.MessageCenterActivity
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class VisibleScreenTest {

    @Test
    fun `ChatActivity maps to CHAT`() {
        assertEquals(VisibleScreen.CHAT, mockk<ChatActivity>().toVisibleScreen())
    }

    @Test
    fun `CallActivity maps to CALL`() {
        assertEquals(VisibleScreen.CALL, mockk<CallActivity>().toVisibleScreen())
    }

    @Test
    fun `ImagePreviewActivity maps to IMAGE_PREVIEW`() {
        assertEquals(VisibleScreen.IMAGE_PREVIEW, mockk<ImagePreviewActivity>().toVisibleScreen())
    }

    @Test
    fun `MessageCenterActivity maps to MESSAGE_CENTER`() {
        assertEquals(VisibleScreen.MESSAGE_CENTER, mockk<MessageCenterActivity>().toVisibleScreen())
    }

    @Test
    fun `DialogHolderActivity maps to DIALOG_HOLDER`() {
        assertEquals(VisibleScreen.DIALOG_HOLDER, mockk<DialogHolderActivity>().toVisibleScreen())
    }

    @Test
    fun `any other activity maps to OTHER`() {
        assertEquals(VisibleScreen.OTHER, mockk<Activity>().toVisibleScreen())
    }
}
