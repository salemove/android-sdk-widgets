package com.glia.widgets.survey

import androidx.recyclerview.widget.RecyclerView
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.view.configuration.survey.SurveyStyle
import android.view.ViewGroup
import com.glia.widgets.R
import com.glia.androidsdk.engagement.Survey.Question.QuestionType
import com.glia.androidsdk.engagement.Survey.Question
import androidx.core.content.ContextCompat
import android.text.Html
import com.glia.widgets.view.button.GliaSurveyOptionButton
import android.graphics.Typeface
import com.glia.widgets.view.configuration.OptionButtonConfiguration
import android.graphics.drawable.LayerDrawable
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import com.glia.widgets.view.configuration.survey.InputQuestionConfiguration
import android.view.View.OnFocusChangeListener
import com.glia.widgets.helper.SimpleTextWatcher
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.glia.widgets.databinding.SurveyBooleanQuestionItemBinding
import com.glia.widgets.databinding.SurveyInputQuestionItemBinding
import com.glia.widgets.databinding.SurveyScaleQuestionItemBinding
import com.glia.widgets.databinding.SurveySingleQuestionItemBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.view.unifiedui.extensions.applyLayerTheme
import com.glia.widgets.view.unifiedui.extensions.applyTextTheme
import com.glia.widgets.view.unifiedui.extensions.applyOptionButtonTheme
import com.glia.widgets.view.unifiedui.theme.survey.SurveyBooleanQuestionTheme
import com.glia.widgets.view.unifiedui.theme.survey.SurveyInputQuestionTheme
import com.glia.widgets.view.unifiedui.theme.survey.SurveyScaleQuestionTheme
import com.glia.widgets.view.unifiedui.theme.survey.SurveySingleQuestionTheme
import java.util.*
import java.util.function.Consumer

class SurveyAdapter(private val listener: SurveyAdapterListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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

    fun getItem(position: Int): QuestionItem {
        return questionItems[position]
    }

    fun setStyle(style: SurveyStyle) {
        this.style = style
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
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

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val questionItem = getItem(position)
        (viewHolder as SurveyViewHolder).onBind(questionItem, listener)
    }

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

    abstract class SurveyViewHolder(
        itemView: View,
        val title: TextView,
        val requiredError: View
    ) : RecyclerView.ViewHolder(itemView),
        SurveyController.AnswerCallback {
        var questionItem: QuestionItem? = null
        var listener: SurveyAdapterListener? = null

        open fun onBind(questionItem: QuestionItem, listener: SurveyAdapterListener?) {
            this.questionItem = questionItem
            this.listener = listener
            this.questionItem?.answerCallback = this
            setItemTitle(questionItem.question)
            showRequiredError(questionItem.isShowError)
        }

        open fun applyAnswer(answer: Survey.Answer?) {}
        private fun setItemTitle(question: Question) {
            val questionText = question.text
            if (question.isRequired) {
                val context = title.context
                val color = ContextCompat.getColor(context, R.color.glia_system_negative_color)
                val colorString = String.format("%X", color).substring(2)
                val source = context.getString(
                    R.string.glia_survey_require_label, questionText, colorString
                )
                title.text = Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY)
                title.contentDescription = context.getString(
                    R.string.glia_survey_require_label_content_description,
                    questionText
                )
            } else {
                title.text = questionText
                title.contentDescription = null
            }
        }

        open fun showRequiredError(error: Boolean) {
            if (error) {
                requiredError.visibility = View.VISIBLE
                itemView.announceForAccessibility(
                    requiredError.context
                        .getString(R.string.glia_survey_required_error_message)
                )
            } else {
                requiredError.visibility = View.GONE
            }
        }

        override fun answerCallback(showError: Boolean) {
            showRequiredError(showError)
        }

        fun setAnswer(response: Int) {
            questionItem?.question?.id
                ?.let{ Survey.Answer.makeAnswer(it, response) }
                ?.also(::onAnswer)
        }

        fun setAnswer(response: Boolean) {
            questionItem?.question?.id
                ?.let{ Survey.Answer.makeAnswer(it, response) }
                ?.also(::onAnswer)
        }

        fun setAnswer(response: String) {
            questionItem?.question?.id
                ?.let{ Survey.Answer.makeAnswer(it, response) }
                ?.also(::onAnswer)
        }

        private fun onAnswer(answer: Survey.Answer) {
            applyAnswer(answer)
            listener?.onAnswer(answer)
        }
    }

    class ScaleQuestionViewHolder(
        private val binding: SurveyScaleQuestionItemBinding,
        private var style: SurveyStyle
    ) : SurveyViewHolder(binding.root, binding.tvTitle, binding.requiredError) {
        private val questionTheme: SurveyScaleQuestionTheme? by lazy {
            Dependencies.getGliaThemeManager().theme?.surveyTheme?.scaleQuestion
        }
        private val buttons: List<GliaSurveyOptionButton> get() = listOf(
            binding.scale1Button,
            binding.scale2Button,
            binding.scale3Button,
            binding.scale4Button,
            binding.scale5Button
        )

        init {
            val titleConfiguration = style.scaleQuestion.title
            title.setTextColor(titleConfiguration.textColor)
            val textSize = titleConfiguration.textSize
            title.textSize = textSize
            if (titleConfiguration.isBold) title.typeface = Typeface.DEFAULT_BOLD
            questionTheme?.title?.also(title::applyTextTheme)

            buttons.forEach(Consumer { button: GliaSurveyOptionButton ->
                button.setStyle(style.scaleQuestion.optionButton)
                button.applyOptionButtonTheme(questionTheme?.optionButton)
            })
        }

        override fun onBind(questionItem: QuestionItem, listener: SurveyAdapterListener?) {
            super.onBind(questionItem, listener)
            applyAnswer(questionItem.answer)
            buttons.forEach(Consumer { button: GliaSurveyOptionButton ->
                button.setOnClickListener {
                    setAnswer(
                        buttons.indexOf(button) + 1
                    )
                }
            })
        }

        override fun applyAnswer(answer: Survey.Answer?) {
            if (answer != null) {
                val value = answer.getResponse<Int>()
                setSelected(value)
            } else {
                unselectAll()
            }
        }

        private fun setSelected(value: Int) {
            for (i in buttons.indices) {
                val button: GliaSurveyOptionButton = buttons[i]
                val isSelected = i + 1 == value
                button.isSelected = isSelected
                button.applyOptionButtonTheme(questionTheme?.optionButton)
            }
        }

        private fun unselectAll() {
            setSelected(0)
        }

        override fun showRequiredError(error: Boolean) {
            super.showRequiredError(error)
            buttons.forEach(
                Consumer { button: GliaSurveyOptionButton ->
                    button.isError = error
                    button.applyOptionButtonTheme(questionTheme?.optionButton)
                }
            )
        }
    }

    class BooleanQuestionViewHolder(
        private val binding: SurveyBooleanQuestionItemBinding,
        style: SurveyStyle
        ) : SurveyViewHolder(binding.root, binding.tvTitle, binding.requiredError) {
        private val booleanTheme: SurveyBooleanQuestionTheme? by lazy {
            Dependencies.getGliaThemeManager().theme?.surveyTheme?.booleanQuestion
        }
        private val yesButton: GliaSurveyOptionButton get() = binding.yesButton
        private val noButton: GliaSurveyOptionButton get() = binding.noButton

        init {
            val questionStyle = style.booleanQuestion
            val titleConfiguration = questionStyle.title
            title.setTextColor(titleConfiguration.textColor)
            val textSize = titleConfiguration.textSize
            title.textSize = textSize
            if (titleConfiguration.isBold) title.typeface = Typeface.DEFAULT_BOLD
            booleanTheme?.title?.also(title::applyTextTheme)

            val buttonConfiguration = questionStyle.optionButton
            yesButton.setStyle(buttonConfiguration)
            yesButton.setOnClickListener { setAnswer(true) }
            noButton.setStyle(buttonConfiguration)
            noButton.setOnClickListener { setAnswer(false) }
        }

        override fun onBind(questionItem: QuestionItem, listener: SurveyAdapterListener?) {
            super.onBind(questionItem, listener)
            applyAnswer(questionItem.answer)
        }

        override fun applyAnswer(answer: Survey.Answer?) {
            if (answer != null) {
                val value = answer.getResponse<Boolean>()
                setSelected(value)
            } else {
                unselectAll()
            }
        }

        private fun setSelected(value: Boolean) {
            yesButton.isSelected = value
            noButton.isSelected = !value
            applyBooleanTheme()
        }

        private fun unselectAll() {
            yesButton.isSelected = false
            noButton.isSelected = false
            applyBooleanTheme()
        }

        override fun showRequiredError(error: Boolean) {
            super.showRequiredError(error)
            yesButton.isError = error
            noButton.isError = error
            applyBooleanTheme()
        }

        private fun applyBooleanTheme() {
            booleanTheme?.optionButton?.also {
                yesButton.applyOptionButtonTheme(it)
                noButton.applyOptionButtonTheme(it)
            }
        }
    }

    class SingleQuestionViewHolder(
        private val binding: SurveySingleQuestionItemBinding,
        var style: SurveyStyle
        ) : SurveyViewHolder(binding.root, binding.tvTitle, binding.requiredError) {
        private val singleTheme: SurveySingleQuestionTheme? by lazy {
            Dependencies.getGliaThemeManager().theme?.surveyTheme?.singleQuestion
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

        override fun onBind(questionItem: QuestionItem, listener: SurveyAdapterListener?) {
            super.onBind(questionItem, listener)
            singleChoice(questionItem)
        }

        private fun singleChoice(item: QuestionItem) {
            val selectedId = Optional.ofNullable(item.answer)
                .map { answer: Survey.Answer -> answer.getResponse<Any>() as String }
                .orElse(null)
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
                        ?: Color.parseColor(style.singleQuestion.tintColor)
                    val colorStateList = getRadioButtonColors(radiobuttonColor)
                    centerDot.setTintList(colorStateList)

                    // Set color for the border
                    val border =
                        drawable.findDrawableByLayerId(R.id.border_item) as GradientDrawable
                    val strokeColor =
                        ContextCompat.getColorStateList(context, R.color.glia_base_shade_color)
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
                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)

                singleTheme?.option?.also(radioButton::applyTextTheme)

                radioGroup.addView(radioButton)
            }
        }

        private fun getRadioButtonColors(radiobuttonColor: Int): ColorStateList {
            return ColorStateList(
                arrayOf(
                    intArrayOf(-android.R.attr.state_checked),
                    intArrayOf(android.R.attr.state_checked)
                ), intArrayOf(
                    ContextCompat.getColor(
                        containerView.context,
                        android.R.color.transparent
                    ),  //disabled
                    radiobuttonColor //enabled
                )
            )
        }
    }

    class InputQuestionViewHolder(
        val binding: SurveyInputQuestionItemBinding,
        var style: SurveyStyle
        ) : SurveyViewHolder(binding.root, binding.tvTitle, binding.requiredError) {
        private val inputTheme: SurveyInputQuestionTheme? by lazy {
            Dependencies.getGliaThemeManager().theme?.surveyTheme?.inputQuestion
        }
        private val comment: EditText get() = binding.etComment

        init {
            val inputQuestionConfig = style.inputQuestion
            setupTitle(inputQuestionConfig)
            setupInputBoxText(inputQuestionConfig.optionButton)
        }

        override fun onBind(questionItem: QuestionItem, listener: SurveyAdapterListener?) {
            super.onBind(questionItem, listener)
            applyAnswer(questionItem.answer)
        }

        override fun applyAnswer(answer: Survey.Answer?) {
            if (answer != null) {
                val oldValue = comment.text.toString()
                val newValue = answer.getResponse<String>()
                if (oldValue != newValue) {
                    comment.setText(newValue)
                }
            } else {
                comment.text = null
            }
        }

        override fun showRequiredError(error: Boolean) {
            super.showRequiredError(error)
            val context = comment.context
            val shape = ContextCompat.getDrawable(
                context,
                R.drawable.bg_survey_edit_text
            ) as GradientDrawable?
            if (shape != null) {
                val inputQuestionConfig = style.inputQuestion
                val optionButtonConfig = inputQuestionConfig.optionButton
                val errorColorString = optionButtonConfig.highlightedLayer.borderColor
                val errorColor = Color.parseColor(errorColorString)
                val normalColorString = optionButtonConfig.normalLayer.borderColor
                val normalColor = Color.parseColor(normalColorString)
                val strokeColor =
                    if (error) ColorStateList.valueOf(errorColor) else ColorStateList.valueOf(
                        normalColor
                    )
                val width = optionButtonConfig.normalLayer.borderWidth
                shape.setStroke(width, strokeColor)
                shape.setColor(Color.parseColor(optionButtonConfig.normalLayer.backgroundColor))
                comment.background = shape
            }
        }

        private fun setupTitle(inputQuestionConfig: InputQuestionConfiguration) {
            val titleConfiguration = inputQuestionConfig.title
            title.setTextColor(titleConfiguration.textColor)
            val textSize = titleConfiguration.textSize
            title.textSize = textSize
            if (titleConfiguration.isBold) title.typeface = Typeface.DEFAULT_BOLD

            inputTheme?.title?.also(title::applyTextTheme)
        }

        private fun setupInputBoxText(optionButtonStyle: OptionButtonConfiguration) {
            comment.setTextColor(optionButtonStyle.normalText.textColor)
            if (optionButtonStyle.normalText.isBold) comment.typeface = Typeface.DEFAULT_BOLD
            comment.setHintTextColor(
                ContextCompat.getColor(
                    comment.context,
                    R.color.glia_base_shade_color
                )
            )
            val textSize = optionButtonStyle.normalText.textSize
            comment.textSize = textSize
            comment.onFocusChangeListener =
                OnFocusChangeListener { _: View?, _: Boolean -> setAnswer(comment.text.toString()) }
            comment.addTextChangedListener(object : SimpleTextWatcher() {
                override fun afterTextChanged(editable: Editable) {
                    setAnswer(editable.toString())
                }
            })

            inputTheme?.text?.also(comment::applyTextTheme)
            inputTheme?.background?.also(comment::applyLayerTheme)
        }
    }

    companion object {
        private const val SURVEY_SCALE = 1
        private const val SURVEY_YES_NO = 2
        private const val SURVEY_SINGLE_CHOICE = 3
        private const val SURVEY_OPEN_TEXT = 4
    }
}
