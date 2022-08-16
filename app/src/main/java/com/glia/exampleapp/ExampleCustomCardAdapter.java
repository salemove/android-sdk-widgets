package com.glia.exampleapp;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.glia.androidsdk.chat.ChatMessage;
import com.glia.widgets.UiTheme;
import com.glia.widgets.chat.adapter.CustomCardAdapter;
import com.glia.widgets.chat.adapter.holder.CustomCardViewHolder;
import com.glia.widgets.chat.adapter.holder.WebViewViewHolder;

import org.json.JSONException;

public class ExampleCustomCardAdapter extends CustomCardAdapter {
    private static final int WEB_VIEW_TYPE = 1;
    private static final int NATIVE_VIEW_TYPE = 2;

    @Override
    @Nullable
    public Integer getItemViewType(ChatMessage message) {
        if (WebViewViewHolder.isWebViewType(message)) {
            return WEB_VIEW_TYPE;
        }
        return NATIVE_VIEW_TYPE;
    }

    @Override
    @NonNull
    public CustomCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, @NonNull LayoutInflater inflater,
                                                   @NonNull UiTheme uiTheme, int viewType) {
        if (viewType == WEB_VIEW_TYPE) {
            return new WebViewViewHolder(parent);
        } else {
            View view = inflater.inflate(R.layout.native_view_item, parent, false);
            return new NativeViewViewHolder(view, uiTheme);
        }
    }

    static class NativeViewViewHolder extends CustomCardViewHolder {
        private final UiTheme uiTheme;
        private final Context context;
        private final View contentView;
        private final TextView messageTextView;
        private final TextView metadataTextView;

        public NativeViewViewHolder(@NonNull View itemView, @NonNull UiTheme uiTheme) {
            super(itemView);
            this.uiTheme = uiTheme;
            this.context = itemView.getContext();
            this.contentView = itemView.findViewById(R.id.content_view);
            this.messageTextView = itemView.findViewById(R.id.message);
            this.metadataTextView = itemView.findViewById(R.id.metadata);
        }

        @Override
        public void bind(@NonNull ChatMessage message, @NonNull ResponseCallback callback) {
            messageTextView.setText(message.getContent());
            try {
                String metadata = message.getMetadata().toString(2);
                metadataTextView.setText(String.format("\"metadata\": %s", metadata));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ColorStateList operatorBgColor =
                    ContextCompat.getColorStateList(context, uiTheme.getOperatorMessageBackgroundColor());
            contentView.setBackgroundTintList(operatorBgColor);
            messageTextView.setTextColor(ContextCompat.getColor(context, uiTheme.getOperatorMessageTextColor()));
            metadataTextView.setTextColor(ContextCompat.getColor(context, uiTheme.getOperatorMessageTextColor()));
            messageTextView.setLinkTextColor(ContextCompat.getColor(context, uiTheme.getOperatorMessageTextColor()));
            if (uiTheme.getFontRes() != null) {
                Typeface fontFamily = ResourcesCompat.getFont(context, uiTheme.getFontRes());
                messageTextView.setTypeface(fontFamily);
                metadataTextView.setTypeface(fontFamily);
            }
        }
    }
}
