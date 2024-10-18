package com.glia.widgets.entrywidget

import android.annotation.SuppressLint
import android.os.Bundle
import com.glia.widgets.base.FadeTransitionActivity
import com.glia.widgets.di.Dependencies

/**
 * EntryWidgetActivity provides a way to display the EntryWidget bottom sheet.
 */
internal class EntryWidgetActivity : FadeTransitionActivity(), EntryWidgetFragment.OnDismissListener {

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            EntryWidgetFragment().show(supportFragmentManager)
        }

        Dependencies.controllerFactory.entryWidgetHideController.onHide.subscribe {
            finish()
        }
    }

    override fun onEntryWidgetDismiss() {
        if (!isChangingConfigurations) {
            finish()
        }
    }
}
