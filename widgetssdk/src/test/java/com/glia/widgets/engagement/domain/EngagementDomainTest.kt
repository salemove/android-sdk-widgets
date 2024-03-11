package com.glia.widgets.engagement.domain

import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.widgets.engagement.EngagementRepository
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.schedulers.Schedulers
import org.junit.After
import org.junit.Before
import org.junit.Test

class EngagementDomainTest {

    @Before
    fun setUp() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

    }

    @After
    fun tearDown() {
        RxAndroidPlugins.reset()
    }

    @Test
    fun `AcceptMediaUpgradeOfferUseCase will acceptMediaUpgradeRequest when invoked`() {
        val repository: EngagementRepository = mockk(relaxed = true)
        val mediaUpgradeOffer: MediaUpgradeOffer = mockk(relaxed = true)

        val useCase: AcceptMediaUpgradeOfferUseCase = AcceptMediaUpgradeOfferUseCaseImpl(engagementRepository = repository)
        verify { repository.mediaUpgradeOfferAcceptResult }
        useCase(mediaUpgradeOffer)

        verify { repository.acceptMediaUpgradeRequest(mediaUpgradeOffer) }
        confirmVerified(repository, mediaUpgradeOffer)
    }

    @Test
    fun `AcceptMediaUpgradeOfferUseCase result will emit result only when result is successful`() {
        val resultFlow = BehaviorProcessor.create<Result<MediaUpgradeOffer>>()
        val repository: EngagementRepository = mockk(relaxUnitFun = true) {
            every { mediaUpgradeOfferAcceptResult } returns resultFlow
        }
        val mediaUpgradeOffer: MediaUpgradeOffer = mockk(relaxed = true)

        val useCase: AcceptMediaUpgradeOfferUseCase = AcceptMediaUpgradeOfferUseCaseImpl(engagementRepository = repository)
        verify { repository.mediaUpgradeOfferAcceptResult }

        val resultTest = useCase.result.test()
        resultFlow.onNext(Result.success(mediaUpgradeOffer))
        resultFlow.onNext(Result.failure(RuntimeException()))

        resultTest.assertNotComplete().assertValue(mediaUpgradeOffer)

        confirmVerified(repository, mediaUpgradeOffer)
    }

}
