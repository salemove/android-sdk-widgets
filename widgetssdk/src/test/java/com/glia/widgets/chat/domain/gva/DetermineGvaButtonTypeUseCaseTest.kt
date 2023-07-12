package com.glia.widgets.chat.domain.gva

import com.glia.androidsdk.chat.SingleChoiceAttachment
import com.glia.widgets.chat.model.Gva
import com.glia.widgets.chat.model.GvaButton
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DetermineGvaButtonTypeUseCaseTest {
    private lateinit var useCase: DetermineGvaButtonTypeUseCase
    private lateinit var determineGvaUrlTypeUseCase: DetermineGvaUrlTypeUseCase
    private lateinit var gvaButton: GvaButton

    @Before
    fun setUp() {
        determineGvaUrlTypeUseCase = mock()
        useCase = DetermineGvaButtonTypeUseCase(determineGvaUrlTypeUseCase)
        gvaButton = mock()
    }

    @Test
    fun `invoke returns Broadcast type when destinationPbBroadcastEvent is defined`() {
        whenever(gvaButton.destinationPbBroadcastEvent) doReturn "stub!"

        val buttonType = useCase(gvaButton)
        assertTrue(buttonType is Gva.ButtonType.BroadcastEvent)
    }

    @Test
    fun `invoke returns PostBack type when url is null or empty`() {
        whenever(gvaButton.url) doReturn null
        val value = "value"
        val text = "text"
        whenever(gvaButton.toResponse()) doReturn SingleChoiceAttachment.from(value, text)

        val buttonType = useCase(gvaButton)
        assertTrue(buttonType is Gva.ButtonType.PostBack)
        (buttonType as Gva.ButtonType.PostBack).apply {
            assertEquals(value, singleChoiceAttachment.selectedOption)
            assertEquals(text, singleChoiceAttachment.selectedOptionText)
        }
    }

    @Test
    fun `invoke returns Url type when url is defined and not empty`() {
        whenever(gvaButton.url) doReturn "asasasasa"
        whenever(determineGvaUrlTypeUseCase(any())) doReturn Gva.ButtonType.Url(mock())

        val buttonType = useCase(gvaButton)
        assertTrue(buttonType is Gva.ButtonType.Url)
    }
}
