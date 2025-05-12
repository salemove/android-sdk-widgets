package com.glia.widgets.secureconversations

import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.widgets.GliaWidgetsException
import com.glia.widgets.callbacks.OnError
import com.glia.widgets.callbacks.OnSuccess
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
    fun `getUnreadMessageCount calls SDK and invokes onSuccess`() {
        val onSuccess: OnSuccess<Int> = mockk(relaxed = true)
        val onError: OnError = mockk(relaxed = true)
        val unreadCount = 5
        every { secureConversationsCore.getUnreadMessageCount(any()) } answers {
            firstArg<RequestCallback<Int>>().onResult(unreadCount, null)
        }

        secureConversationsWidgets.getUnreadMessageCount(onSuccess, onError)

        verify { onSuccess.onSuccess(unreadCount) }
        verify(exactly = 0) { onError.onError(any()) }
    }

    @Test
    fun `getUnreadMessageCount calls SDK and invokes onError on failure`() {
        val onSuccess: OnSuccess<Int> = mockk(relaxed = true)
        val onError: OnError = mockk(relaxed = true)
        val exception = mock<GliaException>()
        val widgetsException = GliaWidgetsException("Error", GliaWidgetsException.Cause.AUTHENTICATION_ERROR)
        mockkStatic("com.glia.widgets.GliaWidgetsExceptionKt")
        every { exception.toWidgetsType() } returns widgetsException
        every { secureConversationsCore.getUnreadMessageCount(any()) } answers {
            firstArg<RequestCallback<Int>>().onResult(null, exception)
        }

        secureConversationsWidgets.getUnreadMessageCount(onSuccess, onError)

        verify { onError.onError(widgetsException) }
        verify(exactly = 0) { onSuccess.onSuccess(any()) }
    }

    @Test
    fun `subscribeToUnreadMessageCount adds callback and calls SDK`() {
        val callback: OnSuccess<Int> = mockk(relaxed = true)
        val requestCallbackSlot = slot<RequestCallback<Int>>()
        val unreadCount = 10
        every { secureConversationsCore.subscribeToUnreadMessageCount(capture(requestCallbackSlot)) } just Runs

        secureConversationsWidgets.subscribeToUnreadMessageCount(callback)

        verify { secureConversationsCore.subscribeToUnreadMessageCount(any()) }
        assert(secureConversationsWidgets.subscribedCallbacks.containsKey(callback.hashCode()))
        requestCallbackSlot.captured.onResult(unreadCount, null)
        verify { callback.onSuccess(unreadCount) }
    }

    @Test
    fun `subscribeToUnreadMessageCount does not subscribe the same callback twice`() {
        val callback: OnSuccess<Int> = mockk(relaxed = true)
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
        val callback: OnSuccess<Int> = mockk(relaxed = true)
        val requestCallback: RequestCallback<Int> = mockk(relaxed = true)
        secureConversationsWidgets.subscribedCallbacks[callback.hashCode()] = requestCallback

        secureConversationsWidgets.unSubscribeFromUnreadMessageCount(callback)

        verify { secureConversationsCore.unSubscribeFromUnreadMessageCount(requestCallback) }
        assert(!secureConversationsWidgets.subscribedCallbacks.containsKey(callback.hashCode()))
    }
}
