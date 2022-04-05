package com.glia.widgets.view.head;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;

import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.core.configuration.GliaSdkConfiguration;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.helper.Utils;
import com.glia.widgets.view.ViewHelpers;
import com.google.android.material.theme.overlay.MaterialThemeOverlay;

public class ChatHeadLayout extends FrameLayout implements ChatHeadLayoutContract.View {
    private ChatHeadView chatHeadView;
    private ChatHeadLayoutContract.Controller controller;

    private NavigationCallback navigationCallback;
    private OnChatHeadClickedListener chatHeadClickedListener;

    private UiTheme uiTheme;

    private boolean isChatView = false;

    public ChatHeadLayout(@NonNull Context context) {
        this(context, null);
    }

    public ChatHeadLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.gliaChatStyle);
    }

    public ChatHeadLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Application_Glia_Chat);
    }

    public ChatHeadLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(
                MaterialThemeOverlay.wrap(
                        context,
                        attrs,
                        defStyleAttr,
                        defStyleRes),
                attrs,
                defStyleAttr,
                defStyleRes
        );
        init(attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void showOperatorImage(String operatorImgUrl) {
        chatHeadView.showOperatorImage(operatorImgUrl);
    }

    @Override
    public void showUnreadMessageCount(int count) {
        chatHeadView.showUnreadMessageCount(count);
    }

    @Override
    public void showPlaceholder() {
        chatHeadView.showPlaceholder();
    }

    @Override
    public void showQueueing() {
        chatHeadView.showQueueing();
    }

    @Override
    public void showOnHold() {
        chatHeadView.showOnHold();
    }

    @Override
    public void hideOnHold() {
        chatHeadView.hideOnHold();
    }

    @Override
    public void navigateToChat() {
        if (navigationCallback != null) {
            navigationCallback.onNavigateToChat();
        } else {
            chatHeadView.navigateToChat();
        }
    }

    @Override
    public void navigateToCall() {
        if (navigationCallback != null) {
            navigationCallback.onNavigateToCall();
        } else {
            chatHeadView.navigateToCall();
        }
    }

    @Override
    public boolean isInChatView() {
        return isChatView;
    }

    @Override
    public void show() {
        post(() -> setVisibility(VISIBLE));
    }

    @Override
    public void hide() {
        post(() -> setVisibility(GONE));
    }

    @Override
    public void setController(ChatHeadLayoutContract.Controller controller) {
        this.controller = controller;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        float floatingViewX = w - getChatHeadSize() - getChatHeadMargin();
        float floatingViewY = h / 10f * 8f;
        chatHeadView.setX(floatingViewX);
        chatHeadView.setY(floatingViewY);
        chatHeadView.invalidate();
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDetachedFromWindow() {
        controller.onDestroy();
        super.onDetachedFromWindow();
    }

    /**
     * Method for the integrator to override if they want to do custom logic when the chat head is
     * clicked.
     *
     * @param listener
     */
    public void setOnChatHeadClickedListener(OnChatHeadClickedListener listener) {
        this.chatHeadClickedListener = listener;
    }

    /**
     * Method that allows integrator to override navigation on click with using own paths
     * <p>
     * if set to null default navigation is restored
     *
     * @param callback
     */
    public void setNavigationCallback(NavigationCallback callback) {
        this.navigationCallback = callback;
    }

    public void setConfiguration(GliaSdkConfiguration configuration) {
        this.chatHeadView.updateConfiguration(uiTheme, configuration);
    }

    public void setIsChatView(boolean value) {
        this.isChatView = value;
    }

    private void init(@Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        setVisibility(GONE);
        initConfigurations();
        initViews();
        setupViewActions();
        readTypedArray(attrs, defStyleAttr, defStyleRes);
        setController(Dependencies.getControllerFactory().getChatHeadLayoutController());
        this.controller.setView(this);
    }

    private void initConfigurations() {
        setClickable(false);
        setFocusable(false);
        ViewCompat.setElevation(this, 100.0f);
    }

    private void initViews() {
        View view = View.inflate(getContext(), R.layout.chat_head_layout, this);
        chatHeadView = view.findViewById(R.id.chat_head_view);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupViewActions() {
        chatHeadView.setOnTouchListener(
                new ViewHelpers.ChatHeadOnTouchListener(
                        this::getChatHeadViewPosition,
                        this::onChatHeadDragged,
                        this::onChatHeadClicked
                )
        );
    }

    private void readTypedArray(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        @SuppressLint("CustomViewStyleable") TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.GliaView, defStyleAttr, defStyleRes);
        setBuildTimeTheme(Utils.getThemeFromTypedArray(typedArray, getContext()));
        typedArray.recycle();
    }

    private void setBuildTimeTheme(UiTheme theme) {
        this.uiTheme = theme;
        this.chatHeadView.updateConfiguration(uiTheme, null);
    }

    private void onChatHeadDragged(float x, float y) {
        chatHeadView.setX(x);
        chatHeadView.setY(y);
        chatHeadView.invalidate();
    }

    private Pair<Integer, Integer> getChatHeadViewPosition() {
        return new Pair<>(
                Float.valueOf(chatHeadView.getX()).intValue(),
                Float.valueOf(chatHeadView.getY()).intValue()
        );
    }

    private void onChatHeadClicked(View v) {
        if (chatHeadClickedListener != null) {
            chatHeadClickedListener.onClicked(null);
        } else {
            controller.onChatHeadClicked();
        }
    }

    private float getChatHeadSize() {
        return getResources().getDimension(R.dimen.glia_chat_head_size);
    }

    private float getChatHeadMargin() {
        return Utils.pxFromDp(this.getContext(), 16);
    }

    public interface OnChatHeadClickedListener {
        void onClicked(GliaSdkConfiguration chatHeadInput);
    }

    public interface NavigationCallback {
        void onNavigateToChat();

        void onNavigateToCall();
    }
}
