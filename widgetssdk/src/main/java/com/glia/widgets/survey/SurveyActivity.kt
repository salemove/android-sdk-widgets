package com.glia.widgets.survey

import android.os.Bundle
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.R
import com.glia.widgets.base.FadeTransitionActivity
import com.glia.widgets.base.GliaFragmentContract
import com.glia.widgets.helper.ExtraKeys
import com.glia.widgets.helper.getParcelableExtraCompat
import com.glia.widgets.locale.LocaleString

/**
 * This activity hosts [SurveyFragment] and serves as an entry point for post-engagement surveys.
 *
 * **Architecture:** This Activity is a thin wrapper that hosts the Fragment. All UI logic
 * is implemented in [SurveyFragment] and [SurveyView]. This Activity handles Intent-based
 * launches for backwards compatibility.
 *
 * This activity is used to display post-engagement surveys.
 *
 * It will be automatically added to the integrator's manifest file by the manifest merger during compilation.
 *
 * @see SurveyFragment
 * @see SurveyView
 */
internal class SurveyActivity : FadeTransitionActivity(), GliaFragmentContract.Host {
    private var surveyFragment: SurveyFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.survey_activity_host)

        if (savedInstanceState == null) {
            val survey = intent.getParcelableExtraCompat<Survey>(ExtraKeys.SURVEY)
                ?: error("Survey must be provided")

            surveyFragment = SurveyFragment.newInstance(survey)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, surveyFragment!!)
                .commit()
        } else {
            surveyFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as? SurveyFragment
        }
    }

    override fun setHostTitle(locale: LocaleString?) {
        setTitle(locale)
    }

    override fun finish() = super.finish()
}
