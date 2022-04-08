package com.glia.widgets.survey;

import static java.util.Arrays.asList;

import android.content.Context;
import android.content.res.ColorStateList;
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
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.glia.androidsdk.engagement.Survey;
import com.glia.widgets.R;

import java.util.List;
import java.util.Optional;

public class SurveyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    interface SurveyAdapterListener {
        void onAnswer(@NonNull Survey.Answer answer);
    }

    private final AsyncListDiffer<QuestionItem> differ = new AsyncListDiffer<>(this, DIFF_CALLBACK);

    public static final DiffUtil.ItemCallback<QuestionItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<QuestionItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull QuestionItem oldItem, @NonNull QuestionItem newItem) {
            return oldItem.getQuestion().getId().equals(newItem.getQuestion().getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull QuestionItem oldItem, @NonNull QuestionItem newItem) {
            return oldItem.equals(newItem);
        }
    };

    private static final int SURVEY_SCALE = 1;
    private static final int SURVEY_YES_NO = 2;
    private static final int SURVEY_SINGLE_CHOICE = 3;
    private static final int SURVEY_OPEN_TEXT = 4;

    private final SurveyAdapterListener listener;

    public SurveyAdapter(SurveyAdapterListener listener) {
        this.listener = listener;
    }

    public void submitList(List<QuestionItem> items) {
        differ.submitList(items);
    }

    public QuestionItem getItem(int position) {
        return differ.getCurrentList().get(position);
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

        if (getItemViewType(position) == SURVEY_SCALE) {
            bindScale(questionItem, question, (SurveyScaleViewHolder) viewHolder);
        } else if (getItemViewType(position) == SURVEY_SINGLE_CHOICE) {
            bindSingle(questionItem, question, (SurveySingleChoiceViewHolder) viewHolder);
        } else if (getItemViewType(position) == SURVEY_YES_NO) {
            bindYesNo(questionItem, question, (SurveyYesNoViewHolder) viewHolder);
        } else if (getItemViewType(position) == SURVEY_OPEN_TEXT) {
            bindOpenText(questionItem, question, (SurveyOpenTextViewHolder) viewHolder);
        }
    }

    private void bindScale(QuestionItem questionItem,
                           Survey.Question question,
                           SurveyScaleViewHolder scaleViewHolder) {
        setItemTitle(scaleViewHolder.titleTextView, question.getText(), question.isRequired());
        scaleViewHolder.setAnswer(questionItem.getAnswer());
        scaleViewHolder.listener = value -> {
            setAnswer(questionItem, value);
            scaleViewHolder.setSelected(value);
        };
        scaleViewHolder.showRequiredError(questionItem.isShowError());
    }

    private void bindSingle(QuestionItem questionItem,
                            Survey.Question question,
                            SurveySingleChoiceViewHolder singleChoiceViewHolder) {
        setItemTitle(singleChoiceViewHolder.title, question.getText(), question.isRequired());
        singleChoiceViewHolder.singleChoice(questionItem);
        singleChoiceViewHolder.showRequiredError(questionItem.isShowError());
    }

    private void bindYesNo(QuestionItem questionItem,
                           Survey.Question question,
                           SurveyYesNoViewHolder yesNoViewHolder) {
        setItemTitle(yesNoViewHolder.titleTextView, question.getText(), question.isRequired());
        yesNoViewHolder.setAnswer(questionItem.getAnswer());
        yesNoViewHolder.listener = value -> {
            setAnswer(questionItem, value);
            yesNoViewHolder.setSelected(value);
        };
        yesNoViewHolder.showRequiredError(questionItem.isShowError());
    }

    private void bindOpenText(QuestionItem questionItem,
                              Survey.Question question,
                              SurveyOpenTextViewHolder surveyOpenTextViewHolder) {
        setItemTitle(surveyOpenTextViewHolder.titleComment, question.getText(), question.isRequired());
        surveyOpenTextViewHolder.setAnswer(questionItem.getAnswer());
        surveyOpenTextViewHolder.showRequiredError(questionItem.isShowError());
    }

    private void setItemTitle(TextView textView, String title, boolean require) {
        if (require) {
            Context context = textView.getContext();
            int color = ContextCompat.getColor(context, R.color.glia_system_negative_color);
            String colorString = String.format("%X", color).substring(2);
            String source = context.getString(R.string.glia_survey_require_label, title, colorString);
            textView.setText(Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY));
        } else {
            textView.setText(title);
        }
    }

    private void setAnswer(QuestionItem questionItem, int response) {
        Survey.Answer answer = Survey.Answer.makeAnswer(questionItem.getQuestion().getId(), response);
        questionItem.setAnswer(answer);
        onAnswer(answer);
    }

    private void setAnswer(QuestionItem questionItem, boolean response) {
        Survey.Answer answer = Survey.Answer.makeAnswer(questionItem.getQuestion().getId(), response);
        questionItem.setAnswer(answer);
        onAnswer(answer);
    }

    private void setAnswer(QuestionItem questionItem, String response) {
        Survey.Answer answer = Survey.Answer.makeAnswer(questionItem.getQuestion().getId(), response);
        questionItem.setAnswer(answer);
        onAnswer(answer);
    }

    private void onAnswer(Survey.Answer answer) {
        listener.onAnswer(answer);
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
        return differ.getCurrentList().size();
    }

    public static class SurveyScaleViewHolder extends RecyclerView.ViewHolder {

        interface OnSurveyScaleClickListener {
            void onSurveyScaleClickListener(int value);
        }

        TextView titleTextView;
        List<Button> buttons;
        View requiredError;
        OnSurveyScaleClickListener listener;

        public SurveyScaleViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tv_title);
            requiredError = itemView.findViewById(R.id.required_error);
            Button scale1Button = itemView.findViewById(R.id.scale_1_button);
            Button scale2Button = itemView.findViewById(R.id.scale_2_button);
            Button scale3Button = itemView.findViewById(R.id.scale_3_button);
            Button scale4Button = itemView.findViewById(R.id.scale_4_button);
            Button scale5Button = itemView.findViewById(R.id.scale_5_button);
            buttons = asList(scale1Button, scale2Button, scale3Button, scale4Button, scale5Button);

            buttons.forEach(button -> button.setOnClickListener(view -> {
                if (listener != null) {
                    listener.onSurveyScaleClickListener(buttons.indexOf(button) + 1);
                }
            }));
        }

        void setAnswer(@Nullable Survey.Answer answer) {
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

        void showRequiredError(boolean error) {
            if (error) {
                requiredError.setVisibility(View.VISIBLE);
            } else {
                requiredError.setVisibility(View.GONE);
            }
        }
    }

    public static class SurveyYesNoViewHolder extends RecyclerView.ViewHolder {

        interface OnSurveyYesNoClickListener {
            void onSurveyYesNoClickListener(boolean value);
        }

        TextView titleTextView;
        Button yesButton;
        Button noButton;
        View requiredError;
        OnSurveyYesNoClickListener listener;

        public SurveyYesNoViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tv_title);
            yesButton = itemView.findViewById(R.id.yes_button);
            noButton = itemView.findViewById(R.id.no_button);
            requiredError = itemView.findViewById(R.id.required_error);

            yesButton.setOnClickListener(view -> {
                if (listener != null) {
                    listener.onSurveyYesNoClickListener(true);
                }
            });
            noButton.setOnClickListener(view -> {
                if (listener != null) {
                    listener.onSurveyYesNoClickListener(false);
                }
            });
        }

        void setAnswer(@Nullable Survey.Answer answer) {
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

        void showRequiredError(boolean error) {
            if (error) {
                requiredError.setVisibility(View.VISIBLE);
            } else {
                requiredError.setVisibility(View.GONE);
            }
        }
    }

    public class SurveySingleChoiceViewHolder extends RecyclerView.ViewHolder {

        LinearLayout containerView;
        TextView title;
        RadioGroup radioGroup;
        View requiredError;

        public SurveySingleChoiceViewHolder(@NonNull View itemView) {
            super(itemView);
            containerView = itemView.findViewById(R.id.single_choice_view);
            title = itemView.findViewById(R.id.tv_title_choice);
            radioGroup = itemView.findViewById(R.id.radio_group);

            int bkgColor = ContextCompat.getColor(itemView.getContext(), R.color.glia_base_light_color);
            containerView.setBackgroundColor(bkgColor);
            requiredError = itemView.findViewById(R.id.required_error);
        }

        public void singleChoice(QuestionItem item) {
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
                radioButton.setOnClickListener(v -> setAnswer(item, option.getId()));
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

        void showRequiredError(boolean error) {
            if (error) {
                requiredError.setVisibility(View.VISIBLE);
            } else {
                requiredError.setVisibility(View.GONE);
            }
        }
    }

    public class SurveyOpenTextViewHolder extends RecyclerView.ViewHolder {

        TextView titleComment;
        EditText comment;
        View requiredError;

        public SurveyOpenTextViewHolder(@NonNull View itemView) {
            super(itemView);
            titleComment = itemView.findViewById(R.id.tv_title_text);
            comment = itemView.findViewById(R.id.et_comment);
            requiredError = itemView.findViewById(R.id.required_error);

            comment.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    SurveyAdapter.this.setAnswer(getItem(getAdapterPosition()), s.toString());
                }
            });
        }

        void setAnswer(@Nullable Survey.Answer answer) {
            if (answer != null) {
                String value = answer.getResponse();
                comment.setText(value);
            } else {
                comment.setText("");
            }
        }

        void showRequiredError(boolean error) {
            if (error) {
                requiredError.setVisibility(View.VISIBLE);
            } else {
                requiredError.setVisibility(View.GONE);
            }
        }
    }
}
