package com.glia.widgets.survey

import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.R
import com.glia.widgets.base.FadeTransitionActivity
import com.glia.widgets.di.Dependencies.controllerFactory
import com.glia.widgets.helper.ExtraKeys
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.getParcelableExtraCompat
import com.glia.widgets.helper.hideKeyboard
import com.glia.widgets.helper.insetsController
import com.glia.widgets.helper.insetsControllerCompat

/**
 * Glia internal class.
 *
 *
 * It will be automatically added to the integrator's manifest file by the manifest merger during compilation.
 *
 *
 * This activity is used to display post-engagement surveys.
 */
internal class SurveyActivity : FadeTransitionActivity(), SurveyView.OnFinishListener {
    private val surveyView: SurveyView by lazy { findViewById(R.id.survey_view) }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        this.enableEdgeToEdge()
        window.insetsControllerCompat.isAppearanceLightStatusBars = false

        super.onCreate(savedInstanceState)

        Logger.i(TAG, "Create Survey screen")
        setContentView(R.layout.survey_activity)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        prepareSurveyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        hideSoftKeyboard()
        surveyView.onDestroyView()
        onBackPressedCallback.remove()
        Logger.i(TAG, "Destroy Survey screen")
    }

    override fun onFinish() {
        finish()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        // Override the default dispatchTouchEvent
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (finishActivityWithAnimationWhenTappedOutsideSurveyView(event)) return true
            removeFocusWhenTappedOutsideSurveyEditText(event)
        }
        return super.dispatchTouchEvent(event)
    }

    fun finishActivityWithAnimationWhenTappedOutsideSurveyView(event: MotionEvent): Boolean {
        val surveyView: View? = findViewById(R.id.survey_view)
        if (surveyView != null) {
            val location = IntArray(2)
            surveyView.getLocationOnScreen(location)
            val x: Float = event.rawX
            val y: Float = event.rawY
            val isTappedOutsideSurveyView =
                x < location[0] || x > location[0] + surveyView.width || y < location[1] || y > location[1] + surveyView.height
            if (isTappedOutsideSurveyView) {
                finishAndRemoveTask()
                return true // consume the event
            }
        }
        return false
    }

    fun removeFocusWhenTappedOutsideSurveyEditText(event: MotionEvent) {
        (currentFocus as? EditText)?.let { editText ->
            val outRect = Rect()
            editText.getGlobalVisibleRect(outRect)
            if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                editText.clearFocus()
                (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(editText.windowToken, 0)
            }
        }
    }

    private fun hideSoftKeyboard() {
        surveyView.insetsController?.hideKeyboard()
    }

    private fun prepareSurveyView() {
        surveyView.setOnTitleUpdatedListener(object : SurveyView.OnTitleUpdatedListener {
            override fun onTitleUpdated(title: String?) {
                updateTitle(title)
            }
        })

        surveyView.setOnFinishListener(this)
        val surveyController = controllerFactory.surveyController
        surveyView.setController(surveyController)

        intent.getParcelableExtraCompat<Survey>(ExtraKeys.SURVEY)?.let {
            surveyController.init(it)
        }
    }

    private fun updateTitle(title: String?) {
        this.title = title
    }
}
