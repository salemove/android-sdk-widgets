package com.glia.widgets.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;

import com.glia.androidsdk.chat.SingleChoiceAttachment;
import com.glia.androidsdk.chat.SingleChoiceOption;
import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.helper.Utils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class SingleChoiceCardView extends FrameLayout {

    private final MaterialCardView materialCardView;
    private final ConstraintLayout layout;
    private final TextView contentView;
    private OnOptionClickedListener onOptionClickedListener;

    public SingleChoiceCardView(@NonNull Context context) {
        this(context, null);
    }

    public SingleChoiceCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SingleChoiceCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.single_choice_card_view, this);
        materialCardView = findViewById(R.id.card_view);
        layout = findViewById(R.id.layout);
        contentView = findViewById(R.id.content_view);
    }

    public void setData(
            String id,
            String content,
            List<SingleChoiceOption> options,
            UiTheme theme
    ) {
        int gliaBaseDarkColor = ContextCompat.getColor(
                this.getContext(), theme.getBaseDarkColor()
        );
        int gliaBrandPrimaryColor = ContextCompat.getColor(
                this.getContext(), theme.getBrandPrimaryColor()
        );
        ColorStateList systemAgentBubbleColorStateList =
                ContextCompat.getColorStateList(this.getContext(), theme.getSystemAgentBubbleColor());

        materialCardView.setStrokeColor(gliaBrandPrimaryColor);
        contentView.setTextColor(gliaBaseDarkColor);

        contentView.setText(content);
        ConstraintSet constraintSet = new ConstraintSet();
        int topViewId = R.id.content_view;
        for (SingleChoiceOption option : options) {
            MaterialButton button = new MaterialButton(
                    new ContextThemeWrapper(
                            this.getContext(),
                            Utils.getAttrResourceId(
                                    this.getContext(),
                                    R.attr.buttonBarNeutralButtonStyle
                            )
                    ),
                    null,
                    0);
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT
            );
            params.setMargins(
                    0,
                    Float.valueOf(getResources().getDimension(R.dimen.medium)).intValue(),
                    0,
                    0
            );
            button.setLayoutParams(params);
            button.setId(View.generateViewId());
            button.setTag(option.getValue());
            button.setText(option.getText());
            layout.addView(button, layout.getChildCount());
            constraintSet.clone(layout);

            constraintSet.connect(
                    button.getId(),
                    ConstraintSet.TOP,
                    topViewId,
                    ConstraintSet.BOTTOM
            );
            constraintSet.connect(
                    button.getId(),
                    ConstraintSet.START,
                    R.id.start_guideline,
                    ConstraintSet.START
            );
            constraintSet.connect(
                    button.getId(),
                    ConstraintSet.END,
                    R.id.end_guideline,
                    ConstraintSet.END
            );

            topViewId = button.getId();

            button.setBackgroundTintList(systemAgentBubbleColorStateList);
            button.setTextColor(gliaBaseDarkColor);

            if (onOptionClickedListener != null) {
                button.setOnClickListener(v -> {
                    if (onOptionClickedListener != null) {
                        onOptionClickedListener.onClicked(id, option.asSingleChoiceResponse());
                    }
                });
            }
            constraintSet.applyTo(layout);
        }
    }

    public void setOnOptionClickedListener(OnOptionClickedListener onOptionClickedListener) {
        this.onOptionClickedListener = onOptionClickedListener;
    }

    public interface OnOptionClickedListener {
        void onClicked(String id, SingleChoiceAttachment singleChoiceAttachment);
    }
}
