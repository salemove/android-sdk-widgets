package com.glia.widgets.chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.collection.SparseArrayCompat
import com.glia.androidsdk.chat.ChatMessage
import com.glia.widgets.chat.adapter.holder.CustomCardViewHolder
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG

/**
 * The base class of CustomCardAdapter.
 *
 * CustomCardAdapter provides a view for rendering chat messages with metadata.
 *
 * @see WebViewCardAdapter
 */
abstract class CustomCardAdapter {

    @JvmField
    @VisibleForTesting
    // Map of relations between custom card view types and adapter view types.
    // - key is custom card view type;
    // - value is recycler view adapter view type.
    val viewTypeMap: SparseArrayCompat<Int> = SparseArrayCompat()

    /**
     * Returns the view type of the chat message item.
     * Override this function for your own implementation of the message renderer.
     * <p>
     * Consider using a resource id to uniquely identify item view types.
     * <p>
     * <b>Usage example:</b>
     * <pre>{@code
     *     override fun getItemViewType(message: ChatMessage): Int? {
     *         if (message.metadata?.has("insurance") == true) {
     *             return INSURANCE_TYPE
     *         }
     *         return null
     *     }
     * }<pre/>
     * @param message a chat message with metadata.
     * @return an integer value that specifies the type of view needed to represent the
     * current message. Or `null` for the default Glia message implementation.
     */
    @Deprecated(
        "Use {@link #getItemViewType(CustomCardMessage message)}",
        ReplaceWith(
            "getItemViewType(message: CustomCardMessage)",
            "com.glia.widgets.chat.adapter.CustomCardMessage"
        )
    )
    open fun getItemViewType(message: ChatMessage): Int? {
        return null
    }

    /**
     * Returns the view type of the chat message item.
     * Override this function for your own implementation of the message renderer.
     * <p>
     * Consider using a resource id to uniquely identify item view types.
     * <p>
     * <b>Usage example:</b>
     * <pre>{@code
     *     override fun getItemViewType(message: CustomCardMessage): Int? {
     *         if (message.metadata?.has("insurance") == true) {
     *             return INSURANCE_TYPE
     *         }
     *         return null
     *     }
     * }<pre/>
     * @param message a chat message with metadata.
     * @return an integer value that specifies the type of view needed to represent the
     * current message. Or `null` for the default Glia message implementation.
     */
    open fun getItemViewType(message: CustomCardMessage): Int? {
        return null
    }

    /**
     * Called when chat needs a new [CustomCardViewHolder] of the given type to represent an item.
     * Implement this function for your own implementation of the message renderer.
     * <p>
     * This [CustomCardViewHolder] must be constructed with a new view that can represent the
     * items of the given type. You can either create a new view manually or inflate it from an
     * XML layout file.
     * <p>
     * The new ViewHolder will be used to display items of the adapter using [CustomCardViewHolder.bind].
     * <p>
     * <b>Usage example:</b>
     * <pre>{@code
     *         override fun onCreateViewHolder(
     *         parent: ViewGroup,
     *         inflater: LayoutInflater,
     *         viewType: Int
     *     ): CustomCardViewHolder {
     *         return if (viewType == INSURANCE_TYPE) {
     *             InsuranceViewHolder(parent)
     *         } else {
     *             WebViewViewHolder(parent)
     *         }
     * }<pre/>
     * @param parent the ViewGroup to which the new view will be added after it has been bound to
     * the adapter position.
     * @param inflater an instance of LayoutInflater. It can be used to inflate an XML layout file.
     * @param viewType the view type of the new view.
     * The type is provided by [.getItemViewType].
     * @return a new [CustomCardViewHolder] that holds a view of the given view type.
     * @see getItemViewType()
     * @see CustomCardViewHolder.bind
     */
    abstract fun onCreateViewHolder(
        parent: ViewGroup,
        inflater: LayoutInflater,
        viewType: Int
    ): CustomCardViewHolder

    /**
     * Whether the custom card view should be shown when the card is interactable
     * and an option is selected
     *
     * @param message a chat message with metadata.
     * @param viewType the view type of the new view.
     * The type is provided by [getItemViewType].
     * @return a boolean indicating if the custom card view should be shown when
     * the card is interactable and an option is selected.
     * The default implementation returns `false`.
     */
    @Deprecated(
        "Use {@link #shouldShowCard(CustomCardMessage message, int viewType)}", ReplaceWith(
            "shouldShowCard(message: CustomCardMessage, viewType: Int)",
            "com.glia.widgets.chat.adapter.CustomCardMessage"
        )
    )
    open fun shouldShowCard(message: ChatMessage, viewType: Int): Boolean {
        Logger.logDeprecatedMethodUse(TAG, "shouldShowCard(ChatMessage)")
        return shouldShowCard(CustomCardMessage(message), viewType)
    }

    /**
     * Whether the custom card view should be shown when the card is interactable
     * and an option is selected
     *
     * @param message a chat message with metadata.
     * @param viewType the view type of the new view.
     * The type is provided by [.getItemViewType].
     * @return a boolean indicating if the custom card view should be shown when
     * the card is interactable and an option is selected.
     * The default implementation returns `false`.
     */
    open fun shouldShowCard(message: CustomCardMessage, viewType: Int): Boolean {
        return false
    }

    fun getChatAdapterViewType(message: ChatMessage): Int? {
        if (getItemViewType(CustomCardMessage(message)) == null && getItemViewType(message) != null) {
            Logger.logDeprecatedMethodUse(TAG, "getItemViewType(ChatMessage)")
        }
        val customCardViewType = getItemViewType(CustomCardMessage(message)) ?: getItemViewType(message)
        if (customCardViewType != null) {
            var chatAdapterViewType = viewTypeMap[customCardViewType]
            if (chatAdapterViewType == null) {
                // A new adapter type is generated using the CUSTOM_CARD_TYPE and the size of
                // the map. The CUSTOM_CARD_TYPE should be the last type and have the highest value.
                // This ensures that the value of the new type for the Recycler View Adapter is unique.
                chatAdapterViewType = ChatAdapter.CUSTOM_CARD_TYPE + viewTypeMap.size()
                viewTypeMap.put(customCardViewType, chatAdapterViewType)
            }
            return chatAdapterViewType
        }
        return null
    }

    fun getCustomCardViewType(chatAdapterViewType: Int): Int? {
        val index = viewTypeMap.indexOfValue(chatAdapterViewType)
        if (index >= 0) {
            return viewTypeMap.keyAt(index)
        }
        return null
    }

    fun getCustomCardViewHolder(
        parent: ViewGroup,
        inflater: LayoutInflater,
        chatAdapterViewType: Int
    ): CustomCardViewHolder? {
        val customCardViewTypeMap = getCustomCardViewType(chatAdapterViewType)
        if (customCardViewTypeMap != null) {
            return onCreateViewHolder(parent, inflater, customCardViewTypeMap)
        }
        return null
    }
}
