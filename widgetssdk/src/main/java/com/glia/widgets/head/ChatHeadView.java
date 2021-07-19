package com.glia.widgets.head;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.helper.Utils;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.theme.overlay.MaterialThemeOverlay;
import com.squareup.picasso.Picasso;

public class ChatHeadView extends ConstraintLayout {

    private ShapeableImageView profilePictureView;
    private ShapeableImageView placeholderView;
    private TextView chatBubbleBadge;

    private final Resources resources;
    private UiTheme theme;

    public ChatHeadView(@NonNull Context context) {
        this(context, null);
    }

    public ChatHeadView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.gliaChatStyle);
    }

    public ChatHeadView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Application_Glia_Chat);
    }

    public ChatHeadView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
        this.resources = getResources();

        initViews();
        readTypedArray(attrs, defStyleAttr, defStyleRes);
        setupViewAppearance();
    }

    private void initViews() {
        View view = View.inflate(this.getContext(), R.layout.chat_head_view, this);
        profilePictureView = view.findViewById(R.id.profile_picture_view);
        placeholderView = view.findViewById(R.id.placeholder_view);
        chatBubbleBadge = view.findViewById(R.id.chat_bubble_badge);
    }

    private void readTypedArray(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        @SuppressLint("CustomViewStyleable") TypedArray typedArray = this.getContext().obtainStyledAttributes(attrs, R.styleable.GliaView, defStyleAttr, defStyleRes);
        setDefaultTheme(typedArray);
        typedArray.recycle();
    }

    private void setDefaultTheme(TypedArray typedArray) {
        this.theme = Utils.getThemeFromTypedArray(typedArray, this.getContext());
    }

    public void setTheme(UiTheme uiTheme) {
        if (uiTheme == null) return;
        this.theme = Utils.getFullHybridTheme(this.theme, theme);
        post(this::setupViewAppearance);
    }

    private void setupViewAppearance() {
        placeholderView.setImageResource(theme.getIconPlaceholder());
        // colors
        ColorStateList backgroundColor =
                ContextCompat.getColorStateList(this.getContext(), theme.getBaseLightColor());
        int primaryColor = ContextCompat.getColor(this.getContext(), theme.getBrandPrimaryColor());
        ColorStateList primaryColorStateList = ContextCompat.getColorStateList(
                this.getContext(), theme.getBrandPrimaryColor());
        profilePictureView.setBackgroundColor(primaryColor);
        placeholderView.setBackgroundColor(primaryColor);
        placeholderView.setImageTintList(backgroundColor);
        chatBubbleBadge.setBackgroundTintList(primaryColorStateList);
        chatBubbleBadge.setTextColor(backgroundColor);
    }

    public void updateImage(String operatorProfileImgUrl) {
        post(() -> {
            if (placeholderView != null && profilePictureView != null) {
                if (operatorProfileImgUrl != null) {
                    Picasso.get().load(operatorProfileImgUrl).into(profilePictureView);
                    placeholderView.setVisibility(View.GONE);
                } else {
                    int primaryColor = ContextCompat.getColor(this.getContext(), theme.getBrandPrimaryColor());
                    profilePictureView.setImageDrawable(null);
                    profilePictureView.setBackgroundColor(primaryColor);
                    placeholderView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void setMessageBadgeCount(int messageCount) {
        post(() -> {
            chatBubbleBadge.setText(String.valueOf(messageCount));
            chatBubbleBadge.setVisibility(messageCount > 0 ? VISIBLE : GONE);
        });
    }
}
