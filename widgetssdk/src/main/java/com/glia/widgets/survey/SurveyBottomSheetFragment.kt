package com.glia.widgets.survey

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import androidx.fragment.app.viewModels
import com.glia.androidsdk.engagement.Survey
import com.glia.telemetry_lib.GliaLogger
import com.glia.telemetry_lib.LogEvents
import com.glia.widgets.HostActivity
import com.glia.widgets.R
import com.glia.widgets.base.BaseBottomSheetFragment
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.hideKeyboard
import com.glia.widgets.helper.insetsController
import com.glia.widgets.helper.showToast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

/**
 * BottomSheetDialogFragment hosting SurveyView with MVI architecture.
 *
 * Displayed as a bottom sheet over the current content (Chat, Call, or transparent).
 * Provides proper bottom sheet behavior (dragging, collapsing, etc.).
 */
internal class SurveyBottomSheetFragment :
    BaseBottomSheetFragment<SurveyUiState, SurveyEffect, SurveyViewModel>() {

    internal companion object {
        internal const val ARG_SURVEY = "arg_survey"
    }

    private var surveyView: SurveyView? = null
    override val viewModel: SurveyViewModel by viewModels { Dependencies.viewModelFactory }
    private val localeProvider by lazy { Dependencies.localeProvider }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog: BottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.behavior.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
            isDraggable = true
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        surveyView = SurveyView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        return surveyView!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Initialize survey from arguments before base class calls setupViews/handleState/handleEffect
        arguments?.let { args ->
            BundleCompat.getParcelable(args, ARG_SURVEY, Survey::class.java)?.let { survey ->
                viewModel.processIntent(SurveyIntent.Initialize(survey))
            }
        }
        super.onViewCreated(view, savedInstanceState)
        GliaLogger.i(LogEvents.SURVEY_SCREEN_SHOWN)
    }

    override fun setupViews() {
        surveyView?.apply {
            setOnAnswerListener { answer ->
                viewModel.processIntent(SurveyIntent.AnswerQuestion(answer))
            }
            setOnSubmitClickListener {
                viewModel.processIntent(SurveyIntent.SubmitSurvey)
            }
            setOnCancelClickListener {
                viewModel.processIntent(SurveyIntent.CancelSurvey)
            }
        }
    }

    override fun handleState(state: SurveyUiState) {
        surveyView?.renderState(
            SurveyState.Builder()
                .setTitle(state.title)
                .setQuestions(state.questions)
                .createSurveyState()
        )
    }

    override fun handleEffect(effect: SurveyEffect) {
        when (effect) {
            SurveyEffect.Dismiss -> {
                dismissAllowingStateLoss()
                (activity as? HostActivity)?.finishIfEmpty()
            }
            SurveyEffect.HideSoftKeyboard -> {
                surveyView?.insetsController?.hideKeyboard()
            }
            is SurveyEffect.ScrollToQuestion -> {
                surveyView?.scrollTo(effect.index)
            }
            SurveyEffect.ShowNetworkError -> {
                context?.showToast(localeProvider.getString(R.string.glia_survey_network_unavailable))
            }
        }
    }

    override fun onDestroyView() {
        GliaLogger.i(LogEvents.SURVEY_SCREEN_CLOSED)
        super.onDestroyView()
        surveyView = null
    }
}