package com.glia.widgets.core.configuration

import com.glia.androidsdk.Glia
import com.glia.widgets.R
import com.glia.widgets.StringProvider
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.ResourceProvider
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkClass
import io.mockk.mockkObject
import io.mockk.mockkStatic
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

private const val DEFAULT_LOCAL_COMPANY_NAME = "Local Company Name"

class GliaSdkConfigurationManagerTest {

    private val configurationManager: GliaSdkConfigurationManager = GliaSdkConfigurationManager()
    private val resourceProvider: ResourceProvider = mockk()
    private val stringProvider: StringProvider = mockk()

    @Before
    fun setup() {
        mockkStatic(Glia::class)
        mockkStatic(Dependencies::class)
        every { Dependencies.getResourceProvider() } returns resourceProvider
        every { Dependencies.getStringProvider() } returns stringProvider
        every { resourceProvider.getResourceKey(R.string.general_company_name)} returns "key"
        every { resourceProvider.getString(any()) } returns DEFAULT_LOCAL_COMPANY_NAME
        justRun { stringProvider.reportImproperInitialisation(any())}
    }

    @Test
    fun `getCompanyName returns local default when remote value is null`() {
        every { Glia.getRemoteString(any()) } returns null
        assertEquals(DEFAULT_LOCAL_COMPANY_NAME, configurationManager.companyName)
    }

    @Test
    fun `getCompanyName returns legacy value when remote value is empty`() {
        configurationManager.setLegacyCompanyName("Legacy name")
        every { configurationManager.fetchRemoteCompanyName() } returns null
        assertEquals("Legacy name", configurationManager.companyName)
    }

    @Test
    fun `getCompanyName returns local default when remote value is empty`() {
        every { Glia.getRemoteString(any()) } returns ""
        assertEquals(DEFAULT_LOCAL_COMPANY_NAME, configurationManager.companyName)
    }

    @Test
    fun `getCompanyName returns remote value when remote value is not empty`() {
        every { Glia.getRemoteString(any()) } returns "Remote company name"
        assertEquals("Remote company name", configurationManager.companyName)
    }

    @Test
    fun `getCompanyName should not be replaced when remote value is empty but local custom value is set using GliaWidgetsConfig setCompanyName`() {
        configurationManager.setLegacyCompanyName("Legacy name")
        configurationManager.companyName = "Init company name"
        every { Glia.getRemoteString(any()) } returns ""
        every { resourceProvider.getString(R.string.general_company_name) } returns DEFAULT_LOCAL_COMPANY_NAME
        assertEquals("Init company name", configurationManager.companyName)
    }
}
