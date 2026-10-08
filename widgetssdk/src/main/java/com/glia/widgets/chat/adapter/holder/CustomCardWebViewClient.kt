package com.glia.widgets.chat.adapter.holder

import android.content.Context
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG

/**
 * Lets a custom card leave the app only on a visitor tap of an `http`, `https`, `tel` or `mailto` link.
 *
 * Neither the card's own document nor its iframes ever navigate. Script-initiated navigation,
 * redirects and every other scheme are ignored. `about:` navigation stays inside the card so
 * fragment links scroll and `srcdoc` iframes render.
 */
internal class CustomCardWebViewClient(
    private val onLinkTapped: (context: Context, uri: Uri) -> Unit
) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val scheme = request.url.scheme?.lowercase()

        // about:blank, about:srcdoc and fragment links stay inside the card, as on iOS.
        if (scheme == SCHEME_ABOUT) return false

        val opensExternally = request.isForMainFrame &&
            scheme in EXTERNAL_SCHEMES &&
            request.hasGesture() &&
            !request.isRedirect

        if (opensExternally) {
            onLinkTapped(view.context, request.url)
        } else {
            // Never log the URL: it is operator-controlled and may contain PII.
            Logger.w(
                TAG,
                "Blocked custom card navigation",
                mapOf(
                    "scheme" to (scheme ?: "none"),
                    "mainFrame" to request.isForMainFrame.toString(),
                    "gesture" to request.hasGesture().toString()
                )
            )
        }
        // Neither the card document nor its iframes navigate.
        return true
    }

    private companion object {
        const val SCHEME_ABOUT: String = "about"
        val EXTERNAL_SCHEMES: Set<String> = setOf("http", "https", "tel", "mailto")
    }
}
