package com.glia.widgets.engagement.domain

import com.glia.widgets.engagement.EndAction
import com.glia.widgets.engagement.MediaType
import com.glia.widgets.engagement.State
import com.glia.widgets.lifecycle.GliaEvent
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Flowable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GliaLifecycleEventUseCaseImplTest {

    private lateinit var engagementStateUseCase: EngagementStateUseCase
    private lateinit var engagementTypeUseCase: EngagementTypeUseCase
    private lateinit var useCase: GliaLifecycleEventUseCaseImpl

    @Before
    fun setUp() {
        engagementStateUseCase = mockk()
        engagementTypeUseCase = mockk()
        every { engagementStateUseCase() } returns Flowable.empty()
        every { engagementTypeUseCase() } returns Flowable.empty()
        useCase = GliaLifecycleEventUseCaseImpl(engagementStateUseCase, engagementTypeUseCase)
    }

    @Test
    fun `invoke emits EngagementStarted when engagement started`() {
        every { engagementStateUseCase() } returns Flowable.just(State.EngagementStarted(isCallVisualizer = false))

        val result = useCase().blockingFirst()

        assertEquals(GliaEvent.EngagementStarted, result)
    }

    @Test
    fun `invoke emits EngagementEnded when engagement ended`() {
        every { engagementStateUseCase() } returns Flowable.just(State.EngagementEnded(EndAction.ClearStateRegular))

        val result = useCase().blockingFirst()

        assertEquals(GliaEvent.EngagementEnded, result)
    }

    @Test
    fun `invoke emits nothing for states with no lifecycle event mapping`() {
        every { engagementStateUseCase() } returns Flowable.just<State>(
            State.NoEngagement,
            State.Queuing("ticketId", MediaType.AUDIO),
            State.PreQueuing(MediaType.AUDIO),
            State.QueueingCanceled,
            State.TransferredToSecureConversation,
            State.QueueUnstaffed,
            State.UnexpectedErrorHappened
        )

        val result = useCase().toList().blockingGet()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `invoke emits EngagementOngoing for each media type change`() {
        every { engagementTypeUseCase() } returns Flowable.just(MediaType.TEXT, MediaType.AUDIO, MediaType.VIDEO)

        val result = useCase().toList().blockingGet()

        assertEquals(
            listOf(
                GliaEvent.EngagementOngoing(MediaType.TEXT),
                GliaEvent.EngagementOngoing(MediaType.AUDIO),
                GliaEvent.EngagementOngoing(MediaType.VIDEO)
            ),
            result
        )
    }

    @Test
    fun `invoke does not emit EngagementOngoing for UNKNOWN media type`() {
        // UNKNOWN is emitted when there's no ongoing engagement at all (e.g. right after SDK
        // init, or after an engagement ends) - it must not be reported as "ongoing".
        every { engagementTypeUseCase() } returns Flowable.just(MediaType.UNKNOWN, MediaType.AUDIO, MediaType.UNKNOWN)

        val result = useCase().toList().blockingGet()

        assertEquals(listOf(GliaEvent.EngagementOngoing(MediaType.AUDIO)), result)
    }

    @Test
    fun `invoke does not emit duplicate consecutive EngagementOngoing events`() {
        every { engagementTypeUseCase() } returns Flowable.just(MediaType.AUDIO, MediaType.AUDIO, MediaType.VIDEO)

        val result = useCase().toList().blockingGet()

        assertEquals(
            listOf(
                GliaEvent.EngagementOngoing(MediaType.AUDIO),
                GliaEvent.EngagementOngoing(MediaType.VIDEO)
            ),
            result
        )
    }
}
