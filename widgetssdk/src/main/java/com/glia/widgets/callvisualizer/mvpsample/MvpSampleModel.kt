package com.glia.widgets.callvisualizer.mvpsample

import android.util.AndroidException
import com.glia.widgets.di.Dependencies
import com.glia.widgets.mvp.MvpModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus

object MvpSampleModel : MvpModel(), MvpSampleContract.Model {

    private var disposable: Disposable? = null

    override fun sendCounterValue(tag: String, value: Int) {
        disposable = Dependencies.getUseCaseFactory().sampleUseCase.invoke(value).subscribeOn(Schedulers.io()).observeOn(
            AndroidSchedulers.mainThread()).subscribe(
            { result ->
                EventBus.getDefault().post(SimpleIntEvent.Success(tag, result))
            },
            { error ->
                EventBus.getDefault().post(SimpleIntEvent.Failure(tag, error as Exception))
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
        disposable = null
    }

}
