package com.glia.widgets.survey;

import static java.util.Arrays.asList;

import android.content.Context;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.glia.androidsdk.engagement.Survey;
import com.glia.widgets.R;

import java.util.List;

public class SurveyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int SURVEY_SCALE = 1;
    private static final int SURVEY_YES_NO = 2;
    private static final int SURVEY_SINGLE_CHOICE = 3;
    private static final int SURVEY_OPEN_TEXT = 4;

    private List<QuestionItem> items;

    public void setItems(List<QuestionItem> items) {
        this.items = items;
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
            return new SurveySingleChoiceViewHolder(view, this::setAnswer);
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

        QuestionItem questionItem = this.items.get(position);
        Survey.Question question = questionItem.getQuestion();
        Survey.Answer answer = questionItem.getAnswer();

        if (getItemViewType(position) == SURVEY_SCALE) {
            bindScale(questionItem, question, answer, (SurveyScaleViewHolder) viewHolder);
        } else if (getItemViewType(position) == SURVEY_SINGLE_CHOICE) {
            bindSingle(questionItem, question, (SurveySingleChoiceViewHolder) viewHolder);
        } else if (getItemViewType(position) == SURVEY_YES_NO) {
            bindYesNo(questionItem, question, answer, (SurveyYesNoViewHolder) viewHolder);
        } else if (getItemViewType(position) == SURVEY_OPEN_TEXT) {
            bindOpenText(question, answer, (SurveyOpenTextViewHolder) viewHolder);
        }
    }

    private void bindScale(QuestionItem questionItem, Survey.Question question, Survey.Answer answer, SurveyScaleViewHolder scaleViewHolder) {
        setItemTitle(scaleViewHolder.titleTextView, question.getText(), question.isRequired());

        scaleViewHolder.setAnswer(answer);

        scaleViewHolder.listener = value -> {
            setAnswer(questionItem, value);
            scaleViewHolder.setSelected(value);
        };
    }

    private void bindSingle(QuestionItem questionItem, Survey.Question question, SurveySingleChoiceViewHolder singleChoiceViewHolder) {
        setItemTitle(singleChoiceViewHolder.title, question.getText(), question.isRequired());

        singleChoiceViewHolder.singleChoice(question.getOptions(), questionItem);
    }

    private void bindYesNo(QuestionItem questionItem, Survey.Question question, Survey.Answer answer, SurveyYesNoViewHolder yesNoViewHolder) {
        setItemTitle(yesNoViewHolder.titleTextView, question.getText(), question.isRequired());

        yesNoViewHolder.setAnswer(answer);

        yesNoViewHolder.listener = value -> {
            setAnswer(questionItem, value);
            yesNoViewHolder.setSelected(value);
        };
    }

    private void bindOpenText(Survey.Question question, Survey.Answer answer, SurveyOpenTextViewHolder surveyOpenTextViewHolder) {
        setItemTitle(surveyOpenTextViewHolder.titleComment, question.getText(), question.isRequired());
        EditText comment = surveyOpenTextViewHolder.comment;
        if (answer != null) {
            comment.setText(answer.getResponse());
        }
        comment.setImeOptions(EditorInfo.IME_ACTION_DONE);
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
        questionItem.setAnswer(Survey.Answer.makeAnswer(questionItem.getQuestion().getId(), response));
    }

    private void setAnswer(QuestionItem questionItem, boolean response) {
        questionItem.setAnswer(Survey.Answer.makeAnswer(questionItem.getQuestion().getId(), response));
    }

    private void setAnswer(QuestionItem questionItem, String response) {
        questionItem.setAnswer(Survey.Answer.makeAnswer(questionItem.getQuestion().getId(), response));
    }

    @Override
    public int getItemViewType(int position) {
        switch (items.get(position).getQuestion().getType()) {
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
        return items == null ? 0 : items.size();
    }

    public static class SurveyScaleViewHolder extends RecyclerView.ViewHolder {

        interface OnSurveyScaleClickListener {
            void onSurveyScaleClickListener(int value);
        }

        TextView titleTextView;
        List<Button> buttons;
        OnSurveyScaleClickListener listener;

        public SurveyScaleViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tv_title);
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
    }

    public static class SurveyYesNoViewHolder extends RecyclerView.ViewHolder {

        interface OnSurveyYesNoClickListener {
            void onSurveyYesNoClickListener(boolean value);
        }

        TextView titleTextView;
        Button yesButton;
        Button noButton;
        OnSurveyYesNoClickListener listener;

        public SurveyYesNoViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tv_title);
            yesButton = itemView.findViewById(R.id.yes_button);
            noButton = itemView.findViewById(R.id.no_button);

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
    }

    public static class SurveySingleChoiceViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        RecyclerView recyclerView;
        SingleChoiceAdapter.SingleChoiceCallback scaleResultCallback;
        QuestionItem item;

        public SurveySingleChoiceViewHolder(@NonNull View itemView, SingleChoiceAdapter.SingleChoiceCallback scaleResultCallback) {
            super(itemView);
            this.scaleResultCallback = scaleResultCallback;
            title = itemView.findViewById(R.id.tv_title_choice);
            recyclerView = itemView.findViewById(R.id.survey_single_choice_list);
        }

        public void singleChoice(List<Survey.Question.Option> options, QuestionItem item) {
            SingleChoiceAdapter singleChoiceAdapter = new SingleChoiceAdapter(item, scaleResultCallback);
            recyclerView.setAdapter(singleChoiceAdapter);
            this.item = item;
        }
    }

    public class SurveyOpenTextViewHolder extends RecyclerView.ViewHolder {

        TextView titleComment;
        EditText comment;

        public SurveyOpenTextViewHolder(@NonNull View itemView) {
            super(itemView);
            titleComment = itemView.findViewById(R.id.tv_title_text);
            comment = itemView.findViewById(R.id.et_comment);

            comment.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    setAnswer(items.get(getAdapterPosition()), s.toString());
                }
            });
        }
    }
}
