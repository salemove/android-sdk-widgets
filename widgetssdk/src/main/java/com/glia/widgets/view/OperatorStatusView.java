package com.glia.widgets.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
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
import com.google.android.material.imageview.ShapeableImageView;
import com.squareup.picasso.Picasso;

public class OperatorStatusView extends ConstraintLayout {
    // Animation behind the view
    private final LottieAnimationView rippleAnimation;

    // Main view
    private final ShapeableImageView profilePictureView;
    // Icon on top of main view - displayed if no image available
    private final ShapeableImageView placeholderView;
    // On Hold status view on top of main view - displayed when on hold status changes
    private final ShapeableImageView onHoldOverlayView;

    private final int placeHolderViewContentPadding;

    private int primaryColor;
    private boolean isOnHold = false;

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

        int imageSize = (int) typedArray.getDimensionPixelSize(
                R.styleable.OperatorStatusView_imageSize,
                (int) getResources().getDimensionPixelSize(R.dimen.glia_chat_profile_picture_size)
        );
        profilePictureView.getLayoutParams().width = imageSize;
        profilePictureView.getLayoutParams().height = imageSize;

        placeHolderViewContentPadding = imageSize / 4;

        typedArray.recycle();
        setOnHoldVisibility();
    }

    public void setTheme(UiTheme theme) {
        // icons
        setPlaceHolderIcon(theme);
        setOnHoldIcon(theme);

        // colors
        ColorStateList backgroundColor = ContextCompat.getColorStateList(
                this.getContext(), theme.getBaseLightColor()
        );
        primaryColor = ContextCompat.getColor(
                this.getContext(), theme.getBrandPrimaryColor()
        );
        rippleAnimation.addValueCallback(
                new KeyPath("**"),
                LottieProperty.COLOR_FILTER,
                frameInfo -> new SimpleColorFilter(this.getContext().getColor(theme.getBrandPrimaryColor()))
        );
        profilePictureView.setImageDrawable(new ColorDrawable(primaryColor));
        placeholderView.setImageTintList(backgroundColor);
    }

    public void showPlaceHolder() {
        profilePictureView.setImageDrawable(new ColorDrawable(primaryColor));
        placeholderView.setContentPadding(0, 0, 0, 0);
        placeholderView.setVisibility(VISIBLE);
    }

    public void showPlaceHolderWithIconPadding() {
        profilePictureView.setImageDrawable(new ColorDrawable(primaryColor));
        placeholderView.setVisibility(VISIBLE);
        placeholderView.post(() -> placeholderView.setContentPadding(placeHolderViewContentPadding, placeHolderViewContentPadding, placeHolderViewContentPadding, placeHolderViewContentPadding));
    }

    public void showPlaceHolderWithIconPaddingOnConnect() {
        int imageSize = getResources().getDimensionPixelSize(R.dimen.glia_ripple_animation_size);
        profilePictureView.getLayoutParams().width = imageSize;
        profilePictureView.getLayoutParams().height = imageSize;
        showPlaceHolderWithIconPadding();
    }

    public void showProfileImageOnConnect(String profileImgUrl) {
        int imageSize = getResources().getDimensionPixelSize(R.dimen.glia_ripple_animation_size);
        profilePictureView.getLayoutParams().width = imageSize;
        profilePictureView.getLayoutParams().height = imageSize;
        showProfileImage(profileImgUrl);
    }

    public void showProfileImage(String profileImgUrl) {
        Picasso.get().load(profileImgUrl).into(profilePictureView);
        placeholderView.setVisibility(GONE);
        placeholderView.post(() -> placeholderView.setContentPadding(placeHolderViewContentPadding, placeHolderViewContentPadding, placeHolderViewContentPadding, placeHolderViewContentPadding));
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
