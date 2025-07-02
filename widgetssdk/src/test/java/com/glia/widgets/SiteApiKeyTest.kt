package com.glia.widgets

import org.junit.Assert.assertEquals
import org.junit.Test
import com.glia.androidsdk.SiteApiKey as CoreSiteApiKey

class SiteApiKeyTest {

    @Test
    fun toCoreType_convertsToCoreSiteApiKey() {
        val widgetsApiKey = SiteApiKey(id = "testId", secret = "testSecret")

        val coreApiKey = widgetsApiKey.toCoreType()

        assertEquals(CoreSiteApiKey::class.java, coreApiKey::class.java)
        assertEquals(widgetsApiKey.id, coreApiKey.id)
        assertEquals(widgetsApiKey.secret, coreApiKey.secret)
    }

    @Test
    fun toWidgetType_convertsToWidgetsSiteApiKey() {
        val coreApiKey = CoreSiteApiKey("testId", "testSecret")

        val widgetsApiKey = coreApiKey.toWidgetType()

        assertEquals(SiteApiKey::class.java, widgetsApiKey::class.java)
        assertEquals(coreApiKey.id, widgetsApiKey.id)
        assertEquals(coreApiKey.secret, widgetsApiKey.secret)
    }
}
