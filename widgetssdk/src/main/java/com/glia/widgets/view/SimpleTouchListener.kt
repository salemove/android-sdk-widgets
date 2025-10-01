package com.glia.widgets.view

import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

internal class SimpleTouchListener(
    private val retrieveInitialCoordinates: () -> PointF,
    private val onMove: (x: Float, y: Float) -> Unit,
    private val onRelease: (() -> Unit)? = null
) : View.OnTouchListener {
    private val diffThreshold = 20f
    private val initialCoordinates = PointF(0f, 0f)
    private val touchPoint = PointF(0f, 0f)

    override fun onTouch(v: View, event: MotionEvent): Boolean {

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                //remember the initial position.
                initialCoordinates.set(retrieveInitialCoordinates())

                //get the touch location
                touchPoint.set(event.rawX, event.rawY)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                //Calculate the X and Y coordinates of the view.
                val xDiff = initialCoordinates.x + event.rawX - touchPoint.x
                val yDiff = initialCoordinates.y + event.rawY - touchPoint.y
                onMove(xDiff, yDiff)
                return true
            }

            MotionEvent.ACTION_UP -> {
                val xDiff = abs(event.rawX - touchPoint.x)
                val yDiff = abs(event.rawY - touchPoint.y)

                if (xDiff < diffThreshold && yDiff < diffThreshold) {
                    v.performClick()
                } else {
                    onRelease?.invoke()
                }
                return true
            }
        }

        return false
    }
}
