package com.glia.widgets

import com.glia.widgets.helper.stringValue
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
        assertEquals(Region.Beta, "beta".toRegion())
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
        assertEquals("beta", Region.Beta.stringValue)
        assertEquals("region: custom, host: dev.salemove.com", Region.Custom("dev.salemove.com").stringValue)
    }
}
