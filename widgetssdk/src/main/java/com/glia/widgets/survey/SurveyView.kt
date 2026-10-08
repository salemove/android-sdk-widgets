package com.glia.widgets.survey

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
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
import com.glia.widgets.helper.gliaAttrColor
import com.glia.widgets.helper.hideKeyboard
import com.glia.widgets.helper.insetsController
import com.glia.widgets.helper.layoutInflater
import com.glia.widgets.helper.setLocaleText
import com.glia.widgets.helper.showToast
import com.glia.widgets.survey.SurveyAdapter.SurveyAdapterListener
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
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ),
    SurveyContract.View,
    SurveyAdapterListener {
    private var onTitleUpdatedListener: OnTitleUpdatedListener? = null
    private var onFinishListener: OnFinishListener? = null
    private var controller: SurveyContract.Controller? = null

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
        initAdapter()
        applyUnifiedTheme()
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
     * Layers the JSON unified theme over what the layout and the composed Glia theme already
     * resolved. Everything the deleted `SurveyStyle` used to apply here - the title colour and size,
     * the button panel background, the Submit/Cancel styling - now comes from `survey_view.xml` and
     * `buttonBar*ButtonStyle`, so only the card background is still built in code: no XML shape can
     * express the JSON gradient fill.
     */
    private fun applyUnifiedTheme() {
        setupCardView()

        surveyTheme?.layer?.fill?.also(buttonPanel::applyColorTheme)
        surveyTheme?.title?.also(title::applyTextTheme)

        surveyTheme?.submitButton?.also(submitButton::applyButtonTheme)
        surveyTheme?.cancelButton?.also(cancelButton::applyButtonTheme)
    }

    private fun setupCardView() {
        val cornerRadius = surveyTheme?.layer?.cornerRadius
            ?.let(Dependencies.resourceProvider::convertDpToPixel)
            ?: resources.getDimension(R.dimen.glia_survey_default_survey_corner_radius)

        val fill = surveyTheme?.layer?.fill
        when {
            fill == null -> setupCardView(cornerRadius, context.gliaAttrColor(R.attr.gliaBaseLightColor, R.color.glia_light_color))
            fill.isGradient -> setupCardView(cornerRadius, fill.valuesArray)
            else -> setupCardView(cornerRadius, fill.primaryColor)
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

    private fun initAdapter() {
        surveyAdapter = SurveyAdapter(this)
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
            controller?.onSubmitClicked()

            GliaLogger.i(LogEvents.SURVEY_SCREEN_BUTTON_CLICKED, null, mapOf(EventAttribute.ButtonName to ButtonNames.SUBMIT))
        }
        cancelButton.setOnClickListener {
            controller?.onCancelClicked()

            GliaLogger.i(LogEvents.SURVEY_SCREEN_BUTTON_CLICKED, null, mapOf(EventAttribute.ButtonName to ButtonNames.CANCEL))
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
