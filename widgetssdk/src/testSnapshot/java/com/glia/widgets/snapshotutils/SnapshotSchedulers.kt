package com.glia.widgets.snapshotutils

import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.rx.GliaWidgetsSchedulers
import com.glia.widgets.helper.rx.Schedulers
import io.reactivex.rxjava3.schedulers.TestScheduler
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal interface SnapshotSchedulers : SnapshotTestLifecycle {

    data class Mock(
        val schedulers: Schedulers,
        val computationScheduler: TestScheduler,
        val mainScheduler: TestScheduler
    ) {
        fun triggerActions() {
            computationScheduler.triggerActions()
            mainScheduler.triggerActions()
        }
    }

    fun schedulersMock(): Mock {
        val computationScheduler = TestScheduler()
        val mainScheduler = TestScheduler()

        val schedulers = mock<Schedulers>()
        whenever(schedulers.computationScheduler) doReturn computationScheduler
        whenever(schedulers.mainScheduler) doReturn mainScheduler
        Dependencies.schedulers = schedulers

        setOnEndListener {
            Dependencies.schedulers = GliaWidgetsSchedulers()
        }

        return Mock(schedulers, computationScheduler, mainScheduler)
    }
}
