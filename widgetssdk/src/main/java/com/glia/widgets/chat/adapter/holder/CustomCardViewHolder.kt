package com.glia.widgets.chat.adapter.holder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.glia.androidsdk.chat.ChatMessage
import com.glia.widgets.chat.adapter.CustomCardMessage

/**
 * CustomCardViewHolder describes the item view.
 *
 * The CustomCardViewHolder implementation should have fields to cache
 * the potentially costly View.findViewById(int) results.
 *
 *
 * <b>Usage example:</b>
 * <pre>{@code class ExampleViewHolder(itemView: View) : CustomCardViewHolder(itemView) {
 *         private val textView: TextView = itemView.findViewById(R.id.text_view)
 *         private val button: Button = itemView.findViewById(R.id.button)
 *
 *         override fun bind(message: CustomCardMessage, callback: ResponseCallback) {
 *             try {
 *                 val customField = message.metadata?.getString("customField");
 *                 textView.text = customField;
 *             } catch (e: JSONException) {
 *                 e.printStackTrace()
 *             }
 *             button.setOnClickListener{ view: View? ->
 *                 callback.sendResponse("OK", "ok_value")
 *             }
 *         }
 *     }
 * }</pre>
 *
 * @see WebViewViewHolder
 */
abstract class CustomCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    /**
     * Called to display the data for the specified [ChatMessage]. This method should
     * update the content of [itemView] to reflect the item for the given message.
     * Override this function for your own implementation of the message renderer.
     * @param message a chat message with metadata.
     * @param callback can be used to send the selected card option.
     */
    @Deprecated(
        "Use {@link #bind(CustomCardMessage, ResponseCallback)}", ReplaceWith(
            "bind(message: CustomCardMessage, callback: ResponseCallback)",
            "com.glia.widgets.chat.adapter.CustomCardMessage"
        )
    )
    open fun bind(message: ChatMessage, callback: ResponseCallback) {
        bind(CustomCardMessage(message), callback)
    }

    /**
     * Called to display the data for the specified [CustomCardMessage]. This method should
     * update the content of [itemView] to reflect the item for the given message.
     * Override this function for your own implementation of the message renderer.
     * @param message a chat message with metadata.
     * @param callback can be used to send the selected card option.
     */
    open fun bind(message: CustomCardMessage, callback: ResponseCallback) {}

    /**
     * Allows returning the selected card.
     */
    fun interface ResponseCallback {
        /**
         * @param text the text displayed to the visitor as a choice label.
         * @param value specific indicator of the selected option sent to the bot.
         */
        fun sendResponse(text: String, value: String)
    }
}
