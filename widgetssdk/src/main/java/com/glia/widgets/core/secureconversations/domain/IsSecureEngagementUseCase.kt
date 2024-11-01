package com.glia.widgets.core.secureconversations.domain

import com.glia.widgets.engagement.domain.IsQueueingOrEngagementUseCase

/*TODO overall think about do we need such a use case? Or it is better to pass the current engagement type to the places where we need to differentiate btw LIVE and SC engagements.
   We need all this repositories and use cases from the chat screen, so it might be clear*/
internal class IsSecureEngagementUseCase(
    private val isQueueingOrEngagementUseCase: IsQueueingOrEngagementUseCase
) {
    operator fun invoke(): Boolean {
        return /* TODO check here for current engagement being SC */ !isQueueingOrEngagementUseCase()
    }
}
