package com.glia.widgets.chat.adapter.holder

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.glia.androidsdk.chat.ChatMessage
import com.glia.widgets.R
import com.glia.widgets.chat.adapter.CustomCardMessage
import org.json.JSONException

/**
 * The implementation of [CustomCardViewHolder] allows displaying the card message as a WebView.
 * <p>
 * It can render HTML content if the [ChatMessage.metadata] has an HTML body with the `html` key.
 * <p>
 * <b>Metadata example:</b>
 * <pre>{@code
 * { "html": "
 *     <style>button {padding: 8px 16px;}p {color: green;}<\/style>
 *     <p>Lorem ipsum dolor sit amet<\/p>
 *     <p>
 *         <button
 *              type=\"button\"
 *              onClick=\"sendResponse('Cancel', 'response_cancel')\">
 *              Cancel
 *         <\/button>
 *         <button
 *              type=\"button\"
 *              onClick=\"sendResponse('OK', 'response_ok')\">
 *              OK
 *         <\/button>
 *     <\/p>
 * " }
 * }<pre/>
 * @see CustomCardViewHolder
</pre> */
class WebViewViewHolder @SuppressLint("SetJavaScriptEnabled") constructor(parent: ViewGroup) :
    CustomCardViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.web_view_layout, parent, false)
    ) {
    private val webView: WebView = itemView.findViewById(R.id.web_view)
    private var responseCallback: ResponseCallback? = null
    private var mobileActionCallback: MobileActionCallback? = null

    init {
        val webViewSettings = webView.settings
        webViewSettings.javaScriptEnabled = true
        webViewSettings.allowFileAccess = false
        webView.addJavascriptInterface(JavaScriptInterface(), "Glia")
    }

    /**
     * Register a callback to be invoked when
     * the JS method `callMobileAction(String)` called inside WebView message.
     * @param mobileActionCallback the callback that will run
     */
    fun setMobileActionCallback(mobileActionCallback: MobileActionCallback?) {
        this.mobileActionCallback = mobileActionCallback
    }

    /**
     * @see CustomCardViewHolder.bind
     */
    override fun bind(message: CustomCardMessage, callback: ResponseCallback) {
        responseCallback = callback

        val metadata = message.metadata
        if (metadata != null) {
            try {
                val html = metadata.getString(METADATA_KEY)
                webView.loadDataWithBaseURL("", html + JS_SCRIPT, MIME_TYPE, ENCODING, "")
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    private inner class JavaScriptInterface {
        @JavascriptInterface
        fun response(text: String, value: String) {
            responseCallback?.sendResponse(text, value)
        }

        @JavascriptInterface
        fun action(action: String) {
            mobileActionCallback?.onMobileAction(action)
        }
    }

    /**
     * Allows sending the action from a custom card.
     */
    fun interface MobileActionCallback {
        /**
         * @param action the value of an action.
         */
        fun onMobileAction(action: String)
    }

    /**
     * Companion object containing constants and utility methods for WebViewViewHolder.
     * Provides metadata keys, MIME type, encoding, and helper functions to determine
     * if a message can be displayed using a WebView.
     */
    companion object {
        private const val MIME_TYPE = "text/html"
        private const val ENCODING = "UTF-8"
        private const val METADATA_KEY = "html"
        private const val JS_SCRIPT = "<script type=\"text/javascript\">" +
            "function sendResponse(text, value){Glia.response(text, value);}" +
            "function callMobileAction(action){Glia.action(action);}" +
            "</script>"

        /**
         * Allows checking if the message can be displayed using [WebViewViewHolder].
         * @param message the chat message with metadata.
         * @return true if the message metadata has the `html` key.
         */
        fun isWebViewType(message: ChatMessage): Boolean {
            val metadata = message.metadata
            if (metadata == null || metadata.length() == 0) {
                return false
            }
            return metadata.has(METADATA_KEY)
        }

        /**
         * Allows checking if the message can be displayed using [WebViewViewHolder].
         * @param message the chat message with metadata.
         * @return true if the message metadata has the `html` key.
         */
        fun isWebViewType(message: CustomCardMessage): Boolean {
            val metadata = message.metadata
            if (metadata == null || metadata.length() == 0) {
                return false
            }
            return metadata.has(METADATA_KEY)
        }
    }
}
