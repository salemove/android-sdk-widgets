package com.glia.widgets.core.dialog.domain

import com.glia.widgets.R
import com.glia.widgets.StringProvider
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ConfirmationDialogLinksUseCaseTest {
    private lateinit var useCase: ConfirmationDialogLinksUseCase
    private lateinit var stringProvider: StringProvider

    @Before
    fun setup() {
        stringProvider = mock()
        useCase = ConfirmationDialogLinksUseCase(stringProvider)
    }

    @Test
    fun `invoke returns both links if both title and url pair are not empty`() {
        whenever(stringProvider.getRemoteString(R.string.engagement_confirm_link1_text)).thenReturn("Title1")
        whenever(stringProvider.getRemoteString(R.string.engagement_confirm_link1_url)).thenReturn("https://link1")
        whenever(stringProvider.getRemoteString(R.string.engagement_confirm_link2_text)).thenReturn("Title2")
        whenever(stringProvider.getRemoteString(R.string.engagement_confirm_link2_url)).thenReturn("https://link2")

        val links = useCase()
        assertNotNull(links.link1)
        assertEquals("Title1", links.link1!!.title)
        assertEquals("https://link1", links.link1.url)
        assertEquals("Title2", links.link2!!.title)
        assertEquals("https://link2", links.link2.url)
    }

    @Test
    fun `invoke returns first link if only first title and url pair is not empty`() {
        whenever(stringProvider.getRemoteString(R.string.engagement_confirm_link1_text)).thenReturn("Title1")
        whenever(stringProvider.getRemoteString(R.string.engagement_confirm_link1_url)).thenReturn("https://link1")
        whenever(stringProvider.getRemoteString(R.string.engagement_confirm_link2_text)).thenReturn("Title2")
        whenever(stringProvider.getRemoteString(R.string.engagement_confirm_link2_url)).thenReturn("")

        val links = useCase()
        assertNotNull(links.link1)
        assertEquals("Title1", links.link1!!.title)
        assertEquals("https://link1", links.link1.url)
        assertNull(links.link2)
    }

    @Test
    fun `invoke returns second link if only second title and url pair is not empty`() {
        whenever(stringProvider.getRemoteString(R.string.engagement_confirm_link1_text)).thenReturn(null)
        whenever(stringProvider.getRemoteString(R.string.engagement_confirm_link1_url)).thenReturn("https://link1")
        whenever(stringProvider.getRemoteString(R.string.engagement_confirm_link2_text)).thenReturn("Title2")
        whenever(stringProvider.getRemoteString(R.string.engagement_confirm_link2_url)).thenReturn("https://link2")

        val links = useCase()
        assertNull(links.link1)
        assertNotNull(links.link2)
        assertEquals("Title2", links.link2!!.title)
        assertEquals("https://link2", links.link2.url)
    }

    @Test
    fun `makeLink returns link if title and url are empty`() {
        val link = useCase.makeLink(
            title = "Title",
            url = "https://glia.com"
        )
        assertNotNull(link)
        assertEquals("Title", link!!.title)
        assertEquals("https://glia.com", link.url)
    }

    @Test
    fun `makeLink returns null if title is empty`() {
        val link = useCase.makeLink(
            title = "",
            url = "https://glia.com"
        )
        assertNull(link)
    }

    @Test
    fun `makeLink returns null if url is empty`() {
        val link = useCase.makeLink(
            title = "Title",
            url = ""
        )
        assertNull(link)
    }

    @Test
    fun `makeLink returns null if title and url are empty`() {
        val link = useCase.makeLink(
            title = "",
            url = ""
        )
        assertNull(link)
    }
}
