package com.glia.widgets.internal.secureconversations.domain

import com.glia.widgets.engagement.EngagementRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ShouldMarkMessagesReadUseCaseTest {
    private lateinit var engagementRepository: EngagementRepository
    private lateinit var useCase: ShouldMarkMessagesReadUseCase

    @Before
    fun setUp() {
        engagementRepository = mockk()
        useCase = ShouldMarkMessagesReadUseCase(engagementRepository)
    }

    @Test
    fun `invoke returns true when there is no ongoing live engagement`() {
        every { engagementRepository.hasOngoingLiveEngagement } returns false
        val result = useCase()
        assertTrue(result)
    }

    @Test
    fun `invoke returns true when the current engagement action on end is retain`() {
        every { engagementRepository.isRetainAfterEnd } returns true
        every { engagementRepository.hasOngoingLiveEngagement } returns true
        val result = useCase()
        assertTrue(result)
    }

    @Test
    fun `invoke returns false when the current engagement action on end is not retain`() {
        every { engagementRepository.isRetainAfterEnd } returns false
        every { engagementRepository.hasOngoingLiveEngagement } returns true
        val result = useCase()
        assertFalse(result)
    }
}
