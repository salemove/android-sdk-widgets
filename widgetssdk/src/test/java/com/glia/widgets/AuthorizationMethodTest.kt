package com.glia.widgets

import org.junit.Assert.assertEquals
import org.junit.Test

class AuthorizationMethodTest {

    @Test
    fun toCoreType_convertsToUserApiKey() {
        val widgetsUserApiKey = AuthorizationMethod.UserApiKey(id = "testId", secret = "testSecret")

        val coreApiKey = widgetsUserApiKey.toCoreType()

        assertEquals(com.glia.androidsdk.AuthorizationMethod.UserApiKey::class.java, coreApiKey::class.java)

        val authorizationMethod = coreApiKey as com.glia.androidsdk.AuthorizationMethod.UserApiKey
        assertEquals(widgetsUserApiKey.id, authorizationMethod.id)
        assertEquals(widgetsUserApiKey.secret, authorizationMethod.secret)
    }
}
