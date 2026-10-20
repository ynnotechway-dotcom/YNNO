package com.example.fasopronostics.data.model

enum class MatchStatus {
    UPCOMING,
    LIVE,
    FINISHED
}

enum class PredictionOutcome {
    PENDING,
    WON,
    LOST,
    VOID
}

data class PredictionItem(
    val id: String,
    val matchId: String,
    val title: String,
    val primaryPick: String,
    val secondaryPick: String,
    val estimatedOdds: Double,
    val probHomeWin: Int, // e.g., 58%
    val probDraw: Int,    // e.g., 24%
    val probAwayWin: Int, // e.g., 18%
    val confidencePercent: Int, // e.g. 78%
    val confidenceLevel: String, // "Élevé", "Modéré", "Spéculatif"
    val expectedGoalsHome: Double,
    val expectedGoalsAway: Double,
    val favorableFactors: List<String>,
    val riskFactors: List<String>,
    val statisticalExplanation: String,
    val modelSource: String, // e.g. "FasoEngine v2.4 (Poisson & Elo)"
    val lastUpdated: String,
    val isPremium: Boolean,
    val outcome: PredictionOutcome = PredictionOutcome.PENDING,
    val resultScore: String? = null
)

data class MatchItem(
    val id: String,
    val homeTeam: String,
    val awayTeam: String,
    val competition: String,
    val country: String,
    val matchDate: String,
    val matchTime: String,
    val sport: SportType,
    val status: MatchStatus,
    val scoreHome: Int? = null,
    val scoreAway: Int? = null,
    val liveMinute: String? = null,
    val formHome: String = "V-V-N-V-D",
    val formAway: String = "D-N-V-D-V",
    val headToHead: String = "3 Victoires Domicile, 1 Nul, 1 Extérieur",
    val prediction: PredictionItem? = null
)
