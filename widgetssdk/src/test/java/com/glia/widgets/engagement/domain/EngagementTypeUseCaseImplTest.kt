package com.glia.widgets.engagement.domain

import com.glia.androidsdk.Engagement.MediaType
import com.glia.androidsdk.comms.Audio
import com.glia.androidsdk.comms.MediaState
import com.glia.androidsdk.comms.Video
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.reactivex.rxjava3.core.Flowable
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class EngagementTypeUseCaseImplTest {

    @MockK
    private lateinit var isQueueingOrLiveEngagementUseCase: IsQueueingOrLiveEngagementUseCase
    @MockK
    private lateinit var isCurrentEngagementCallVisualizerUseCase: IsCurrentEngagementCallVisualizerUseCase
    @MockK
    private lateinit var screenSharingUseCase: ScreenSharingUseCase
    @MockK
    private lateinit var operatorMediaUseCase: OperatorMediaUseCase
    @MockK
    private lateinit var visitorMediaUseCase: VisitorMediaUseCase
    @MockK
    private lateinit var isOperatorPresentUseCase: IsOperatorPresentUseCase

    private lateinit var useCase: EngagementTypeUseCaseImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = EngagementTypeUseCaseImpl(
            isQueueingOrLiveEngagementUseCase,
            isCurrentEngagementCallVisualizerUseCase,
            screenSharingUseCase,
            operatorMediaUseCase,
            visitorMediaUseCase,
            isOperatorPresentUseCase
        )
    }

    @Test
    fun `invoke returns VIDEO when operator has only video`() {
        val operatorMediaState = mockk<MediaState>()
        val video = mockk<Video>()
        every { operatorMediaState.video } returns video
        every { operatorMediaState.audio } returns null
        every { operatorMediaUseCase() } returns Flowable.just(operatorMediaState)

        val result = useCase().blockingFirst()
        assertEquals(MediaType.VIDEO, result)
    }

    @Test
    fun `invoke returns AUDIO when operator has only audio`() {
        val operatorMediaState = mockk<MediaState>()
        val audio = mockk<Audio>()
        every { operatorMediaState.video } returns null
        every { operatorMediaState.audio } returns audio
        every { operatorMediaUseCase() } returns Flowable.just(operatorMediaState)

        val result = useCase().blockingFirst()
        assertEquals(MediaType.AUDIO, result)
    }

    @Test
    fun `invoke returns VIDEO when operator has audio and video`() {
        val operatorMediaState = mockk<MediaState>()
        val video = mockk<Video>()
        val audio = mockk<Audio>()
        every { operatorMediaState.video } returns video
        every { operatorMediaState.audio } returns audio
        every { operatorMediaUseCase() } returns Flowable.just(operatorMediaState)

        val result = useCase().blockingFirst()
        assertEquals(MediaType.VIDEO, result)
    }

    @Test
    fun `invoke returns UNKNOWN when operator has no media`() {
        val operatorMediaState = mockk<MediaState>()
        every { operatorMediaState.video } returns null
        every { operatorMediaState.audio } returns null
        every { operatorMediaUseCase() } returns Flowable.just(operatorMediaState)

        val result = useCase().blockingFirst()
        assertEquals(MediaType.UNKNOWN, result)
    }
}
