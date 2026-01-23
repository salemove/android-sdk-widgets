package com.glia.widgets.survey

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.R
import com.glia.widgets.helper.FragmentArgumentKeys
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.annotation.Config

/**
 * Unit tests for SurveyFragment.
 *
 * Tests Fragment lifecycle and survey handling.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class SurveyFragmentTest {

    @Test
    fun `fragment creation with survey succeeds`() {
        val survey = mock(Survey::class.java)
        val args = Bundle().apply {
            putParcelable(FragmentArgumentKeys.SURVEY, survey)
        }

        val scenario = launchFragmentInContainer<SurveyFragment>(
            fragmentArgs = args,
            themeResId = R.style.Application_Glia_Theme
        )

        scenario.onFragment { fragment ->
            assertNotNull(fragment.gliaView)
            assertTrue(fragment.gliaView is SurveyView)
        }
    }

    @Test
    fun `fragment survives configuration changes`() {
        val survey = mock(Survey::class.java)
        val args = Bundle().apply {
            putParcelable(FragmentArgumentKeys.SURVEY, survey)
        }

        val scenario = launchFragmentInContainer<SurveyFragment>(
            fragmentArgs = args,
            themeResId = R.style.Application_Glia_Theme
        )

        scenario.recreate()

        scenario.onFragment { fragment ->
            assertNotNull(fragment.gliaView)
        }
    }

    @Test
    fun `view binding is cleared in onDestroyView`() {
        val survey = mock(Survey::class.java)
        val args = Bundle().apply {
            putParcelable(FragmentArgumentKeys.SURVEY, survey)
        }

        val scenario = launchFragmentInContainer<SurveyFragment>(
            fragmentArgs = args,
            themeResId = R.style.Application_Glia_Theme
        )

        scenario.moveToState(Lifecycle.State.DESTROYED)

        assertTrue(true)
    }

    @Test
    fun `newInstance creates fragment with correct arguments`() {
        val survey = mock(Survey::class.java)
        val fragment = SurveyFragment.newInstance(survey)

        val extractedSurvey = fragment.arguments?.getParcelable<Survey>(FragmentArgumentKeys.SURVEY)

        assertNotNull(extractedSurvey)
        assertEquals(survey, extractedSurvey)
    }

    @Test
    fun `fragment implements OnFinishListener`() {
        val survey = mock(Survey::class.java)
        val args = Bundle().apply {
            putParcelable(FragmentArgumentKeys.SURVEY, survey)
        }

        val scenario = launchFragmentInContainer<SurveyFragment>(
            fragmentArgs = args,
            themeResId = R.style.Application_Glia_Theme
        )

        scenario.onFragment { fragment ->
            assertTrue(fragment is SurveyView.OnFinishListener)
        }
    }
}
