package com.example.fasopronostics.data.model

data class AdminStatSummary(
    val activeSubscribersCount: Int,
    val totalRevenueFCFA: Long,
    val pendingRequestsCount: Int,
    val totalPredictionsCount: Int,
    val wonPredictionsCount: Int,
    val globalStrikeRate: Double,
    val availableCodesCount: Int
)

data class AuditLogItem(
    val id: String,
    val action: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
