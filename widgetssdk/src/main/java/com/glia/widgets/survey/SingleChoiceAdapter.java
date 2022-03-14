package com.glia.widgets.survey;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.glia.androidsdk.engagement.Survey;
import com.glia.widgets.R;

import java.util.List;

public class SingleChoiceAdapter extends RecyclerView.Adapter<SingleChoiceAdapter.ViewHolder> {

    private final QuestionItem questionItem;
    private final SingleChoiceAdapter.SingleChoiceCallback scaleResultCallback;

    public SingleChoiceAdapter(QuestionItem questionItem, SingleChoiceAdapter.SingleChoiceCallback scaleResultCallback) {
        this.questionItem = questionItem;
        this.scaleResultCallback = scaleResultCallback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = createNewLayout(parent, R.layout.survey_single_choice_option);
        return new SingleChoiceAdapter.ViewHolder(view);
    }

    private static View createNewLayout(ViewGroup parent, @LayoutRes int layoutId) {
        return LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        List<Survey.Question.Option> optionList = getOptions();
        if (optionList == null) {
            return;
        }
        Survey.Question.Option option = optionList.get(position);

        holder.tvChoice.setText(option.getLabel());

        Survey.Answer answer = questionItem.getAnswer();
        if (answer != null && option.getId().equals(answer.getResponse())) {
            holder.layoutChoice.setBackgroundResource(R.drawable.border_background);
        } else {
            holder.layoutChoice.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @Override
    public int getItemCount() {
        List<Survey.Question.Option> optionList = getOptions();
        return optionList == null ? 0 : optionList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvChoice;
        LinearLayout layoutChoice;

        @SuppressLint("NotifyDataSetChanged")
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChoice = itemView.findViewById(R.id.tv_choice);
            layoutChoice = itemView.findViewById(R.id.layout_choice);
            layoutChoice.setOnClickListener(v -> {
                List<Survey.Question.Option> optionList = getOptions();
                if (optionList == null) {
                    return;
                }
                scaleResultCallback.singleChoiceCallback(questionItem, optionList.get(getAdapterPosition()).getId());
                notifyDataSetChanged();
            });
        }
    }

    @Nullable
    private List<Survey.Question.Option> getOptions() {
        return questionItem.getQuestion().getOptions();
    }

    public interface SingleChoiceCallback {
        void singleChoiceCallback(QuestionItem item, String response);
    }
}
