package com.glia.widgets.secureconversations

import com.glia.androidsdk.RequestCallback
import com.glia.telemetry_lib.GliaLogger
import com.glia.widgets.callbacks.OnResult

/**
 * Secure Conversations offers the ability to asynchronously and securely communications for authenticated visitors.
 * It is a secure alternative to SMS and email which also allow asynchronous interactions but are considered less secure.
 *
 * Read more about [Secure Conversations](https://docs.glia.com/glia-mobile/docs/android-widgets-secure-conversations)
 */
interface SecureConversations {
    /**
     * Subscribes to updates of the unread message count.
     *
     * This method allows you to receive updates whenever the unread message count for
     * the secure conversations changes. It doesn't count the live chat messages.
     * The provided callback will be triggered with the updated count.
     *
     * @param callback [OnResult] A callback that will be invoked with the updated unread message count.
     *
     * Note: Ensure to unsubscribe using [unSubscribeFromUnreadMessageCount] when updates are no longer needed
     * to avoid memory leaks or unnecessary updates.
     */
    fun subscribeToUnreadMessageCount(callback: OnResult<Int>)

    /**
     * Unsubscribes from updates of the unread message count.
     *
     * This method stops receiving updates for the unread message count for the provided callback.
     *
     * @param callback [OnResult] A callback that was previously subscribed to receive updates.
     *
     * Note: Ensure that the callback passed to this method is the same instance that was used
     * during subscription to successfully unsubscribe.
     */
    fun unSubscribeFromUnreadMessageCount(callback: OnResult<Int>)
}

/**
 * @hide
 */
class SecureConversationsImpl(
    private val secureConversations: com.glia.androidsdk.secureconversations.SecureConversations
) : SecureConversations {

    internal val subscribedCallbacks: MutableMap<Int, RequestCallback<Int>> = mutableMapOf()

    override fun subscribeToUnreadMessageCount(callback: OnResult<Int>) {
        GliaLogger.logMethodUse(SecureConversations::class, "subscribeToUnreadMessageCount")
        if (subscribedCallbacks.containsKey(callback.hashCode())) {
            // Already subscribed
            return
        }
        val requestCallback: RequestCallback<Int> = RequestCallback { count, gliaException ->
            if (gliaException == null && count != null) {
                callback.onResult(count)
            }
        }
        subscribedCallbacks[callback.hashCode()] = requestCallback
        secureConversations.subscribeToUnreadMessageCount(requestCallback)
    }

    override fun unSubscribeFromUnreadMessageCount(callback: OnResult<Int>) {
        GliaLogger.logMethodUse(SecureConversations::class, "unSubscribeFromUnreadMessageCount")
        subscribedCallbacks[callback.hashCode()]?.let { secureConversations.unSubscribeFromUnreadMessageCount(it) }
        subscribedCallbacks.remove(callback.hashCode())
    }
}
