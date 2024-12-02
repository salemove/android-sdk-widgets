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
    private val titleStringKey: Int = 123

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
        ChatActivitySnackBarDelegate(chatActivity, titleStringKey, mock(), mock())
        verify(chatActivity).findViewById<View>(R.id.chat_view)
    }

    @Test
    fun `CallActivitySnackBarDelegate passes the corresponding view when initialized`() {
        CallActivitySnackBarDelegate(callActivity, titleStringKey, mock(), mock())
        verify(callActivity).findViewById<View>(R.id.call_view)
    }

    @Test
    fun `CommonSnackBarDelegate passes the root view when initialized`() {
        CommonSnackBarDelegate(commonActivity, titleStringKey, mock(), mock())
        verify(commonActivity, times(2)).rootView
    }

    @Test
    fun `anchorViewId returns corresponding ids when chat or call activity is passed`() {
        CommonSnackBarDelegate(commonActivity, titleStringKey, mock(), mock()).apply { assertNull(anchorViewId) }
        CallActivitySnackBarDelegate(callActivity, titleStringKey, mock(), mock()).apply { assertEquals(R.id.buttons_layout_bg, anchorViewId) }
        ChatActivitySnackBarDelegate(chatActivity, titleStringKey, mock(), mock()).apply { assertEquals(R.id.message_input_background, anchorViewId) }
    }

    @Test
    fun `marginBottom margin is calculated when the passed activity is not a call or chat activity`() {
        CommonSnackBarDelegate(commonActivity, titleStringKey, mock(), mock()).apply { assertEquals(bottomMargin, marginBottom) }
        CallActivitySnackBarDelegate(callActivity, titleStringKey, mock(), mock()).apply { assertNull(marginBottom) }
        ChatActivitySnackBarDelegate(chatActivity, titleStringKey, mock(), mock()).apply { assertNull(marginBottom) }
    }

    @Test
    fun `background and text colors are inverse in CallActivitySnackBarDelegate`() {
        val common = CommonSnackBarDelegate(commonActivity, titleStringKey, mock(), mock())
        val call = CallActivitySnackBarDelegate(callActivity, titleStringKey, mock(), mock())
        val chat = ChatActivitySnackBarDelegate(chatActivity, titleStringKey, mock(), mock())

        assertEquals(common.fallbackBackgroundColor, chat.fallbackBackgroundColor)
        assertEquals(common.fallbackTextColor, chat.fallbackTextColor)
        assertEquals(common.fallbackTextColor, call.fallbackBackgroundColor)
        assertEquals(common.fallbackBackgroundColor, call.fallbackTextColor)
    }

    @Test
    fun `snackBarDelegateFactory creates appropriate instance`() {
        SnackBarDelegateFactory(callActivity, titleStringKey, mock(), mock()).apply { assertTrue(createDelegate() is CallActivitySnackBarDelegate) }
        SnackBarDelegateFactory(chatActivity, titleStringKey, mock(), mock()).apply { assertTrue(createDelegate() is ChatActivitySnackBarDelegate) }
        SnackBarDelegateFactory(commonActivity, titleStringKey, mock(), mock()).apply { assertTrue(createDelegate() is CommonSnackBarDelegate) }
    }

}
