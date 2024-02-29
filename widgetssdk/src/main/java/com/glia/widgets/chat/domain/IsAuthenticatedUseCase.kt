package com.glia.widgets.chat.domain

import com.glia.androidsdk.visitor.Authentication

internal class IsAuthenticatedUseCase(private val authentication: Authentication?) {
    operator fun invoke(): Boolean = authentication?.isAuthenticated == true
}
