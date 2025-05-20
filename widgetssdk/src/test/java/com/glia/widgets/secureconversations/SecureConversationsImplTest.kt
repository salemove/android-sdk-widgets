package com.glia.widgets.secureconversations

import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.widgets.GliaWidgetsException
import com.glia.widgets.callbacks.OnError
import com.glia.widgets.callbacks.OnResult
import com.glia.widgets.toWidgetsType
import io.mockk.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock

class SecureConversationsImplTest {

    private lateinit var secureConversationsCore: com.glia.androidsdk.secureconversations.SecureConversations
    private lateinit var secureConversationsWidgets: SecureConversationsImpl

    @Before
    fun setUp() {
        secureConversationsCore = mockk(relaxed = true)
        secureConversationsWidgets = SecureConversationsImpl(secureConversationsCore)
    }

    @Test
    fun `subscribeToUnreadMessageCount adds callback and calls SDK`() {
        val callback: OnResult<Int> = mockk(relaxed = true)
        val requestCallbackSlot = slot<RequestCallback<Int>>()
        val unreadCount = 10
        every { secureConversationsCore.subscribeToUnreadMessageCount(capture(requestCallbackSlot)) } just Runs

        secureConversationsWidgets.subscribeToUnreadMessageCount(callback)

        verify { secureConversationsCore.subscribeToUnreadMessageCount(any()) }
        assert(secureConversationsWidgets.subscribedCallbacks.containsKey(callback.hashCode()))
        requestCallbackSlot.captured.onResult(unreadCount, null)
        verify { callback.onResult(unreadCount) }
    }

    @Test
    fun `subscribeToUnreadMessageCount does not subscribe the same callback twice`() {
        val callback: OnResult<Int> = mockk(relaxed = true)
        val requestCallbackSlot = slot<RequestCallback<Int>>()
        every { secureConversationsCore.subscribeToUnreadMessageCount(capture(requestCallbackSlot)) } just Runs

        // Subscribe the same callback twice
        secureConversationsWidgets.subscribeToUnreadMessageCount(callback)
        secureConversationsWidgets.subscribeToUnreadMessageCount(callback)

        // Verify that the SDK's subscribeToUnreadMessageCount is called only once
        verify(exactly = 1) { secureConversationsCore.subscribeToUnreadMessageCount(any()) }
        // Verify that the callback is stored only once in subscribedCallbacks
        assert(secureConversationsWidgets.subscribedCallbacks.size == 1)
        assert(secureConversationsWidgets.subscribedCallbacks.containsKey(callback.hashCode()))
    }

    @Test
    fun `unSubscribeFromUnreadMessageCount removes callback and calls SDK`() {
        val callback: OnResult<Int> = mockk(relaxed = true)
        val requestCallback: RequestCallback<Int> = mockk(relaxed = true)
        secureConversationsWidgets.subscribedCallbacks[callback.hashCode()] = requestCallback

        secureConversationsWidgets.unSubscribeFromUnreadMessageCount(callback)

        verify { secureConversationsCore.unSubscribeFromUnreadMessageCount(requestCallback) }
        assert(!secureConversationsWidgets.subscribedCallbacks.containsKey(callback.hashCode()))
    }
}
