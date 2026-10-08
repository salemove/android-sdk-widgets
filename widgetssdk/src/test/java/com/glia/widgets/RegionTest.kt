package com.glia.widgets

import com.glia.widgets.helper.stringValue
import com.glia.widgets.helper.toCoreType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RegionTest {
    @Test
    fun `toRegion maps the known region strings`() {
        assertEquals(Region.US, "us".toRegion())
        assertEquals(Region.EU, "eu".toRegion())
        assertEquals(BetaRegion, "beta".toRegion())
    }

    @Test
    fun `toRegion is case insensitive`() {
        assertEquals(Region.US, "US".toRegion())
        assertEquals(Region.EU, "Eu".toRegion())
    }

    @Test
    fun `toRegion treats an unknown value as a custom region`() {
        assertEquals(Region.Custom("acceptance.salemove.com"), "acceptance.salemove.com".toRegion())
    }

    @Test
    fun `Custom strips the scheme from a full url`() {
        assertEquals("acceptance.salemove.com", Region.Custom("https://acceptance.salemove.com").host)
    }

    @Test
    fun `Custom keeps a bare domain as the host`() {
        assertEquals("acceptance.salemove.com", Region.Custom("acceptance.salemove.com").host)
    }

    @Test
    fun `Custom equality is based on the host`() {
        assertEquals(Region.Custom("https://acceptance.salemove.com"), Region.Custom("acceptance.salemove.com"))
        assertNotEquals(Region.Custom("acceptance.salemove.com"), Region.Custom("dev.salemove.com"))
    }

    @Test
    fun `stringValue renders every region`() {
        assertEquals("us", Region.US.stringValue)
        assertEquals("eu", Region.EU.stringValue)
        assertEquals("beta", BetaRegion.stringValue)
        assertEquals("region: custom, host: dev.salemove.com", Region.Custom("dev.salemove.com").stringValue)
    }

    @Test
    fun `beta maps to the core beta region rather than a custom one`() {
        assertEquals(com.glia.androidsdk.Region.Beta, "beta".toRegion().toCoreType())
    }
}
