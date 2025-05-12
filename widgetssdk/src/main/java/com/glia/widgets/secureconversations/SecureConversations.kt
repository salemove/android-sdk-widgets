package com.glia.widgets.secureconversations

import com.glia.androidsdk.RequestCallback
import com.glia.widgets.GliaWidgetsException
import com.glia.widgets.callbacks.OnError
import com.glia.widgets.callbacks.OnSuccess
import com.glia.widgets.toWidgetsType

/**
 * Secure Conversations offers the ability to asynchronously and securely communications for authenticated visitors.
 * It is a secure alternative to SMS and email which also allow asynchronous interactions but are considered less secure.
 *
 * Read more about [Secure Conversations](https://docs.glia.com/glia-mobile/docs/android-widgets-secure-conversations)
 */
interface SecureConversations {
    /**
     * Returns the number of unread messages for the secure conversations.
     * This number will increase with each message sent by the operator
     * that the visitor has not yet marked as read.
     *
     * @param onSuccess [OnSuccess] a callback that returns the number of unread
     * secure messages on success.
     * @param onError [OnError] a callback that returns [GliaWidgetsException] on failure.
     *
     * Exception may have one of the following causes:
     * [GliaWidgetsException.Cause.AUTHENTICATION_ERROR] -when a visitor is not authenticated
     * [GliaWidgetsException.Cause.INVALID_INPUT] - when SDK is not initialized
     */
    fun getUnreadMessageCount(onSuccess: OnSuccess<Int>, onError: OnError? = null)

    /**
     * Subscribes to updates of the unread message count.
     *
     * This method allows you to receive updates whenever the unread message count changes.
     * The provided callback will be triggered with the updated count.
     *
     * @param callback [OnSuccess] A callback that will be invoked with the updated unread message count.
     *
     * Note: Ensure to unsubscribe using [unSubscribeFromUnreadMessageCount] when updates are no longer needed
     * to avoid memory leaks or unnecessary updates.
     */
    fun subscribeToUnreadMessageCount(callback: OnSuccess<Int>)

    /**
     * Unsubscribes from updates of the unread message count.
     *
     * This method stops receiving updates for the unread message count for the provided callback.
     *
     * @param callback [OnSuccess] A callback that was previously subscribed to receive updates.
     *
     * Note: Ensure that the callback passed to this method is the same instance that was used
     * during subscription to successfully unsubscribe.
     */
    fun unSubscribeFromUnreadMessageCount(callback: OnSuccess<Int>)
}

/**
 * @hide
 */
class SecureConversationsImpl(
    private val secureConversations: com.glia.androidsdk.secureconversations.SecureConversations
) : SecureConversations {

    internal val subscribedCallbacks: MutableMap<Int, RequestCallback<Int>> = mutableMapOf()

    override fun getUnreadMessageCount(onSuccess: OnSuccess<Int>, onError: OnError?) {
        secureConversations.getUnreadMessageCount { count, gliaException ->
            if (gliaException != null || count == null) {
                onError?.onError(gliaException.toWidgetsType("Failed to get unread message count"))
            } else {
                onSuccess.onSuccess(count)
            }
        }
    }

    override fun subscribeToUnreadMessageCount(callback: OnSuccess<Int>) {
        if (subscribedCallbacks.containsKey(callback.hashCode())) {
            // Already subscribed
            return
        }
        val requestCallback: RequestCallback<Int> = RequestCallback { count, gliaException ->
                if (gliaException == null && count != null) {
                    callback.onSuccess(count)
                }
            }
        subscribedCallbacks[callback.hashCode()] = requestCallback
        secureConversations.subscribeToUnreadMessageCount(requestCallback)
    }

    override fun unSubscribeFromUnreadMessageCount(callback: OnSuccess<Int>) {
        subscribedCallbacks[callback.hashCode()]?.let { secureConversations.unSubscribeFromUnreadMessageCount(it) }
        subscribedCallbacks.remove(callback.hashCode())
    }
}
