package com.glia.widgets.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SingleChoiceCardView extends FrameLayout {

    private final MaterialCardView materialCardView;
    private final ImageView imageView;
    private final ConstraintLayout layout;
    private final TextView contentView;
    private OnOptionClickedListener onOptionClickedListener;
    private OnImageLoadedListener onImageLoadedListener;

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
            Integer selectedIndex,
            UiTheme theme,
            int adapterPosition,
            int messagePosition,
            OnImageLoadedListener onImageLoadedListener
    ) {
        this.onImageLoadedListener = onImageLoadedListener;

        int gliaBaseDarkColor = ContextCompat.getColor(
                this.getContext(), theme.getBaseDarkColor()
        );
        int gliaBaseLightColor = ContextCompat.getColor(
                this.getContext(), theme.getBaseLightColor()
        );
        int gliaBrandPrimaryColor = ContextCompat.getColor(
                this.getContext(), theme.getBrandPrimaryColor()
        );
        ColorStateList systemAgentBubbleColorStateList =
                ContextCompat.getColorStateList(this.getContext(), theme.getSystemAgentBubbleColor());
        ColorStateList brandPrimaryColorStateList =
                ContextCompat.getColorStateList(this.getContext(), theme.getBrandPrimaryColor());

        materialCardView.setStrokeColor(gliaBrandPrimaryColor);
        materialCardView.setBackgroundColor(gliaBaseLightColor);

        if (imageUrl != null) {
            Picasso.with(this.getContext()).load(imageUrl).into(imageView, new Callback() {
                @Override
                public void onSuccess() {
                    if (SingleChoiceCardView.this.onImageLoadedListener != null) {
                        onImageLoadedListener.onLoaded();
                    }
                }

                @Override
                public void onError() {
                    // do nothing
                }
            });
        }
        imageView.setVisibility(imageUrl != null ? VISIBLE : GONE);
        contentView.setTextColor(gliaBaseDarkColor);

        contentView.setText(content);
        ConstraintSet constraintSet = new ConstraintSet();
        int topViewId = R.id.content_view;
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
            params.setMargins(
                    0,
                    Float.valueOf(getResources().getDimension(R.dimen.medium)).intValue(),
                    0,
                    0
            );
            button.setLayoutParams(params);
            button.setId(View.generateViewId());
            button.setText(option.getText());
            button.setEnabled(selectedIndex == null);
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

            button.setBackgroundTintList(
                    selectedIndex != null && selectedIndex == index ?
                            brandPrimaryColorStateList :
                            systemAgentBubbleColorStateList);
            button.setTextColor(selectedIndex != null && selectedIndex == index ?
                    gliaBaseLightColor :
                    gliaBaseDarkColor);
            if (theme.getFontRes() != null) {
                button.setTypeface(
                        ResourcesCompat.getFont(
                                this.getContext(),
                                theme.getFontRes())
                );
            }
            if (onOptionClickedListener != null) {
                final int optionIndex = index;
                button.setOnClickListener(v -> {
                    if (onOptionClickedListener != null) {
                        onOptionClickedListener.onClicked(
                                id,
                                adapterPosition,
                                messagePosition,
                                optionIndex
                        );
                    }
                });
            }
            constraintSet.applyTo(layout);
        }
        if (theme.getFontRes() != null) {
            contentView.setTypeface(
                    ResourcesCompat.getFont(
                            this.getContext(),
                            theme.getFontRes())
            );
        }
    }

    public void setOnOptionClickedListener(OnOptionClickedListener onOptionClickedListener) {
        this.onOptionClickedListener = onOptionClickedListener;
    }

    public interface OnOptionClickedListener {
        void onClicked(
                String id,
                int indexInList,
                int indexOfMessage,
                int indexOfOption
        );
    }

    public interface OnImageLoadedListener {
        void onLoaded();
    }

    @Override
    protected void onDetachedFromWindow() {
        onImageLoadedListener = null;
        onOptionClickedListener = null;
        super.onDetachedFromWindow();
    }
}
