package com.glia.widgets.engagement.domain

import com.glia.androidsdk.comms.MediaQuality
import com.glia.widgets.engagement.EngagementRepository
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.processors.BehaviorProcessor
import org.junit.Test

class IsMediaQualityPoorUseCaseImplTest {

    private val engagementRepository: EngagementRepository = mockk(relaxed = true)
    private val mediaQualitySubject = BehaviorProcessor.create<MediaQuality>()

    @Test
    fun `invoke emits true when media quality is poor`() {
        every { engagementRepository.mediaQuality } returns mediaQualitySubject

        val useCase = IsMediaQualityPoorUseCaseImpl(engagementRepository)
        val testObserver = useCase().test()

        mediaQualitySubject.onNext(MediaQuality.POOR)
        testObserver.assertValue(true)
    }

    @Test
    fun `invoke emits false when media quality is good`() {
        every { engagementRepository.mediaQuality } returns mediaQualitySubject

        val useCase = IsMediaQualityPoorUseCaseImpl(engagementRepository)
        val testObserver = useCase().test()

        mediaQualitySubject.onNext(MediaQuality.GOOD)
        testObserver.assertValue(false)
    }

    @Test
    fun `invoke emits only on change`() {
        every { engagementRepository.mediaQuality } returns mediaQualitySubject

        val useCase = IsMediaQualityPoorUseCaseImpl(engagementRepository)
        val testObserver = useCase().test()

        mediaQualitySubject.onNext(MediaQuality.POOR)
        mediaQualitySubject.onNext(MediaQuality.POOR)
        mediaQualitySubject.onNext(MediaQuality.GOOD)
        mediaQualitySubject.onNext(MediaQuality.GOOD)
        mediaQualitySubject.onNext(MediaQuality.POOR)

        testObserver.assertValues(true, false, true)
    }
}
