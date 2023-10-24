package com.glia.widgets.core.configuration

import com.glia.widgets.R
import com.glia.widgets.helper.ResourceProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

private const val DEFAULT_LOCAL_COMPANY_NAME = "Local Company Name"

class GliaSdkConfigurationManagerTest {

    private val configurationManager: GliaSdkConfigurationManager = mock()
    private val resourceProvider: ResourceProvider = mock()
    @Test
    fun `getCompanyName returns local default when remote value is null`() {
        whenever(configurationManager.fetchRemoteCompanyName()).thenReturn(null)
        whenever(configurationManager.resourceProvider).thenReturn(resourceProvider)
        whenever(resourceProvider.getString(R.string.general_company_name)).thenReturn(DEFAULT_LOCAL_COMPANY_NAME)

        whenever(configurationManager.companyName).thenCallRealMethod()

        assertEquals(DEFAULT_LOCAL_COMPANY_NAME, configurationManager.companyName)
    }

    @Test
    fun `getCompanyName returns local default when remote value is empty`() {
        whenever(configurationManager.fetchRemoteCompanyName()).thenReturn("")
        whenever(configurationManager.resourceProvider).thenReturn(resourceProvider)
        whenever(resourceProvider.getString(R.string.general_company_name)).thenReturn(DEFAULT_LOCAL_COMPANY_NAME)

        whenever(configurationManager.companyName).thenCallRealMethod()

        assertEquals(DEFAULT_LOCAL_COMPANY_NAME, configurationManager.companyName)
    }

    @Test
    fun `getCompanyName returns remote value when remote value is not empty`() {
        whenever(configurationManager.fetchRemoteCompanyName()).thenReturn("Remote company name")
        whenever(configurationManager.resourceProvider).thenReturn(resourceProvider)
        whenever(resourceProvider.getString(R.string.general_company_name)).thenReturn(DEFAULT_LOCAL_COMPANY_NAME)

        whenever(configurationManager.companyName).thenCallRealMethod()

        assertEquals("Remote company name", configurationManager.companyName)
    }

    @Test
    fun `getCompanyName should not be replaced when remote value is empty but local custom value is set using GliaWidgetsConfig setCompanyName`() {
        whenever(configurationManager.fetchRemoteCompanyName()).thenReturn("")
        whenever(configurationManager.resourceProvider).thenReturn(resourceProvider)
        whenever(resourceProvider.getString(R.string.general_company_name)).thenReturn(DEFAULT_LOCAL_COMPANY_NAME)
        whenever(configurationManager.isCompanyNameSetFromWidgetsConfig).thenReturn(true)

        whenever(configurationManager.companyName).thenCallRealMethod()

        assertEquals(null, configurationManager.companyName)
    }
}
