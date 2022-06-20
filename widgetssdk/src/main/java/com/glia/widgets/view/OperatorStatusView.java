package com.glia.widgets.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.SimpleColorFilter;
import com.airbnb.lottie.model.KeyPath;
import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.view.configuration.chat.ChatStyle;
import com.google.android.material.imageview.ShapeableImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class OperatorStatusView extends ConstraintLayout {
    private static final int NO_PADDING = 0;

    // Animation behind the view
    private final LottieAnimationView rippleAnimation;

    // Main view
    private final ShapeableImageView profilePictureView;
    // Icon on top of main view - displayed if no image available
    private final ShapeableImageView placeholderView;
    // On Hold status view on top of main view - displayed when on hold status changes
    private final ShapeableImageView onHoldOverlayView;

    private final int operatorImageSize;
    private final int operatorImageContentPadding;
    private final int operatorImageLargeSize;
    private final int operatorImageLargeContentPadding;

    private boolean isOnHold = false;
    private ColorDrawable profilePictureBackgroundColorDrawable = null;

    public OperatorStatusView(@NonNull Context context) {
        this(context, null);
    }

    public OperatorStatusView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OperatorStatusView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = inflate(context, R.layout.operator_status_view, this);
        rippleAnimation = view.findViewById(R.id.ripple_animation);
        profilePictureView = view.findViewById(R.id.profile_picture_view);
        placeholderView = view.findViewById(R.id.placeholder_view);
        onHoldOverlayView = view.findViewById(R.id.on_hold_icon);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.OperatorStatusView);
        int rippleAnimationColor = typedArray.getResourceId(R.styleable.OperatorStatusView_rippleTint, 0);
        if (rippleAnimationColor != 0) {
            rippleAnimation.addValueCallback(
                    new KeyPath("**"),
                    LottieProperty.COLOR_FILTER,
                    frameInfo -> new SimpleColorFilter(this.getContext().getColor(rippleAnimationColor))
            );
        }

        operatorImageLargeSize = getResources().getDimensionPixelSize(R.dimen.glia_chat_profile_picture_large_size);
        operatorImageLargeContentPadding = getResources().getDimensionPixelSize(R.dimen.glia_chat_profile_picture_large_content_padding);

        operatorImageSize = typedArray.getDimensionPixelSize(
                R.styleable.OperatorStatusView_imageSize,
                getResources().getDimensionPixelSize(R.dimen.glia_chat_profile_picture_size)
        );
        operatorImageContentPadding = typedArray.getDimensionPixelSize(
                R.styleable.OperatorStatusView_imageContentPadding,
                getResources().getDimensionPixelSize(R.dimen.glia_chat_profile_picture_content_padding)
        );
        updateProfilePictureViewSize(operatorImageSize);
        typedArray.recycle();
        setOnHoldVisibility();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Picasso.get().cancelRequest(profilePictureView);
    }

    public void setTheme(UiTheme theme, @Nullable ChatStyle chatStyle) { // shouldn't be nullable
        // icons
        setPlaceHolderIcon(theme);
        setOnHoldIcon(theme);

        // colors
        int color;
        if (chatStyle != null) {
            color = Color.parseColor(chatStyle.welcomeView.tintColor);
        } else {
            color = ContextCompat.getColor(this.getContext(), theme.getBrandPrimaryColor());
        }
        profilePictureBackgroundColorDrawable =
                new ColorDrawable(color);
        rippleAnimation.addValueCallback(
                new KeyPath("**"),
                LottieProperty.COLOR_FILTER,
                frameInfo -> new SimpleColorFilter(color)
        );
        profilePictureView.setImageDrawable(profilePictureBackgroundColorDrawable);
        placeholderView.setImageTintList(
                ContextCompat.getColorStateList(this.getContext(), theme.getBaseLightColor())
        );
    }

    public void showTransferring() {
        profilePictureView.setImageDrawable(profilePictureBackgroundColorDrawable);
        updateProfilePictureViewSize(operatorImageSize);
        updatePlaceholderView(
                operatorImageSize,
                operatorImageContentPadding,
                VISIBLE
        );
    }

    public void showPlaceHolderWithIconPaddingOnConnect() {
        profilePictureView.setImageDrawable(profilePictureBackgroundColorDrawable);
        updateProfilePictureViewSize(operatorImageLargeSize);
        updatePlaceholderView(
                operatorImageLargeSize,
                operatorImageLargeContentPadding,
                VISIBLE
        );
    }

    public void showProfileImageOnConnect(String profileImgUrl) {
        updateProfilePictureViewSize(operatorImageLargeSize);
        Picasso.get().load(profileImgUrl).into(profilePictureView, new Callback() {
            @Override
            public void onSuccess() {
                updatePlaceholderView(
                        operatorImageLargeSize,
                        NO_PADDING,
                        GONE
                );
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    public void showProfileImage(String profileImgUrl) {
        updateProfilePictureViewSize(operatorImageSize);
        Picasso.get().load(profileImgUrl).into(profilePictureView, new Callback() {
            @Override
            public void onSuccess() {
                updatePlaceholderView(
                        operatorImageSize,
                        NO_PADDING,
                        GONE
                );
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    public void showPlaceholder() {
        profilePictureView.setImageDrawable(profilePictureBackgroundColorDrawable);
        updateProfilePictureViewSize(operatorImageSize);
        updatePlaceholderView(
                operatorImageSize,
                operatorImageContentPadding,
                VISIBLE
        );
    }

    public void setShowOnHold(boolean isOnHold) {
        if (this.isOnHold != isOnHold) {
            this.isOnHold = isOnHold;
            setOnHoldVisibility();
        }
    }

    public void setShowRippleAnimation(boolean show) {
        if (show) {
            showRippleAnimation();
        } else {
            hideRippleAnimation();
        }
    }

    private void updatePlaceholderView(
            int size,
            int contentPadding,
            int visibility
    ) {
        placeholderView.getLayoutParams().width = size;
        placeholderView.getLayoutParams().height = size;
        placeholderView.setVisibility(visibility);
        setPlaceholderViewContentPadding(contentPadding);
    }

    private void setPlaceholderViewContentPadding(int contentPadding) {
        placeholderView.setPaddingRelative(
                contentPadding,
                contentPadding,
                contentPadding,
                contentPadding
        );
    }

    private void updateProfilePictureViewSize(int size) {
        profilePictureView.getLayoutParams().width = size;
        profilePictureView.getLayoutParams().height = size;
    }

    private void showRippleAnimation() {
        rippleAnimation.playAnimation();
        rippleAnimation.setVisibility(VISIBLE);
    }

    private void hideRippleAnimation() {
        rippleAnimation.cancelAnimation();
        rippleAnimation.setVisibility(GONE);
    }

    private void setOnHoldVisibility() {
        onHoldOverlayView.setVisibility(isOnHold ? View.VISIBLE : View.GONE);
    }

    private void setPlaceHolderIcon(UiTheme theme) {
        placeholderView.setImageResource(theme.getIconPlaceholder());
    }

    private void setOnHoldIcon(UiTheme theme) {
        onHoldOverlayView.setImageResource(theme.getIconOnHold());
    }
}
