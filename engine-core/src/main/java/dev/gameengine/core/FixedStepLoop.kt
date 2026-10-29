package dev.gameengine.core

/**
 * Deterministic fixed-timestep accumulator with bounded catch-up work.
 *
 * The caller supplies frame time and performs simulation in [update]. Rendering can use
 * [interpolation] to blend the two most recent simulation states. Excess backlog is dropped
 * and recorded in [droppedSeconds] instead of causing an unbounded "spiral of death".
 */
class FixedStepLoop(
    val stepSeconds: Double = 1.0 / 60.0,
    private val maxFrameSeconds: Double = 0.25,
    private val maxStepsPerFrame: Int = 8,
) {
    var accumulatorSeconds: Double = 0.0
        private set

    var simulationSeconds: Double = 0.0
        private set

    var interpolation: Double = 0.0
        private set

    var droppedSeconds: Double = 0.0
        private set

    init {
        require(stepSeconds.isFinite() && stepSeconds > 0.0) {
            "stepSeconds must be finite and positive"
        }
        require(maxFrameSeconds.isFinite() && maxFrameSeconds > 0.0) {
            "maxFrameSeconds must be finite and positive"
        }
        require(maxStepsPerFrame > 0) { "maxStepsPerFrame must be positive" }
    }

    /** Advances simulation and returns the number of fixed updates executed. */
    fun advance(frameSeconds: Double, update: (stepSeconds: Double) -> Unit): Int {
        require(frameSeconds.isFinite() && frameSeconds >= 0.0) {
            "frameSeconds must be finite and non-negative"
        }

        val acceptedFrameSeconds = frameSeconds.coerceAtMost(maxFrameSeconds)
        droppedSeconds += frameSeconds - acceptedFrameSeconds
        accumulatorSeconds += acceptedFrameSeconds

        var steps = 0
        while (accumulatorSeconds + EPSILON >= stepSeconds && steps < maxStepsPerFrame) {
            update(stepSeconds)
            accumulatorSeconds -= stepSeconds
            if (accumulatorSeconds < 0.0) accumulatorSeconds = 0.0
            simulationSeconds += stepSeconds
            steps++
        }

        if (steps == maxStepsPerFrame && accumulatorSeconds >= stepSeconds) {
            val retainedSeconds = accumulatorSeconds % stepSeconds
            droppedSeconds += accumulatorSeconds - retainedSeconds
            accumulatorSeconds = retainedSeconds
        }

        interpolation = (accumulatorSeconds / stepSeconds).coerceIn(0.0, 1.0)
        return steps
    }

    fun reset() {
        accumulatorSeconds = 0.0
        simulationSeconds = 0.0
        interpolation = 0.0
        droppedSeconds = 0.0
    }

    private companion object {
        const val EPSILON = 1e-12
    }
}
