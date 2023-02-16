package com.glia.widgets.callvisualizer.domain

import android.app.Activity
import com.glia.widgets.base.GliaActivity
import com.glia.widgets.call.CallActivity
import com.glia.widgets.chat.ChatActivity
import com.glia.widgets.filepreview.ui.FilePreviewActivity
import com.glia.widgets.messagecenter.MessageCenterActivity
import com.glia.widgets.survey.SurveyActivity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock

internal class IsGliaActivityUseCaseTest {
    private val subjectUnderTest by lazy { IsGliaActivityUseCase() }
    @Test
    fun isGliaActivityUseCase_ReturnsFalse_WhenExternalActivityPassed() {
        assertFalse(subjectUnderTest(mock()))
    }

    @Test
    fun isGliaActivityUseCase_ReturnsTrue_WhenGliaActivityPassed() {
        val resumedActivity = mock<Activity>(extraInterfaces = arrayOf(GliaActivity::class))

        assertTrue(subjectUnderTest(resumedActivity))
    }

    @Test
    fun isGliaActivityUseCase_ReturnsTrue_WhenChatActivityPassed() {
        val resumedActivity = mock<ChatActivity>()

        assertTrue(subjectUnderTest(resumedActivity))
    }

    @Test
    fun isGliaActivityUseCase_ReturnsTrue_WhenCallActivityPassed() {
        val resumedActivity = mock<CallActivity>()

        assertTrue(subjectUnderTest(resumedActivity))
    }

    @Test
    fun isGliaActivityUseCase_ReturnsTrue_WhenMessageCenterActivityPassed() {
        val resumedActivity = mock<MessageCenterActivity>()

        assertTrue(subjectUnderTest(resumedActivity))
    }

    @Test
    fun isGliaActivityUseCase_ReturnsTrue_WhenSurveyActivityPassed() {
        val resumedActivity = mock<SurveyActivity>()

        assertTrue(subjectUnderTest(resumedActivity))
    }

    @Test
    fun isGliaActivityUseCase_ReturnsTrue_WhenFilePreviewActivityPassed() {
        val resumedActivity = mock<FilePreviewActivity>()

        assertTrue(subjectUnderTest(resumedActivity))
    }
}
