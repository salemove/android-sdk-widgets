package com.glia.widgets.chat.domain.gva

import com.glia.widgets.chat.model.Gva
import com.glia.widgets.chat.model.GvaButton

internal const val PHONE_SCHEME = "tel"
internal const val EMAIL_SCHEME = "mailto"

internal class DetermineGvaButtonTypeUseCase(private val determineGvaUrlTypeUseCase: DetermineGvaUrlTypeUseCase) {

    operator fun invoke(button: GvaButton): Gva.ButtonType = when {
        !button.destinationPbBroadcastEvent.isNullOrBlank() -> Gva.ButtonType.BroadcastEvent
        button.url.isNullOrBlank() -> Gva.ButtonType.PostBack(button.toResponse())
        else -> determineGvaUrlTypeUseCase(button.url)
    }
}
