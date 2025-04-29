package com.glia.widgets.fcm

/**
 * Represents a push notification event sent to the device.
 *
 * @param type The type of the event.
 * @param message The notification message.
 */
data class PushNotificationEvent(
    val type: PushNotificationType,
    val message: String
)

internal fun PushNotificationEvent.toCoreType(): com.glia.androidsdk.fcm.PushNotificationEvent {
    return com.glia.androidsdk.fcm.PushNotificationEvent(
        type.toCoreType(),
        message
    )
}

internal fun Collection<PushNotificationEvent>.toCoreType(): Collection<com.glia.androidsdk.fcm.PushNotificationEvent> {
    return map { it.toCoreType() }
}

internal fun com.glia.androidsdk.fcm.PushNotificationEvent.toWidgetsType(): PushNotificationEvent {
    return PushNotificationEvent(
        type = type.toWidgetsType(),
        message = message
    )
}

internal fun Collection<com.glia.androidsdk.fcm.PushNotificationEvent>.toWidgetsType(): Collection<PushNotificationEvent> {
    return map { it.toWidgetsType() }
}
