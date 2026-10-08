package com.glia.widgets.internal.chathead

/**
 * Where the visitor left the bubble, as fractions of the host's draggable range instead of pixels.
 *
 * The two hosts draw on surfaces of different sizes — the overlay window covers the screen, the
 * in-app layout only the activity's content area — so pixel coordinates are not transferable between
 * them, but "how far along each axis" is. `(0, 0)` is the top-left limit of wherever the bubble can
 * be dragged, `(1, 1)` the bottom-right limit; each host maps that into its own bounds. Fractions are
 * also orientation-independent, so a rotation keeps the bubble in the same relative place.
 */
internal data class BubblePosition(val xFraction: Float, val yFraction: Float) {

    /** Maps [xFraction] into a host's horizontal draggable range. */
    fun xWithin(min: Float, max: Float): Float = min + xFraction * (max - min).coerceAtLeast(0f)

    /** Maps [yFraction] into a host's vertical draggable range. */
    fun yWithin(min: Float, max: Float): Float = min + yFraction * (max - min).coerceAtLeast(0f)

    internal companion object {
        /**
         * Captures pixel coordinates as fractions of the host's draggable range. Coordinates outside
         * the range clamp to its limits, so an overlay bubble flung past the edge comes back inside.
         */
        fun within(x: Float, y: Float, xMin: Float, xMax: Float, yMin: Float, yMax: Float): BubblePosition =
            BubblePosition(fraction(x, xMin, xMax), fraction(y, yMin, yMax))

        private fun fraction(value: Float, min: Float, max: Float): Float =
            if (max <= min) 0f else ((value - min) / (max - min)).coerceIn(0f, 1f)
    }
}
