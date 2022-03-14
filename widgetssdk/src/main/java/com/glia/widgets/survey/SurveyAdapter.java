package com.glia.widgets.survey;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
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
            bindScale(position, questionItem, question, answer, (SurveyScaleViewHolder) viewHolder);
        } else if (getItemViewType(position) == SURVEY_SINGLE_CHOICE) {
            bindSingle(position, questionItem, question, (SurveySingleChoiceViewHolder) viewHolder);
        } else if (getItemViewType(position) == SURVEY_YES_NO) {
            bindYesNo(position, questionItem, question, answer, (SurveyYesNoViewHolder) viewHolder);
        } else if (getItemViewType(position) == SURVEY_OPEN_TEXT) {
            bindOpenText(position, question, answer, (SurveyOpenTextViewHolder) viewHolder);
        }
    }

    private void bindScale(int position, QuestionItem questionItem, Survey.Question question, Survey.Answer answer, SurveyScaleViewHolder scaleViewHolder) {
        scaleViewHolder.titleScale.setText(question.getText());

        RatingBar ratingBedGood = scaleViewHolder.ratingBedGood;
        if (answer != null) {
            int response = answer.getResponse();
            ratingBedGood.setRating(response);
        }
        ratingBedGood.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            setAnswer(questionItem, Math.round(rating));
        });
        updateRequiredVisibility(position, scaleViewHolder.tvRequiredScale);
    }

    private void bindSingle(int position, QuestionItem questionItem, Survey.Question question, SurveySingleChoiceViewHolder singleChoiceViewHolder) {
        singleChoiceViewHolder.title.setText(question.getText());
        updateRequiredVisibility(position, singleChoiceViewHolder.tvRequiredChoice);

        singleChoiceViewHolder.singleChoice(question.getOptions(), questionItem);
    }

    private void bindYesNo(int position, QuestionItem questionItem, Survey.Question question, Survey.Answer answer, SurveyYesNoViewHolder yesNoViewHolder) {
        yesNoViewHolder.titleYesNo.setText(question.getText());
        updateRequiredVisibility(position, yesNoViewHolder.tvRequiredYesNo);

        SwitchCompat switchCompat = yesNoViewHolder.switchCompat;
        if (answer != null) {
            switchCompat.setChecked(answer.getResponse());
        }
        switchCompat.setOnCheckedChangeListener(
                (compoundButton, value) -> {
                    setAnswer(questionItem, value);
                });
    }

    private void bindOpenText(int position, Survey.Question question, Survey.Answer answer, SurveyOpenTextViewHolder surveyOpenTextViewHolder) {
        surveyOpenTextViewHolder.titleComment.setText(question.getText());
        EditText comment = surveyOpenTextViewHolder.comment;
        if (answer != null) {
            comment.setText(answer.getResponse());
        }
        comment.setImeOptions(EditorInfo.IME_ACTION_DONE);
        updateRequiredVisibility(position, surveyOpenTextViewHolder.tvRequiredText);
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

    private void updateRequiredVisibility(int position, TextView view) {
        if (items.get(position).getQuestion().isRequired())
            view.setVisibility(View.VISIBLE);
        else {
            view.setVisibility(View.GONE);
        }
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

        TextView titleScale;
        TextView tvRequiredScale;
        RatingBar ratingBedGood;

        public SurveyScaleViewHolder(@NonNull View itemView) {
            super(itemView);
            titleScale = itemView.findViewById(R.id.tv_title_scale);
            ratingBedGood = itemView.findViewById(R.id.rating_bed_good);
            tvRequiredScale = itemView.findViewById(R.id.tv_required_scale);
        }
    }

    public static class SurveyYesNoViewHolder extends RecyclerView.ViewHolder {

        TextView titleYesNo;
        TextView tvRequiredYesNo;
        SwitchCompat switchCompat;

        public SurveyYesNoViewHolder(@NonNull View itemView) {
            super(itemView);
            titleYesNo = itemView.findViewById(R.id.tv_title_yes_no);
            switchCompat = itemView.findViewById(R.id.switch_compat);
            tvRequiredYesNo = itemView.findViewById(R.id.tv_required_yes_no);
        }
    }

    public static class SurveySingleChoiceViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView tvRequiredChoice;
        RecyclerView recyclerView;
        SingleChoiceAdapter.SingleChoiceCallback scaleResultCallback;
        QuestionItem item;

        public SurveySingleChoiceViewHolder(@NonNull View itemView, SingleChoiceAdapter.SingleChoiceCallback scaleResultCallback) {
            super(itemView);
            this.scaleResultCallback = scaleResultCallback;
            title = itemView.findViewById(R.id.tv_title_choice);
            tvRequiredChoice = itemView.findViewById(R.id.tv_required_choice);
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
        TextView tvRequiredText;
        EditText comment;

        public SurveyOpenTextViewHolder(@NonNull View itemView) {
            super(itemView);
            titleComment = itemView.findViewById(R.id.tv_title_text);
            tvRequiredText = itemView.findViewById(R.id.tv_required_text);
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
