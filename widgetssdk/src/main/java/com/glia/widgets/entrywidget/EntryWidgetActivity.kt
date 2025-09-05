package com.glia.widgets.entrywidget

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.glia.widgets.di.Dependencies
import io.reactivex.rxjava3.disposables.CompositeDisposable

/**
 * EntryWidgetActivity provides a way to display the EntryWidget bottom sheet.
 */
internal class EntryWidgetActivity : AppCompatActivity(), EntryWidgetFragment.OnDismissListener {

    private var disposable: CompositeDisposable = CompositeDisposable()

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
