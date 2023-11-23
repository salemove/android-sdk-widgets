package com.glia.widgets.engagement

import com.glia.androidsdk.Engagement
import com.glia.androidsdk.Glia
import com.glia.androidsdk.engagement.EngagementState
import com.glia.androidsdk.omnicore.OmnicoreEngagement
import com.glia.widgets.di.GliaCore
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.TAG
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import java.util.function.Consumer

internal interface EngagementDataSource {
    fun subscribeToEngagementStart(): Flowable<Engagement>
    fun subscribeToEngagementEnd(engagement: Engagement): Single<Engagement>
    fun subscribeToEngagementState(engagement: Engagement): Flowable<EngagementState>
    infix fun end(engagement: Engagement)
}

internal class EngagementDataSourceImpl(private val core: GliaCore) : EngagementDataSource {

    // Engagement Start --------------------------------------------------------------------------------------------
    override fun subscribeToEngagementStart(): Flowable<Engagement> = Flowable.create({ emitter ->
        val callback = Consumer<OmnicoreEngagement> {
            if (emitter.isCancelled) return@Consumer

            emitter.onNext(it)
        }

        core.on(Glia.Events.ENGAGEMENT, callback)
        emitter.setCancellable { core.off(Glia.Events.ENGAGEMENT, callback) }

    }, BackpressureStrategy.LATEST)

    // Engagement End --------------------------------------------------------------------------------------------
    override fun subscribeToEngagementEnd(engagement: Engagement): Single<Engagement> = Single.create { emitter ->
        val callback = Runnable {
            if (emitter.isDisposed) return@Runnable

            emitter.onSuccess(engagement)
        }
        engagement.on(Engagement.Events.END, callback)
    }

    // Engagement State --------------------------------------------------------------------------------------------
    override fun subscribeToEngagementState(engagement: Engagement): Flowable<EngagementState> = Flowable.create({ emitter ->
        val consumer: Consumer<EngagementState> = Consumer {
            if (emitter.isCancelled) return@Consumer

            emitter.onNext(it)
        }

        engagement.on(Engagement.Events.STATE_UPDATE, consumer)
    }, BackpressureStrategy.LATEST)

    override infix fun end(engagement: Engagement) = engagement.end {
        Logger.d(TAG, "Ending engagement failed")
    }

}
