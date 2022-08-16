package com.glia.widgets.chat.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.glia.androidsdk.chat.ChatMessage;
import com.glia.widgets.GliaWidgets;
import com.glia.widgets.UiTheme;
import com.glia.widgets.chat.adapter.holder.CustomCardViewHolder;
import com.glia.widgets.chat.adapter.holder.WebViewViewHolder;

/**
 * The implementation of {@link CustomCardAdapter} allows displaying all card messages in WebViews.
 * <p>
 * It provides a simple way to set up {@link GliaWidgets#setCustomCardAdapter(CustomCardAdapter)}.
 * <p>
 * <b>Usage example:</b>
 * <pre>{@code
 * GliaWidgets.setCustomCardAdapter(new WebViewCardAdapter());
 * }<pre/>
 * @see CustomCardAdapter
 * @see WebViewViewHolder
 */
public class WebViewCardAdapter extends CustomCardAdapter {
    private static final int WEB_VIEW_TYPE = 0;

    /**
     * @see CustomCardAdapter#getItemViewType(ChatMessage)
     */
    @Nullable
    @Override
    public Integer getItemViewType(ChatMessage message) {
        if (WebViewViewHolder.isWebViewType(message)) {
            return WEB_VIEW_TYPE;
        }
        return null;
    }

    /**
     * @see CustomCardAdapter#onCreateViewHolder(ViewGroup, LayoutInflater, UiTheme, int)
     */
    @NonNull
    @Override
    public CustomCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                   @NonNull LayoutInflater inflater,
                                                   @NonNull UiTheme uiTheme,
                                                   int viewType) {
        return new WebViewViewHolder(parent);
    }
}
