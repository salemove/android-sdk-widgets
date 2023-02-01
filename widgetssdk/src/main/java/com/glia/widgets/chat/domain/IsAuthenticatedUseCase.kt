package com.glia.widgets.chat.domain

import com.glia.androidsdk.visitor.Authentication

class IsAuthenticatedUseCase(private val authentication: Authentication) {
    fun execute(): Boolean = authentication.isAuthenticated
}