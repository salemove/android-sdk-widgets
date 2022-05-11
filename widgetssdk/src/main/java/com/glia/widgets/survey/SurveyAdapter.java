package com.glia.widgets.survey;

import static java.util.Arrays.asList;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.glia.androidsdk.engagement.Survey;
import com.glia.widgets.R;
import com.glia.widgets.view.button.GliaSurveyOptionButton;
import com.glia.widgets.view.configuration.OptionButtonConfiguration;
import com.glia.widgets.view.configuration.TextConfiguration;
import com.glia.widgets.view.configuration.survey.BooleanQuestionConfiguration;
import com.glia.widgets.view.configuration.survey.InputQuestionConfiguration;
import com.glia.widgets.view.configuration.survey.SurveyStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SurveyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    interface SurveyAdapterListener {
        void onAnswer(@NonNull Survey.Answer answer);
    }

    private static final int SURVEY_SCALE = 1;
    private static final int SURVEY_YES_NO = 2;
    private static final int SURVEY_SINGLE_CHOICE = 3;
    private static final int SURVEY_OPEN_TEXT = 4;

    private SurveyStyle style;
    private final List<QuestionItem> questionItems = new ArrayList<>();
    private final SurveyAdapterListener listener;

    public SurveyAdapter(SurveyAdapterListener listener) {
        this.listener = listener;
    }

    public void submitList(@Nullable List<QuestionItem> items) {
        questionItems.clear();
        if (items == null) {
            return;
        }
        questionItems.addAll(items);
    }

    public QuestionItem getItem(int position) {
        return questionItems.get(position);
    }

    public void setStyle(SurveyStyle style) {
        this.style = style;
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == SURVEY_SCALE) {
            view = createNewLayout(parent, R.layout.survey_scale_question_item);
            return new ScaleQuestionViewHolder(view, style);
        } else if (viewType == SURVEY_YES_NO) {
            view = createNewLayout(parent, R.layout.survey_boolean_question_item);
            return new BooleanQuestionViewHolder(view, style);
        } else if (viewType == SURVEY_SINGLE_CHOICE) {
            view = createNewLayout(parent, R.layout.survey_single_question_item);
            return new SingleQuestionViewHolder(view, style);
        } else {
            view = createNewLayout(parent, R.layout.survey_input_question_item);
            return new InputQuestionViewHolder(view, style);
        }
    }

    private static View createNewLayout(ViewGroup parent, @LayoutRes int layoutId) {
        return LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {

        QuestionItem questionItem = getItem(position);

        ((SurveyViewHolder) viewHolder).onBind(questionItem, listener);
    }

    @Override
    public int getItemViewType(int position) {
        switch (getItem(position).getQuestion().getType()) {
            case TEXT:
                return SURVEY_OPEN_TEXT;
            case BOOLEAN:
                return SURVEY_YES_NO;
            case SINGLE_CHOICE:
                return SURVEY_SINGLE_CHOICE;
            default:
                return SURVEY_SCALE;
        }
    }

    @Override
    public int getItemCount() {
        return questionItems.size();
    }

    abstract static class SurveyViewHolder extends RecyclerView.ViewHolder
            implements SurveyController.AnswerCallback {
        TextView titleTextView;
        View requiredError;
        QuestionItem questionItem;
        SurveyAdapterListener listener;

        public SurveyViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tv_title);
            requiredError = itemView.findViewById(R.id.required_error);
        }

        void onBind(QuestionItem questionItem,
                    SurveyAdapterListener listener) {
            this.questionItem = questionItem;
            this.listener = listener;
            this.questionItem.setAnswerCallback(this);
            setItemTitle(questionItem.getQuestion());
            showRequiredError(questionItem.isShowError());
        }

        void applyAnswer(@Nullable Survey.Answer answer) {
        }

        private void setItemTitle(Survey.Question question) {
            String questionText = question.getText();
            if (question.isRequired()) {
                Context context = titleTextView.getContext();
                int color = ContextCompat.getColor(context, R.color.glia_system_negative_color);
                String colorString = String.format("%X", color).substring(2);
                String source = context.getString(
                        R.string.glia_survey_require_label, questionText, colorString);
                titleTextView.setText(Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY));
                titleTextView.setContentDescription(
                        context.getString(
                                R.string.glia_survey_require_label_content_description,
                                questionText
                        )
                );
            } else {
                titleTextView.setText(questionText);
                titleTextView.setContentDescription(null);
            }
        }

        void showRequiredError(boolean error) {
            if (error) {
                requiredError.setVisibility(View.VISIBLE);
                itemView.announceForAccessibility(requiredError.getContext()
                        .getString(R.string.glia_survey_required_error_message));
            } else {
                requiredError.setVisibility(View.GONE);
            }
        }

        @Override
        public void answerCallback(boolean showError) {
            showRequiredError(showError);
        }

        void setAnswer(int response) {
            onAnswer(Survey.Answer.makeAnswer(questionItem.getQuestion().getId(), response));
        }

        void setAnswer(boolean response) {
            onAnswer(Survey.Answer.makeAnswer(questionItem.getQuestion().getId(), response));
        }

        void setAnswer(String response) {
            onAnswer(Survey.Answer.makeAnswer(questionItem.getQuestion().getId(), response));
        }

        private void onAnswer(Survey.Answer answer) {
            applyAnswer(answer);
            listener.onAnswer(answer);
        }
    }

    public static class ScaleQuestionViewHolder extends SurveyViewHolder {
        TextView title;
        List<GliaSurveyOptionButton> buttons;
        SurveyStyle style;

        public ScaleQuestionViewHolder(@NonNull View itemView, SurveyStyle style) {
            super(itemView);
            this.style = style;

            title = itemView.findViewById(R.id.tv_title);
            TextConfiguration titleConfiguration = style.getScaleQuestion().getTitle();
            this.title.setTextColor(titleConfiguration.getTextColor());
            float textSize = titleConfiguration.getTextSize();
            this.title.setTextSize(textSize);
            if (titleConfiguration.isBold()) this.title.setTypeface(Typeface.DEFAULT_BOLD);

            buttons = asList(
                    itemView.findViewById(R.id.scale_1_button),
                    itemView.findViewById(R.id.scale_2_button),
                    itemView.findViewById(R.id.scale_3_button),
                    itemView.findViewById(R.id.scale_4_button),
                    itemView.findViewById(R.id.scale_5_button));
            buttons.forEach(button -> button.setStyle(style.getScaleQuestion().getOptionButton()));
        }

        @Override
        void onBind(QuestionItem questionItem,
                    SurveyAdapterListener listener) {
            super.onBind(questionItem, listener);
            applyAnswer(questionItem.getAnswer());
            buttons.forEach(button -> button.setOnClickListener(view ->
                    setAnswer(buttons.indexOf(button) + 1)
            ));
        }

        @Override
        void applyAnswer(@Nullable Survey.Answer answer) {
            if (answer != null) {
                int value = answer.getResponse();
                setSelected(value);
            } else {
                unselectAll();
            }
        }

        void setSelected(int value) {
            for (int i = 0; i < buttons.size(); i++) {
                Button button = buttons.get(i);
                boolean isSelected = (i + 1) == value;

                button.setSelected(isSelected);
            }
        }

        void unselectAll() {
            setSelected(0);
        }

        @Override
        void showRequiredError(boolean error) {
            super.showRequiredError(error);

            buttons.forEach(button -> button.setError(error));
        }
    }

    public static class BooleanQuestionViewHolder extends SurveyViewHolder {
        TextView title;
        GliaSurveyOptionButton yesButton;
        GliaSurveyOptionButton noButton;
        SurveyStyle style;

        public BooleanQuestionViewHolder(@NonNull View itemView, SurveyStyle style) {
            super(itemView);
            this.style = style;

            BooleanQuestionConfiguration questionStyle = style.getBooleanQuestion();

            title = itemView.findViewById(R.id.tv_title);
            TextConfiguration titleConfiguration = questionStyle.getTitle();
            this.title.setTextColor(titleConfiguration.getTextColor());
            float textSize = titleConfiguration.getTextSize();
            this.title.setTextSize(textSize);
            if (titleConfiguration.isBold()) this.title.setTypeface(Typeface.DEFAULT_BOLD);

            OptionButtonConfiguration buttonConfiguration = questionStyle.getOptionButton();

            yesButton = itemView.findViewById(R.id.yes_button);
            yesButton.setStyle(buttonConfiguration);
            yesButton.setOnClickListener(view -> setAnswer(true));

            noButton = itemView.findViewById(R.id.no_button);
            noButton.setStyle(buttonConfiguration);
            noButton.setOnClickListener(view -> setAnswer(false));
        }

        @Override
        void onBind(QuestionItem questionItem,
                    SurveyAdapterListener listener) {
            super.onBind(questionItem, listener);
            applyAnswer(questionItem.getAnswer());
        }

        @Override
        void applyAnswer(@Nullable Survey.Answer answer) {
            if (answer != null) {
                boolean value = answer.getResponse();
                setSelected(value);
            } else {
                unselectAll();
            }
        }

        void setSelected(boolean value) {
            yesButton.setSelected(value);
            noButton.setSelected(!value);
        }

        void unselectAll() {
            yesButton.setSelected(false);
            noButton.setSelected(false);
        }

        @Override
        void showRequiredError(boolean error) {
            super.showRequiredError(error);

            yesButton.setError(error);
            noButton.setError(error);
        }
    }

    public static class SingleQuestionViewHolder extends SurveyViewHolder {
        LinearLayout containerView;
        TextView title;
        RadioGroup radioGroup;
        SurveyStyle style;

        public SingleQuestionViewHolder(@NonNull View itemView, SurveyStyle style) {
            super(itemView);
            this.style = style;

            containerView = itemView.findViewById(R.id.single_choice_view);
            title = itemView.findViewById(R.id.tv_title);
            radioGroup = itemView.findViewById(R.id.radio_group);
            requiredError = itemView.findViewById(R.id.required_error);

            TextConfiguration titleConfiguration = style.getSingleQuestion().getTitle();
            this.title.setTextColor(titleConfiguration.getTextColor());
            float textSize = titleConfiguration.getTextSize();
            this.title.setTextSize(textSize);
            if (titleConfiguration.isBold()) this.title.setTypeface(Typeface.DEFAULT_BOLD);
        }

        @Override
        void onBind(QuestionItem questionItem,
                    SurveyAdapterListener listener) {
            super.onBind(questionItem, listener);
            singleChoice(questionItem);
        }

        void singleChoice(QuestionItem item) {
            String selectedId = Optional.ofNullable(item.getAnswer())
                    .map(answer -> (String) answer.getResponse())
                    .orElse(null);
            List<Survey.Question.Option> options = item.getQuestion().getOptions();
            if (options == null) {
                return;
            }

            radioGroup.removeAllViews();
            for (int i = 0; i < options.size(); i++) {
                Survey.Question.Option option = options.get(i);

                Context context = itemView.getContext();
                RadioButton radioButton = new RadioButton(context);
                radioButton.setId(View.generateViewId());
                radioButton.setText(option.getLabel());
                radioButton.setTextColor(style.getSingleQuestion().getTitle().getTextColor());
                float textSize = style.getSingleQuestion().getOptionText().getTextSize();
                radioButton.setTextSize(textSize);
                radioButton.setChecked(option.getId().equals(selectedId));
                radioButton.setOnClickListener(v -> setAnswer(option.getId()));
                LayerDrawable drawable = (LayerDrawable) ContextCompat.getDrawable(context, R.drawable.bg_survey_radio_button);
                if (drawable != null) {
                    // Set color for the center dot
                    Drawable centerDot = drawable.findDrawableByLayerId(R.id.center_item);
                    ColorStateList colorStateList = getRadioButtonColor();
                    centerDot.setTintList(colorStateList);

                    // Set color for the border
                    GradientDrawable border = (GradientDrawable) drawable.findDrawableByLayerId(R.id.border_item);
                    ColorStateList strokeColor = ContextCompat.getColorStateList(context, R.color.glia_base_shade_color);
                    int width = context.getResources().getDimensionPixelSize(R.dimen.glia_px);
                    border.setStroke(width, strokeColor);
                }
                radioButton.setButtonDrawable(drawable);
                int start = context.getResources().getDimensionPixelSize(R.dimen.glia_medium);
                boolean isRtl = context.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
                radioButton.setPadding(isRtl ? 0 : start, 0, isRtl ? start : 0, 0);
                int height = context.getResources().getDimensionPixelSize(R.dimen.glia_survey_radio_button_height);
                radioButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
                radioGroup.addView(radioButton);
            }
        }

        @NonNull
        private ColorStateList getRadioButtonColor() {
            return new ColorStateList(
                    new int[][]{
                            new int[]{-android.R.attr.state_checked},
                            new int[]{android.R.attr.state_checked}
                    },
                    new int[]{
                            ContextCompat.getColor(containerView.getContext(),
                                    android.R.color.transparent), //disabled
                            ContextCompat.getColor(containerView.getContext(),
                                    R.color.glia_brand_primary_color) //enabled
                    }
            );
        }
    }

    public static class InputQuestionViewHolder extends SurveyViewHolder {
        TextView title;
        EditText comment;
        View requiredError;
        SurveyStyle style;

        public InputQuestionViewHolder(@NonNull View itemView, SurveyStyle style) {
            super(itemView);
            this.style = style;
            title = itemView.findViewById(R.id.tv_title);
            comment = itemView.findViewById(R.id.et_comment);
            requiredError = itemView.findViewById(R.id.required_error);

            InputQuestionConfiguration inputQuestionConfig = style.getInputQuestion();
            setupTitle(inputQuestionConfig);
            setupInputBoxText(inputQuestionConfig.getOptionButton());
        }

        @Override
        void onBind(QuestionItem questionItem,
                    SurveyAdapterListener listener) {
            super.onBind(questionItem, listener);
            applyAnswer(questionItem.getAnswer());
        }

        @Override
        void applyAnswer(@Nullable Survey.Answer answer) {
            if (answer != null) {
                String oldValue = comment.getText().toString();
                String newValue = answer.getResponse();
                if (!oldValue.equals(newValue)) {
                    comment.setText(newValue);
                }
            } else {
                comment.setText(null);
            }
        }

        @Override
        void showRequiredError(boolean error) {
            super.showRequiredError(error);

            Context context = comment.getContext();

            GradientDrawable shape = (GradientDrawable) ContextCompat.getDrawable(
                    context,
                    R.drawable.bg_survey_edit_text);
            if (shape != null) {
                InputQuestionConfiguration inputQuestionConfig = style.getInputQuestion();
                String errorColorString = inputQuestionConfig.getOptionButton().getHighlightedLayer().getBorderColor();
                int errorColor = Color.parseColor(errorColorString);
                String normalColorString = inputQuestionConfig.getOptionButton().getNormalLayer().getBorderColor();
                int normalColor = Color.parseColor(normalColorString);
                ColorStateList strokeColor =
                        error ? ColorStateList.valueOf(errorColor) :
                                ColorStateList.valueOf(normalColor);
                int width = context.getResources().getDimensionPixelSize(R.dimen.glia_px);
                shape.setStroke(width, strokeColor);
                shape.setColor(Color.parseColor(inputQuestionConfig.getOptionButton().getNormalLayer().getBackgroundColor()));
                comment.setBackground(shape);
            }
        }

        private void setupTitle(InputQuestionConfiguration inputQuestionConfig) {
            TextConfiguration titleConfiguration = inputQuestionConfig.getTitle();
            this.title.setTextColor(titleConfiguration.getTextColor());
            float textSize = titleConfiguration.getTextSize();
            this.title.setTextSize(textSize);
            if (titleConfiguration.isBold()) this.title.setTypeface(Typeface.DEFAULT_BOLD);
        }

        private void setupInputBoxText(OptionButtonConfiguration optionButtonStyle) {
            comment.setTextColor(optionButtonStyle.getNormalText().getTextColor());
            if (optionButtonStyle.getNormalText().isBold()) this.comment.setTypeface(Typeface.DEFAULT_BOLD);
            comment.setHintTextColor(ContextCompat.getColor(comment.getContext(), R.color.glia_base_shade_color));
            float textSize = optionButtonStyle.getNormalText().getTextSize();
            comment.setTextSize(textSize);
            comment.setOnFocusChangeListener((v, hasFocus) -> setAnswer(comment.getText().toString()));
            comment.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    setAnswer(s.toString());
                }
            });
        }
    }
}
