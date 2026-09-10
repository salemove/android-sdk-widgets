package com.glia.widgets.internal.chathead

import org.junit.Assert.assertEquals
import org.junit.Test

internal class BubblePositionTest {

    @Test
    fun `a position round trips through the same range unchanged`() {
        val position = BubblePosition.within(x = 250f, y = 700f, xMin = 50f, xMax = 450f, yMin = 50f, yMax = 900f)

        assertEquals(250f, position.xWithin(50f, 450f), 0.01f)
        assertEquals(700f, position.yWithin(50f, 900f), 0.01f)
    }

    @Test
    fun `mapping into a smaller range keeps the relative place`() {
        // Given the bubble in the middle of the overlay's range
        val position = BubblePosition.within(x = 500f, y = 500f, xMin = 0f, xMax = 1000f, yMin = 0f, yMax = 1000f)

        // Then it lands in the middle of the in-app layout's smaller range too
        assertEquals(216f, position.xWithin(16f, 416f), 0.01f)
        assertEquals(316f, position.yWithin(16f, 616f), 0.01f)
    }

    @Test
    fun `mapping is orientation independent`() {
        // Given the bubble at the right edge, 80% down, in portrait
        val position = BubblePosition.within(x = 1000f, y = 1600f, xMin = 0f, xMax = 1000f, yMin = 0f, yMax = 2000f)

        // Then in landscape it stays at the right edge, 80% down
        assertEquals(2000f, position.xWithin(0f, 2000f), 0.01f)
        assertEquals(800f, position.yWithin(0f, 1000f), 0.01f)
    }

    @Test
    fun `coordinates outside the range clamp to its limits`() {
        // Given an overlay bubble flung past the edge
        val position = BubblePosition.within(x = -40f, y = 1200f, xMin = 0f, xMax = 1000f, yMin = 0f, yMax = 1000f)

        assertEquals(0f, position.xWithin(0f, 1000f), 0.01f)
        assertEquals(1000f, position.yWithin(0f, 1000f), 0.01f)
    }

    @Test
    fun `a degenerate range does not blow up`() {
        // Given a host that has not been measured yet
        val position = BubblePosition.within(x = 10f, y = 10f, xMin = 0f, xMax = 0f, yMin = 16f, yMax = 0f)

        assertEquals(BubblePosition(0f, 0f), position)
        assertEquals(0f, position.xWithin(0f, 0f), 0.01f)
        assertEquals(16f, position.yWithin(16f, 0f), 0.01f)
    }
}
