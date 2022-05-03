package com.glia.widgets.view;

import android.content.Context;
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

    private final int IMAGE_SIZE_DEFAULT;
    private final int IMAGE_SIZE_LARGE;
    private final int PLACEHOLDER_ICON_PADDING_DEFAULT;
    private final int PLACEHOLDER_ICON_PADDING_LARGE;
    private final int NO_PADDING = 0;

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

        IMAGE_SIZE_LARGE = getResources().getDimensionPixelSize(R.dimen.glia_ripple_animation_size);
        PLACEHOLDER_ICON_PADDING_LARGE = IMAGE_SIZE_LARGE / 4;

        IMAGE_SIZE_DEFAULT = typedArray.getDimensionPixelSize(
                R.styleable.OperatorStatusView_imageSize,
                getResources().getDimensionPixelSize(R.dimen.glia_chat_profile_picture_size)
        );

        updateProfilePictureViewSize(IMAGE_SIZE_DEFAULT);
        PLACEHOLDER_ICON_PADDING_DEFAULT = IMAGE_SIZE_DEFAULT / 4;

        typedArray.recycle();
        setOnHoldVisibility();
    }

    public void setTheme(UiTheme theme) {
        // icons
        setPlaceHolderIcon(theme);
        setOnHoldIcon(theme);

        // colors
        profilePictureBackgroundColorDrawable = new ColorDrawable(
                ContextCompat.getColor(this.getContext(), theme.getBrandPrimaryColor())
        );
        rippleAnimation.addValueCallback(
                new KeyPath("**"),
                LottieProperty.COLOR_FILTER,
                frameInfo -> new SimpleColorFilter(this.getContext().getColor(theme.getBrandPrimaryColor()))
        );
        profilePictureView.setImageDrawable(profilePictureBackgroundColorDrawable);
        placeholderView.setImageTintList(
                ContextCompat.getColorStateList(this.getContext(), theme.getBaseLightColor())
        );
    }

    public void showPlaceHolder() {
        profilePictureView.setImageDrawable(profilePictureBackgroundColorDrawable);
        updateProfilePictureViewSize(IMAGE_SIZE_DEFAULT);
        updatePlaceholderView(
                IMAGE_SIZE_DEFAULT,
                NO_PADDING,
                VISIBLE
        );
    }

    public void showPlaceHolderWithIconPaddingOnConnect() {
        profilePictureView.setImageDrawable(profilePictureBackgroundColorDrawable);
        updateProfilePictureViewSize(IMAGE_SIZE_LARGE);
        updatePlaceholderView(
                IMAGE_SIZE_LARGE,
                PLACEHOLDER_ICON_PADDING_LARGE,
                VISIBLE
        );
    }

    public void showProfileImageOnConnect(String profileImgUrl) {
        updateProfilePictureViewSize(IMAGE_SIZE_LARGE);
        Picasso.get().load(profileImgUrl).into(profilePictureView);
        updatePlaceholderView(
                IMAGE_SIZE_LARGE,
                NO_PADDING,
                GONE
        );
    }

    public void showProfileImage(String profileImgUrl) {
        updateProfilePictureViewSize(IMAGE_SIZE_DEFAULT);
        Picasso.get().load(profileImgUrl).into(profilePictureView);
        updatePlaceholderView(
                IMAGE_SIZE_DEFAULT,
                NO_PADDING,
                GONE
        );
    }

    public void showPlaceHolderWithIconPadding() {
        profilePictureView.setImageDrawable(profilePictureBackgroundColorDrawable);
        updateProfilePictureViewSize(IMAGE_SIZE_DEFAULT);
        updatePlaceholderView(
                IMAGE_SIZE_DEFAULT,
                PLACEHOLDER_ICON_PADDING_DEFAULT,
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
        placeholderView.post(() -> {
            placeholderView.getLayoutParams().width = size;
            placeholderView.getLayoutParams().height = size;
            placeholderView.setContentPadding(
                    contentPadding,
                    contentPadding,
                    contentPadding,
                    contentPadding
            );
            placeholderView.setVisibility(visibility);
        });
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
