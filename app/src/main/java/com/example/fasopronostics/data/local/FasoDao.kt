package com.example.fasopronostics.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FasoDao {

    // Matches & Predictions
    @Query("SELECT * FROM matches ORDER BY matchDate ASC, matchTime ASC")
    fun getAllMatches(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE sportName = :sport ORDER BY matchDate ASC, matchTime ASC")
    fun getMatchesBySport(sport: String): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE id = :matchId LIMIT 1")
    suspend fun getMatchById(matchId: String): MatchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatches(matches: List<MatchEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchEntity)

    @Query("UPDATE matches SET scoreHome = :scoreHome, scoreAway = :scoreAway, statusName = :status, outcomeName = :outcome WHERE id = :matchId")
    suspend fun updateMatchOutcome(matchId: String, scoreHome: Int?, scoreAway: Int?, status: String, outcome: String)

    // Activation Codes
    @Query("SELECT * FROM activation_codes ORDER BY createdAt DESC")
    fun getAllActivationCodes(): Flow<List<ActivationCodeEntity>>

    @Query("SELECT * FROM activation_codes WHERE code = :code LIMIT 1")
    suspend fun getActivationCode(code: String): ActivationCodeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivationCode(code: ActivationCodeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivationCodes(codes: List<ActivationCodeEntity>)

    @Query("UPDATE activation_codes SET isUsed = 1, usedAt = :usedAt, usedBy = :userId WHERE code = :code")
    suspend fun markCodeAsUsed(code: String, usedAt: Long, userId: String)

    @Query("UPDATE activation_codes SET isRevoked = 1 WHERE code = :code")
    suspend fun revokeCode(code: String)

    // Config (e.g. subscription status, admin phone, etc.)
    @Query("SELECT value FROM app_config WHERE `key` = :key LIMIT 1")
    suspend fun getConfigValue(key: String): String?

    @Query("SELECT * FROM app_config")
    fun getAllConfig(): Flow<List<AppConfigEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setConfigValue(config: AppConfigEntity)

    // Audit logs
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 50")
    fun getAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)
}
