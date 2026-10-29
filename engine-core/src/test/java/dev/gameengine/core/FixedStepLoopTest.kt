package dev.gameengine.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FixedStepLoopTest {
    @Test
    fun advance_executesOnlyWholeFixedSteps() {
        val loop = FixedStepLoop(stepSeconds = 0.01)
        var updates = 0

        val steps = loop.advance(0.035) { updates++ }

        assertEquals(3, steps)
        assertEquals(3, updates)
        assertEquals(0.03, loop.simulationSeconds, 1e-9)
        assertEquals(0.005, loop.accumulatorSeconds, 1e-9)
        assertEquals(0.5, loop.interpolation, 1e-9)
    }

    @Test
    fun advance_clampsLongFramesAndBoundsCatchUp() {
        val loop = FixedStepLoop(stepSeconds = 0.01, maxFrameSeconds = 0.25, maxStepsPerFrame = 8)

        val steps = loop.advance(2.0) {}

        assertEquals(8, steps)
        assertEquals(0.08, loop.simulationSeconds, 1e-9)
        assertTrue(loop.accumulatorSeconds < loop.stepSeconds)
        assertTrue(loop.droppedSeconds > 1.9)
        assertTrue(loop.interpolation in 0.0..1.0)
    }

    @Test
    fun advance_zeroDeltaDoesNotSimulate() {
        val loop = FixedStepLoop()

        assertEquals(0, loop.advance(0.0) {})
        assertEquals(0.0, loop.simulationSeconds, 0.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun advance_rejectsNegativeDelta() {
        FixedStepLoop().advance(-0.1) {}
    }

    @Test(expected = IllegalArgumentException::class)
    fun advance_rejectsNonFiniteDelta() {
        FixedStepLoop().advance(Double.NaN) {}
    }

    @Test
    fun reset_clearsAllTimingState() {
        val loop = FixedStepLoop(stepSeconds = 0.01)
        loop.advance(0.1) {}

        loop.reset()

        assertEquals(0.0, loop.simulationSeconds, 0.0)
        assertEquals(0.0, loop.accumulatorSeconds, 0.0)
        assertEquals(0.0, loop.interpolation, 0.0)
        assertEquals(0.0, loop.droppedSeconds, 0.0)
    }
}
