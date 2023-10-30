package com.glia.widgets.view.snackbar

import com.glia.androidsdk.omnibrowse.OmnibrowseEngagement
import com.glia.androidsdk.omnicore.OmnicoreEngagement
import com.glia.widgets.core.engagement.GliaEngagementRepository
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.KArgumentCaptor
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.util.function.Consumer

class LiveObservationUseCaseTest {

    private lateinit var useCase: LiveObservationUseCase
    private val repository: GliaEngagementRepository = mock()
    private val omnicoreEngagement: OmnicoreEngagement = mock()
    private val omnibrowseEngagement: OmnibrowseEngagement = mock()
    private val startCallback: Runnable = mock()

    private val omnicoreCaptor: KArgumentCaptor<Consumer<OmnicoreEngagement>> = argumentCaptor()
    private val omnibrowseEngagementCaptor: KArgumentCaptor<Consumer<OmnibrowseEngagement>> = argumentCaptor()

    @Before
    fun setUp() {
        useCase = LiveObservationUseCase(repository).apply { init() }
        verify(repository).listenForOmnicoreEngagement(omnicoreCaptor.capture())
        verify(repository).listenForCallVisualizerEngagement(omnibrowseEngagementCaptor.capture())
    }

    @After
    fun tearDown() {
        Mockito.clearInvocations(
            repository,
            omnicoreEngagement,
            omnibrowseEngagement
        )
    }

    @Test
    fun `LiveObservationUseCase triggers startCallback when omnicore engagement started`() {
        useCase(startCallback)
        omnicoreCaptor.lastValue.accept(omnicoreEngagement)
        verify(startCallback).run()
    }

    @Test
    fun `LiveObservationUseCase triggers startCallback when call visualizer engagement started`() {
        useCase(startCallback)
        omnibrowseEngagementCaptor.lastValue.accept(omnibrowseEngagement)
        verify(startCallback).run()
    }
}
