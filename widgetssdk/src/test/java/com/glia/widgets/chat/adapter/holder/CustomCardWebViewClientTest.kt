package com.glia.widgets.chat.adapter.holder

import android.content.Context
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import com.glia.widgets.helper.Logger
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CustomCardWebViewClientTest {

    private lateinit var context: Context
    private lateinit var webView: WebView
    private lateinit var onLinkTapped: (Context, Uri) -> Unit
    private lateinit var client: CustomCardWebViewClient

    @Before
    fun setUp() {
        Logger.setIsDebug(false)
        context = mockk(relaxed = true)
        webView = mockk(relaxed = true)
        every { webView.context } returns context
        onLinkTapped = mockk(relaxed = true)
        client = CustomCardWebViewClient(onLinkTapped)
    }

    @Test
    fun `about navigation stays inside the card in any frame`() {
        listOf(true, false).forEach { isForMainFrame ->
            val request = request("about", hasGesture = true, isForMainFrame = isForMainFrame)

            assertFalse("mainFrame=$isForMainFrame", client.shouldOverrideUrlLoading(webView, request))
        }
        verify(exactly = 0) { onLinkTapped(any(), any()) }
    }

    @Test
    fun `tapped main frame link with allowed scheme opens externally`() {
        listOf("https", "HTTPS", "http", "tel", "mailto").forEach { scheme ->
            val request = request(scheme, hasGesture = true)
            val uri = request.url

            assertTrue(scheme, client.shouldOverrideUrlLoading(webView, request))
            verify(exactly = 1) { onLinkTapped(context, uri) }
        }
    }

    @Test
    fun `tapped main frame link with other scheme is blocked`() {
        listOf("intent", "market", "myapp", "file", "data", "javascript").forEach { scheme ->
            val request = request(scheme, hasGesture = true)

            assertTrue(scheme, client.shouldOverrideUrlLoading(webView, request))
        }
        verify(exactly = 0) { onLinkTapped(any(), any()) }
    }

    @Test
    fun `tapped subframe link is blocked`() {
        listOf("https", "tel").forEach { scheme ->
            val request = request(scheme, hasGesture = true, isForMainFrame = false)

            assertTrue(scheme, client.shouldOverrideUrlLoading(webView, request))
        }
        verify(exactly = 0) { onLinkTapped(any(), any()) }
    }

    @Test
    fun `navigation without gesture is blocked`() {
        val request = request("https", hasGesture = false)

        assertTrue(client.shouldOverrideUrlLoading(webView, request))
        verify(exactly = 0) { onLinkTapped(any(), any()) }
    }

    @Test
    fun `tapped redirect is blocked`() {
        val request = request("https", hasGesture = true, isRedirect = true)

        assertTrue(client.shouldOverrideUrlLoading(webView, request))
        verify(exactly = 0) { onLinkTapped(any(), any()) }
    }

    @Test
    fun `navigation without scheme is blocked`() {
        val request = request(null, hasGesture = true)

        assertTrue(client.shouldOverrideUrlLoading(webView, request))
        verify(exactly = 0) { onLinkTapped(any(), any()) }
    }

    private fun request(
        scheme: String?,
        hasGesture: Boolean,
        isRedirect: Boolean = false,
        isForMainFrame: Boolean = true
    ): WebResourceRequest {
        val uri: Uri = mockk { every { this@mockk.scheme } returns scheme }
        return mockk {
            every { url } returns uri
            every { hasGesture() } returns hasGesture
            every { this@mockk.isRedirect } returns isRedirect
            every { this@mockk.isForMainFrame } returns isForMainFrame
        }
    }
}
