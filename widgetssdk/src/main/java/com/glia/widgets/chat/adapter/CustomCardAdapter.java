package com.glia.widgets.chat.adapter;

import static com.glia.widgets.chat.adapter.ChatAdapter.CUSTOM_CARD_TYPE;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.collection.SparseArrayCompat;

import com.glia.androidsdk.chat.ChatMessage;
import com.glia.widgets.UiTheme;
import com.glia.widgets.chat.adapter.holder.CustomCardViewHolder;

/**
 * The base class of CustomCardAdapter.
 * <p>
 * CustomCardAdapter provides a view for rendering chat messages with metadata.
 *
 * @see WebViewCardAdapter
 */
public abstract class CustomCardAdapter {

    @VisibleForTesting
    // Map of relations between custom card view types and adapter view types.
    // - key is custom card view type;
    // - value is recycler view adapter view type.
    final SparseArrayCompat<Integer> viewTypeMap = new SparseArrayCompat<>();

    /**
     * Returns the view type of the chat message item.
     * <p>
     * Consider using a resource id to uniquely identify item view types.
     * <p>
     * <b>Usage example:</b>
     * <pre>{@code
     *     @Nullable
     *     @Override
     *     public Integer getItemViewType(ChatMessage message) {
     *         if (message.getMetadata().has("insurance")) {
     *             return INSURANCE_TYPE;
     *         }
     *         return null;
     *     }
     * }<pre/>
     * @param message a chat message with metadata.
     * @return an integer value that specifies the type of view needed to represent the
     *         current message. Or {@code null} for the default Glia message implementation.
     */
    @Nullable
    abstract public Integer getItemViewType(ChatMessage message);

    /**
     * Called when chat needs a new {@link CustomCardViewHolder}
     * of the given type to represent an item.
     * <p>
     * This {@link CustomCardViewHolder} must be constructed with a new view that can represent the
     * items of the given type. You can either create a new view manually or inflate it from an
     * XML layout file.
     * <p>
     * The new ViewHolder will be used to display items of the adapter using
     * {@link CustomCardViewHolder#bind(ChatMessage, CustomCardViewHolder.ResponseCallback)}.
     * <p>
     * <b>Usage example:</b>
     * <pre>{@code
     *     @NonNull
     *     @Override
     *     public CustomCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
     *                                                    @NonNull LayoutInflater inflater,
     *                                                    @NonNull UiTheme uiTheme,
     *                                                    int viewType) {
     *         if (viewType == INSURANCE_TYPE) {
     *             return new InsuranceViewHolder(parent);
     *         } else {
     *             return new WebViewViewHolder(parent);
     *         }
     *     }
     * }<pre/>
     * @param parent the ViewGroup to which the new view will be added after it has been bound to
     *               the adapter position.
     * @param inflater an instance of LayoutInflater. It can be used to inflate an XML layout file.
     * @param uiTheme contains the current theme attributes.
     * @param viewType the view type of the new view.
     *                 The type is provided by {@link #getItemViewType(ChatMessage)}.
     * @return a new {@link CustomCardViewHolder} that holds a view of the given view type.
     * @see #getItemViewType(ChatMessage)
     * @see CustomCardViewHolder#bind(ChatMessage, CustomCardViewHolder.ResponseCallback)
     */
    @NonNull
    abstract public CustomCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                            @NonNull LayoutInflater inflater,
                                                            @NonNull UiTheme uiTheme,
                                                            int viewType);

    /**
     *
     * @param message a chat message with metadata.
     * @param viewType the view type of the new view.
     *                 The type is provided by {@link #getItemViewType(ChatMessage)}.
     * @return a boolean indicating if the custom card view should be shown when
     *         the card is interactable and an option is selected.
     *         The default implementation returns {@code false}.
     */
    public boolean shouldShowCard(ChatMessage message, int viewType) {
        return false;
    }

    @Nullable
    public final Integer getChatAdapterViewType(ChatMessage chatMessage) {
        Integer customCardViewType = getItemViewType(chatMessage);
        if (customCardViewType != null) {
            Integer chatAdapterViewType = viewTypeMap.get(customCardViewType);
            if (chatAdapterViewType == null) {
                // A new adapter type is generated using the CUSTOM_CARD_TYPE and the size of
                // the map. The CUSTOM_CARD_TYPE should be the last type and have the highest value.
                // This ensures that the value of the new type for the Recycler View Adapter is unique.
                chatAdapterViewType = CUSTOM_CARD_TYPE + viewTypeMap.size();
                viewTypeMap.put(customCardViewType, chatAdapterViewType);
            }
            return chatAdapterViewType;
        }
        return null;
    }

    @Nullable
    public final Integer getCustomCardViewType(int chatAdapterViewType) {
        int index = viewTypeMap.indexOfValue(chatAdapterViewType);
        if (index >= 0) {
            return viewTypeMap.keyAt(index);
        }
        return null;
    }

    @Nullable
    final CustomCardViewHolder getCustomCardViewHolder(@NonNull ViewGroup parent,
                                                       @NonNull LayoutInflater inflater,
                                                       @NonNull UiTheme uiTheme,
                                                       int chatAdapterViewType) {
        Integer customCardViewTypeMap = getCustomCardViewType(chatAdapterViewType);
        if (customCardViewTypeMap != null) {
            return onCreateViewHolder(parent, inflater, uiTheme, customCardViewTypeMap);
        }
        return null;
    }
}
