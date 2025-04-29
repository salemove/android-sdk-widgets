package com.glia.widgets.fcm

/**
 * Defines the possible types the Glia push notification events
 */
enum class PushNotificationType {
    /**
     * Triggers when an engagement starts.
     */
    START,

    /**
     * Triggers when an engagement ends.
     */
    END,

    /**
     * Triggers when the engagement request ends without resulting in an engagement.
     */
    FAILED,

    /**
     * Triggers when a chat message is sent.
     */
    MESSAGE,

    /**
     * Triggers when the engagement is transferred to another user.
     */
    TRANSFER,

    /**
     * Default value, could be received if value returned by server is not supported by current version of SDK
     */
    UNKNOWN
}

internal fun PushNotificationType.toCoreType(): com.glia.androidsdk.fcm.PushNotificationType =
    when (this) {
        PushNotificationType.START -> com.glia.androidsdk.fcm.PushNotificationType.START
        PushNotificationType.END -> com.glia.androidsdk.fcm.PushNotificationType.END
        PushNotificationType.FAILED -> com.glia.androidsdk.fcm.PushNotificationType.FAILED
        PushNotificationType.MESSAGE -> com.glia.androidsdk.fcm.PushNotificationType.MESSAGE
        PushNotificationType.TRANSFER -> com.glia.androidsdk.fcm.PushNotificationType.TRANSFER
        PushNotificationType.UNKNOWN -> com.glia.androidsdk.fcm.PushNotificationType.UNKNOWN
    }

internal fun Collection<PushNotificationType>.toCoreType(): List<com.glia.androidsdk.fcm.PushNotificationType> =
    map { it.toCoreType() }

internal fun com.glia.androidsdk.fcm.PushNotificationType.toWidgetsType(): PushNotificationType =
    when (this) {
        com.glia.androidsdk.fcm.PushNotificationType.START -> PushNotificationType.START
        com.glia.androidsdk.fcm.PushNotificationType.END -> PushNotificationType.END
        com.glia.androidsdk.fcm.PushNotificationType.FAILED -> PushNotificationType.FAILED
        com.glia.androidsdk.fcm.PushNotificationType.MESSAGE -> PushNotificationType.MESSAGE
        com.glia.androidsdk.fcm.PushNotificationType.TRANSFER -> PushNotificationType.TRANSFER
        com.glia.androidsdk.fcm.PushNotificationType.UNKNOWN -> PushNotificationType.UNKNOWN
    }
