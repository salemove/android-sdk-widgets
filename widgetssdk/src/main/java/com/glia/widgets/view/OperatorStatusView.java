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

    private final LottieAnimationView pulsationAnimation;
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
        pulsationAnimation = view.findViewById(R.id.pulsation_animation);
        profilePictureView = view.findViewById(R.id.profile_picture_view);
        placeholderView = view.findViewById(R.id.placeholder_view);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.OperatorStatusView);
        placeholderBackgroundSize =
                typedArray.getDimensionPixelSize(
                        R.styleable.OperatorStatusView_placeholderBackgroundSize,
                        (int) getResources().getDimension(R.dimen.chat_profile_picture_size)
                );
        placeHolderSize = placeholderBackgroundSize / 2;
        connectedImageSize =
                typedArray.getDimensionPixelSize(
                        R.styleable.OperatorStatusView_connectedImageSize,
                        placeholderBackgroundSize
                );
        typedArray.recycle();
    }

    public void setTheme(UiTheme theme) {
        // icons
        placeholderView.setImageResource(theme.getIconPlaceholder());

        // colors
        ColorStateList backgroundColor = ContextCompat.getColorStateList(this.getContext(), theme.getBaseLightColor());
        primaryColor = ContextCompat.getColor(this.getContext(), theme.getBrandPrimaryColor());
        pulsationAnimation.addValueCallback(
                new KeyPath("**"),
                LottieProperty.COLOR_FILTER,
                frameInfo -> new SimpleColorFilter(this.getContext().getColor(theme.getBrandPrimaryColor()))
        );
        profilePictureView.setBackgroundColor(primaryColor);
        placeholderView.setBackgroundColor(primaryColor);
        placeholderView.setImageTintList(backgroundColor);
    }

    public void setOperatorImage(String profileImgUrl) {
        profilePictureView.getLayoutParams().width = connectedImageSize;
        profilePictureView.getLayoutParams().height = connectedImageSize;
        Picasso.with(this.getContext()).load(profileImgUrl).into(profilePictureView);
        placeholderView.setVisibility(GONE);
    }

    public void showPlaceHolder() {
        profilePictureView.getLayoutParams().width = placeholderBackgroundSize;
        profilePictureView.getLayoutParams().height = placeholderBackgroundSize;
        placeholderView.getLayoutParams().width = placeHolderSize;
        placeholderView.getLayoutParams().height = placeHolderSize;
        ColorDrawable cd = new ColorDrawable(primaryColor);
        profilePictureView.setImageDrawable(cd);
        placeholderView.setVisibility(VISIBLE);
    }

    public void isPulsationAnimationShowing(boolean show) {
        if (show) {
            pulsationAnimation.playAnimation();
            pulsationAnimation.setVisibility(VISIBLE);
        } else {
            pulsationAnimation.cancelAnimation();
            pulsationAnimation.setVisibility(GONE);
        }
    }
}
