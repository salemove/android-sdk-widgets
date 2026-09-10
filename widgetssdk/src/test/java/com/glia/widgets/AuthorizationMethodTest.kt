package com.glia.widgets

import org.junit.Assert.assertEquals
import org.junit.Test

class AuthorizationMethodTest {

    @Test
    fun toCoreType_convertsToCoreSiteApiKey() {
        val widgetsSiteApiKey = SiteApiKey(id = "testId", secret = "testSecret")

        val coreApiKey = widgetsSiteApiKey.toCoreType()

        assertEquals(com.glia.androidsdk.AuthorizationMethod.SiteApiKey::class.java, coreApiKey::class.java)

        val authorizationMethod = coreApiKey as com.glia.androidsdk.AuthorizationMethod.SiteApiKey
        assertEquals(widgetsSiteApiKey.id, authorizationMethod.id)
        assertEquals(widgetsSiteApiKey.secret, authorizationMethod.secret)
    }

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
