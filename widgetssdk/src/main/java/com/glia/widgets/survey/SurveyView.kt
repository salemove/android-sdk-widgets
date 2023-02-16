package com.glia.widgets.survey

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.R
import com.glia.widgets.UiTheme
import com.glia.widgets.databinding.SurveyViewBinding
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.Utils
import com.glia.widgets.survey.SurveyAdapter.SurveyAdapterListener
import com.glia.widgets.view.configuration.ButtonConfiguration
import com.glia.widgets.view.configuration.survey.SurveyStyle
import com.glia.widgets.view.unifiedui.extensions.applyButtonTheme
import com.glia.widgets.view.unifiedui.extensions.applyColorTheme
import com.glia.widgets.view.unifiedui.exstensions.applyTextTheme
import com.glia.widgets.view.unifiedui.extensions.layoutInflater
import com.glia.widgets.view.unifiedui.theme.survey.SurveyTheme
import com.google.android.material.button.MaterialButton
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.theme.overlay.MaterialThemeOverlay
import kotlin.properties.Delegates

class SurveyView(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
    FrameLayout(
        MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, defStyleRes),
        attrs,
        defStyleAttr,
        defStyleRes
    ), SurveyContract.View, SurveyAdapterListener {
    private var onTitleUpdatedListener: OnTitleUpdatedListener? = null
    private var onFinishListener: OnFinishListener? = null
    private var controller: SurveyContract.Controller? = null

    private var uiTheme: UiTheme by Delegates.notNull()

    private val surveyTheme: SurveyTheme? by lazy {
        Dependencies.getGliaThemeManager().theme?.surveyTheme
    }

    private val binding: SurveyViewBinding by lazy {
        SurveyViewBinding.inflate(layoutInflater, this)
    }

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
    ) : this(context, attrs, defStyleAttr, R.style.Application_Glia_Chat) {
    }

    init {
        readTypedArray(attrs, defStyleAttr, defStyleRes)
        initCallbacks()
        initAdapter()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        applyStyle(uiTheme.surveyStyle)
    }

    fun setOnTitleUpdatedListener(onTitleUpdatedListener: OnTitleUpdatedListener?) {
        this.onTitleUpdatedListener = onTitleUpdatedListener
    }

    fun setOnFinishListener(onFinishListener: OnFinishListener?) {
        this.onFinishListener = onFinishListener
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
        val resourceProvider = Dependencies.getResourceProvider()
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
            setupCardView(cornerRadius, Color.parseColor(backgroundColor))
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
            cornerRadius, cornerRadius, cornerRadius, cornerRadius, 0f, 0f, 0f, 0f
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
        @SuppressLint("CustomViewStyleable") val typedArray = this.context.obtainStyledAttributes(
            attrs,
            R.styleable.GliaView,
            defStyleAttr,
            defStyleRes
        )
        setDefaultTheme(attrs, defStyleAttr, defStyleRes)
        typedArray.recycle()
    }

    private fun setDefaultTheme(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        @SuppressLint("CustomViewStyleable") val typedArray = this.context.obtainStyledAttributes(
            attrs,
            R.styleable.GliaView,
            defStyleAttr,
            defStyleRes
        )
        uiTheme = Utils.getThemeFromTypedArray(typedArray, this.context)
        typedArray.recycle()
    }

    fun setTheme(uiTheme: UiTheme?) {
        if (uiTheme == null) return
        this.uiTheme = Utils.getFullHybridTheme(uiTheme, this.uiTheme)
        this.uiTheme.surveyStyle?.also { surveyAdapter?.setStyle(it) }
    }

    private fun initAdapter() {
        surveyAdapter = SurveyAdapter(this)
        recyclerView.adapter = surveyAdapter
    }

    private fun initCallbacks() {
        submitButton.setOnClickListener {
            controller?.onSubmitClicked()
        }
        cancelButton.setOnClickListener {
            controller?.onCancelClicked()
        }
    }

    override fun setController(controller: SurveyContract.Controller) {
        this.controller = controller
        controller.setView(this)
    }

    override fun onAnswer(answer: Survey.Answer) {
        controller?.onAnswer(answer)
    }

    override fun onStateUpdated(state: SurveyState) {
        onTitleUpdatedListener?.onTitleUpdated(state.title)
        title.text = state.title
        surveyAdapter?.submitList(state.questions)
    }

    override fun scrollTo(index: Int) {
        recyclerView.scrollToPosition(index)
    }

    override fun hideSoftKeyboard() {
        Utils.hideSoftKeyboard(context, windowToken)
    }

    override fun onNetworkTimeout() {
        Toast.makeText(context, R.string.glia_survey_network_unavailable, Toast.LENGTH_LONG).show()
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
