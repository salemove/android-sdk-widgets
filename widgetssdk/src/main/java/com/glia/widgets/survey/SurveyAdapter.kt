package com.glia.widgets.survey

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.glia.androidsdk.engagement.Survey
import com.glia.androidsdk.engagement.Survey.Question.QuestionType
import com.glia.widgets.databinding.SurveyBooleanQuestionItemBinding
import com.glia.widgets.databinding.SurveyInputQuestionItemBinding
import com.glia.widgets.databinding.SurveyScaleQuestionItemBinding
import com.glia.widgets.databinding.SurveySingleQuestionItemBinding
import com.glia.widgets.survey.viewholder.*
import com.glia.widgets.view.configuration.survey.SurveyStyle

class SurveyAdapter(private val listener: SurveyAdapterListener) :
    RecyclerView.Adapter<SurveyViewHolder>() {
    interface SurveyAdapterListener {
        fun onAnswer(answer: Survey.Answer)
    }

    private var style: SurveyStyle
    private val questionItems: MutableList<QuestionItem> = ArrayList()

    init {
        // initialize style with default empty SurveyStyle, to make sure that style usage is safe even if new style is not set.
        style = SurveyStyle.Builder().build()
    }

    fun submitList(items: List<QuestionItem>?) {
        questionItems.clear()
        if (items == null) {
            return
        }
        questionItems.addAll(items)
    }

    private fun getItem(position: Int): QuestionItem = questionItems[position]

    fun setStyle(style: SurveyStyle) {
        this.style = style
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SurveyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            SURVEY_SCALE -> {
                val binding = SurveyScaleQuestionItemBinding.inflate(inflater, parent, false)
                ScaleQuestionViewHolder(binding, style)
            }
            SURVEY_YES_NO -> {
                val binding = SurveyBooleanQuestionItemBinding.inflate(inflater, parent, false)
                BooleanQuestionViewHolder(binding, style)
            }
            SURVEY_SINGLE_CHOICE -> {
                val binding = SurveySingleQuestionItemBinding.inflate(inflater, parent, false)
                SingleQuestionViewHolder(binding, style)
            }
            else -> {
                val binding = SurveyInputQuestionItemBinding.inflate(inflater, parent, false)
                InputQuestionViewHolder(binding, style)
            }
        }
    }

    override fun onBindViewHolder(viewHolder: SurveyViewHolder, position: Int) =
        viewHolder.onBind(getItem(position), listener)

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position).question.type) {
            QuestionType.TEXT -> SURVEY_OPEN_TEXT
            QuestionType.BOOLEAN -> SURVEY_YES_NO
            QuestionType.SINGLE_CHOICE -> SURVEY_SINGLE_CHOICE
            else -> SURVEY_SCALE
        }
    }

    override fun getItemCount(): Int {
        return questionItems.size
    }

    companion object {
        private const val SURVEY_SCALE = 1
        private const val SURVEY_YES_NO = 2
        private const val SURVEY_SINGLE_CHOICE = 3
        private const val SURVEY_OPEN_TEXT = 4
    }
}
