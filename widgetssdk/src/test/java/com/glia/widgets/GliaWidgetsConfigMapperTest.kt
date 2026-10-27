package com.glia.widgets

import android.app.Application
import android.content.Context
import com.glia.androidsdk.GliaException
import com.glia.widgets.helper.toCoreType
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GliaWidgetsConfigMapperTest {

    @Test(expected = GliaException::class)
    fun `toCoreType fails when the siteID is blank`() {
        GliaWidgetsConfig.Builder()
            .setSiteId("")
            .setRegion(Region.US)
            .setContext(mockContext())
            .build()
            .toCoreType()
    }

    @Test(expected = GliaWidgetsException::class)
    fun `toCoreType fails when the siteID is missing`() {
        GliaWidgetsConfig.Builder()
            .setRegion(Region.US)
            .setContext(mockContext())
            .build()
            .toCoreType()
    }

    @Test(expected = GliaWidgetsException::class)
    fun `toCoreType fails when the context is missing`() {
        GliaWidgetsConfig.Builder()
            .setSiteId("SiteId")
            .setRegion(Region.US)
            .build()
            .toCoreType()
    }

    @Test(expected = GliaWidgetsException::class)
    fun `toCoreType fails when the region is missing`() {
        GliaWidgetsConfig.Builder()
            .setSiteId("SiteId")
            .setContext(mockContext())
            .build()
            .toCoreType()
    }

    @Test
    fun `toCoreType succeeds when all required fields are present`() {
        val coreConfig = GliaWidgetsConfig.Builder()
            .setSiteId("SiteId")
            .setRegion(Region.Custom("beta"))
            .setContext(mockContext())
            .build()
            .toCoreType()

        assertEquals("SiteId", coreConfig.siteId)
        assertEquals(com.glia.androidsdk.Region.Beta, coreConfig.region)
    }

    private fun mockContext(): Context {
        val applicationContext = mock<Application>()
        whenever(applicationContext.applicationContext).thenReturn(applicationContext)
        val context = mock<Context>()
        whenever(context.applicationContext).thenReturn(applicationContext)
        return context
    }

}
