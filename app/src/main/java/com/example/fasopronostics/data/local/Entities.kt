package com.example.fasopronostics.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val id: String,
    val homeTeam: String,
    val awayTeam: String,
    val competition: String,
    val country: String,
    val matchDate: String,
    val matchTime: String,
    val sportName: String,
    val statusName: String,
    val scoreHome: Int?,
    val scoreAway: Int?,
    val liveMinute: String?,
    val formHome: String,
    val formAway: String,
    val headToHead: String,
    // Prediction data
    val hasPrediction: Boolean,
    val predictionTitle: String?,
    val primaryPick: String?,
    val secondaryPick: String?,
    val estimatedOdds: Double?,
    val probHomeWin: Int?,
    val probDraw: Int?,
    val probAwayWin: Int?,
    val confidencePercent: Int?,
    val confidenceLevel: String?,
    val expectedGoalsHome: Double?,
    val expectedGoalsAway: Double?,
    val favorableFactors: List<String>?,
    val riskFactors: List<String>?,
    val statisticalExplanation: String?,
    val modelSource: String?,
    val lastUpdated: String?,
    val isPremium: Boolean,
    val outcomeName: String
)

@Entity(tableName = "activation_codes")
data class ActivationCodeEntity(
    @PrimaryKey val code: String,
    val planId: String,
    val planName: String,
    val durationDays: Int,
    val isUsed: Boolean,
    val isRevoked: Boolean,
    val createdAt: Long,
    val usedAt: Long?,
    val usedBy: String?
)

@Entity(tableName = "app_config")
data class AppConfigEntity(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val action: String,
    val details: String,
    val timestamp: Long
)
