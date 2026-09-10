package com.glia.widgets.view.textview;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.glia.widgets.R;

/**
 * @hide
 */
public class ChoiceCardContentTextView extends BaseConfigurableTextView {
    public ChoiceCardContentTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, R.attr.choiceCardContentTextStyle, R.style.Application_Glia_ChoiceCard_ContentText);
    }
}
