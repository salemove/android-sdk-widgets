package com.glia.widgets.chat.adapter.holder;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.glia.androidsdk.chat.ChatMessage;
import com.glia.widgets.R;
import com.glia.widgets.chat.adapter.CustomCardMessage;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * The implementation of {@link CustomCardViewHolder} allows
 * displaying the card message as a WebView.
 * <p>
 * It can render HTML content if the {@link ChatMessage#getMetadata()}
 * has an HTML body with the <code>html</code> key.
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
 */
public class WebViewViewHolder extends CustomCardViewHolder {
    private static final String MIME_TYPE = "text/html";
    private static final String ENCODING = "UTF-8";
    private static final String METADATA_KEY = "html";
    private static final String JS_SCRIPT =
            "<script type=\"text/javascript\">" +
                    "function sendResponse(text, value){Glia.response(text, value);}" +
                    "function callMobileAction(action){Glia.action(action);}" +
            "</script>";

    private final WebView webView;

    @Nullable
    private ResponseCallback responseCallback;

    @Nullable
    private MobileActionCallback mobileActionCallback;

    @SuppressLint("SetJavaScriptEnabled")
    public WebViewViewHolder(@NonNull ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.web_view_layout, parent, false));

        webView = itemView.findViewById(R.id.web_view);
        WebSettings webViewSettings = webView.getSettings();
        webViewSettings.setJavaScriptEnabled(true);
        webViewSettings.setAllowFileAccess(false);
        webView.addJavascriptInterface(new JavaScriptInterface(), "Glia");
    }

    /**
     * Register a callback to be invoked when
     * the JS method {@code callMobileAction(String)} called inside WebView message.
     * @param mobileActionCallback the callback that will run
     */
    public void setMobileActionCallback(@Nullable MobileActionCallback mobileActionCallback) {
        this.mobileActionCallback = mobileActionCallback;
    }

    /**
     * @see CustomCardViewHolder#bind(CustomCardMessage, ResponseCallback)
     */
    @Override
    public void bind(@NonNull CustomCardMessage message, @NonNull ResponseCallback callback) {
        responseCallback = callback;

        JSONObject metadata = message.getMetadata();
        if (metadata != null) {
            try {
                String html = metadata.getString(METADATA_KEY);
                webView.loadDataWithBaseURL("", html + JS_SCRIPT, MIME_TYPE, ENCODING, "");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Allows checking if the message can be displayed using {@link WebViewViewHolder}.
     * @param message the chat message with metadata.
     * @return true if the message metadata has the <code>html</code> key.
     */
    public static boolean isWebViewType(@NonNull ChatMessage message) {
        JSONObject metadata = message.getMetadata();
        if (metadata == null || metadata.length() == 0) {
            return false;
        }
        return metadata.has(METADATA_KEY);
    }

    /**
     * Allows checking if the message can be displayed using {@link WebViewViewHolder}.
     * @param message the chat message with metadata.
     * @return true if the message metadata has the <code>html</code> key.
     */
    public static boolean isWebViewType(@NonNull CustomCardMessage message) {
        JSONObject metadata = message.getMetadata();
        if (metadata == null || metadata.length() == 0) {
            return false;
        }
        return metadata.has(METADATA_KEY);
    }

    private class JavaScriptInterface {

        @JavascriptInterface
        public void response(String text, String value) {
            if (responseCallback != null) {
                responseCallback.sendResponse(text, value);
            }
        }

        @JavascriptInterface
        public void action(String action) {
            if (mobileActionCallback != null) {
                mobileActionCallback.onMobileAction(action);
            }
        }
    }

    /**
     * Allows sending the action from a custom card.
     */
    public interface MobileActionCallback {
        /**
         * @param action the value of an action.
         */
        void onMobileAction(String action);
    }
}
