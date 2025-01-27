package com.glia.widgets.view.snackbar.liveobservation

import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.site.SiteInfo
import com.glia.widgets.chat.domain.SiteInfoUseCase
import io.reactivex.rxjava3.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class LiveObservationPopupUseCaseTest {

    private lateinit var useCase: LiveObservationPopupUseCase
    private lateinit var siteInfoUseCase: SiteInfoUseCase
    private val siteInfoEmitter: PublishSubject<SiteInfo> = PublishSubject.create()

    @Before
    fun setUp() {
        siteInfoUseCase = mock {
            on { invoke(any()) } doAnswer {
                val callback: RequestCallback<SiteInfo> = it.getArgument(0)
                siteInfoEmitter
                    .firstOrError()
                    .doOnSuccess { siteInfo -> callback.onResult(siteInfo, null) }
                    .doOnError { error -> callback.onResult(null, GliaException.from(error)) }
                    .subscribe()
                return@doAnswer
            }
        }
        useCase = LiveObservationPopupUseCase(siteInfoUseCase)
    }

    @Test
    fun `invoke returns true when LO is enable and indication is enabled`() {
        val siteInfo: SiteInfo = mock {
            on { isLiveObservationEnabled } doReturn true
            on { isObservationIndicationEnabled } doReturn true
        }

        val testResultObserver = useCase.invoke().test()
        emitSiteInfoResult(siteInfo)

        testResultObserver.assertValue(true)
    }

    @Test
    fun `invoke returns true when LO is enabled and indication is null`() {
        val siteInfo: SiteInfo = mock {
            on { isLiveObservationEnabled } doReturn true
            on { isObservationIndicationEnabled } doReturn null
        }

        val testResultObserver = useCase.invoke().test()
        emitSiteInfoResult(siteInfo)

        testResultObserver.assertValue(true)
    }

    @Test
    fun `invoke returns false when LO is disabled and indication is enabled`() {
        val siteInfo: SiteInfo = mock {
            on { isLiveObservationEnabled } doReturn false
            on { isObservationIndicationEnabled } doReturn true
        }

        val testResultObserver = useCase.invoke().test()
        emitSiteInfoResult(siteInfo)

        testResultObserver.assertValue(false)
    }

    @Test
    fun `invoke returns false when LO is enabled and indication is disabled`() {
        val siteInfo: SiteInfo = mock {
            on { isLiveObservationEnabled } doReturn true
            on { isObservationIndicationEnabled } doReturn false
        }

        val testResultObserver = useCase.invoke().test()
        emitSiteInfoResult(siteInfo)

        testResultObserver.assertValue(false)
    }

    @Test
    fun `invoke returns true when site info failed to load`() {
        val testResultObserver = useCase.invoke().test()
        emitSiteInfoError(GliaException("Something-something", GliaException.Cause.INTERNAL_ERROR))

        testResultObserver.assertValue(true)
    }

    private fun emitSiteInfoResult(siteInfo: SiteInfo) {
        siteInfoEmitter.onNext(siteInfo)
    }

    private fun emitSiteInfoError(error: GliaException) {
        siteInfoEmitter.onError(error)
    }
}
