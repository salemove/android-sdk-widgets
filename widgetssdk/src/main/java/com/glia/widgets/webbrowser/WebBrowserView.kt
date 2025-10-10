package com.glia.widgets.webbrowser

import android.content.Context
import android.content.res.TypedArray
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import androidx.core.content.withStyledAttributes
import com.glia.widgets.R
import com.glia.widgets.databinding.WebBrowserViewBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.SimpleWindowInsetsAndAnimationHandler
import com.glia.widgets.helper.Utils
import com.glia.widgets.helper.layoutInflater
import com.glia.widgets.locale.LocaleString
import com.glia.widgets.view.header.AppBarView
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.glia.widgets.view.unifiedui.theme.base.HeaderTheme
import com.google.android.material.theme.overlay.MaterialThemeOverlay

internal class WebBrowserView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : LinearLayout(
    MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, defStyleRes),
    attrs,
    defStyleAttr,
    defStyleRes
) {

    private val unifiedTheme: UnifiedTheme? by lazy { Dependencies.gliaThemeManager.theme }

    var onFinishListener: OnFinishListener? = null
    var onLinkClickListener: OnLinkClickListener? = null

    private var binding: WebBrowserViewBinding? = null

    private val appBar: AppBarView? get() = binding?.appBarView
    private val webView: WebView? get() = binding?.webView

    init {
        isSaveEnabled = true
        orientation = VERTICAL
        readTypedArray(attrs, defStyleAttr, defStyleRes)

        setupViewAppearance()
        SimpleWindowInsetsAndAnimationHandler(this, appBar)
    }

    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = R.attr.gliaChatStyle
    ) : this(context, attrs, defStyleAttr, R.style.Application_Glia_Chat)

    fun setTitle(title: LocaleString?) {
        appBar?.setTitle(title)
    }

    fun load(url: String) {
        webView?.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                if (!request.isRedirect) {
                    onLinkClickListener?.onLinkClick(request.url)
                    return true
                }
                return super.shouldOverrideUrlLoading(view, request)
            }
        }
        webView?.loadUrl(url)
    }

    private fun setupViewAppearance() {
        // This is done to avoid view appearance when a visitor is not authenticated.
        binding = WebBrowserViewBinding.inflate(layoutInflater, this)

        setupAppBarUnifiedTheme(unifiedTheme?.webBrowserTheme?.header)
        appBar?.hideBackButton()
        initCallbacks()

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            webView?.settings?.allowFileAccess = false
        }
    }

    private fun setupAppBarUnifiedTheme(headerTheme: HeaderTheme?) {
        appBar?.applyHeaderTheme(headerTheme)
    }

    private fun readTypedArray(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        context.withStyledAttributes(attrs, R.styleable.GliaView, defStyleAttr, defStyleRes) {
            setDefaultTheme(this)
        }
    }

    private fun setDefaultTheme(typedArray: TypedArray) {
        binding?.appBarView?.setTheme(Utils.getThemeFromTypedArray(typedArray, this.context))
    }

    private fun initCallbacks() {
        appBar?.setOnXClickedListener {
            onFinishListener?.finish()
        }
    }

    interface OnLinkClickListener {
        fun onLinkClick(url: Uri)
    }

    interface OnFinishListener {
        fun finish()
    }
}
