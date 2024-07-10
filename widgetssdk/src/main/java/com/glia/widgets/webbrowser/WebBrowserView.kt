package com.glia.widgets.webbrowser

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.net.Uri
import android.util.AttributeSet
import android.view.Window
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.ViewCompat
import com.glia.widgets.Constants
import com.glia.widgets.locale.LocaleString
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.databinding.WebBrowserViewBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Utils
import com.glia.widgets.helper.asActivity
import com.glia.widgets.helper.changeStatusBarColor
import com.glia.widgets.helper.getColorCompat
import com.glia.widgets.helper.layoutInflater
import com.glia.widgets.view.header.AppBarView
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.glia.widgets.view.unifiedui.theme.base.HeaderTheme
import com.google.android.material.theme.overlay.MaterialThemeOverlay
import kotlin.properties.Delegates

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

    private var theme: UiTheme by Delegates.notNull()
    private val unifiedTheme: UnifiedTheme? by lazy { Dependencies.getGliaThemeManager().theme }

    var onFinishListener: OnFinishListener? = null
    var onLinkClickListener: OnLinkClickListener? = null

    private var binding: WebBrowserViewBinding? = null

    private val appBar: AppBarView? get() = binding?.appBarView
    private val webView: WebView? get() = binding?.webView

    // Is needed for setting status bar color back when the view is gone
    private var defaultStatusBarColor: Int? = null
    private var statusBarColor: Int by Delegates.notNull()

    private val window: Window? by lazy { context.asActivity()?.window }

    init {
        isSaveEnabled = true
        orientation = VERTICAL
        defaultStatusBarColor = window?.statusBarColor
        // Is needed to overlap existing app bar in existing view with this view's app bar.
        ViewCompat.setElevation(this, Constants.WIDGETS_SDK_LAYER_ELEVATION)
        readTypedArray(attrs, defStyleAttr, defStyleRes)

        setupViewAppearance()
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

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        changeStatusBarColor(defaultStatusBarColor ?: return)
    }

    private fun setupViewAppearance() {
        // This is done to avoid view appearance when a visitor is not authenticated.
        binding = WebBrowserViewBinding.inflate(layoutInflater, this)

        setupAppBarUnifiedTheme(unifiedTheme?.webBrowserTheme?.header)
        appBar?.hideBackButton()
        initCallbacks()
    }

    private fun setupAppBarUnifiedTheme(headerTheme: HeaderTheme?) {
        appBar?.applyHeaderTheme(headerTheme)

        headerTheme?.background?.fill?.primaryColor?.also {
            statusBarColor = it
        }

        changeStatusBarColor(statusBarColor)
    }

    private fun readTypedArray(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        context.withStyledAttributes(attrs, R.styleable.GliaView, defStyleAttr, defStyleRes) {
            setDefaultTheme(this)
        }
    }

    private fun setDefaultTheme(typedArray: TypedArray) {
        theme = Utils.getThemeFromTypedArray(typedArray, this.context)
        statusBarColor = theme.brandPrimaryColor?.let(::getColorCompat) ?: Color.TRANSPARENT
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
