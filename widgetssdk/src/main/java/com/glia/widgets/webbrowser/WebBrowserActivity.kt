package com.glia.widgets.webbrowser

import android.os.Bundle
import com.glia.widgets.R
import com.glia.widgets.base.FadeTransitionActivity
import com.glia.widgets.base.GliaFragmentContract
import com.glia.widgets.helper.ExtraKeys
import com.glia.widgets.helper.getParcelable
import com.glia.widgets.locale.LocaleString

/**
 * This activity hosts [WebBrowserFragment] and serves as an entry point for web browsing.
 *
 * **Architecture:** This Activity is a thin wrapper that hosts the Fragment. All UI logic
 * is implemented in [WebBrowserFragment] and [WebBrowserView]. This Activity handles Intent-based
 * launches for backwards compatibility.
 *
 * This activity is used to display simple web pages. For example, it can be used to display links to the
 * client's Terms and Conditions or Privacy Policy, which can be part of the engagement confirmation dialog.
 *
 * It will be automatically added to the integrator's manifest file by the manifest merger during compilation.
 *
 * @see WebBrowserFragment
 * @see WebBrowserView
 */
internal class WebBrowserActivity : FadeTransitionActivity(), GliaFragmentContract.Host {
    private var webBrowserFragment: WebBrowserFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.web_browser_activity_host)

        if (savedInstanceState == null) {
            val title = intent.getParcelable<LocaleString>(ExtraKeys.WEB_BROWSER_TITLE)
                ?: error("Title must be provided")
            val url = intent.getStringExtra(ExtraKeys.WEB_BROWSER_URL)
                ?: error("URL must be provided")

            webBrowserFragment = WebBrowserFragment.newInstance(url, title)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, webBrowserFragment!!)
                .commit()
        } else {
            webBrowserFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as? WebBrowserFragment
        }
    }

    override fun setHostTitle(locale: LocaleString?) {
        setTitle(locale)
    }

    override fun finish() = super.finish()
}
