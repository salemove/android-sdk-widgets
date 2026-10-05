package com.glia.widgets.helper

import android.assertRethrownOnMainThread
import android.os.Looper
import android.runOnCoreThread
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
internal class IntegratorCallbacksTest {

    @Test
    fun `runIntegratorCallback runs the callback and posts nothing to the main thread when it does not throw`() {
        var calls = 0

        runOnCoreThread { runIntegratorCallback { calls++ } }

        assertEquals(1, calls)
        assertTrue(shadowOf(Looper.getMainLooper()).isIdle)
    }

    @Test
    fun `runIntegratorCallback re-throws the callback exception on the main thread instead of the calling thread`() {
        val integratorBug = IllegalStateException("integrator bug")

        runOnCoreThread { runIntegratorCallback { throw integratorBug } }

        assertRethrownOnMainThread(integratorBug)
    }
}
