package com.glia.widgets.survey

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.glia.androidsdk.engagement.Survey
import com.glia.telemetry_lib.ButtonNames
import com.glia.telemetry_lib.EventAttribute
import com.glia.telemetry_lib.GliaLogger
import com.glia.telemetry_lib.LogEvents
import com.glia.widgets.R
import com.glia.widgets.databinding.SurveyViewBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.SimpleWindowInsetsAndAnimationHandler
import com.glia.widgets.helper.Utils
import com.glia.widgets.helper.hideKeyboard
import com.glia.widgets.helper.insetsController
import com.glia.widgets.helper.layoutInflater
import com.glia.widgets.helper.setLocaleText
import com.glia.widgets.helper.showToast
import com.glia.widgets.helper.wrapWithMaterialThemeOverlay
import com.glia.widgets.survey.SurveyAdapter.SurveyAdapterListener
import com.glia.widgets.view.configuration.ButtonConfiguration
import com.glia.widgets.view.configuration.survey.SurveyStyle
import com.glia.widgets.view.unifiedui.applyButtonTheme
import com.glia.widgets.view.unifiedui.applyColorTheme
import com.glia.widgets.view.unifiedui.applyTextTheme
import com.glia.widgets.view.unifiedui.theme.survey.SurveyTheme
import com.google.android.material.button.MaterialButton
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel

internal class SurveyView(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
    FrameLayout(
        context.wrapWithMaterialThemeOverlay(attrs, defStyleAttr, defStyleRes),
        attrs,
        defStyleAttr,
        defStyleRes
    ),
    SurveyContract.View,
    SurveyAdapterListener {
    private var onTitleUpdatedListener: OnTitleUpdatedListener? = null
    private var onFinishListener: OnFinishListener? = null
    private var controller: SurveyContract.Controller? = null

    // MVI listeners for Fragment integration
    private var onAnswerListener: ((Survey.Answer) -> Unit)? = null
    private var onSubmitClickListener: (() -> Unit)? = null
    private var onCancelClickListener: (() -> Unit)? = null

    private val surveyTheme: SurveyTheme? by lazy {
        Dependencies.gliaThemeManager.theme?.surveyTheme
    }

    private val binding: SurveyViewBinding by lazy {
        SurveyViewBinding.inflate(layoutInflater, this)
    }
    private val localeProvider = Dependencies.localeProvider

    private val cardView: CardView get() = binding.cardView
    private val title: TextView get() = binding.surveyTitle
    private val recyclerView: RecyclerView get() = binding.surveyList
    private val buttonPanel: LinearLayout get() = binding.buttonPanel
    private val submitButton: MaterialButton get() = binding.btnSubmit
    private val cancelButton: MaterialButton get() = binding.btnCancel

    private var surveyAdapter: SurveyAdapter? = null

    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = R.attr.gliaChatStyle
    ) : this(context, attrs, defStyleAttr, R.style.Application_Glia_Chat)

    init {
        SimpleWindowInsetsAndAnimationHandler(this)
        readTypedArray(attrs, defStyleAttr, defStyleRes)
        setupViewAppearance()
        initCallbacks()
    }

    private fun setupViewAppearance() {
        submitButton.setLocaleText(R.string.general_submit)
        cancelButton.setLocaleText(R.string.general_cancel)
    }

    fun setOnTitleUpdatedListener(onTitleUpdatedListener: OnTitleUpdatedListener?) {
        this.onTitleUpdatedListener = onTitleUpdatedListener
    }

    fun setOnFinishListener(onFinishListener: OnFinishListener?) {
        this.onFinishListener = onFinishListener
    }

    /**
     * Sets listener for answer updates (MVI pattern).
     * Called when user provides an answer to a question.
     */
    fun setOnAnswerListener(listener: ((Survey.Answer) -> Unit)?) {
        this.onAnswerListener = listener
    }

    /**
     * Sets listener for submit button clicks (MVI pattern).
     */
    fun setOnSubmitClickListener(listener: (() -> Unit)?) {
        this.onSubmitClickListener = listener
    }

    /**
     * Sets listener for cancel button clicks (MVI pattern).
     */
    fun setOnCancelClickListener(listener: (() -> Unit)?) {
        this.onCancelClickListener = listener
    }

    private fun applyStyle(surveyStyle: SurveyStyle?) {
        setupCardView(surveyStyle, surveyTheme)

        surveyStyle?.title?.also { textConfiguration ->
            textConfiguration.textColor.also(title::setTextColor)
            textConfiguration.textSize.also(title::setTextSize)
            if (textConfiguration.isBold) title.typeface = Typeface.DEFAULT_BOLD
        }

        // The elevated view (buttonPanel) needs to have a background to cast a shadow
        surveyStyle?.layer?.backgroundColor
            ?.let(Color::parseColor)
            ?.also(buttonPanel::setBackgroundColor)
        surveyTheme?.layer?.fill?.also(buttonPanel::applyColorTheme)
        surveyTheme?.title?.also(title::applyTextTheme)

        applyButtonStyle(surveyStyle?.submitButton, submitButton)
        applyButtonStyle(surveyStyle?.cancelButton, cancelButton)
        surveyTheme?.submitButton?.also(submitButton::applyButtonTheme)
        surveyTheme?.cancelButton?.also(cancelButton::applyButtonTheme)
    }

    private fun setupCardView(surveyStyle: SurveyStyle?, surveyTheme: SurveyTheme?) {
        if (surveyStyle == null && surveyTheme == null) {
            return
        }

        val cornerRadiusFloat = surveyTheme?.layer?.cornerRadius
            ?: surveyStyle?.layer?.cornerRadius?.toFloat()
            ?: SurveyStyle.Builder().build().layer.cornerRadius.toFloat() // default value
        val resourceProvider = Dependencies.resourceProvider
        val cornerRadius = resourceProvider.convertDpToPixel(cornerRadiusFloat)

        surveyTheme?.layer?.fill?.also {
            if (it.isGradient) {
                setupCardView(cornerRadius, it.valuesArray)
            } else {
                setupCardView(cornerRadius, it.primaryColor)
            }
        } ?: run {
            val backgroundColor = surveyStyle?.layer?.backgroundColor
                ?: SurveyStyle.Builder().build().layer.backgroundColor // default value
            setupCardView(cornerRadius, backgroundColor.toColorInt())
        }
    }

    private fun setupCardView(cornerRadius: Float, backgroundColor: Int) {
        val cardViewShapeBuilder = ShapeAppearanceModel().toBuilder()
        cardViewShapeBuilder.setTopLeftCorner(CornerFamily.ROUNDED, cornerRadius)
        cardViewShapeBuilder.setTopRightCorner(CornerFamily.ROUNDED, cornerRadius)
        val background = MaterialShapeDrawable(cardViewShapeBuilder.build())
        background.fillColor = ColorStateList.valueOf(backgroundColor)
        cardView.background = background
    }

    private fun setupCardView(cornerRadius: Float, colors: IntArray) {
        val background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, null)
        background.cornerRadii = listOf(
            cornerRadius,
            cornerRadius,
            cornerRadius,
            cornerRadius,
            0f,
            0f,
            0f,
            0f
        ).toFloatArray()
        cardView.background = background
    }

    private fun applyButtonStyle(configuration: ButtonConfiguration?, button: MaterialButton?) {
        if (configuration == null) {
            // Default attributes from
            // "Application.GliaAndroidSdkWidgetsExample.Button" styles
            // will be in use
            return
        }
        val backgroundColor = configuration.backgroundColor
        button?.backgroundTintList = backgroundColor
        val textColor = configuration.textConfiguration.textColor
        button?.setTextColor(textColor)
        button?.textSize = configuration.textConfiguration.textSize
        button?.strokeColor = configuration.strokeColor
        configuration.strokeWidth?.let { button?.strokeWidth = it }
        if (configuration.textConfiguration.isBold) button?.typeface = Typeface.DEFAULT_BOLD
    }

    private fun readTypedArray(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        this.context.withStyledAttributes(attrs, R.styleable.GliaView, defStyleAttr, defStyleRes) {
            setDefaultTheme(attrs, defStyleAttr, defStyleRes)
        }
    }

    private fun setDefaultTheme(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        this.context.withStyledAttributes(attrs, R.styleable.GliaView, defStyleAttr, defStyleRes) {
            val surveyStyle = Utils.getThemeFromTypedArray(this, this@SurveyView.context).surveyStyle
            initAdapter(surveyStyle)
            applyStyle(surveyStyle)
        }
    }

    private fun initAdapter(surveyStyle: SurveyStyle?) {
        surveyAdapter = SurveyAdapter(this, surveyStyle ?: SurveyStyle.Builder().build())
        recyclerView.adapter = surveyAdapter

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                insetsController?.hideKeyboard()
            }
        })
    }

    private fun initCallbacks() {
        submitButton.setOnClickListener {
            // MVI pattern: notify listener (telemetry handled in ViewModel)
            onSubmitClickListener?.invoke()

            // Legacy MVP pattern: notify controller with telemetry
            controller?.let {
                it.onSubmitClicked()
                GliaLogger.i(LogEvents.SURVEY_SCREEN_BUTTON_CLICKED, null, mapOf(EventAttribute.ButtonName to ButtonNames.SUBMIT))
            }
        }
        cancelButton.setOnClickListener {
            // MVI pattern: notify listener (telemetry handled in ViewModel)
            onCancelClickListener?.invoke()

            // Legacy MVP pattern: notify controller with telemetry
            controller?.let {
                it.onCancelClicked()
                GliaLogger.i(LogEvents.SURVEY_SCREEN_BUTTON_CLICKED, null, mapOf(EventAttribute.ButtonName to ButtonNames.CANCEL))
            }
        }
    }

    override fun setController(controller: SurveyContract.Controller) {
        this.controller = controller
        controller.setView(this)
    }

    override fun onAnswer(answer: Survey.Answer) {
        // MVI pattern: notify listener
        onAnswerListener?.invoke(answer)
        // Legacy MVP pattern: notify controller
        controller?.onAnswer(answer)
    }

    override fun onStateUpdated(state: SurveyState) {
        renderState(state)
    }

    /**
     * Renders the survey state to the UI.
     * Called by Fragment when observing state changes (MVI pattern).
     */
    fun renderState(state: SurveyState) {
        onTitleUpdatedListener?.onTitleUpdated(state.title)
        title.text = state.title
        surveyAdapter?.submitList(state.questions)
    }

    override fun scrollTo(index: Int) {
        recyclerView.scrollToPosition(index)
    }

    override fun hideSoftKeyboard() {
        insetsController?.hideKeyboard()
    }

    override fun onNetworkTimeout() {
        context.showToast(localeProvider.getString(R.string.glia_survey_network_unavailable))
    }

    override fun finish() {
        onFinishListener?.onFinish()
    }

    fun onDestroyView() {
        controller?.onDestroy()
        controller = null
    }

    interface OnTitleUpdatedListener {
        fun onTitleUpdated(title: String?)
    }

    interface OnFinishListener {
        fun onFinish()
    }
}
