package com.glia.widgets.fcm

/**
 * Push object sent by the Glia client library
 */
interface GliaPushMessage {
    /**
     * Defines the possible types of the Glia push notifications
     */
    enum class PushType {
        /**
         * When a chat message is sent
         */
        CHAT_MESSAGE,

        /**
         * When a queued message is sent
         */
        QUEUED_MESSAGE,

        /**
         * When this is chat message from secure conversations
         */
        SECURE_CONVERSATION,

        /**
         * Default value, could be received if value returned by server is not supported by current version of SDK
         */
        UNIDENTIFIED
    }

    /**
     * Defines the possible types of the push notification actions
     */
    enum class Action {
        /**
         * When Firebase messaging service receives a push notification
         */
        NOTIFICATION_RECEIVED,

        /**
         * When a visitor clicks on the notification
         */
        NOTIFICATION_OPENED
    }

    /**
     * @return [Action]
     */
    val action: Action

    /**
     * @return [PushType]
     */
    val type: PushType
}

internal class GliaPushMessageImpl(
    override val action: GliaPushMessage.Action,
    override val type: GliaPushMessage.PushType
) : GliaPushMessage {
    constructor(gliaPushMessage: com.glia.androidsdk.fcm.GliaPushMessage) : this(
        action = gliaPushMessage.action.toWidgetsType(),
        type = gliaPushMessage.type.toWidgetsType()
    )
}

internal fun com.glia.androidsdk.fcm.GliaPushMessage.Action.toWidgetsType() : GliaPushMessage.Action =
    when (this) {
        com.glia.androidsdk.fcm.GliaPushMessage.Action.NOTIFICATION_RECEIVED -> GliaPushMessage.Action.NOTIFICATION_RECEIVED
        com.glia.androidsdk.fcm.GliaPushMessage.Action.NOTIFICATION_OPENED -> GliaPushMessage.Action.NOTIFICATION_OPENED
    }

internal fun com.glia.androidsdk.fcm.GliaPushMessage.PushType.toWidgetsType() : GliaPushMessage.PushType =
    when (this) {
        com.glia.androidsdk.fcm.GliaPushMessage.PushType.CHAT_MESSAGE -> GliaPushMessage.PushType.CHAT_MESSAGE
        com.glia.androidsdk.fcm.GliaPushMessage.PushType.QUEUED_MESSAGE -> GliaPushMessage.PushType.QUEUED_MESSAGE
        com.glia.androidsdk.fcm.GliaPushMessage.PushType.SECURE_CONVERSATION -> GliaPushMessage.PushType.SECURE_CONVERSATION
        com.glia.androidsdk.fcm.GliaPushMessage.PushType.UNIDENTIFIED -> GliaPushMessage.PushType.UNIDENTIFIED
    }
