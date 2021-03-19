package com.glia.widgets.head;

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

import com.glia.widgets.GliaWidgets;
import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.helper.Utils;
import com.glia.widgets.model.ChatHeadInput;
import com.glia.widgets.view.ViewHelpers;
import com.google.android.material.theme.overlay.MaterialThemeOverlay;

public class ChatHeadLayout extends FrameLayout {

    private ChatHeadView chatHeadView;
    private String returnDestination;
    private final ChatHeadsController chatHeadsController;
    private final ChatHeadsController.OnChatheadSettingsChangedListener chatHeadListener =
            new ChatHeadsController.OnChatheadSettingsChangedListener() {

                @Override
                public void emitState(ChatHeadState chatHeadState) {
                    post(() -> {
                        chatHeadView.setMessageBadgeCount(chatHeadState.messageCount);
                        chatHeadView.updateImage(chatHeadState.operatorProfileImgUrl);
                        if (chatHeadState.theme != null) {
                            chatHeadView.setTheme(chatHeadState.theme);
                        }
                        chatHeadView.setVisibility(
                                chatHeadState.areIntegratedViewsVisible ? VISIBLE : GONE
                        );
                        returnDestination = chatHeadState.returnDestination;
                    });
                }
            };

    private OnChatHeadClickedListener onChatHeadClickedListener = chatHeadInput ->
            Utils.getActivity(getContext()).startActivity(
                    Utils.getReturnToEngagementIntent(
                            getContext(),
                            chatHeadInput,
                            returnDestination
                    )
            );

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
        initConfigurations();
        initViews();
        readTypedArray(attrs, defStyleAttr, defStyleRes);
        setupViewActions();
        chatHeadsController = GliaWidgets.getControllerFactory().getChatHeadsController();
        chatHeadsController.addListener(chatHeadListener);
    }

    /**
     * Method for the integrator to overriide if they want to do custom logic when the chat head is
     * clicked.
     *
     * @param listener
     */
    public void setOnChatHeadClickedListener(OnChatHeadClickedListener listener) {
        this.onChatHeadClickedListener = listener;
    }

    private void initConfigurations() {
        setClickable(false);
        setFocusable(false);
        ViewCompat.setElevation(this, 100.0f);
    }

    private void initViews() {
        View view = View.inflate(this.getContext(), R.layout.chat_head_layout, this);
        chatHeadView = view.findViewById(R.id.chat_head_view);
    }

    private void readTypedArray(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        @SuppressLint("CustomViewStyleable") TypedArray typedArray = this.getContext().obtainStyledAttributes(attrs, R.styleable.GliaView, defStyleAttr, defStyleRes);
        setDefaultTheme(typedArray);
        typedArray.recycle();
    }

    private void setDefaultTheme(TypedArray typedArray) {
        UiTheme theme = Utils.getThemeFromTypedArray(typedArray, this.getContext());
        // forwarding call to chat head view. Always using same attrs and attributeSet
        chatHeadView.setTheme(theme);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupViewActions() {
        chatHeadView.setOnTouchListener(
                new ViewHelpers.ChatHeadOnTouchListener(
                        () -> new Pair(
                                Float.valueOf(chatHeadView.getX()).intValue(),
                                Float.valueOf(chatHeadView.getY()).intValue()
                        ),
                        (x, y) -> {
                            chatHeadView.setX(x);
                            chatHeadView.setY(y);

                            chatHeadView.invalidate();
                        },
                        v -> onChatHeadClickedListener.onClicked(
                                chatHeadsController.chatHeadClicked())
                )
        );
    }

    @Override
    protected void onDetachedFromWindow() {
        chatHeadsController.removeListener(chatHeadListener);
        super.onDetachedFromWindow();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        float floatingViewX = w - getResources().getDimension(R.dimen.chat_head_size)
                - Utils.pxFromDp(this.getContext(), 16);
        float floatingViewY = h / 10f * 8f;
        chatHeadView.setX(floatingViewX);
        chatHeadView.setY(floatingViewY);
        chatHeadView.invalidate();
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public interface OnChatHeadClickedListener {
        void onClicked(ChatHeadInput chatHeadInput);
    }
}
