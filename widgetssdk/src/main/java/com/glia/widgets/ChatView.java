package com.glia.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.glia.widgets.chat.ChatAdapter;
import com.google.android.material.theme.overlay.MaterialThemeOverlay;

public class ChatView extends LinearLayout {

    private RecyclerView chatRecyclerView;
    private ImageView sendView;
    private EditText chatEditText;

    private ColorStateList backgroundTint;
    private ColorStateList colorPrimary;
    private ColorStateList senderMessageTint;

    public ChatView(Context context) {
        this(context, null);
    }

    public ChatView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.gliaChatTheme);
    }

    public ChatView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Application_Glia_Chat_Layout);
    }

    public ChatView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
        setAttrValues(this.getContext(), attrs, defStyleAttr);
        initViews(this.getContext());
        setupViews(this.getContext());
        setupViewActions();
    }

    public void setupUiConfig(UiTheme uiTheme) {
        if (uiTheme.getBackgroundTint() != null) {
            this.backgroundTint = uiTheme.getBackgroundTint();
        }
        if (uiTheme.getColorPrimary() != null) {
            this.colorPrimary = uiTheme.getColorPrimary();
        }
        if (uiTheme.getSenderBackgroundTint() != null) {
            this.senderMessageTint = uiTheme.getSenderBackgroundTint();
        }
        setupViews(this.getContext());
    }

    private void setAttrValues(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ChatView, defStyleAttr, 0);
        backgroundTint = typedArray.getColorStateList(R.styleable.ChatView_backgroundTint);
        if (backgroundTint == null) {
            backgroundTint = ContextCompat.getColorStateList(context, R.color.color_white);
        }
        senderMessageTint = typedArray.getColorStateList(R.styleable.ChatView_senderMessageTint);
        if (senderMessageTint == null) {
            senderMessageTint = ContextCompat.getColorStateList(context, R.color.color_primary);
        }
        colorPrimary = typedArray.getColorStateList(R.styleable.ChatView_receiverMessageTint);
        if (colorPrimary == null) {
            colorPrimary = ContextCompat.getColorStateList(context, R.color.light_gray_color);
        }
        typedArray.recycle();
    }

    private void initConfigurations() {
        setOrientation(VERTICAL);
    }

    private void initViews(Context context) {
        View view = View.inflate(context, R.layout.chat_view, this);
        chatRecyclerView = view.findViewById(R.id.chat_recycler_view);
        sendView = view.findViewById(R.id.send_view);
        chatEditText = view.findViewById(R.id.chat_edit_text);
    }

    private void setupViews(Context context) {
        ChatAdapter adapter = new ChatAdapter(senderMessageTint, colorPrimary);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        chatRecyclerView.setAdapter(adapter);

        setBackgroundColor(backgroundTint.getDefaultColor());
        sendView.setBackgroundTintList(backgroundTint);

        // TODO Remove when start using real data.
        adapter.initDefault();
    }

    private void setupViewActions() {
        chatEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    sendView.setVisibility(VISIBLE);
                } else {
                    sendView.setVisibility(GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
}
