package com.glia.widgets.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.SimpleColorFilter;
import com.airbnb.lottie.model.KeyPath;
import com.glia.widgets.R;
import com.google.android.material.imageview.ShapeableImageView;

public class OperatorStatusView extends ConstraintLayout {

    private final LottieAnimationView pulsationAnimation;
    private final ShapeableImageView profilePictureView;
    private final ShapeableImageView placeholderView;
    private int primaryColor;

    public OperatorStatusView(@NonNull Context context) {
        this(context, null);
    }

    public OperatorStatusView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OperatorStatusView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = inflate(context, R.layout.operator_status_view, this);
        pulsationAnimation = view.findViewById(R.id.pulsation_animation);
        profilePictureView = view.findViewById(R.id.profile_picture_view);
        placeholderView = view.findViewById(R.id.placeholder_view);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.OperatorStatusView);
        int defaultProfilePictureSize =
                (int) getResources().getDimension(R.dimen.chat_profile_picture_size);
        int profilePictureSize =
                typedArray.getDimensionPixelSize(R.styleable.OperatorStatusView_connectedIconSize, defaultProfilePictureSize);
        profilePictureView.getLayoutParams().width = profilePictureSize;
        profilePictureView.getLayoutParams().height = profilePictureSize;
        typedArray.recycle();
    }

    public void setTint(@ColorRes int primaryBrandColorRes, @ColorRes int baseLightColorRes) {
        ColorStateList backgroundColor = ContextCompat.getColorStateList(this.getContext(), baseLightColorRes);
        primaryColor = ContextCompat.getColor(this.getContext(), primaryBrandColorRes);
        pulsationAnimation.addValueCallback(
                new KeyPath("**"),
                LottieProperty.COLOR_FILTER,
                frameInfo -> new SimpleColorFilter(this.getContext().getColor(primaryBrandColorRes))
        );
        profilePictureView.setBackgroundColor(primaryColor);
        placeholderView.setBackgroundColor(primaryColor);
        placeholderView.setImageTintList(backgroundColor);
    }

    public void setOperatorImage(@DrawableRes int profileDrawableRes, boolean loading) {
        if (loading) {
            pulsationAnimation.playAnimation();
            pulsationAnimation.setVisibility(VISIBLE);
        } else {
            pulsationAnimation.cancelAnimation();
            pulsationAnimation.setVisibility(GONE);
        }
        profilePictureView.setImageDrawable(ContextCompat.getDrawable(this.getContext(), profileDrawableRes));
        placeholderView.setVisibility(GONE);
    }

    public void removeOperatorImage() {
        pulsationAnimation.playAnimation();
        pulsationAnimation.setVisibility(VISIBLE);
        ColorDrawable cd = new ColorDrawable(primaryColor);
        profilePictureView.setImageDrawable(cd);
        placeholderView.setVisibility(VISIBLE);
    }
}
