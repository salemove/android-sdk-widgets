package com.glia.widgets.survey.viewholder

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
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
import com.glia.widgets.survey.QuestionItem
import com.glia.widgets.survey.SurveyAdapter
import com.glia.widgets.view.configuration.survey.SurveyStyle
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.survey.SurveySingleQuestionTheme
import java.util.Optional
import kotlin.jvm.optionals.getOrNull
import androidx.core.graphics.toColorInt

internal class SingleQuestionViewHolder(
    private val binding: SurveySingleQuestionItemBinding,
    var style: SurveyStyle
) : SurveyViewHolder(binding.root, binding.tvTitle, binding.requiredError) {
    private val singleTheme: SurveySingleQuestionTheme? by lazy {
        Dependencies.gliaThemeManager.theme?.surveyTheme?.singleQuestion
    }
    private val containerView: LinearLayout get() = binding.singleChoiceView
    private val radioGroup: RadioGroup get() = binding.radioGroup

    init {
        val titleConfiguration = style.singleQuestion.title
        title.setTextColor(titleConfiguration.textColor)
        val textSize = titleConfiguration.textSize
        title.textSize = textSize
        if (titleConfiguration.isBold) title.typeface = Typeface.DEFAULT_BOLD

        singleTheme?.title?.also(title::applyTextTheme)
    }

    override fun onBind(questionItem: QuestionItem, listener: SurveyAdapter.SurveyAdapterListener?) {
        super.onBind(questionItem, listener)
        singleChoice(questionItem)
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
            radioButton.setTextColor(style.singleQuestion.title.textColor)
            val textSize = style.singleQuestion.optionText.textSize
            radioButton.textSize = textSize
            radioButton.isChecked = option.id == selectedId
            radioButton.setOnClickListener { setAnswer(option.id) }
            val drawable = ContextCompat.getDrawable(
                context,
                R.drawable.bg_survey_radio_button
            ) as LayerDrawable?
            if (drawable != null) {
                // Set color for the center dot
                val centerDot = drawable.findDrawableByLayerId(R.id.center_item)
                val radiobuttonColor = singleTheme?.tintColor?.primaryColor
                    ?: style.singleQuestion.tintColor.toColorInt()
                val colorStateList = getRadioButtonColors(radiobuttonColor)
                centerDot.setTintList(colorStateList)

                // Set color for the border
                val border =
                    drawable.findDrawableByLayerId(R.id.border_item) as GradientDrawable
                val strokeColor =
                    ContextCompat.getColorStateList(context, R.color.glia_normal_color_opacity_30)
                val width = context.resources.getDimensionPixelSize(R.dimen.glia_px)
                border.setStroke(width, strokeColor)
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
