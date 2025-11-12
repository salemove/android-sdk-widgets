package com.glia.widgets.base

import android.view.View

/**
 * Glia internal interface.
 *
 * Represents an activity that has a Glia view.
 * [T] is the type of the Glia(root) view.
 */
internal interface GliaActivity<T : View> {
    val gliaView: T
}
