package com.glia.widgets.webbrowser.domain

import com.glia.widgets.internal.dialog.model.Link
import com.glia.widgets.locale.LocaleProvider
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetUrlFromLinkUseCaseTest {
    private lateinit var localeProvider: LocaleProvider

    private lateinit var useCase: GetUrlFromLinkUseCase

    @Before
    fun setUp() {
        localeProvider = mockk()
        useCase = GetUrlFromLinkUseCaseImpl(localeProvider)
    }

    @Test
    fun `should return null when link url is blank`() {
        val link = Link(mockk(), mockk())

        every { localeProvider.getString(link.url) } returns ""

        assertNull(useCase(link))
    }

    @Test
    fun `should return url when link url is not blank`() {
        val link = Link(mockk(), mockk())

        every { localeProvider.getString(link.url) } returns "https://glia.com"

        assertEquals("https://glia.com", useCase(link))
    }
}
