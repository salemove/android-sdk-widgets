package com.glia.widgets.secureconversations

import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback

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
     * @param callback a callback that returns [RequestCallback] with the number of unread
     * secure messages on success, or [GliaException] on failure.
     *
     * Exception may have one of the following causes:
     * [GliaException.Cause.AUTHENTICATION_ERROR] -when a visitor is not authenticated
     * [GliaException.Cause.INVALID_INPUT] - when SDK is not initialized
     */
    fun getUnreadMessageCount(callback: RequestCallback<Int?>)

    /**
     * The same as [SecureConversations.getUnreadMessageCount] but with the ability to subscribe to updates.
     */
    fun subscribeToUnreadMessageCount(callback: RequestCallback<Int?>)

    /**
     * Unsubscribe from updates of the unread message count.
     */
    fun unSubscribeFromUnreadMessageCount(callback: RequestCallback<Int?>)
}

/**
 * @hide
 */
class SecureConversationsImpl(
    private val secureConversations: com.glia.androidsdk.secureconversations.SecureConversations
) : SecureConversations {

    override fun getUnreadMessageCount(callback: RequestCallback<Int?>) {
        secureConversations.getUnreadMessageCount(callback)
    }

    override fun subscribeToUnreadMessageCount(callback: RequestCallback<Int?>) {
        secureConversations.subscribeToUnreadMessageCount(callback)
    }

    override fun unSubscribeFromUnreadMessageCount(callback: RequestCallback<Int?>) {
        secureConversations.unSubscribeFromUnreadMessageCount(callback)
    }
}

