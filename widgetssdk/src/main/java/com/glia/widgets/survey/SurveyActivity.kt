package com.glia.widgets.survey

import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.R
import com.glia.widgets.di.Dependencies.controllerFactory
import com.glia.widgets.helper.ExtraKeys
import com.glia.widgets.helper.Logger.i
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
internal class SurveyActivity : AppCompatActivity(), SurveyView.OnFinishListener {
    private val surveyView: SurveyView by lazy { findViewById(R.id.survey_view) }

    override fun onCreate(savedInstanceState: Bundle?) {
        this.enableEdgeToEdge()
        window.insetsControllerCompat.isAppearanceLightStatusBars = false

        overrideEnterAnimation()
        super.onCreate(savedInstanceState)

        i(TAG, "Create Survey screen")
        setContentView(R.layout.survey_activity)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAndRemoveTask()
            }
        })

        prepareSurveyView()
    }

    override fun onDestroy() {
        i(TAG, "Destroy Survey screen")
        hideSoftKeyboard()
        surveyView.onDestroyView()
        super.onDestroy()
    }

    override fun onFinish() {
        // In case the engagement ends, Activity is removed from the device's Recents menu
        // to avoid app users to accidentally start queueing for another call when they resume
        // the app from the Recents menu and the app's backstack was empty.

        finishAndRemoveTask()
    }

    // Override the default dispatchTouchEvent to remove focus when tapping outside of an EditText
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
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
        return super.dispatchTouchEvent(event)
    }

    override fun finishAndRemoveTask() {
        overrideExitAnimation()
        super.finishAndRemoveTask()
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

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun overrideAnimations() {
        overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, R.anim.slide_up, 0)
        overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, R.anim.slide_down)
    }

    private fun overrideEnterAnimation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideAnimations()
            return
        }

        overridePendingTransition(R.anim.slide_up, 0)
    }

    @Suppress("DEPRECATION")
    private fun overrideExitAnimation() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overridePendingTransition(0, R.anim.slide_down)
        }
    }
}
