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

    private final LottieAnimationView rippleAnimation;
    private final ShapeableImageView profilePictureView;
    private final ShapeableImageView placeholderView;
    private int primaryColor;
    private final int connectedImageSize;
    private final int placeholderBackgroundSize;
    private final int placeHolderSize;

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

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.OperatorStatusView);
        int rippleAnimationColor = typedArray.getResourceId(R.styleable.OperatorStatusView_rippleTint, 0);
        if (rippleAnimationColor != 0) {
            rippleAnimation.addValueCallback(
                    new KeyPath("**"),
                    LottieProperty.COLOR_FILTER,
                    frameInfo -> new SimpleColorFilter(this.getContext().getColor(rippleAnimationColor))
            );
        }
        placeholderBackgroundSize =
                typedArray.getDimensionPixelSize(
                        R.styleable.OperatorStatusView_imageSize,
                        (int) getResources().getDimension(R.dimen.chat_profile_picture_size)
                );
        placeHolderSize = placeholderBackgroundSize / 2;
        connectedImageSize =
                typedArray.getDimensionPixelSize(
                        R.styleable.OperatorStatusView_imageSize,
                        placeholderBackgroundSize
                );
        typedArray.recycle();
    }

    public void setPlaceHolderIcon(UiTheme theme) {
        placeholderView.setImageResource(theme.getIconPlaceholder());
    }

    public void setTheme(UiTheme theme) {
        // icons
        setPlaceHolderIcon(theme);

        // colors
        ColorStateList backgroundColor = ContextCompat.getColorStateList(this.getContext(), theme.getBaseLightColor());
        primaryColor = ContextCompat.getColor(this.getContext(), theme.getBrandPrimaryColor());
        rippleAnimation.addValueCallback(
                new KeyPath("**"),
                LottieProperty.COLOR_FILTER,
                frameInfo -> new SimpleColorFilter(this.getContext().getColor(theme.getBrandPrimaryColor()))
        );
        profilePictureView.setBackgroundColor(primaryColor);
        placeholderView.setBackgroundColor(primaryColor);
        placeholderView.setImageTintList(backgroundColor);
    }

    public void showProfileImage(String profileImgUrl) {
        profilePictureView.getLayoutParams().width = connectedImageSize;
        profilePictureView.getLayoutParams().height = connectedImageSize;
        showProfilePictureView(profileImgUrl);
    }

    public void showPlaceHolder() {
        profilePictureView.getLayoutParams().width = placeholderBackgroundSize;
        profilePictureView.getLayoutParams().height = placeholderBackgroundSize;
        placeholderView.getLayoutParams().width = placeHolderSize;
        placeholderView.getLayoutParams().height = placeHolderSize;
        showPlaceHolderView();
    }

    public void showDefaultSizeProfileImage(String profileImgUrl) {
        int backgroundSize = (int) getResources().getDimension(R.dimen.chat_profile_picture_size);
        profilePictureView.getLayoutParams().width = backgroundSize;
        profilePictureView.getLayoutParams().height = backgroundSize;
        showProfilePictureView(profileImgUrl);
    }

    public void showDefaultSizePlaceHolder() {
        int backgroundSize = (int) getResources().getDimension(R.dimen.chat_profile_picture_size);
        profilePictureView.getLayoutParams().width = backgroundSize;
        profilePictureView.getLayoutParams().height = backgroundSize;
        placeholderView.getLayoutParams().width = backgroundSize / 2;
        placeholderView.getLayoutParams().height = backgroundSize / 2;
        showPlaceHolderView();
    }

    private void showPlaceHolderView() {
        ColorDrawable cd = new ColorDrawable(primaryColor);
        profilePictureView.setImageDrawable(cd);
        placeholderView.setVisibility(VISIBLE);
    }

    private void showProfilePictureView(String profileImgUrl) {
        Picasso.with(this.getContext()).load(profileImgUrl).into(profilePictureView);
        placeholderView.setVisibility(GONE);
    }

    public void isRippleAnimationShowing(boolean show) {
        if (show) {
            rippleAnimation.playAnimation();
            rippleAnimation.setVisibility(VISIBLE);
        } else {
            rippleAnimation.cancelAnimation();
            rippleAnimation.setVisibility(GONE);
        }
    }
}
