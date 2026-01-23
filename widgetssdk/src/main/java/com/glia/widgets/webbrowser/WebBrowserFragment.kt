package com.glia.widgets.webbrowser

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.glia.widgets.base.GliaFragment
import com.glia.widgets.base.GliaFragmentContract
import com.glia.widgets.databinding.WebBrowserFragmentBinding
import com.glia.widgets.helper.FragmentArgumentKeys
import com.glia.widgets.helper.getParcelable
import com.glia.widgets.locale.LocaleString

/**
 * Fragment for displaying simple web pages.
 *
 * This fragment can be used to display links to the client's Terms and Conditions or Privacy Policy,
 * which can be part of the engagement confirmation dialog.
 *
 * This Fragment is hosted by [WebBrowserActivity] which handles Intent-based launches for backwards compatibility.
 *
 * @see WebBrowserActivity
 * @see WebBrowserView
 */
internal class WebBrowserFragment : GliaFragment(),
    WebBrowserView.OnFinishListener,
    WebBrowserView.OnLinkClickListener {

    private var _binding: WebBrowserFragmentBinding? = null
    private val binding get() = _binding!!

    private val webBrowserView: WebBrowserView
        get() = binding.webBrowserView

    private var host: GliaFragmentContract.Host? = null

    override val gliaView: View
        get() = webBrowserView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = WebBrowserFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        host = activity as? GliaFragmentContract.Host

        val title = arguments?.getParcelable<LocaleString>(FragmentArgumentKeys.WEB_BROWSER_TITLE)
        val url = arguments?.getString(FragmentArgumentKeys.WEB_BROWSER_URL).orEmpty()

        webBrowserView.onLinkClickListener = this
        webBrowserView.onFinishListener = this
        webBrowserView.setTitle(title)
        webBrowserView.load(url)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onDetach() {
        super.onDetach()
        host = null
    }

    override fun onLinkClick(url: Uri) {
        startActivity(Intent(Intent.ACTION_VIEW, url))
    }

    override fun finish() {
        host?.finish()
    }

    companion object {
        /**
         * Create a new instance of WebBrowserFragment with the given URL and title.
         *
         * @param url The URL to load
         * @param title The localized title
         * @return A new WebBrowserFragment instance
         */
        fun newInstance(url: String, title: LocaleString): WebBrowserFragment {
            return WebBrowserFragment().apply {
                arguments = Bundle().apply {
                    putString(FragmentArgumentKeys.WEB_BROWSER_URL, url)
                    putParcelable(FragmentArgumentKeys.WEB_BROWSER_TITLE, title)
                }
            }
        }
    }
}
