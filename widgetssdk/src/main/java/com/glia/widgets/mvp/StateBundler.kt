package com.glia.widgets.mvp

import android.os.Bundle

/**
 * An interface to implement saving MVP presenter state into an Android Bundle object.
 */
interface StateBundler<STATE : Any> {
    fun write(bundle: Bundle, value: STATE)
    fun read(bundle: Bundle): STATE?
}
