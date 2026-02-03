package com.glia.widgets.webbrowser

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.glia.widgets.HostActivity
import com.glia.widgets.R
import com.glia.widgets.base.BaseFragment
import com.glia.widgets.di.Dependencies

/**
 * Fragment hosting WebBrowserView with MVI architecture.
 *
 * Displays a web page with a custom header and handles:
 * - Loading the initial URL
 * - Intercepting link clicks and opening them in external browser
 * - Close button navigation
 */
internal class WebBrowserFragment :
    BaseFragment<WebBrowserUiState, WebBrowserEffect, WebBrowserViewModel>(R.layout.fragment_web_browser) {

    internal companion object {
        internal const val ARG_TITLE = "arg_title"
        internal const val ARG_URL = "arg_url"
    }

    private var webBrowserView: WebBrowserView? = null
    override val viewModel: WebBrowserViewModel by viewModels { Dependencies.viewModelFactory }

    private var hasInitialized: Boolean = false
    private var hasLoadedUrl: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        webBrowserView = view.findViewById(R.id.web_browser_view)

        // Initialize from arguments before base class calls setupViews/handleState/handleEffect
        if (!hasInitialized) {
            arguments?.let { args ->
                val title: String? = args.getString(ARG_TITLE)
                val url: String = args.getString(ARG_URL, "")
                viewModel.processIntent(WebBrowserIntent.Initialize(title, url))
            }
            hasInitialized = true
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun setupViews() {
        webBrowserView?.apply {
            onLinkClickListener = object : WebBrowserView.OnLinkClickListener {
                override fun onLinkClick(url: Uri) {
                    viewModel.processIntent(WebBrowserIntent.OnLinkClicked(url))
                }
            }
            onFinishListener = object : WebBrowserView.OnFinishListener {
                override fun finish() {
                    viewModel.processIntent(WebBrowserIntent.Close)
                }
            }
        }
    }

    override fun handleState(state: WebBrowserUiState) {
        webBrowserView?.apply {
            if (state.title != null) {
                setTitleText(state.title)
            }
            // Only load URL once
            if (state.url != null && state.isLoaded && !hasLoadedUrl) {
                hasLoadedUrl = true
                load(state.url)
            }
        }
    }

    override fun handleEffect(effect: WebBrowserEffect) {
        when (effect) {
            is WebBrowserEffect.OpenExternalLink -> {
                startActivity(Intent(Intent.ACTION_VIEW, effect.uri))
            }
            WebBrowserEffect.Finish -> {
                // Pop back stack if we're in the back stack, otherwise finish activity
                parentFragmentManager.popBackStack()
                (activity as? HostActivity)?.finishIfEmpty()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webBrowserView = null
    }
}