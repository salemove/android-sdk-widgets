package com.glia.widgets.survey.viewholder

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.R
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.setAccessibilityHint
import com.glia.widgets.helper.setLocaleContentDescription
import com.glia.widgets.helper.setLocaleText
import com.glia.widgets.locale.LocaleProvider
import com.glia.widgets.survey.QuestionItem
import com.glia.widgets.survey.SurveyAdapter
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private const val VIEW_EXTENSIONS_CLASS_PATH = "com.glia.widgets.helper.ViewExtensionsKt"

@RunWith(RobolectricTestRunner::class)
class SurveyViewHolderTest {

    private lateinit var itemView: View
    private lateinit var titleView: TextView
    private lateinit var requiredErrorView: TextView
    private lateinit var localeProvider: LocaleProvider
    private lateinit var listener: SurveyAdapter.SurveyAdapterListener
    private lateinit var context: Context

    @Before
    fun setUp() {
        mockkStatic(VIEW_EXTENSIONS_CLASS_PATH)
        mockkStatic(ContextCompat::class)

        localeProvider = mockk(relaxed = true)
        Dependencies.localeProvider = localeProvider

        context = mockk(relaxed = true)
        // Return a color value with enough hex digits (e.g., 0xFFFF0000 = -65536)
        // String.format("%X", 0xFFFF0000.toInt()) -> "FFFF0000", substring(2) -> "FF0000"
        every { ContextCompat.getColor(any(), any()) } returns 0xFFFF0000.toInt()
        every { context.getString(eq(R.string.glia_survey_require_label), any(), any()) } returns "<b>Question text<font color='#FF0000'>*</font></b>"

        itemView = mockk(relaxed = true)
        titleView = mockk(relaxed = true)
        every { titleView.context } returns context

        requiredErrorView = mockk(relaxed = true)

        every { requiredErrorView.setLocaleText(any<Int>()) } just Runs
        every { titleView.setLocaleContentDescription(any<Int>()) } just Runs
        every { titleView.setAccessibilityHint(any<Int>()) } just Runs

        listener = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        unmockkStatic(VIEW_EXTENSIONS_CLASS_PATH)
        unmockkStatic(ContextCompat::class)
    }

    private fun createViewHolder(): TestSurveyViewHolder {
        return TestSurveyViewHolder(itemView, titleView, requiredErrorView)
    }

    @Test
    fun `onBind sets locale content description for required question`() {
        val question = mockk<Survey.Question>(relaxed = true)
        every { question.isRequired } returns true
        every { question.text } returns "How was your experience?"

        val questionItem = QuestionItem(question, null)
        val viewHolder = createViewHolder()

        viewHolder.onBind(questionItem, listener)

        verify { titleView.setLocaleContentDescription(R.string.survey_question_required_accessibility_label) }
    }

    @Test
    fun `onBind sets accessibility hint and content description for required question`() {
        val question = mockk<Survey.Question>(relaxed = true)
        every { question.isRequired } returns true
        every { question.text } returns "How was your experience?"

        val questionItem = QuestionItem(question, null)
        val viewHolder = createViewHolder()

        viewHolder.onBind(questionItem, listener)

        verify { titleView.setLocaleContentDescription(R.string.survey_question_required_accessibility_label) }
        verify { titleView.setAccessibilityHint("How was your experience?") }
    }

    @Test
    fun `onBind sets content description directly for non-required question`() {
        val question = mockk<Survey.Question>(relaxed = true)
        every { question.isRequired } returns false
        every { question.text } returns "Any additional feedback?"

        val questionItem = QuestionItem(question, null)
        val viewHolder = createViewHolder()

        viewHolder.onBind(questionItem, listener)

        verify { titleView.contentDescription = "Any additional feedback?" }
    }

    @Test
    fun `onBind does not set accessibility hint for non-required question`() {
        val question = mockk<Survey.Question>(relaxed = true)
        every { question.isRequired } returns false
        every { question.text } returns "Any additional feedback?"

        val questionItem = QuestionItem(question, null)
        val viewHolder = createViewHolder()

        viewHolder.onBind(questionItem, listener)

        verify(exactly = 0) { titleView.setAccessibilityHint(any<Int>()) }
    }

    @Test
    fun `onBind does not set locale content description for non-required question`() {
        val question = mockk<Survey.Question>(relaxed = true)
        every { question.isRequired } returns false
        every { question.text } returns "Any additional feedback?"

        val questionItem = QuestionItem(question, null)
        val viewHolder = createViewHolder()

        viewHolder.onBind(questionItem, listener)

        verify(exactly = 0) { titleView.setLocaleContentDescription(any<Int>()) }
    }

    @Test
    fun `onBind sets title text to question text for non-required question`() {
        val question = mockk<Survey.Question>(relaxed = true)
        every { question.isRequired } returns false
        every { question.text } returns "Any additional feedback?"

        val questionItem = QuestionItem(question, null)
        val viewHolder = createViewHolder()

        viewHolder.onBind(questionItem, listener)

        verify { titleView.text = "Any additional feedback?" }
    }

    /**
     * Minimal concrete subclass of [SurveyViewHolder] for testing purposes.
     */
    private class TestSurveyViewHolder(
        itemView: View,
        title: TextView,
        requiredError: TextView
    ) : SurveyViewHolder(itemView, title, requiredError) {
        override fun answerCallback(showError: Boolean) {
            showRequiredError(showError)
        }
    }
}
