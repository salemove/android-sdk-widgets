package com.glia.widgets.chat.adapter.holder

import android.webkit.WebView
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import com.glia.widgets.R
import com.glia.widgets.chat.adapter.CustomCardMessage
import org.json.JSONObject
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class WebViewViewHolderTest {

    private lateinit var holder: WebViewViewHolder
    private lateinit var webView: WebView

    @Before
    fun setUp() {
        holder = WebViewViewHolder(FrameLayout(ApplicationProvider.getApplicationContext()))
        webView = holder.itemView.findViewById(R.id.web_view)
    }

    @Test
    fun `card WebView cannot access local files or content providers`() {
        assertFalse(webView.settings.allowFileAccess)
        assertFalse(webView.settings.allowContentAccess)
    }

    @Test
    fun `card WebView cannot open new windows`() {
        assertFalse(webView.settings.javaScriptCanOpenWindowsAutomatically)
        assertFalse(webView.settings.supportMultipleWindows())
    }

    @Test
    fun `card WebView uses the gesture-gated client`() {
        assertTrue(shadowOf(webView).webViewClient is CustomCardWebViewClient)
    }

    @Test
    fun `bind loads the content security policy ahead of the card HTML`() {
        val cardHtml = "<p>hi</p>"

        holder.bind(message(cardHtml)) { _, _ -> }

        val loaded = requireNotNull(shadowOf(webView).lastLoadDataWithBaseURL).data
        val cspIndex = loaded.indexOf("http-equiv=\"Content-Security-Policy\"")
        val cardIndex = loaded.indexOf(cardHtml)
        assertTrue(cspIndex >= 0)
        assertTrue(cardIndex > cspIndex)
    }

    private fun message(html: String): CustomCardMessage =
        CustomCardMessage(id = "id", metadata = JSONObject().put("html", html), selectedOption = null)
}
