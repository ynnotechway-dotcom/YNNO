package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.fasopronostics.data.engine.SportsAnalyticsEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("JUJU MULTI PRONOS", appName)
    }

    @Test
    fun `sports analytics engine calculates realistic probabilities`() {
        val probs = SportsAnalyticsEngine.calculateMatchProbabilities(
            homeAttack = 1.6,
            homeDefense = 0.9,
            awayAttack = 1.0,
            awayDefense = 1.2
        )
        assertTrue("Home prob should be positive", probs.probHomeWin > 0)
        assertTrue("Draw prob should be positive", probs.probDraw > 0)
        assertTrue("Away prob should be positive", probs.probAwayWin > 0)
        assertTrue("Probabilities sum close to 100", (probs.probHomeWin + probs.probDraw + probs.probAwayWin) in 98..102)
        assertTrue("Confidence is within bounds", probs.confidencePercent in 40..95)
        assertNotNull(probs.confidenceLevel)
    }
}
