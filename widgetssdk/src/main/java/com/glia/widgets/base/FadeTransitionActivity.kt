package com.glia.widgets.base

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.glia.androidsdk.Glia
import com.glia.widgets.di.Dependencies
import com.glia.widgets.locale.LocaleString
import io.reactivex.rxjava3.disposables.Disposable

/**
 * @hide
 */
open class FadeTransitionActivity : AppCompatActivity() {

    private var disposable: Disposable? = null
    private val localeProvider = Dependencies.localeProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun finishAndRemoveTask() {
        super.finishAndRemoveTask()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        disposable?.dispose()
    }

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun startActivity(intent: Intent?, options: Bundle?) {
        super.startActivity(intent, options)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    fun setTitle(locale: LocaleString?) {
        if (locale == null) {
            super.setTitle(null)
            return
        }

        disposable?.dispose()
        disposable = localeProvider.getLocaleObservable()
            .startWithItem("stub")
            .map { localeProvider.getString(locale.stringKey, locale.values) }
            .distinctUntilChanged()
            .subscribe(
                { super.setTitle(it) },
                { /* no-op */ }
        )
    }

}
