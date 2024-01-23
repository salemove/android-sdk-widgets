package com.glia.widgets.snapshotutils

import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.rx.Schedulers
import io.reactivex.schedulers.TestScheduler
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

interface SnapshotSchedulers {

    data class Mock(
        val computationScheduler: TestScheduler,
        val mainScheduler: TestScheduler
    )

    fun schedulersMock(): Mock {
        val computationScheduler = TestScheduler()
        val mainScheduler = TestScheduler()

        val schedulers = mock<Schedulers>()
        whenever(schedulers.computationScheduler) doReturn computationScheduler
        whenever(schedulers.mainScheduler) doReturn mainScheduler
        Dependencies.setSchedulers(schedulers)

        return Mock(computationScheduler, mainScheduler)
    }
}
