package com.glia.widgets;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.glia.widgets.chat.ChatAdapter;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.theme.overlay.MaterialThemeOverlay;

public class ChatView extends LinearLayout {

    private boolean started = false;

    private RecyclerView chatRecyclerView;
    private ImageView sendView;
    private EditText chatEditText;
    private ChatAdapter adapter;
    private AppBarLayout toolbarLayout;
    private MaterialToolbar toolbar;
    private TextView toolbarTitle;

    private UiTheme theme;
    private UiTheme defaultTheme;
    // needs to be separate as it is not from styleable resources
    private Integer defaultStatusbarColor;
    private ColorStateList primaryColor;
    private ColorStateList operatorBackgroundColor;
    private ColorStateList backgroundTint;
    private Typeface fontFamily;
    private ColorStateList primaryTextColor;

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
        readTypedArray(attrs, defStyleAttr);
        initViews();
    }


    public void setTheme(UiTheme uiTheme) {
        if (uiTheme == null) return;
        this.theme = uiTheme;
        if (uiTheme.getTitle() != null) {
            setTitle(uiTheme.getTitle());
        } else if (defaultTheme.getTitle() != null) {
            setTitle(defaultTheme.getTitle());
        }
        if (uiTheme.getBackgroundColorRes() != null) {
            this.backgroundTint = ContextCompat.getColorStateList(this.getContext(), uiTheme.getBackgroundColorRes());
        } else {
            this.backgroundTint = ContextCompat.getColorStateList(this.getContext(), defaultTheme.getBackgroundColorRes());
        }
        if (uiTheme.getPrimaryBrandColorRes() != null) {
            this.primaryColor = ContextCompat.getColorStateList(this.getContext(), uiTheme.getPrimaryBrandColorRes());
        } else {
            this.primaryColor = ContextCompat.getColorStateList(this.getContext(), defaultTheme.getPrimaryBrandColorRes());
        }
        if (uiTheme.getOperatorMessageBgColorRes() != null) {
            this.operatorBackgroundColor = ContextCompat.getColorStateList(this.getContext(), uiTheme.getOperatorMessageBgColorRes());
        } else {
            this.operatorBackgroundColor = ContextCompat.getColorStateList(this.getContext(), defaultTheme.getOperatorMessageBgColorRes());
        }
        if (uiTheme.getFontRes() != null) {
            this.fontFamily = ResourcesCompat.getFont(this.getContext(), uiTheme.getFontRes());
        } else if (defaultTheme.getFontRes() != 0) {
            this.fontFamily = ResourcesCompat.getFont(this.getContext(), defaultTheme.getFontRes());
        }
        if (uiTheme.getPrimaryTextColorRes() != null) {
            this.primaryTextColor = ContextCompat.getColorStateList(this.getContext(), uiTheme.getPrimaryTextColorRes());
        } else {
            this.primaryTextColor = ContextCompat.getColorStateList(this.getContext(), defaultTheme.getPrimaryTextColorRes());
        }
    }

    public void setOnBackClickedListener(View.OnClickListener onClickListener) {
        toolbarLayout.setVisibility(VISIBLE);
        toolbar.setNavigationOnClickListener(onClickListener);
    }

    private void setTitle(String title) {
        toolbarTitle.setText(title);
        toolbarLayout.setVisibility(VISIBLE);
    }

    public void start() {
        setupViews(this.getContext());
        setupViewActions();
        setVisibility(VISIBLE);
        // TODO Remove when start using real data.
        adapter.initDefault();
        started = true;
    }

    public void stop() {
        setVisibility(INVISIBLE);
        started = false;
        if (defaultStatusbarColor != null) {
            getActivity().getWindow().setStatusBarColor(defaultStatusbarColor);
        }
    }

    private void readTypedArray(AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = this.getContext().obtainStyledAttributes(attrs, R.styleable.ChatView, defStyleAttr, 0);
        setViewAttrValues(typedArray);
        setDefaultTheme(typedArray);
        typedArray.recycle();
    }

    private void setDefaultTheme(TypedArray typedArray) {
        UiTheme.UiThemeBuilder defaultThemeBuilder = new UiTheme.UiThemeBuilder();
        defaultThemeBuilder.setTitle(getTitle(typedArray));
        defaultThemeBuilder.setBackgroundColorRes(getBackgroundColorRes(typedArray));
        defaultThemeBuilder.setOperatorMessageBgColorRes(getOperatorBackgroundColorRes(typedArray));
        defaultThemeBuilder.setPrimaryBrandColorRes(getPrimaryColorRes(typedArray));
        defaultThemeBuilder.setFontRes(getFontFamilyRes(typedArray));
        defaultThemeBuilder.setPrimaryTextColorRes(getPrimaryTextColorRes(typedArray));
        defaultTheme = defaultThemeBuilder.build();
    }

    private void setViewAttrValues(TypedArray typedArray) {
        String title = getTitle(typedArray);
        if (title != null) {
            setTitle(title);
        }

        backgroundTint = ContextCompat.getColorStateList(this.getContext(), getBackgroundColorRes(typedArray));
        primaryColor = ContextCompat.getColorStateList(this.getContext(), getPrimaryColorRes(typedArray));
        operatorBackgroundColor = ContextCompat.getColorStateList(this.getContext(), getOperatorBackgroundColorRes(typedArray));
        primaryTextColor = ContextCompat.getColorStateList(this.getContext(), getPrimaryTextColorRes(typedArray));

        int fontFamilyResId = getFontFamilyRes(typedArray);
        if (fontFamilyResId != 0) {
            fontFamily = ResourcesCompat.getFont(this.getContext(), fontFamilyResId);
        }
    }

    private String getTitle(TypedArray typedArray) {
        if (typedArray.hasValue(R.styleable.ChatView_title)) {
            return typedArray.getString(R.styleable.ChatView_title);
        } else {
            return null;
        }
    }

    private int getBackgroundColorRes(TypedArray typedArray) {
        if (typedArray.hasValue(R.styleable.ChatView_chatBgColor)) {
            return typedArray.getResourceId(R.styleable.ChatView_chatBgColor, 0);
        } else {
            return R.color.color_white;
        }
    }

    private int getOperatorBackgroundColorRes(TypedArray typedArray) {
        if (typedArray.hasValue(R.styleable.ChatView_operatorBackgroundColor)) {
            return typedArray.getResourceId(R.styleable.ChatView_operatorBackgroundColor, 0);
        } else {
            return R.color.light_gray_color;
        }
    }

    private int getPrimaryColorRes(TypedArray typedArray) {
        if (typedArray.hasValue(R.styleable.ChatView_primaryColor)) {
            return typedArray.getResourceId(R.styleable.ChatView_primaryColor, 0);
        } else {
            return R.color.color_primary;
        }
    }

    private int getFontFamilyRes(TypedArray typedArray) {
        return typedArray.getResourceId(R.styleable.ChatView_android_fontFamily, 0);
    }

    private int getPrimaryTextColorRes(TypedArray typedArray) {
        if (typedArray.hasValue(R.styleable.ChatView_primaryTextColor)) {
            return typedArray.getResourceId(R.styleable.ChatView_primaryTextColor, 0);
        } else {
            return R.color.color_white;
        }
    }

    private void initConfigurations() {
        setOrientation(VERTICAL);
        setVisibility(INVISIBLE);
        // needed to overlap existing app bar in existing view with this view's app bar.
        ViewCompat.setElevation(this, 100.0f);
    }

    private void initViews() {
        View view = View.inflate(this.getContext(), R.layout.chat_view, this);
        chatRecyclerView = view.findViewById(R.id.chat_recycler_view);
        sendView = view.findViewById(R.id.send_view);
        chatEditText = view.findViewById(R.id.chat_edit_text);
        toolbarLayout = view.findViewById(R.id.chat_tool_bar);
        toolbar = view.findViewById(R.id.chat_top_app_bar);
        toolbarTitle = toolbar.findViewById(R.id.title);
    }

    private void setupViews(Context context) {
        adapter = new ChatAdapter(primaryColor, operatorBackgroundColor, fontFamily, primaryTextColor);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        chatRecyclerView.setAdapter(adapter);

        setBackgroundColor(backgroundTint.getDefaultColor());
        sendView.setImageTintList(primaryColor);
        toolbar.setBackgroundTintList(primaryColor);
        if (defaultStatusbarColor == null && theme.getPrimaryBrandColorRes() != null) {
            defaultStatusbarColor = getActivity().getWindow().getStatusBarColor();
            getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(this.getContext(), theme.getPrimaryBrandColorRes()));
        }
        if (fontFamily != null) {
            chatEditText.setTypeface(fontFamily);
            toolbarTitle.setTypeface(fontFamily);
        }
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

    private Activity getActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    @Nullable
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        ChatSaveState saveState = new ChatSaveState(superState);
        saveState.started = started;
        if (theme != null) {
            saveState.uiTheme = theme;
        }
        saveState.defaultStatusbarColor = defaultStatusbarColor;
        return saveState;
    }

    protected void onRestoreInstanceState(@Nullable Parcelable state) {
        if (!(state instanceof ChatSaveState)) {
            super.onRestoreInstanceState(state);
        } else {
            ChatSaveState saveState = (ChatSaveState) state;
            super.onRestoreInstanceState(saveState.getSuperState());
            setTheme(saveState.uiTheme);
            if (saveState.started) {
                this.start();
            }
            defaultStatusbarColor = saveState.defaultStatusbarColor;
        }
    }
}
