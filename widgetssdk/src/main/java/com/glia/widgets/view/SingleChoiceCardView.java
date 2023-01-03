package com.glia.widgets.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.glia.androidsdk.chat.SingleChoiceOption;
import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.helper.Utils;
import com.glia.widgets.view.textview.ChoiceCardContentTextView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SingleChoiceCardView extends FrameLayout {

    private final MaterialCardView materialCardView;
    private final ImageView imageView;
    private final ConstraintLayout layout;
    private final ChoiceCardContentTextView contentView;
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
        imageView = findViewById(R.id.image);
        layout = findViewById(R.id.layout);
        contentView = findViewById(R.id.content_view);
    }

    public void setData(
            String id,
            String imageUrl,
            String content,
            List<SingleChoiceOption> options,
            UiTheme theme,
            int adapterPosition) {
        setupCardView(theme);
        setupImage(imageUrl);
        setupText(content, theme);
        setupButtons(id, options, theme, adapterPosition);
    }

    public void setOnOptionClickedListener(OnOptionClickedListener onOptionClickedListener) {
        this.onOptionClickedListener = onOptionClickedListener;
    }

    public interface OnOptionClickedListener {
        void onClicked(String id, int indexInList, int indexOfOption);
    }

    private void setupCardView(UiTheme theme) {
        int gliaBaseLightColor = ContextCompat.getColor(this.getContext(), theme.getBaseLightColor());
        int gliaBrandPrimaryColor = ContextCompat.getColor(this.getContext(), theme.getBrandPrimaryColor());
        materialCardView.setStrokeColor(gliaBrandPrimaryColor);
        materialCardView.setBackgroundColor(gliaBaseLightColor);
    }

    private void setupImage(String imageUrl) {
        imageView.setVisibility(imageUrl != null ? VISIBLE : GONE);
        Picasso.get().load(imageUrl).into(imageView);
    }

    private void setupText(String content, UiTheme theme) {
        contentView.setText(content);
        contentView.setTheme(theme);
    }

    private void setupButtons(String id, List<SingleChoiceOption> options, UiTheme theme, int adapterPosition) {
        ConstraintSet constraintSet = new ConstraintSet();
        int topViewId = R.id.content_view;

        int horizontalMargin = getResources().getDimensionPixelOffset(R.dimen.glia_large);
        int topMargin = getResources().getDimensionPixelOffset(R.dimen.glia_medium);
        for (int index = 0; index < options.size(); index++) {
            SingleChoiceOption option = options.get(index);
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
            params.setMargins(horizontalMargin, topMargin, horizontalMargin, 0);
            button.setLayoutParams(params);
            button.setId(View.generateViewId());
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

            ColorStateList actionButtonBackgroundColor =
                    ContextCompat.getColorStateList(this.getContext(), theme.getBotActionButtonBackgroundColor());

            ColorStateList actionButtonTextColor =
                    ContextCompat.getColorStateList(this.getContext(), theme.getBotActionButtonTextColor());

            button.setBackgroundTintList(actionButtonBackgroundColor);
            button.setTextColor(actionButtonTextColor);

            if (theme.getFontRes() != null) {
                button.setTypeface(ResourcesCompat.getFont(this.getContext(), theme.getFontRes())
                );
            }

            if (onOptionClickedListener != null) {
                final int optionIndex = index;
                button.setOnClickListener(v -> {
                    if (onOptionClickedListener != null) {
                        onOptionClickedListener.onClicked(id, adapterPosition, optionIndex);
                    }
                });
            }
            constraintSet.applyTo(layout);
        }
    }
}
