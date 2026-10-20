package com.example

import com.example.fasopronostics.data.engine.SportsAnalyticsEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `poisson probability calculation`() {
        val p0 = SportsAnalyticsEngine.poissonProbability(1.5, 0)
        val p1 = SportsAnalyticsEngine.poissonProbability(1.5, 1)
        val p2 = SportsAnalyticsEngine.poissonProbability(1.5, 2)

        assertTrue(p0 > 0.0)
        assertTrue(p1 > p0)
        assertTrue(p2 > 0.0)
    }

    @Test
    fun `match probabilities confidence bounds`() {
        val probs = SportsAnalyticsEngine.calculateMatchProbabilities(
            homeAttack = 2.2,
            homeDefense = 0.7,
            awayAttack = 0.8,
            awayDefense = 1.5
        )
        // Dominant home team should have higher probHomeWin
        assertTrue(probs.probHomeWin > probs.probAwayWin)
        assertTrue(probs.confidencePercent >= 68)
        assertEquals("Élevé", probs.confidenceLevel)
    }
}
