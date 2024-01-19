package com.glia.widgets.webbrowser

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.glia.widgets.base.FadeTransitionActivity
import com.glia.widgets.databinding.WebBrowserAvtivityBinding


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

        val title = intent.getStringExtra(TITLE_KEY).orEmpty()
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

        fun intent(context: Context, title: String, url: String): Intent {
            return Intent(context, WebBrowserActivity::class.java)
                .putExtra(TITLE_KEY, title)
                .putExtra(URL_KEY, url)
        }
    }
}
