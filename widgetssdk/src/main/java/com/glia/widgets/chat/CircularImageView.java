package com.glia.widgets.chat;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewOutlineProvider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.glia.widgets.R;

class CircularImageView extends AppCompatImageView {

    public CircularImageView(@NonNull Context context) {
        this(context, null);
    }

    public CircularImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircularImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        setClipToOutline(true);
        setBackgroundResource(R.drawable.bg_circle);
        setScaleType(ScaleType.CENTER_CROP);
    }
}
