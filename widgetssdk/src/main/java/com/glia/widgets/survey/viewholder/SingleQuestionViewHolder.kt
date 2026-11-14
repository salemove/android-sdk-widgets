package com.glia.widgets.survey.viewholder

import android.content.res.ColorStateList
import android.graphics.drawable.LayerDrawable
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.content.ContextCompat
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.R
import com.glia.widgets.databinding.SurveySingleQuestionItemBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.gliaAttrColor
import com.glia.widgets.survey.QuestionItem
import com.glia.widgets.survey.SurveyAdapter
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.survey.SurveySingleQuestionTheme
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

internal class SingleQuestionViewHolder(
    private val binding: SurveySingleQuestionItemBinding
) : SurveyViewHolder(binding.root, binding.tvTitle, binding.requiredError) {
    private val singleTheme: SurveySingleQuestionTheme? by lazy {
        Dependencies.gliaThemeManager.theme?.surveyTheme?.singleQuestion
    }
    private val containerView: LinearLayout get() = binding.singleChoiceView
    private val radioGroup: RadioGroup get() = binding.radioGroup

    init {
        // The title's colour, size and weight come from `survey_single_question_item.xml`.
        singleTheme?.title?.also(title::applyTextTheme)
    }

    override fun onBind(questionItem: QuestionItem, listener: SurveyAdapter.SurveyAdapterListener?) {
        super.onBind(questionItem, listener)
        singleChoice(questionItem)
    }

    override fun showRequiredError(error: Boolean) {
        super.showRequiredError(error)
        if (error) requiredError.applyTextTheme(singleTheme?.error)
    }

    private fun singleChoice(item: QuestionItem) {
        val selectedId = Optional.ofNullable(item.answer)
            .map { answer: Survey.Answer -> answer.getResponse<Any>() as String }
            .getOrNull()
        val options = item.question.options ?: return
        radioGroup.removeAllViews()
        for (i in options.indices) {
            val option = options[i]
            val context = itemView.context
            val radioButton = RadioButton(context)
            radioButton.id = View.generateViewId()
            radioButton.text = option.label
            // The option label is built in code, so it has no layout to carry its appearance.
            radioButton.setTextColor(context.gliaAttrColor(R.attr.gliaBaseDarkColor, R.color.glia_dark_color))
            radioButton.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                context.resources.getDimension(R.dimen.glia_survey_default_text_size)
            )
            radioButton.isChecked = option.id == selectedId
            radioButton.setOnClickListener { setAnswer(option.id) }
            // `bg_survey_radio_button` resolves the ring and the checked dot from the theme itself;
            // only the JSON theme's tint has to be pushed in from here.
            val drawable = ContextCompat.getDrawable(
                context,
                R.drawable.bg_survey_radio_button
            ) as LayerDrawable?
            singleTheme?.tintColor?.primaryColor?.also { tintColor ->
                drawable?.findDrawableByLayerId(R.id.center_item)
                    ?.setTintList(getRadioButtonColors(tintColor))
            }
            radioButton.buttonDrawable = drawable
            val start = context.resources.getDimensionPixelSize(R.dimen.glia_medium)
            val isRtl =
                context.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
            radioButton.setPadding(if (isRtl) 0 else start, 0, if (isRtl) start else 0, 0)
            val height =
                context.resources.getDimensionPixelSize(R.dimen.glia_survey_radio_button_height)
            radioButton.layoutParams =
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            radioButton.minHeight = height

            singleTheme?.option?.also(radioButton::applyTextTheme)

            radioGroup.addView(radioButton)
        }
    }

    private fun getRadioButtonColors(radiobuttonColor: Int): ColorStateList {
        return ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_checked),
                intArrayOf(android.R.attr.state_checked)
            ),
            intArrayOf(
                ContextCompat.getColor(
                    containerView.context,
                    android.R.color.transparent
                ), // disabled
                radiobuttonColor // enabled
            )
        )
    }
}
