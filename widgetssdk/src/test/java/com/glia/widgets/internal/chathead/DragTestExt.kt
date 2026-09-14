package com.glia.widgets.internal.chathead

import android.view.MotionEvent
import android.view.View

/**
 * Simulates a visitor dragging [this] view by ([dx], [dy]): a press, a move past the click threshold,
 * and a release — the release is what hands the bubble position to the controller.
 */
internal fun View.drag(dx: Float, dy: Float) {
    dispatchTouchEvent(MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_DOWN, 500f, 500f, 0))
    dispatchTouchEvent(MotionEvent.obtain(0L, 10L, MotionEvent.ACTION_MOVE, 500f + dx, 500f + dy, 0))
    dispatchTouchEvent(MotionEvent.obtain(0L, 20L, MotionEvent.ACTION_UP, 500f + dx, 500f + dy, 0))
}
