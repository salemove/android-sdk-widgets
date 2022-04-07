package com.glia.widgets.survey;

import static java.util.Arrays.asList;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
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

    private final List<QuestionItem> list = new ArrayList<>();
    private final SurveyAdapterListener listener;

    public SurveyAdapter(SurveyAdapterListener listener) {
        this.listener = listener;
    }

    public void submitList(List<QuestionItem> items) {
        list.clear();
        list.addAll(items);
    }

    public QuestionItem getItem(int position) {
        return list.get(position);
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == SURVEY_SCALE) {
            view = createNewLayout(parent, R.layout.survey_scale_item);
            return new SurveyScaleViewHolder(view);
        } else if (viewType == SURVEY_YES_NO) {
            view = createNewLayout(parent, R.layout.survey_yes_no_item);
            return new SurveyYesNoViewHolder(view);
        } else if (viewType == SURVEY_SINGLE_CHOICE) {
            view = createNewLayout(parent, R.layout.survey_single_choice_item);
            return new SurveySingleChoiceViewHolder(view);
        } else {
            view = createNewLayout(parent, R.layout.survey_open_text_item);
            return new SurveyOpenTextViewHolder(view);
        }
    }

    private static View createNewLayout(ViewGroup parent, @LayoutRes int layoutId) {
        return LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {

        QuestionItem questionItem = getItem(position);
        Survey.Question question = questionItem.getQuestion();
        Survey.Answer answer = questionItem.getAnswer();

        ((SurveyViewHolder) viewHolder).onBind(questionItem, question, answer, listener);
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
        return list.size();
    }

    abstract static class SurveyViewHolder extends RecyclerView.ViewHolder implements SurveyController.AnswerCallback {
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
                    Survey.Question question,
                    Survey.Answer answer,
                    SurveyAdapterListener listener) {
            this.questionItem = questionItem;
            this.listener = listener;
            this.questionItem.setAnswerCallback(this);
            setItemTitle(question.getText(), question.isRequired());
            showRequiredError(questionItem.isShowError());
        }

        void applyAnswer(@Nullable Survey.Answer answer) {}

        private void setItemTitle(String title, boolean require) {
            if (require) {
                Context context = titleTextView.getContext();
                int color = ContextCompat.getColor(context, R.color.glia_system_negative_color);
                String colorString = String.format("%X", color).substring(2);
                String source = context.getString(R.string.glia_survey_require_label, title, colorString);
                titleTextView.setText(Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY));
            } else {
                titleTextView.setText(title);
            }
        }

        void showRequiredError(boolean error) {
            if (error) {
                requiredError.setVisibility(View.VISIBLE);
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

    public static class SurveyScaleViewHolder extends SurveyViewHolder {
        List<GliaSurveyOptionButton> buttons;

        public SurveyScaleViewHolder(@NonNull View itemView) {
            super(itemView);
            buttons = asList(
                    itemView.findViewById(R.id.scale_1_button),
                    itemView.findViewById(R.id.scale_2_button),
                    itemView.findViewById(R.id.scale_3_button),
                    itemView.findViewById(R.id.scale_4_button),
                    itemView.findViewById(R.id.scale_5_button));
        }

        @Override
        void onBind(QuestionItem questionItem,
                    Survey.Question question,
                    Survey.Answer answer,
                    SurveyAdapterListener listener) {
            super.onBind(questionItem, question, answer, listener);
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

    public static class SurveyYesNoViewHolder extends SurveyViewHolder {
        GliaSurveyOptionButton yesButton;
        GliaSurveyOptionButton noButton;

        public SurveyYesNoViewHolder(@NonNull View itemView) {
            super(itemView);
            yesButton = itemView.findViewById(R.id.yes_button);
            noButton = itemView.findViewById(R.id.no_button);

            yesButton.setOnClickListener(view -> setAnswer(true));
            noButton.setOnClickListener(view -> setAnswer(false));
        }

        @Override
        void onBind(QuestionItem questionItem,
                    Survey.Question question,
                    Survey.Answer answer,
                    SurveyAdapterListener listener) {
            super.onBind(questionItem, question, answer, listener);
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

    public static class SurveySingleChoiceViewHolder extends SurveyViewHolder {
        LinearLayout containerView;
        RadioGroup radioGroup;

        public SurveySingleChoiceViewHolder(@NonNull View itemView) {
            super(itemView);
            containerView = itemView.findViewById(R.id.single_choice_view);
            radioGroup = itemView.findViewById(R.id.radio_group);

            int bkgColor = ContextCompat.getColor(itemView.getContext(), R.color.glia_base_light_color);
            containerView.setBackgroundColor(bkgColor);
            requiredError = itemView.findViewById(R.id.required_error);
        }

        @Override
        void onBind(QuestionItem questionItem,
                    Survey.Question question,
                    Survey.Answer answer,
                    SurveyAdapterListener listener) {
            super.onBind(questionItem, question, answer, listener);
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

                RadioButton radioButton = new RadioButton(itemView.getContext());
                radioButton.setId(View.generateViewId());
                radioButton.setText(option.getLabel());
                radioButton.setChecked(option.getId().equals(selectedId));
                radioButton.setOnClickListener(v -> setAnswer(option.getId()));
                ColorStateList colorStateList = getRadioButtonColor();
                radioButton.setButtonTintList(colorStateList);
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
                                    R.color.glia_base_shade_color), //disabled
                            ContextCompat.getColor(containerView.getContext(),
                                    R.color.glia_brand_primary_color) //enabled
                    }
            );
        }
    }

    public static class SurveyOpenTextViewHolder extends SurveyViewHolder {
        EditText comment;
        View requiredError;

        public SurveyOpenTextViewHolder(@NonNull View itemView) {
            super(itemView);
            comment = itemView.findViewById(R.id.et_comment);
            requiredError = itemView.findViewById(R.id.required_error);

            comment.setOnFocusChangeListener((v, hasFocus) -> {
                setAnswer(comment.getText().toString());
            });

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

        @Override
        void onBind(QuestionItem questionItem,
                    Survey.Question question,
                    Survey.Answer answer,
                    SurveyAdapterListener listener) {
            super.onBind(questionItem, question, answer, listener);
            applyAnswer(answer);
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

            GradientDrawable shape = (GradientDrawable) ContextCompat.getDrawable(context, R.drawable.bg_survey_edit_text);
            if (shape != null) {
                // TODO: get colors from theme
                ColorStateList strokeColor =
                        error ? ContextCompat.getColorStateList(context, R.color.glia_system_negative_color) :
                                ContextCompat.getColorStateList(context, R.color.glia_base_shade_color);
                int width = (int) Math.round(context.getResources().getDimension(R.dimen.glia_px));
                shape.setStroke(width, strokeColor);
                comment.setBackground(shape);
            }
        }
    }
}
