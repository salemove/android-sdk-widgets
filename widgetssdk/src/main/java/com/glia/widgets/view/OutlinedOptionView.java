package com.glia.widgets.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.google.android.material.card.MaterialCardView;

/**
 * @hide
 */
public class OutlinedOptionView extends FrameLayout {

    private final MaterialCardView cardView;
    private final ImageView iconView;
    private final TextView titleView;
    private final TextView captionView;

    public OutlinedOptionView(@NonNull Context context) {
        this(context, null);
    }

    public OutlinedOptionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OutlinedOptionView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setClickable(true);
        setFocusable(true);
        addRippleEffect();
        View view = inflate(context, R.layout.outlined_option_view, this);
        cardView = view.findViewById(R.id.card_view);
        iconView = view.findViewById(R.id.icon_view);
        titleView = view.findViewById(R.id.title_view);
        captionView = view.findViewById(R.id.caption_view);
        readTypedArray(attrs);
    }

    private void readTypedArray(@Nullable AttributeSet attrs) {
        TypedArray typedArray = this.getContext().obtainStyledAttributes(attrs, R.styleable.OutlinedOptionView);
        if (typedArray.hasValue(R.styleable.OutlinedOptionView_icon)) {
            int icon = typedArray.getResourceId(R.styleable.OutlinedOptionView_icon, 0);
            iconView.setImageResource(icon);
        }
        if (typedArray.hasValue(R.styleable.OutlinedOptionView_title)) {
            String title = typedArray.getString(R.styleable.OutlinedOptionView_title);
            titleView.setText(title);
        }
        if (typedArray.hasValue(R.styleable.OutlinedOptionView_caption)) {
            String caption = typedArray.getString(R.styleable.OutlinedOptionView_caption);
            captionView.setText(caption);
        }
        typedArray.recycle();
    }

    private void addRippleEffect() {
        TypedValue outValue = new TypedValue();
        this.getContext().getTheme().resolveAttribute(
            android.R.attr.selectableItemBackground, outValue, true);
        setForeground(ContextCompat.getDrawable(this.getContext(), outValue.resourceId));
    }

    public void setTheme(UiTheme theme) {
        cardView.setStrokeColor(ContextCompat.getColor(this.getContext(), theme.getBrandPrimaryColor()));
        cardView.setBackgroundTintList(ContextCompat.getColorStateList(this.getContext(), theme.getBaseLightColor()));
        iconView.setImageTintList(ContextCompat.getColorStateList(this.getContext(), theme.getBrandPrimaryColor()));
        titleView.setTextColor(ContextCompat.getColor(this.getContext(), theme.getBaseDarkColor()));
        captionView.setTextColor(ContextCompat.getColor(this.getContext(), theme.getBaseDarkColor()));
    }
}
