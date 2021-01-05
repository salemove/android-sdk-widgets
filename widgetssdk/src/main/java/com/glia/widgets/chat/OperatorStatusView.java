package com.glia.widgets.chat;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.SimpleColorFilter;
import com.airbnb.lottie.model.KeyPath;
import com.glia.widgets.R;
import com.google.android.material.imageview.ShapeableImageView;

class OperatorStatusView extends ConstraintLayout {

    private final ImageView placeHolderView;
    private final LottieAnimationView pulsationAnimation;
    private final View cornersView;
    private final ShapeableImageView profilePictureView;

    public OperatorStatusView(@NonNull Context context) {
        this(context, null);
    }

    public OperatorStatusView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OperatorStatusView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = inflate(context, R.layout.operator_status_view, this);
        placeHolderView = view.findViewById(R.id.place_holder_view);
        pulsationAnimation = view.findViewById(R.id.pulsation_animation);
        cornersView = view.findViewById(R.id.corners_view);
        profilePictureView = view.findViewById(R.id.profile_picture_view);
    }

    public void setTint(@ColorRes int primaryBrandColorRes, @ColorRes int baseLightColorRes) {
        ColorStateList backgroundColor = ContextCompat.getColorStateList(this.getContext(), baseLightColorRes);
        pulsationAnimation.addValueCallback(
                new KeyPath("**"),
                LottieProperty.COLOR_FILTER,
                frameInfo -> new SimpleColorFilter(this.getContext().getColor(primaryBrandColorRes))
        );
        ImageViewCompat.setImageTintList(placeHolderView, backgroundColor);
        placeHolderView.setBackgroundTintList(
                ContextCompat.getColorStateList(this.getContext(), primaryBrandColorRes));
        GradientDrawable gradientDrawable = (GradientDrawable) cornersView.getBackground();
        gradientDrawable.setStroke((int) Utils.pxFromDp(this.getContext(), 24),
                ContextCompat.getColor(this.getContext(), primaryBrandColorRes));
    }

    public void setOperatorImage(@DrawableRes int profileDrawableRes, boolean loading) {
        placeHolderView.setVisibility(GONE);
        cornersView.setVisibility(GONE);
        if (loading) {
            pulsationAnimation.playAnimation();
            pulsationAnimation.setVisibility(VISIBLE);
        } else {
            pulsationAnimation.cancelAnimation();
            pulsationAnimation.setVisibility(GONE);
        }
        profilePictureView.setImageDrawable(ContextCompat.getDrawable(this.getContext(), profileDrawableRes));
        profilePictureView.setVisibility(VISIBLE);
    }

    public void removeOperatorImage() {
        placeHolderView.setVisibility(VISIBLE);
        cornersView.setVisibility(VISIBLE);
        pulsationAnimation.playAnimation();
        pulsationAnimation.setVisibility(VISIBLE);
        profilePictureView.setVisibility(GONE);
    }
}
