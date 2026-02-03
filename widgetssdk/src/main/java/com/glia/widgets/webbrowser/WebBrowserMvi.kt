package com.glia.widgets.webbrowser

import android.net.Uri
import com.glia.widgets.base.UiEffect
import com.glia.widgets.base.UiIntent
import com.glia.widgets.base.UiState

/**
 * UI state for WebBrowser screen.
 */
internal data class WebBrowserUiState(
    val title: String? = null,
    val url: String? = null,
    val isLoaded: Boolean = false
) : UiState

/**
 * User intents for WebBrowser screen.
 */
internal sealed interface WebBrowserIntent : UiIntent {
    data class Initialize(val title: String?, val url: String) : WebBrowserIntent
    data class OnLinkClicked(val uri: Uri) : WebBrowserIntent
    data object Close : WebBrowserIntent
}

/**
 * One-time effects for WebBrowser screen.
 */
internal sealed interface WebBrowserEffect : UiEffect {
    data class OpenExternalLink(val uri: Uri) : WebBrowserEffect
    data object Finish : WebBrowserEffect
}