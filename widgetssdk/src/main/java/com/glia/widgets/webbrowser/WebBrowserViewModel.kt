package com.glia.widgets.webbrowser

import android.net.Uri
import com.glia.widgets.base.BaseViewModel

/**
 * ViewModel for WebBrowser screen.
 *
 * Handles:
 * - Initialization with title and URL
 * - Link click delegation to external browser
 * - Close navigation
 */
internal class WebBrowserViewModel : BaseViewModel<WebBrowserUiState, WebBrowserIntent, WebBrowserEffect>(
    WebBrowserUiState()
) {

    override suspend fun handleIntent(intent: WebBrowserIntent) {
        when (intent) {
            is WebBrowserIntent.Initialize -> handleInitialize(intent.title, intent.url)
            is WebBrowserIntent.OnLinkClicked -> handleLinkClicked(intent.uri)
            WebBrowserIntent.Close -> handleClose()
        }
    }

    private fun handleInitialize(title: String?, url: String) {
        // Only initialize once
        if (currentState.isLoaded) return

        updateState {
            copy(
                title = title,
                url = url,
                isLoaded = true
            )
        }
    }

    private suspend fun handleLinkClicked(uri: Uri) {
        emitEffect(WebBrowserEffect.OpenExternalLink(uri))
    }

    private suspend fun handleClose() {
        emitEffect(WebBrowserEffect.Finish)
    }
}