package com.glia.widgets.webbrowser

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.glia.widgets.locale.LocaleString
import com.glia.widgets.base.FadeTransitionActivity
import com.glia.widgets.databinding.WebBrowserAvtivityBinding
import com.glia.widgets.helper.getParcelable

/**
 * Glia internal class.
 *
 * It will be automatically added to the integrator's manifest file by the manifest merger during compilation.
 *
 * This activity is used to display simple web pages. For example, it can be used to display links to the
 * client's Terms and Conditions or Privacy Policy, which can be part of the engagement confirmation dialog.
 */
internal class WebBrowserActivity :
    FadeTransitionActivity(),
    WebBrowserView.OnFinishListener,
    WebBrowserView.OnLinkClickListener {

    private val binding: WebBrowserAvtivityBinding by lazy {
        WebBrowserAvtivityBinding.inflate(
            layoutInflater
        )
    }
    private val webBrowserView get() = binding.webBrowserView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val title = intent.getParcelable<LocaleString>(TITLE_KEY)
        val url = intent.getStringExtra(URL_KEY).orEmpty()

        webBrowserView.onLinkClickListener = this
        webBrowserView.onFinishListener = this
        webBrowserView.setTitle(title)
        webBrowserView.load(url)
    }

    override fun onLinkClick(url: Uri) {
        startActivity(Intent(Intent.ACTION_VIEW, url))
    }

    companion object {
        private const val TITLE_KEY = "title"
        private const val URL_KEY = "url"

        fun intent(context: Context, title: LocaleString, url: String): Intent {
            return Intent(context, WebBrowserActivity::class.java)
                .putExtra(TITLE_KEY, title)
                .putExtra(URL_KEY, url)
        }
    }
}
