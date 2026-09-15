package com.glia.widgets.fcm

import com.glia.androidsdk.fcm.GliaPushMessage.PushType as CorePushType

/**
 * Defines the kinds of Glia push notification a visitor can open the application from.
 *
 * Not to be confused with [PushNotificationType], which defines the events the SDK subscribes to.
 */
enum class PushMessageType {
    /**
     * A message in an ongoing live chat engagement.
     */
    CHAT_MESSAGE,

    /**
     * A response to a message the visitor left while no operator was available.
     */
    QUEUED_MESSAGE,

    /**
     * A secure conversation message. Handled by the SDK, see [PushNotifications.onNewMessage].
     */
    SECURE_CONVERSATION,

    /**
     * A Glia push notification of a type this version of the SDK does not recognise.
     */
    UNIDENTIFIED
}

internal fun CorePushType.toWidgetsType(): PushMessageType = when (this) {
    CorePushType.CHAT_MESSAGE -> PushMessageType.CHAT_MESSAGE
    CorePushType.QUEUED_MESSAGE -> PushMessageType.QUEUED_MESSAGE
    CorePushType.SECURE_CONVERSATION -> PushMessageType.SECURE_CONVERSATION
    CorePushType.UNIDENTIFIED -> PushMessageType.UNIDENTIFIED
}
