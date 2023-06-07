package com.glia.widgets.helper.rx

import io.reactivex.CompletableTransformer
import io.reactivex.ObservableTransformer
import io.reactivex.SingleTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Helpful rx tools to simplify subscriptions and disposals
 *
 * For the schedulers, the use is through the compose() function in the subscription flow.
 *
 * Example
 * observable.compose(observableSchedulers()).subscribe(...)
 */

fun <T> observableSchedulers(): ObservableTransformer<T, T> {
    return ObservableTransformer {
        it.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}

fun <T> singleSchedulers(): SingleTransformer<T, T> {
    return SingleTransformer {
        it.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}

fun <T> maybeSchedulers(): ObservableTransformer<T, T> {
    return ObservableTransformer {
        it.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}

fun completableSchedulers(): CompletableTransformer {
    return CompletableTransformer {
        it.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}

fun dispose(vararg disposables: Disposable?) {
    for (disposable in disposables) {
        disposable?.dispose()
    }
}

fun clearDisposable(disposable: CompositeDisposable?) {
    disposable?.clear()
}

fun isNotWorking(disposable: Disposable?): Boolean {
    return disposable == null || disposable.isDisposed
}
