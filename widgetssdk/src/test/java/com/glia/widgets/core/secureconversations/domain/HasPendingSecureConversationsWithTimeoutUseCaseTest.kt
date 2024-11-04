package com.glia.widgets.core.secureconversations.domain

import com.glia.widgets.chat.domain.IsAuthenticatedUseCase
import com.glia.widgets.core.secureconversations.SecureConversationsRepository
import com.glia.widgets.di.GliaCore
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class HasPendingSecureConversationsWithTimeoutUseCaseTest {
    private lateinit var repository: SecureConversationsRepository
    private lateinit var useCase: HasPendingSecureConversationsWithTimeoutUseCase
    private lateinit var isAuthenticatedUseCase: IsAuthenticatedUseCase
    private lateinit var core: GliaCore

    @Before
    fun setUp() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        repository = mockk()
        isAuthenticatedUseCase = mockk()
        core = mockk()
        useCase = HasPendingSecureConversationsWithTimeoutUseCase(repository, isAuthenticatedUseCase, core)
    }

    @Test
    fun `invoke returns false when GliaCore is not initialized`() {
        every { core.isInitialized } returns false

        val result = useCase().blockingGet()
        assertEquals(false, result)
    }

    @Test
    fun `invoke returns false when user is not authenticated`() {
        every { core.isInitialized } returns true
        every { isAuthenticatedUseCase() } returns false

        val result = useCase().blockingGet()
        assertEquals(false, result)
    }

    @Test
    fun `invoke returns false when there is no answer during timeout`() {
        mockInitializedAndAuthenticated()

        every { repository.getHasPendingSecureConversations() } returns Single.never()

        val result = useCase().blockingGet()
        assertEquals(false, result)
    }

    @Test
    fun `invoke returns false when error is returned`() {
        mockInitializedAndAuthenticated()

        every { repository.getHasPendingSecureConversations() } returns Single.error(Exception("Error"))

        val result = useCase().blockingGet()
        assertEquals(false, result)
    }

    @Test
    fun `invoke returns true when there are pending secure conversations`() {
        mockInitializedAndAuthenticated()

        every { repository.getHasPendingSecureConversations() } returns Single.just(true)

        val result = useCase().blockingGet()
        assertEquals(true, result)
    }

    @Test
    fun `invoke returns false when there are no pending secure conversations`() {
        mockInitializedAndAuthenticated()

        every { repository.getHasPendingSecureConversations() } returns Single.just(false)

        val result = useCase().blockingGet()
        assertEquals(false, result)
    }

    private fun mockInitializedAndAuthenticated() {
        every { core.isInitialized } returns true
        every { isAuthenticatedUseCase() } returns true
    }
}
