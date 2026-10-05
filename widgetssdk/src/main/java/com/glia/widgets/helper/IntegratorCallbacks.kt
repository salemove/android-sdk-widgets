package com.glia.widgets.helper

import android.os.Handler
import android.os.Looper

/**
 * Runs a callback supplied by the integrator app. If it throws, the exception is re-thrown on the main thread.
 *
 * Core SDK calls us from its own background coroutines, which have no exception handler. Without this, an
 * integrator bug crashes from inside Core SDK and skips the Widgets work that should run after the callback.
 * Never swallow the exception here: the integrator's bug must still crash their app.
 */
internal fun runIntegratorCallback(callback: () -> Unit) {
    try {
        callback()
    } catch (e: Throwable) {
        Handler(Looper.getMainLooper()).post { throw e }
    }
}
