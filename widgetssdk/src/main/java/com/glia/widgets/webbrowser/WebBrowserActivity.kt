package com.glia.widgets.webbrowser

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.glia.widgets.base.FadeTransitionActivity
import com.glia.widgets.databinding.WebBrowserAvtivityBinding
import com.glia.widgets.helper.ExtraKeys
import com.glia.widgets.helper.getParcelable
import com.glia.widgets.locale.LocaleString

/**
 * Glia internal class.
 *
 * It will be automatically added to the integrator's manifest file by the manifest merger during compilation.
 *
 * This activity is used to display simple web pages. For example, it can be used to display links to the
 * client's Terms and Conditions or Privacy Policy, which can be part of the engagement confirmation dialog.
 *
 * @deprecated Use [com.glia.widgets.HostActivity] with [com.glia.widgets.navigation.Destination.WebBrowser] instead.
 * This Activity is kept for backward compatibility and will be removed in a future version.
 */
@Deprecated(
    message = "Use HostActivity with Destination.WebBrowser instead",
    replaceWith = ReplaceWith(
        "HostActivity.start(context, Destination.WebBrowser(title, url))",
        "com.glia.widgets.HostActivity",
        "com.glia.widgets.navigation.Destination"
    )
)
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

        val title = intent.getParcelable<LocaleString>(ExtraKeys.WEB_BROWSER_TITLE)
        val url = intent.getStringExtra(ExtraKeys.WEB_BROWSER_URL).orEmpty()

        webBrowserView.onLinkClickListener = this
        webBrowserView.onFinishListener = this
        webBrowserView.setTitle(title)
        webBrowserView.load(url)
    }

    override fun onLinkClick(url: Uri) {
        startActivity(Intent(Intent.ACTION_VIEW, url))
    }
}
