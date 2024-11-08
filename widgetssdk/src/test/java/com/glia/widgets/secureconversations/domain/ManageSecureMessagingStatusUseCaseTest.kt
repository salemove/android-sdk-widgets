package com.glia.widgets.secureconversations.domain

import com.glia.widgets.core.secureconversations.domain.ManageSecureMessagingStatusUseCase
import com.glia.widgets.engagement.EngagementRepository
import com.glia.widgets.engagement.domain.IsQueueingOrEngagementUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test

class ManageSecureMessagingStatusUseCaseTest {

    private lateinit var isQueueingOrEngagementUseCase: IsQueueingOrEngagementUseCase
    private lateinit var engagementRepository: EngagementRepository
    private lateinit var useCase: ManageSecureMessagingStatusUseCase

    @Before
    fun setUp() {
        isQueueingOrEngagementUseCase = mockk()
        engagementRepository = mockk(relaxed = true)
        useCase = ManageSecureMessagingStatusUseCase(isQueueingOrEngagementUseCase, engagementRepository)
    }

    @Test
    fun `shouldUseSecureMessagingEndpoints returns true when secure messaging is requested and not queueing or engaged`() {
        every { engagementRepository.isSecureMessagingRequested } returns true
        every { isQueueingOrEngagementUseCase() } returns false

        val result = useCase.shouldUseSecureMessagingEndpoints()

        assertTrue(result)
    }

    @Test
    fun `shouldUseSecureMessagingEndpoints returns false when secure messaging is not requested`() {
        every { engagementRepository.isSecureMessagingRequested } returns false

        val result = useCase.shouldUseSecureMessagingEndpoints()

        assertFalse(result)
    }

    @Test
    fun `shouldUseSecureMessagingEndpoints returns false when queueing or engaged`() {
        every { engagementRepository.isSecureMessagingRequested } returns true
        every { isQueueingOrEngagementUseCase() } returns true

        val result = useCase.shouldUseSecureMessagingEndpoints()

        assertFalse(result)
    }

    @Test
    fun `shouldBehaveAsSecureMessaging returns true when secure messaging is requested`() {
        every { engagementRepository.isSecureMessagingRequested } returns true

        val result = useCase.shouldBehaveAsSecureMessaging()

        assertTrue(result)
    }

    @Test
    fun `shouldBehaveAsSecureMessaging returns false when secure messaging is not requested`() {
        every { engagementRepository.isSecureMessagingRequested } returns false

        val result = useCase.shouldBehaveAsSecureMessaging()

        assertFalse(result)
    }

    @Test
    fun `updateSecureMessagingStatus updates the secure messaging status in the repository`() {
        val isRequested = true

        useCase.updateSecureMessagingStatus(isRequested)

        verify { engagementRepository.updateIsSecureMessagingRequested(isRequested) }
    }
}
