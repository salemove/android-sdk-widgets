package com.glia.widgets.chat.adapter.holder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.glia.widgets.chat.adapter.CustomCardMessage;

/**
 * CustomCardViewHolder describes the item view.
 * <p>
 * The CustomCardViewHolder implementation should have fields to cache
 * the potentially costly View.findViewById(int) results.
 * <p>
 * <b>Usage example:</b>
 * <pre>{@code
 * class ExampleViewHolder extends CustomCardViewHolder {
 *     private final TextView textView;
 *     private final Button button;
 *
 *     public ExampleViewHolder(@NonNull View itemView) {
 *         super(itemView);
 *         this.textView = itemView.findViewById(R.id.text_view);
 *         this.button = itemView.findViewById(R.id.button);
 *     }
 *
 *     @Override
 *     public void bind(@NonNull CustomCardMessage message, @NonNull ResponseCallback callback) {
 *         messageTextView.setText(message.getContent());
 *         try {
 *             String customField = metadata.getMetadata().getString("customField");
 *             textView.setText(customField);
 *         } catch (JSONException e) {
 *             e.printStackTrace();
 *         }
 *         button.setOnClickListener(view -> {
 *             callback.sendResponse("Response text", "response_value");
 *         });
 *     }
 * }
 * }<pre/>
 *
 * @see WebViewViewHolder
 */
public abstract class CustomCardViewHolder extends RecyclerView.ViewHolder {
    public CustomCardViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    /**
     * Called to display the data for the specified {@link CustomCardMessage}. This method should
     * update the content of {@link #itemView} to reflect the item for the given message.
     * @param message a chat message with metadata.
     * @param callback can be used to send the selected card option.
     */
    public abstract void bind(@NonNull CustomCardMessage message, @NonNull ResponseCallback callback);

    /**
     * Allows returning the selected card.
     */
    public interface ResponseCallback {
        /**
         * @param text the text displayed to the visitor as a choice label.
         * @param value specific indicator of the selected option sent to the bot.
         */
        void sendResponse(String text, String value);
    }
}
