package com.glia.widgets.chat

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Determines which type of chat engagement to launch.
 *
 * Code example:
 * ```
 * Intent intent = new Intent(requireContext(), ChatActivity.class);
 * intent.putExtra(GliaWidgets.QUEUE_ID, "MESSAGING_QUEUE_ID");
 * intent.putExtra(GliaWidgets.CHAT_TYPE, (Parcelable) ChatType.SECURE_MESSAGING);
 * startActivity(intent);
 * ```
 */
@Parcelize
enum class ChatType : Parcelable {
    /**
     * Regular engagements with live chat.
     */
    LIVE_CHAT,

    /**
     * Secure Messaging.
     *
     * It is a secure alternative to SMS and email, which also allows for asynchronous interactions.
     *
     * @see <a href="https://docs.glia.com/glia-impl/docs/secure-conversations">Secure conversations</a>
     */
    SECURE_MESSAGING;
}
