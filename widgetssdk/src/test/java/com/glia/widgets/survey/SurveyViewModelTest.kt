package com.glia.widgets.survey

import app.cash.turbine.test
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.engagement.Survey
import com.glia.widgets.internal.survey.domain.GliaSurveyAnswerUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.function.Consumer

@OptIn(ExperimentalCoroutinesApi::class)
class SurveyViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var surveyAnswerUseCase: GliaSurveyAnswerUseCase
    private lateinit var viewModel: SurveyViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        surveyAnswerUseCase = mock()
        viewModel = SurveyViewModel(surveyAnswerUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // region Initialize tests

    @Test
    fun `initialize sets title and questions from survey`() = runTest {
        val survey = createMockSurvey()

        viewModel.processIntent(SurveyIntent.Initialize(survey))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(SURVEY_TITLE, state.title)
        assertEquals(2, state.questions.size)
        assertFalse(state.isSubmitting)
    }

    @Test
    fun `initialize with same survey does not update state again`() = runTest {
        val survey = createMockSurvey()

        // First initialization
        viewModel.processIntent(SurveyIntent.Initialize(survey))
        testDispatcher.scheduler.advanceUntilIdle()

        val firstState = viewModel.state.value
        assertEquals(2, firstState.questions.size)

        // Second initialization with same survey should be skipped
        viewModel.processIntent(SurveyIntent.Initialize(survey))
        testDispatcher.scheduler.advanceUntilIdle()

        // State should remain the same
        assertEquals(firstState, viewModel.state.value)
    }

    @Test
    fun `initialize with different survey updates state`() = runTest {
        val survey1 = createMockSurvey(surveyId = "survey1")
        val survey2 = createMockSurvey(surveyId = "survey2", title = "Different Title")

        viewModel.processIntent(SurveyIntent.Initialize(survey1))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(SURVEY_TITLE, viewModel.state.value.title)

        viewModel.processIntent(SurveyIntent.Initialize(survey2))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("Different Title", viewModel.state.value.title)
    }

    @Test
    fun `initialize sets default answer for single choice question with default option`() = runTest {
        val defaultOption: Survey.Question.Option = mock {
            on { id } doAnswer { "option1" }
            on { isDefault } doAnswer { true }
        }
        val question: Survey.Question = mock {
            on { id } doAnswer { "q1" }
            on { type } doAnswer { Survey.Question.QuestionType.SINGLE_CHOICE }
            on { options } doAnswer { listOf(defaultOption) }
        }
        val survey: Survey = mock {
            on { id } doAnswer { SURVEY_ID }
            on { engagementId } doAnswer { ENGAGEMENT_ID }
            on { title } doAnswer { SURVEY_TITLE }
            on { questions } doAnswer { listOf(question) }
        }

        viewModel.processIntent(SurveyIntent.Initialize(survey))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(1, state.questions.size)
        val questionItem = state.questions[0]
        assertEquals("q1", questionItem.answer?.questionId)
    }

    // endregion

    // region AnswerQuestion tests

    @Test
    fun `answerQuestion updates question item with answer`() = runTest {
        val survey = createMockSurvey()
        viewModel.processIntent(SurveyIntent.Initialize(survey))
        testDispatcher.scheduler.advanceUntilIdle()

        val answer: Survey.Answer = mock {
            on { questionId } doAnswer { "q1" }
        }
        viewModel.processIntent(SurveyIntent.AnswerQuestion(answer))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        val questionItem = state.questions.find { it.question.id == "q1" }
        assertEquals(answer, questionItem?.answer)
    }

    @Test
    fun `answerQuestion clears error on question`() = runTest {
        val survey = createMockSurvey()
        viewModel.processIntent(SurveyIntent.Initialize(survey))
        testDispatcher.scheduler.advanceUntilIdle()

        // Manually set error state
        val state = viewModel.state.value
        state.questions[0].isShowError = true

        val answer: Survey.Answer = mock {
            on { questionId } doAnswer { "q1" }
        }
        viewModel.processIntent(SurveyIntent.AnswerQuestion(answer))
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedItem = viewModel.state.value.questions.find { it.question.id == "q1" }
        assertFalse(updatedItem?.isShowError ?: true)
    }

    @Test
    fun `answerQuestion for non-text question emits HideSoftKeyboard effect`() = runTest {
        val survey = createMockSurvey()
        viewModel.processIntent(SurveyIntent.Initialize(survey))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            val answer: Survey.Answer = mock {
                on { questionId } doAnswer { "q1" }  // q1 is BOOLEAN type
            }
            viewModel.processIntent(SurveyIntent.AnswerQuestion(answer))
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(SurveyEffect.HideSoftKeyboard, awaitItem())
        }
    }

    @Test
    fun `answerQuestion for text question does not emit HideSoftKeyboard effect`() = runTest {
        val survey = createMockSurvey()
        viewModel.processIntent(SurveyIntent.Initialize(survey))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            val answer: Survey.Answer = mock {
                on { questionId } doAnswer { "q2" }  // q2 is TEXT type
            }
            viewModel.processIntent(SurveyIntent.AnswerQuestion(answer))
            testDispatcher.scheduler.advanceUntilIdle()

            expectNoEvents()
        }
    }

    // endregion

    // region SubmitSurvey tests

    @Test
    fun `submitSurvey calls useCase and emits Dismiss on success`() = runTest {
        val survey = createMockSurvey()
        viewModel.processIntent(SurveyIntent.Initialize(survey))
        testDispatcher.scheduler.advanceUntilIdle()

        // Setup successful submission
        whenever(surveyAnswerUseCase.submit(any(), eq(survey), any())).doAnswer { invocation ->
            @Suppress("UNCHECKED_CAST")
            val callback = invocation.arguments[2] as Consumer<RuntimeException?>
            callback.accept(null)  // Success
        }

        viewModel.effect.test {
            viewModel.processIntent(SurveyIntent.SubmitSurvey)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(SurveyEffect.Dismiss, awaitItem())
        }
    }

    @Test
    fun `submitSurvey sets isSubmitting while processing`() = runTest {
        val survey = createMockSurvey()
        viewModel.processIntent(SurveyIntent.Initialize(survey))
        testDispatcher.scheduler.advanceUntilIdle()

        // Setup submission that calls back after we check state
        var capturedCallback: Consumer<RuntimeException?>? = null
        whenever(surveyAnswerUseCase.submit(any(), eq(survey), any())).doAnswer { invocation ->
            @Suppress("UNCHECKED_CAST")
            capturedCallback = invocation.arguments[2] as Consumer<RuntimeException?>
        }

        viewModel.state.test {
            skipItems(1) // Skip initial state

            viewModel.processIntent(SurveyIntent.SubmitSurvey)
            testDispatcher.scheduler.advanceUntilIdle()

            val submittingState = awaitItem()
            assertTrue(submittingState.isSubmitting)

            // Complete the submission
            capturedCallback?.accept(null)

            val completedState = awaitItem()
            assertFalse(completedState.isSubmitting)
        }
    }

    @Test
    fun `submitSurvey emits ShowNetworkError on network timeout`() = runTest {
        val survey = createMockSurvey()
        viewModel.processIntent(SurveyIntent.Initialize(survey))
        testDispatcher.scheduler.advanceUntilIdle()

        val networkException = GliaException("Network timeout", GliaException.Cause.NETWORK_TIMEOUT)
        whenever(surveyAnswerUseCase.submit(any(), eq(survey), any())).doAnswer { invocation ->
            @Suppress("UNCHECKED_CAST")
            val callback = invocation.arguments[2] as Consumer<RuntimeException?>
            callback.accept(networkException)
        }

        viewModel.effect.test {
            viewModel.processIntent(SurveyIntent.SubmitSurvey)
            testDispatcher.scheduler.advanceUntilIdle()

            // First effect should be ShowNetworkError
            val effect = awaitItem()
            assertEquals(SurveyEffect.ShowNetworkError, effect)

            // Cancel any remaining effects
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `submitSurvey emits ScrollToQuestion when validation fails`() = runTest {
        val survey = createMockSurveyWithRequiredQuestion()
        viewModel.processIntent(SurveyIntent.Initialize(survey))
        testDispatcher.scheduler.advanceUntilIdle()

        val validationException: SurveyValidationException = mock()
        whenever(surveyAnswerUseCase.submit(any(), eq(survey), any())).doAnswer { invocation ->
            @Suppress("UNCHECKED_CAST")
            val questions = invocation.arguments[0] as List<QuestionItem>
            // Mark first question as having error
            questions[0].isShowError = true
            val callback = invocation.arguments[2] as Consumer<RuntimeException?>
            callback.accept(validationException)
        }

        viewModel.effect.test {
            viewModel.processIntent(SurveyIntent.SubmitSurvey)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is SurveyEffect.ScrollToQuestion)
            assertEquals(0, (effect as SurveyEffect.ScrollToQuestion).index)
        }
    }

    @Test
    fun `submitSurvey does nothing when survey is not initialized`() = runTest {
        viewModel.effect.test {
            viewModel.processIntent(SurveyIntent.SubmitSurvey)
            testDispatcher.scheduler.advanceUntilIdle()

            expectNoEvents()
        }

        verify(surveyAnswerUseCase, never()).submit(any(), any(), any())
    }

    // endregion

    // region CancelSurvey tests

    @Test
    fun `cancelSurvey emits Dismiss effect`() = runTest {
        viewModel.effect.test {
            viewModel.processIntent(SurveyIntent.CancelSurvey)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(SurveyEffect.Dismiss, awaitItem())
        }
    }

    // endregion

    // region Helper methods

    private fun createMockSurvey(
        surveyId: String = SURVEY_ID,
        engagementId: String = ENGAGEMENT_ID,
        title: String = SURVEY_TITLE
    ): Survey {
        val question1: Survey.Question = mock {
            on { id } doAnswer { "q1" }
            on { type } doAnswer { Survey.Question.QuestionType.BOOLEAN }
            on { isRequired } doAnswer { false }
        }
        val question2: Survey.Question = mock {
            on { id } doAnswer { "q2" }
            on { type } doAnswer { Survey.Question.QuestionType.TEXT }
            on { isRequired } doAnswer { false }
        }

        return mock {
            on { id } doAnswer { surveyId }
            on { this.engagementId } doAnswer { engagementId }
            on { this.title } doAnswer { title }
            on { questions } doAnswer { listOf(question1, question2) }
        }
    }

    private fun createMockSurveyWithRequiredQuestion(): Survey {
        val question: Survey.Question = mock {
            on { id } doAnswer { "q1" }
            on { type } doAnswer { Survey.Question.QuestionType.TEXT }
            on { isRequired } doAnswer { true }
        }

        return mock {
            on { id } doAnswer { SURVEY_ID }
            on { engagementId } doAnswer { ENGAGEMENT_ID }
            on { title } doAnswer { SURVEY_TITLE }
            on { questions } doAnswer { listOf(question) }
        }
    }

    // endregion

    companion object {
        private const val SURVEY_ID = "survey_123"
        private const val ENGAGEMENT_ID = "engagement_456"
        private const val SURVEY_TITLE = "Post Chat Survey"
    }
}
