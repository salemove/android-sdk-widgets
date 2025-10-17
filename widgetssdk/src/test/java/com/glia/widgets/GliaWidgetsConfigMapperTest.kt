package com.glia.widgets

import android.app.Application
import android.content.Context
import com.glia.androidsdk.GliaException
import com.glia.widgets.helper.toCoreType
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GliaWidgetsConfigMapperTest {

    @Test(expected = GliaWidgetsException::class)
    fun `toCoreType fails when the siteApiKey is missing`() {
        GliaWidgetsConfig.Builder()
            .setSiteId("SiteId")
            .setRegion(GliaWidgetsConfig.Regions.US)
            .setContext(mockContext())
            .build()
            .toCoreType()
    }

    @Test(expected = GliaException::class)
    fun `toCoreType fails when the siteApiKey ID is blank`() {
        GliaWidgetsConfig.Builder()
            .setSiteApiKey(com.glia.androidsdk.SiteApiKey("", "Secret"))
            .setSiteId("SiteId")
            .setRegion(GliaWidgetsConfig.Regions.US)
            .setContext(mockContext())
            .build()
            .toCoreType()
    }

    @Test(expected = GliaException::class)
    fun `toCoreType fails when the siteApiKey Secret is blank`() {
        GliaWidgetsConfig.Builder()
            .setSiteApiKey(SiteApiKey("Id", ""))
            .setSiteId("SiteId")
            .setRegion(GliaWidgetsConfig.Regions.US)
            .setContext(mockContext())
            .build()
            .toCoreType()
    }

    @Test(expected = GliaException::class)
    fun `toCoreType fails when the siteID is blank`() {
        GliaWidgetsConfig.Builder()
            .setSiteApiKey(SiteApiKey("Id", "Secret"))
            .setSiteId("")
            .setRegion(Region.US)
            .setContext(mockContext())
            .build()
            .toCoreType()
    }

    @Test(expected = GliaWidgetsException::class)
    fun `toCoreType fails when the siteID is missing`() {
        GliaWidgetsConfig.Builder()
            .setSiteApiKey(SiteApiKey("Id", "Secret"))
            .setRegion(Region.US)
            .setContext(mockContext())
            .build()
            .toCoreType()
    }

    @Test(expected = GliaWidgetsException::class)
    fun `toCoreType fails when the context is missing`() {
        GliaWidgetsConfig.Builder()
            .setSiteApiKey(SiteApiKey("Id", "Secret"))
            .setSiteId("SiteId")
            .setRegion(Region.US)
            .build()
            .toCoreType()
    }

    @Test(expected = GliaWidgetsException::class)
    fun `toCoreType fails when the region is missing`() {
        GliaWidgetsConfig.Builder()
            .setSiteApiKey(SiteApiKey("Id", "Secret"))
            .setSiteId("SiteId")
            .setContext(mockContext())
            .build()
            .toCoreType()
    }

    @Test(expected = GliaWidgetsException::class)
    fun `toCoreType fails when both regions are present`() {
        GliaWidgetsConfig.Builder()
            .setSiteApiKey(SiteApiKey("Id", "Secret"))
            .setSiteId("SiteId")
            .setRegion(Region.US)
            .setRegion(GliaWidgetsConfig.Regions.US)
            .setContext(mockContext())
            .build()
            .toCoreType()
    }

    @Test(expected = GliaWidgetsException::class)
    fun `toCoreType fails when provided incorrect string region`() {
        GliaWidgetsConfig.Builder()
            .setSiteApiKey(SiteApiKey("Id", "Secret"))
            .setSiteId("SiteId")
            .setRegion("beta")
            .setContext(mockContext())
            .build()
            .toCoreType()
    }

    @Test
    fun `toCoreType succeeds when all required fields are present`() {
        GliaWidgetsConfig.Builder()
            .setSiteApiKey(SiteApiKey("Id", "Secret"))
            .setSiteId("SiteId")
            .setRegion(Region.Beta)
            .setContext(mockContext())
            .build()
            .toCoreType()
    }

    private fun mockContext(): Context {
        val applicationContext = mock<Application>()
        whenever(applicationContext.applicationContext).thenReturn(applicationContext)
        val context = mock<Context>()
        whenever(context.applicationContext).thenReturn(applicationContext)
        return context
    }

}
