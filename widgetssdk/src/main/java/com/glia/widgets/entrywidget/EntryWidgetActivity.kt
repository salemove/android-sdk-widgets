package com.glia.widgets.entrywidget

import android.content.res.Resources
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.applyGliaThemeOverlays
import io.reactivex.rxjava3.disposables.CompositeDisposable

/**
 * EntryWidgetActivity provides a way to display the EntryWidget bottom sheet.
 */
internal class EntryWidgetActivity : AppCompatActivity(), EntryWidgetFragment.OnDismissListener {

    private var disposable: CompositeDisposable = CompositeDisposable()

    /** See `FadeTransitionActivity.onApplyThemeResource` for why this is the hook. */
    override fun onApplyThemeResource(theme: Resources.Theme, resid: Int, first: Boolean) {
        super.onApplyThemeResource(theme, resid, first)
        applyGliaThemeOverlays()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            EntryWidgetFragment().show(supportFragmentManager)
        }

        disposable.add(Dependencies.controllerFactory.entryWidgetHideController.onHide.subscribe {
            finish()
        })
    }

    override fun onEntryWidgetDismiss() {
        if (!isChangingConfigurations) {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }
}
