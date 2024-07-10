package com.glia.widgets.core.dialog.domain

import com.glia.widgets.R
import com.glia.widgets.locale.LocaleProvider
import com.glia.widgets.locale.LocaleString
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ConfirmationDialogLinksUseCaseTest {
    private lateinit var useCase: ConfirmationDialogLinksUseCase
    private lateinit var localeProvider: LocaleProvider
    private lateinit var mockLocale: LocaleString

    @Before
    fun setup() {
        mockLocale = mock()
        localeProvider = mock()
        useCase = ConfirmationDialogLinksUseCase(localeProvider)
    }

    @Test
    fun `invoke returns both links if both title and url pair are not empty`() {
        whenever(localeProvider.getString(R.string.engagement_confirm_link1_text)).thenReturn("Title1")
        whenever(localeProvider.getString(R.string.engagement_confirm_link1_url)).thenReturn("https://link1")
        whenever(localeProvider.getString(R.string.engagement_confirm_link2_text)).thenReturn("Title2")
        whenever(localeProvider.getString(R.string.engagement_confirm_link2_url)).thenReturn("https://link2")

        val links = useCase()
        assertNotNull(links.link1)
        assertEquals(LocaleString(R.string.engagement_confirm_link1_text), links.link1!!.title)
        assertEquals("https://link1", links.link1!!.url)
        assertEquals(LocaleString(R.string.engagement_confirm_link2_text), links.link2!!.title)
        assertEquals("https://link2", links.link2!!.url)
    }

    @Test
    fun `invoke returns first link if only first title and url pair is not empty`() {
        whenever(localeProvider.getString(R.string.engagement_confirm_link1_text)).thenReturn("Title1")
        whenever(localeProvider.getString(R.string.engagement_confirm_link1_url)).thenReturn("https://link1")
        whenever(localeProvider.getString(R.string.engagement_confirm_link2_text)).thenReturn("Title2")
        whenever(localeProvider.getString(R.string.engagement_confirm_link2_url)).thenReturn("")

        val links = useCase()
        assertNotNull(links.link1)
        assertEquals(LocaleString(R.string.engagement_confirm_link1_text), links.link1!!.title)
        assertEquals("https://link1", links.link1!!.url)
        assertNull(links.link2)
    }

    @Test
    fun `invoke returns second link if only second title and url pair is not empty`() {
        whenever(localeProvider.getString(R.string.engagement_confirm_link1_text)).thenReturn(null)
        whenever(localeProvider.getString(R.string.engagement_confirm_link1_url)).thenReturn("https://link1")
        whenever(localeProvider.getString(R.string.engagement_confirm_link2_text)).thenReturn("Title2")
        whenever(localeProvider.getString(R.string.engagement_confirm_link2_url)).thenReturn("https://link2")

        val links = useCase()
        assertNotNull(links.link2)
        assertEquals(LocaleString(R.string.engagement_confirm_link2_text), links.link2!!.title)
        assertEquals("https://link2", links.link2!!.url)
    }

    @Test
    fun `makeLink returns link if title and url are valid`() {
        val link = useCase.makeLink(
            title = mockLocale,
            url = "https://glia.com"
        )
        assertNotNull(link)
        assertEquals(mockLocale, link!!.title)
        assertEquals("https://glia.com", link.url)
    }

    @Test
    fun `makeLink returns null if url is empty`() {
        val link = useCase.makeLink(
            title = mockLocale,
            url = ""
        )
        assertNull(link)
    }
}
