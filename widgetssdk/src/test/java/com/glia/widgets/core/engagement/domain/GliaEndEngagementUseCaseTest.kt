package com.glia.widgets.core.engagement.domain

import com.glia.widgets.chat.data.ChatScreenRepository
import com.glia.widgets.core.engagement.GliaEngagementRepository
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class GliaEndEngagementUseCaseTest {
    private lateinit var useCase: GliaEndEngagementUseCase
    private lateinit var endEngagementRepository: GliaEngagementRepository
    private lateinit var chatScreenRepository: ChatScreenRepository

    @Before
    fun setUp() {
        endEngagementRepository = mock()
        chatScreenRepository = mock()
        useCase = GliaEndEngagementUseCase(endEngagementRepository, chatScreenRepository)
    }

    @Test
    fun `gliaEndEngagementUseCase resets ChatScreenRepository when invoked`() {
        useCase()
        verify(chatScreenRepository).isFromCallScreen = false
    }

    @Test
    fun `gliaEndEngagementUseCase calls GliaEngagementRepository endEngagement when invoked`() {
        useCase()
        verify(endEngagementRepository).endEngagement()
    }
}
