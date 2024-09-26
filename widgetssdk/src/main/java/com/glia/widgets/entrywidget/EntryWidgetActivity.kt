package com.glia.widgets.entrywidget

import android.os.Bundle
import com.glia.widgets.base.FadeTransitionActivity

/**
 * EntryWidgetActivity provides a way to display the EntryWidget bottom sheet.
 */
internal class EntryWidgetActivity : FadeTransitionActivity(), EntryWidgetFragment.OnDismissListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            EntryWidgetFragment().show(supportFragmentManager)
        }
    }

    override fun onEntryWidgetDismiss() {
        if (!isChangingConfigurations) {
            finish()
        }
    }
}
