package com.glia.widgets.base

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.insetsControllerCompat
import com.glia.widgets.locale.LocaleString
import io.reactivex.rxjava3.disposables.Disposable

/**
 * @hide
 */
open class FadeTransitionActivity : AppCompatActivity() {

    private var disposable: Disposable? = null
    private val localeProvider = Dependencies.localeProvider


    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun overrideAnimation() {
        overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out)
        overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun overrideAnimationCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideAnimation()
            return
        }

        overrideAnimationApi33()
    }

    @Suppress("DEPRECATION")
    private fun overrideAnimationApi33() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        window.insetsControllerCompat.isAppearanceLightStatusBars = false
        overrideAnimationCompat()
        super.onCreate(savedInstanceState)
    }

    override fun finishAfterTransition() {
        overrideAnimationApi33()
        super.finishAfterTransition()
    }

    override fun startActivity(intent: Intent) {
        overrideAnimationApi33()
        super.startActivity(intent)
    }

    override fun startActivity(intent: Intent, options: Bundle?) {
        overrideAnimationApi33()
        super.startActivity(intent, options)
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

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

}
