package com.glia.exampleapp;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.glia.androidsdk.chat.ChatMessage;
import com.glia.androidsdk.chat.SingleChoiceAttachment;
import com.glia.widgets.UiTheme;
import com.glia.widgets.chat.adapter.CustomCardAdapter;
import com.glia.widgets.chat.adapter.holder.CustomCardViewHolder;
import com.glia.widgets.chat.adapter.holder.WebViewViewHolder;

import org.json.JSONException;
import org.json.JSONObject;

public class ExampleCustomCardAdapter extends CustomCardAdapter {
    private static final Integer WEB_VIEW_TYPE = 1;
    private static final Integer NATIVE_VIEW_TYPE = 2;
    private static final Integer SDK_DEFAULT_TYPE = null;


    @Override
    @Nullable
    public Integer getItemViewType(ChatMessage message) {
        if (WebViewViewHolder.isWebViewType(message)) {
            return WEB_VIEW_TYPE;
        } else if (NativeViewViewHolder.isNativeViewType(message)) {
            return NATIVE_VIEW_TYPE;
        } else {
            // Use default Widgets SDK message rendering as fallback
            return SDK_DEFAULT_TYPE;
        }
    }

    @Override
    @NonNull
    public CustomCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, @NonNull LayoutInflater inflater,
                                                   @NonNull UiTheme uiTheme, int viewType) {
        if (viewType == WEB_VIEW_TYPE) {
            WebViewViewHolder webViewViewHolder = new WebViewViewHolder(parent);
            webViewViewHolder.setMobileActionCallback(action ->
                    Toast.makeText(parent.getContext(), action, Toast.LENGTH_SHORT).show()
            );
            return webViewViewHolder;
        } else {
            View view = inflater.inflate(R.layout.native_view_item, parent, false);
            return new NativeViewViewHolder(view, uiTheme);
        }
    }

    @Override
    public boolean shouldShowCard(ChatMessage message, int viewType) {
        if (viewType == NATIVE_VIEW_TYPE) {
            return message.getMetadata()
                    .optBoolean(NativeViewViewHolder.SHOULD_SHOW_VIEW_KEY, false);
        }
        return super.shouldShowCard(message, viewType);
    }

    static class NativeViewViewHolder extends CustomCardViewHolder {
        static final String SHOW_BUTTON_KEY = "showButton";
        static final String SHOULD_SHOW_VIEW_KEY = "shouldShow";

        private final UiTheme uiTheme;
        private final Context context;
        private final View contentView;
        private final TextView messageTextView;
        private final TextView metadataTextView;
        private final Button okButton;

        public NativeViewViewHolder(@NonNull View itemView, @NonNull UiTheme uiTheme) {
            super(itemView);
            this.uiTheme = uiTheme;
            this.context = itemView.getContext();
            this.contentView = itemView.findViewById(R.id.content_view);
            this.messageTextView = itemView.findViewById(R.id.message);
            this.metadataTextView = itemView.findViewById(R.id.metadata);
            this.okButton = itemView.findViewById(R.id.ok_button);
        }

        public static boolean isNativeViewType(ChatMessage message) {
            JSONObject metadata = message == null ? null : message.getMetadata();
            if (metadata == null || metadata.length() == 0) {
                return false;
            } else {
                return metadata.has(NativeViewViewHolder.SHOW_BUTTON_KEY)
                  || metadata.has(NativeViewViewHolder.SHOULD_SHOW_VIEW_KEY);
            }
        }

        @Override
        public void bind(@NonNull ChatMessage message, @NonNull ResponseCallback callback) {
            messageTextView.setText(message.getContent());
            try {
                String metadata = message.getMetadata().toString(2);
                metadataTextView.setText(String.format("\"metadata\": %s", metadata));

                if (message.getMetadata().optBoolean(SHOW_BUTTON_KEY, false)) {
                    SingleChoiceAttachment singleChoiceAttachment = null;
                    if (message.getAttachment() != null && message.getAttachment() instanceof SingleChoiceAttachment) {
                        singleChoiceAttachment = (SingleChoiceAttachment) message.getAttachment();
                    }

                    if (singleChoiceAttachment != null && "ok_value".equals(singleChoiceAttachment.getSelectedOption())) {
                        okButton.setOnClickListener(null);
                        okButton.setSelected(true);
                        okButton.setClickable(false);
                    } else {
                        okButton.setOnClickListener(view -> {
                            callback.sendResponse("OK", "ok_value");
                        });
                        okButton.setSelected(false);
                        okButton.setClickable(true);
                    }
                    okButton.setVisibility(View.VISIBLE);
                } else {
                    okButton.setOnClickListener(null);
                    okButton.setVisibility(View.GONE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            applyTheme();
        }

        private void applyTheme() {
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

            ColorStateList actionButtonBackgroundColor =
                    okButton.isSelected() ?
                            ContextCompat.getColorStateList(context, uiTheme.getBotActionButtonSelectedBackgroundColor()) :
                            ContextCompat.getColorStateList(context, uiTheme.getBotActionButtonBackgroundColor());

            ColorStateList actionButtonTextColor =
                    okButton.isSelected() ?
                            ContextCompat.getColorStateList(context, uiTheme.getBotActionButtonSelectedTextColor()) :
                            ContextCompat.getColorStateList(context, uiTheme.getBotActionButtonTextColor());

            okButton.setBackgroundTintList(actionButtonBackgroundColor);
            okButton.setTextColor(actionButtonTextColor);
        }
    }
}
