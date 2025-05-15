package com.glia.widgets.chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.glia.widgets.chat.adapter.holder.CustomCardViewHolder
import com.glia.widgets.chat.adapter.holder.WebViewViewHolder
import com.glia.widgets.chat.adapter.holder.WebViewViewHolder.MobileActionCallback

/**
 * The implementation of [CustomCardAdapter] allows displaying all card messages in WebViews.
 * @see CustomCardAdapter
 *
 * @see WebViewViewHolder
 */
class WebViewCardAdapter : CustomCardAdapter() {
    private var mobileActionCallback: MobileActionCallback? = null

    /**
     * Register a callback to be invoked when
     * the JS method `callMobileAction(String)` called inside WebView message.
     * @param mobileActionCallback the callback that will run
     */
    fun setMobileActionCallback(mobileActionCallback: MobileActionCallback?) {
        this.mobileActionCallback = mobileActionCallback
    }

    /**
     * @see CustomCardAdapter.getItemViewType
     */
    override fun getItemViewType(message: CustomCardMessage): Int? {
        if (WebViewViewHolder.isWebViewType(message)) {
            return WEB_VIEW_TYPE
        }
        return null
    }

    /**
     * @see CustomCardAdapter.onCreateViewHolder
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        inflater: LayoutInflater,
        viewType: Int
    ): CustomCardViewHolder {
        val webViewViewHolder = WebViewViewHolder(parent)
        webViewViewHolder.setMobileActionCallback(mobileActionCallback)
        return webViewViewHolder
    }

    /**
     * Companion object containing constants for the [WebViewCardAdapter].
     * Provides the view type for the WebView card.
     */
    companion object {

        /**
         * The view type for the WebView card.
         */
        const val WEB_VIEW_TYPE = 0
    }
}
