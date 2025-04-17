package com.glia.exampleapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.glia.androidsdk.chat.ChatMessage
import com.glia.widgets.chat.adapter.CustomCardAdapter
import com.glia.widgets.chat.adapter.CustomCardMessage
import com.glia.widgets.chat.adapter.holder.CustomCardViewHolder
import com.glia.widgets.chat.adapter.holder.WebViewViewHolder
import org.json.JSONException

class DeprecatedExampleCustomCardAdapter : CustomCardAdapter() {
    override fun getItemViewType(message: ChatMessage): Int? {
        return if (WebViewViewHolder.isWebViewType(message)) {
            WEB_VIEW_TYPE
        } else if (NativeViewViewHolder.isNativeViewType(
                message
            )
        ) {
            NATIVE_VIEW_TYPE
        } else {
            // Use default Widgets SDK message rendering as fallback
            SDK_DEFAULT_TYPE
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        inflater: LayoutInflater,
        viewType: Int
    ): CustomCardViewHolder {
        if (viewType == WEB_VIEW_TYPE) {
            val webViewViewHolder = WebViewViewHolder(parent)
            webViewViewHolder.setMobileActionCallback { action: String? ->
                Toast.makeText(
                    parent.context,
                    action,
                    Toast.LENGTH_SHORT
                ).show()
            }
            return webViewViewHolder
        } else {
            val view = inflater.inflate(R.layout.native_view_item, parent, false)
            return NativeViewViewHolder(view)
        }
    }

    override fun shouldShowCard(message: ChatMessage, viewType: Int): Boolean {
        if (viewType == NATIVE_VIEW_TYPE) {
            return message.metadata?.optBoolean(NativeViewViewHolder.SHOULD_SHOW_VIEW_KEY, false) ?: false
        }
        return super.shouldShowCard(message, viewType)
    }

    internal class NativeViewViewHolder(itemView: View) : CustomCardViewHolder(itemView) {
        private val metadataTextView: TextView =
            itemView.findViewById(R.id.metadata)
        private val okButton: Button =
            itemView.findViewById(R.id.ok_button)

        override fun bind(message: CustomCardMessage, callback: ResponseCallback) {
            try {
                val metadata = message.metadata?.toString(2)
                metadataTextView.text = String.format("\"metadata\": %s", metadata)

                if (message.metadata?.optBoolean(SHOW_BUTTON_KEY, false) == true) {
                    if ("ok_value" == message.selectedOption) {
                        okButton.setOnClickListener(null)
                        okButton.isSelected = true
                        okButton.isClickable = false
                    } else {
                        okButton.setOnClickListener { _: View? ->
                            callback.sendResponse(
                                "OK",
                                "ok_value"
                            )
                        }
                        okButton.isSelected = false
                        okButton.isClickable = true
                    }
                    okButton.visibility = View.VISIBLE
                } else {
                    okButton.setOnClickListener(null)
                    okButton.visibility = View.GONE
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        companion object {
            const val SHOW_BUTTON_KEY: String = "showButton"
            const val SHOULD_SHOW_VIEW_KEY: String = "shouldShow"

            fun isNativeViewType(message: ChatMessage?): Boolean {
                val metadata = message?.metadata
                return if (metadata == null || metadata.length() == 0) {
                    false
                } else {
                    (metadata.has(SHOW_BUTTON_KEY) || metadata.has(SHOULD_SHOW_VIEW_KEY))
                }
            }
        }
    }

    companion object {
        private const val WEB_VIEW_TYPE = 1
        private const val NATIVE_VIEW_TYPE = 2
        private val SDK_DEFAULT_TYPE: Int? = null
    }
}
