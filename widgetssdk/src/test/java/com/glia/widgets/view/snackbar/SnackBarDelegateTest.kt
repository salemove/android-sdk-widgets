package com.glia.widgets.view.snackbar

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.view.View
import android.view.Window
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.glia.widgets.R
import com.glia.widgets.call.CallActivity
import com.glia.widgets.chat.ChatActivity
import com.glia.widgets.helper.rootView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SnackBarDelegateTest {
    private val viewId: Int = ViewCompat.generateViewId()
    private val bottomMargin: Int = 10
    private val mockContext: Context = mock()
    private val mockResources: Resources = mock {
        on { getDimensionPixelSize(any()) } doReturn bottomMargin
    }

    private val mockViewWithResources: View = mock<CoordinatorLayout> {
        on { context } doReturn mockContext
        on { resources } doReturn mockResources
        on { id } doReturn viewId
    }

    private val mockDecorView: View = mock {
        on { findViewById<View>(any()) } doReturn mockViewWithResources
    }
    private val mockWindow: Window = mock {
        on { decorView } doReturn mockDecorView
    }

    private val commonActivity: Activity = mock {
        on { window } doReturn mockWindow
    }

    private val callActivity: CallActivity = mock {
        on { findViewById<View>(any()) } doReturn mockViewWithResources
    }

    private val chatActivity: ChatActivity = mock {
        on { findViewById<View>(any()) } doReturn mockViewWithResources
    }

    @Test
    fun `ChatActivitySnackBarDelegate passes the corresponding view when initialized`() {
        ChatActivitySnackBarDelegate(chatActivity, mock(), mock())
        verify(chatActivity).findViewById<View>(R.id.chat_view)
    }

    @Test
    fun `CallActivitySnackBarDelegate passes the corresponding view when initialized`() {
        CallActivitySnackBarDelegate(callActivity, mock(), mock())
        verify(callActivity).findViewById<View>(R.id.call_view)
    }

    @Test
    fun `CommonSnackBarDelegate passes the root view when initialized`() {
        CommonSnackBarDelegate(commonActivity, mock(), mock())
        verify(commonActivity, times(2)).rootView
    }

    @Test
    fun `anchorViewId returns corresponding ids when chat or call activity is passed`() {
        CommonSnackBarDelegate(commonActivity, mock(), mock()).apply { assertNull(anchorViewId) }
        CallActivitySnackBarDelegate(callActivity, mock(), mock()).apply { assertEquals(R.id.buttons_layout_bg, anchorViewId) }
        ChatActivitySnackBarDelegate(chatActivity, mock(), mock()).apply { assertEquals(R.id.chat_message_layout, anchorViewId) }
    }

    @Test
    fun `marginBottom margin is calculated when the passed activity is not a call or chat activity`() {
        CommonSnackBarDelegate(commonActivity, mock(), mock()).apply { assertEquals(bottomMargin, marginBottom) }
        CallActivitySnackBarDelegate(callActivity, mock(), mock()).apply { assertNull(marginBottom) }
        ChatActivitySnackBarDelegate(chatActivity, mock(), mock()).apply { assertNull(marginBottom) }
    }

    @Test
    fun `background and text colors are inverse in CallActivitySnackBarDelegate`() {
        val common = CommonSnackBarDelegate(commonActivity, mock(), mock())
        val call = CallActivitySnackBarDelegate(callActivity, mock(), mock())
        val chat = ChatActivitySnackBarDelegate(chatActivity, mock(), mock())

        assertEquals(common.fallbackBackgroundColor, chat.fallbackBackgroundColor)
        assertEquals(common.fallbackTextColor, chat.fallbackTextColor)
        assertEquals(common.fallbackTextColor, call.fallbackBackgroundColor)
        assertEquals(common.fallbackBackgroundColor, call.fallbackTextColor)
    }

    @Test
    fun `snackBarDelegateFactory creates appropriate instance`() {
        SnackBarDelegateFactory(callActivity, mock(), mock()).apply { assertTrue(createDelegate() is CallActivitySnackBarDelegate) }
        SnackBarDelegateFactory(chatActivity, mock(), mock()).apply { assertTrue(createDelegate() is ChatActivitySnackBarDelegate) }
        SnackBarDelegateFactory(commonActivity, mock(), mock()).apply { assertTrue(createDelegate() is CommonSnackBarDelegate) }
    }

}
