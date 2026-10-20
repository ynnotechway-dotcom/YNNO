package com.example.fasopronostics.data.engine

import kotlin.math.exp
import kotlin.math.pow

object SportsAnalyticsEngine {

    /**
     * Factorial approximation for small integer values (goals/points).
     */
    private fun factorial(k: Int): Double {
        var res = 1.0
        for (i in 2..k) res *= i
        return res
    }

    /**
     * Poisson probability P(X = k) = (lambda^k * e^(-lambda)) / k!
     */
    fun poissonProbability(lambda: Double, k: Int): Double {
        if (lambda <= 0.0) return if (k == 0) 1.0 else 0.0
        return (lambda.pow(k.toDouble()) * exp(-lambda)) / factorial(k)
    }

    /**
     * Computes 1X2 probabilities for soccer based on bivariate Poisson model
     * with home advantage factor and team attack/defense ratings.
     */
    fun calculateMatchProbabilities(
        homeAttack: Double,
        homeDefense: Double,
        awayAttack: Double,
        awayDefense: Double,
        homeAdvantage: Double = 1.15
    ): MatchProbabilities {
        val lambdaHome = (homeAttack * awayDefense * homeAdvantage).coerceIn(0.4, 3.8)
        val lambdaAway = (awayAttack * homeDefense).coerceIn(0.3, 3.2)

        var homeWinProb = 0.0
        var drawProb = 0.0
        var awayWinProb = 0.0
        var over25Prob = 0.0

        for (h in 0..7) {
            for (a in 0..7) {
                val p = poissonProbability(lambdaHome, h) * poissonProbability(lambdaAway, a)
                when {
                    h > a -> homeWinProb += p
                    h == a -> drawProb += p
                    else -> awayWinProb += p
                }
                if (h + a > 2) {
                    over25Prob += p
                }
            }
        }

        val total = (homeWinProb + drawProb + awayWinProb).coerceAtLeast(0.001)
        val pHome = ((homeWinProb / total) * 100).toInt()
        val pDraw = ((drawProb / total) * 100).toInt()
        val pAway = (100 - pHome - pDraw).coerceAtLeast(1)

        val confidence = when {
            pHome >= 60 || pAway >= 60 -> (68 + ((pHome.coerceAtLeast(pAway) - 60) * 0.5)).toInt().coerceIn(68, 88)
            pHome >= 48 || pAway >= 48 -> (56 + (pHome.coerceAtLeast(pAway) - 48)).toInt().coerceIn(55, 67)
            else -> 48
        }

        val confidenceLevel = when {
            confidence >= 72 -> "Élevé"
            confidence >= 58 -> "Modéré"
            else -> "Spéculatif"
        }

        return MatchProbabilities(
            expectedGoalsHome = Math.round(lambdaHome * 100.0) / 100.0,
            expectedGoalsAway = Math.round(lambdaAway * 100.0) / 100.0,
            probHomeWin = pHome,
            probDraw = pDraw,
            probAwayWin = pAway,
            probOver2_5 = (over25Prob * 100).toInt(),
            confidencePercent = confidence,
            confidenceLevel = confidenceLevel
        )
    }

    data class MatchProbabilities(
        val expectedGoalsHome: Double,
        val expectedGoalsAway: Double,
        val probHomeWin: Int,
        val probDraw: Int,
        val probAwayWin: Int,
        val probOver2_5: Int,
        val confidencePercent: Int,
        val confidenceLevel: String
    )
}
