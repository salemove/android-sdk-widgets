package com.glia.widgets.survey

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import com.glia.androidsdk.engagement.Survey
import com.glia.telemetry_lib.GliaLogger
import com.glia.telemetry_lib.LogEvents
import com.glia.widgets.R
import com.glia.widgets.base.GliaFragment
import com.glia.widgets.base.GliaFragmentContract
import com.glia.widgets.databinding.SurveyFragmentBinding
import com.glia.widgets.di.Dependencies.controllerFactory
import com.glia.widgets.helper.FragmentArgumentKeys
import com.glia.widgets.helper.getParcelable
import com.glia.widgets.helper.insetsController
import com.glia.widgets.helper.insetsControllerCompat

/**
 * Fragment for displaying post-engagement surveys.
 *
 * This Fragment is hosted by [SurveyActivity] which handles Intent-based launches for backwards compatibility.
 *
 * @see SurveyActivity
 * @see SurveyView
 */
internal class SurveyFragment : GliaFragment(), SurveyView.OnFinishListener {
    private var _binding: SurveyFragmentBinding? = null
    private val binding get() = _binding!!

    private val surveyView: SurveyView
        get() = binding.surveyView

    private var host: GliaFragmentContract.Host? = null

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            host?.finish()
        }
    }

    override val gliaView: View
        get() = surveyView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireActivity().enableEdgeToEdge()
        requireActivity().window.insetsControllerCompat.isAppearanceLightStatusBars = false

        _binding = SurveyFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        host = activity as? GliaFragmentContract.Host

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )

        prepareSurveyView()

        val slideUpAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
        surveyView.startAnimation(slideUpAnim)

        GliaLogger.i(LogEvents.SURVEY_SCREEN_SHOWN)

        view.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (finishActivityWithAnimationWhenTappedOutsideSurveyView(event)) {
                    return@setOnTouchListener true
                }
                removeFocusWhenTappedOutsideSurveyEditText(event)
            }
            false
        }
    }

    override fun onDestroyView() {
        hideSoftKeyboard()
        surveyView.onDestroyView()
        _binding = null
        GliaLogger.i(LogEvents.SURVEY_SCREEN_CLOSED)
        super.onDestroyView()
    }

    override fun onDetach() {
        super.onDetach()
        host = null
    }

    override fun onFinish() {
        val slideDownAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down)
        surveyView.startAnimation(slideDownAnim)
        host?.finish()
    }

    private fun finishActivityWithAnimationWhenTappedOutsideSurveyView(event: MotionEvent): Boolean {
        val surveyView: View? = view?.findViewById(R.id.survey_view)
        if (surveyView != null) {
            val location = IntArray(2)
            surveyView.getLocationOnScreen(location)
            val x: Float = event.rawX
            val y: Float = event.rawY
            val isTappedOutsideSurveyView =
                x < location[0] || x > location[0] + surveyView.width || y < location[1] || y > location[1] + surveyView.height
            if (isTappedOutsideSurveyView) {
                onFinish()
                return true // consume the event
            }
        }
        return false
    }

    private fun removeFocusWhenTappedOutsideSurveyEditText(event: MotionEvent) {
        (activity?.currentFocus as? EditText)?.let { editText ->
            val outRect = Rect()
            editText.getGlobalVisibleRect(outRect)
            if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                editText.clearFocus()
                (requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(editText.windowToken, 0)
            }
        }
    }

    private fun hideSoftKeyboard() {
        requireView().insetsController?.let { controller ->
            controller.hide(androidx.core.view.WindowInsetsCompat.Type.ime())
        }
    }

    private fun prepareSurveyView() {
        surveyView.setOnTitleUpdatedListener(object : SurveyView.OnTitleUpdatedListener {
            override fun onTitleUpdated(title: String?) {
                requireActivity().setTitle(title)
            }
        })

        surveyView.setOnFinishListener(this)
        val surveyController = controllerFactory.surveyController
        surveyView.setController(surveyController)

        arguments?.getParcelable<Survey>(FragmentArgumentKeys.SURVEY)?.let {
            surveyController.init(it)
        }
    }

    companion object {
        /**
         * Create a new instance of SurveyFragment with the given survey.
         *
         * @param survey The survey to display
         * @return A new SurveyFragment instance
         */
        fun newInstance(survey: Survey): SurveyFragment {
            return SurveyFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(FragmentArgumentKeys.SURVEY, survey)
                }
            }
        }
    }
}
