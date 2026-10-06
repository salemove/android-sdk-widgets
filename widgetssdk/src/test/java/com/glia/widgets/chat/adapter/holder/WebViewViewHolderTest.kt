package com.glia.widgets.chat.adapter.holder

import android.webkit.WebView
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import com.glia.widgets.R
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class WebViewViewHolderTest {

    private lateinit var webView: WebView

    @Before
    fun setUp() {
        val holder = WebViewViewHolder(FrameLayout(ApplicationProvider.getApplicationContext()))
        webView = holder.itemView.findViewById(R.id.web_view)
    }

    @Test
    fun `card WebView uses the gesture-gated client`() {
        assertTrue(shadowOf(webView).webViewClient is CustomCardWebViewClient)
    }
}
