package com.example.fasopronostics.data.repository

import com.example.fasopronostics.data.engine.SportsAnalyticsEngine
import com.example.fasopronostics.data.local.ActivationCodeEntity
import com.example.fasopronostics.data.local.AppConfigEntity
import com.example.fasopronostics.data.local.AuditLogEntity
import com.example.fasopronostics.data.local.FasoDao
import com.example.fasopronostics.data.local.MatchEntity
import com.example.fasopronostics.data.model.ActivationCode
import com.example.fasopronostics.data.model.AdminStatSummary
import com.example.fasopronostics.data.model.AuditLogItem
import com.example.fasopronostics.data.model.CommunityPoll
import com.example.fasopronostics.data.model.MatchItem
import com.example.fasopronostics.data.model.MatchStatus
import com.example.fasopronostics.data.model.PredictionItem
import com.example.fasopronostics.data.model.PredictionOutcome
import com.example.fasopronostics.data.model.ReferralProfile
import com.example.fasopronostics.data.model.SportType
import com.example.fasopronostics.data.model.SubscriptionPlan
import com.example.fasopronostics.data.model.UserSubscription
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

class FasoRepository(private val dao: FasoDao) {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    // In-memory poll and referral state
    private val _dailyPoll = MutableStateFlow(
        CommunityPoll(
            id = "poll-1",
            matchTitle = "ASFA Yennenga vs RC Bobo-Dioulasso",
            question = "Quel est votre pronostic pour ce choc de Premier Division Burkinabè ?",
            optionHome = "Victoire ASFA",
            votesHome = 248,
            optionDraw = "Match Nul",
            votesDraw = 112,
            optionAway = "Victoire RCB",
            votesAway = 195,
            userVotedOption = null
        )
    )
    val dailyPoll: Flow<CommunityPoll> = _dailyPoll.asStateFlow()

    private val _referralProfile = MutableStateFlow(
        ReferralProfile(
            referralCode = "FASO-7842",
            totalInvited = 2,
            completedReferrals = 2,
            targetForReward = 3,
            rewardDaysGranted = 0
        )
    )
    val referralProfile: Flow<ReferralProfile> = _referralProfile.asStateFlow()

    // Config keys
    companion object {
        const val KEY_SUB_EXPIRY = "sub_expiry"
        const val KEY_SUB_PLAN = "sub_plan"
        const val KEY_WHATSAPP_PHONE = "whatsapp_phone"
        const val DEFAULT_WHATSAPP = "+226 70 12 34 56"
        const val KEY_FAILED_ATTEMPTS = "failed_attempts"
        const val KEY_LOCKOUT_UNTIL = "lockout_until"
    }

    init {
        repositoryScope.launch {
            seedInitialDataIfNeeded()
        }
    }

    // Default Subscription Plans (FCFA)
    val defaultPlans = listOf(
        SubscriptionPlan(
            id = "pass_daily",
            name = "Pass Journalier",
            priceFCFA = 500,
            durationDays = 1,
            description = "Accès complet aux prédictions Premium pendant 24 heures.",
            badge = "Flash",
            isPopular = false,
            features = listOf(
                "Tous les pronostics Premium du jour",
                "Analyses statistiques détaillées",
                "Indice de confiance & facteurs clés"
            )
        ),
        SubscriptionPlan(
            id = "pass_weekly",
            name = "Pass Hebdomadaire",
            priceFCFA = 1000,
            durationDays = 7,
            description = "7 jours de prédictions multisports et suivi des championnats.",
            badge = "Recommandé",
            isPopular = false,
            features = listOf(
                "Accès 7 jours continus multisports",
                "Historique vérifiable des matchs",
                "Conseils de gestion de bankroll",
                "Support WhatsApp prioritaire"
            )
        ),
        SubscriptionPlan(
            id = "pass_monthly",
            name = "Pass Mensuel",
            priceFCFA = 5000,
            durationDays = 30,
            description = "Formule populaire pour toute la saison sportive.",
            badge = "Plus Populaire ⭐",
            isPopular = true,
            features = listOf(
                "Accès 30 jours illimité (Football, Basket, Tennis)",
                "Analyses Poisson et Elo avancées",
                "Bilan de performance hebdomadaire",
                "Notifications exclusives des meilleures cotes",
                "Canal WhatsApp VIP"
            )
        ),
        SubscriptionPlan(
            id = "pass_quarterly",
            name = "Pass Trimestriel",
            priceFCFA = 12000,
            durationDays = 90,
            description = "3 mois d'analyses pour les passionnés réguliers.",
            badge = "Économie",
            isPopular = false,
            features = listOf(
                "Économisez 3 000 FCFA sur 3 mois",
                "Accès aux compétitions internationales et locales",
                "Statistiques et détection de value-bets"
            )
        ),
        SubscriptionPlan(
            id = "pass_biannual",
            name = "Pass Semestriel",
            priceFCFA = 22000,
            durationDays = 180,
            description = "6 mois de sérénité et d'analyses expertes.",
            badge = "Premium Pro",
            isPopular = false,
            features = listOf(
                "Économisez 8 000 FCFA",
                "Accès complet à toutes les disciplines",
                "Support direct du collège d'analystes"
            )
        ),
        SubscriptionPlan(
            id = "pass_annual",
            name = "Pass Annuel",
            priceFCFA = 40000,
            durationDays = 365,
            description = "L'expérience ultime FASO PRONOSTICS toute l'année.",
            badge = "VIP Or",
            isPopular = false,
            features = listOf(
                "Accès VIP 365 jours complets",
                "Meilleur tarif par jour (110 FCFA/jour)",
                "Statut Ambassadeur de la plateforme"
            )
        )
    )

    // Matches Stream
    val allMatches: Flow<List<MatchItem>> = dao.getAllMatches().map { entities ->
        entities.map { it.toDomain() }
    }

    fun getMatchesBySport(sport: SportType): Flow<List<MatchItem>> {
        return if (sport == SportType.ALL) {
            allMatches
        } else {
            dao.getMatchesBySport(sport.name).map { list -> list.map { it.toDomain() } }
        }
    }

    // Activation Codes Stream
    val allActivationCodes: Flow<List<ActivationCode>> = dao.getAllActivationCodes().map { entities ->
        entities.map { it.toDomain() }
    }

    // Audit Logs Stream
    val auditLogs: Flow<List<AuditLogItem>> = dao.getAuditLogs().map { list ->
        list.map { AuditLogItem(it.id, it.action, it.details, it.timestamp) }
    }

    // User Subscription Stream
    fun getUserSubscription(): Flow<UserSubscription> {
        return dao.getAllConfig().map { configs ->
            val expiryStr = configs.find { it.key == KEY_SUB_EXPIRY }?.value
            val planName = configs.find { it.key == KEY_SUB_PLAN }?.value
            val now = System.currentTimeMillis()
            val expiry = expiryStr?.toLongOrNull()

            if (expiry != null && expiry > now) {
                val diffDays = ((expiry - now) / (1000 * 60 * 60 * 24)).toInt() + 1
                UserSubscription(
                    isPremium = true,
                    planName = planName ?: "Abonnement Actif",
                    expiryTimestamp = expiry,
                    daysRemaining = diffDays
                )
            } else {
                UserSubscription(
                    isPremium = false,
                    planName = null,
                    expiryTimestamp = null,
                    daysRemaining = 0
                )
            }
        }
    }

    // WhatsApp Phone
    suspend fun getWhatsAppPhone(): String {
        return dao.getConfigValue(KEY_WHATSAPP_PHONE) ?: DEFAULT_WHATSAPP
    }

    suspend fun setWhatsAppPhone(newPhone: String) {
        dao.setConfigValue(AppConfigEntity(KEY_WHATSAPP_PHONE, newPhone))
        logAudit("CONFIG_UPDATE", "Numéro WhatsApp mis à jour: $newPhone")
    }

    // Activation Code Validation & Execution
    sealed class ActivationResult {
        data class Success(val planName: String, val durationDays: Int, val newExpiryTimestamp: Long) : ActivationResult()
        data class Error(val message: String) : ActivationResult()
        data class Lockout(val secondsRemaining: Long) : ActivationResult()
    }

    suspend fun activateCode(inputCode: String, userId: String = "BURKINA_USER_01"): ActivationResult {
        val cleanCode = inputCode.trim().uppercase()
        val now = System.currentTimeMillis()

        // Check lockout
        val lockoutUntil = dao.getConfigValue(KEY_LOCKOUT_UNTIL)?.toLongOrNull() ?: 0L
        if (now < lockoutUntil) {
            val sec = (lockoutUntil - now) / 1000
            return ActivationResult.Lockout(sec)
        }

        val codeEntity = dao.getActivationCode(cleanCode)
        if (codeEntity == null) {
            handleFailedAttempt(now)
            return ActivationResult.Error("Code d'activation inexistant ou mal saisi. Vérifiez les caractères.")
        }

        if (codeEntity.isRevoked) {
            handleFailedAttempt(now)
            return ActivationResult.Error("Ce code a été révoqué par l'administrateur.")
        }

        if (codeEntity.isUsed) {
            handleFailedAttempt(now)
            return ActivationResult.Error("Ce code a déjà été utilisé le ${formatDate(codeEntity.usedAt ?: 0L)}.")
        }

        // Valid code! Apply subscription
        val currentExpiry = dao.getConfigValue(KEY_SUB_EXPIRY)?.toLongOrNull() ?: 0L
        val baseTime = if (currentExpiry > now) currentExpiry else now
        val durationMillis = codeEntity.durationDays * 24L * 60 * 60 * 1000
        val newExpiry = baseTime + durationMillis

        // Atomic DB updates
        dao.markCodeAsUsed(cleanCode, now, userId)
        dao.setConfigValue(AppConfigEntity(KEY_SUB_EXPIRY, newExpiry.toString()))
        dao.setConfigValue(AppConfigEntity(KEY_SUB_PLAN, codeEntity.planName))
        // Reset failed attempts
        dao.setConfigValue(AppConfigEntity(KEY_FAILED_ATTEMPTS, "0"))

        logAudit("CODE_ACTIVATED", "Code ${codeEntity.code} (${codeEntity.planName}, +${codeEntity.durationDays}j) activé par $userId")

        return ActivationResult.Success(
            planName = codeEntity.planName,
            durationDays = codeEntity.durationDays,
            newExpiryTimestamp = newExpiry
        )
    }

    private suspend fun handleFailedAttempt(now: Long) {
        val currentAttempts = (dao.getConfigValue(KEY_FAILED_ATTEMPTS)?.toIntOrNull() ?: 0) + 1
        dao.setConfigValue(AppConfigEntity(KEY_FAILED_ATTEMPTS, currentAttempts.toString()))
        if (currentAttempts >= 5) {
            val lockout = now + (10 * 60 * 1000) // 10 minutes lockout
            dao.setConfigValue(AppConfigEntity(KEY_LOCKOUT_UNTIL, lockout.toString()))
            logAudit("SECURITY_LOCKOUT", "Blocage anti-bruteforce activé après 5 échecs consécutifs.")
        }
    }

    // Admin: Generate Activation Codes
    suspend fun generateActivationCodes(plan: SubscriptionPlan, count: Int = 1): List<String> {
        val generated = mutableListOf<String>()
        val entities = mutableListOf<ActivationCodeEntity>()
        val prefix = when (plan.id) {
            "pass_daily" -> "FASO-DAY"
            "pass_weekly" -> "FASO-WEEK"
            "pass_monthly" -> "FASO-MONTH"
            "pass_quarterly" -> "FASO-TRIM"
            "pass_biannual" -> "FASO-SEME"
            "pass_annual" -> "FASO-VIP"
            else -> "FASO-PREM"
        }

        for (i in 1..count) {
            val randomSuffix = generateRandomCodeString(6)
            val fullCode = "$prefix-$randomSuffix"
            generated.add(fullCode)
            entities.add(
                ActivationCodeEntity(
                    code = fullCode,
                    planId = plan.id,
                    planName = plan.name,
                    durationDays = plan.durationDays,
                    isUsed = false,
                    isRevoked = false,
                    createdAt = System.currentTimeMillis(),
                    usedAt = null,
                    usedBy = null
                )
            )
        }

        dao.insertActivationCodes(entities)
        logAudit("CODES_GENERATED", "$count code(s) généré(s) pour la formule ${plan.name} ($prefix-...)")
        return generated
    }

    // Admin: Revoke Code
    suspend fun revokeCode(code: String) {
        dao.revokeCode(code)
        logAudit("CODE_REVOKED", "Code $code révoqué par l'administrateur")
    }

    // Admin: Update Match Outcome (WON, LOST, VOID)
    suspend fun updateMatchOutcome(matchId: String, scoreHome: Int?, scoreAway: Int?, status: MatchStatus, outcome: PredictionOutcome) {
        dao.updateMatchOutcome(
            matchId = matchId,
            scoreHome = scoreHome,
            scoreAway = scoreAway,
            status = status.name,
            outcome = outcome.name
        )
        logAudit("MATCH_RESULT_AUDIT", "Match $matchId validé: $scoreHome-$scoreAway (${outcome.name})")
    }

    // Admin: Publish new custom tip
    suspend fun publishNewPrediction(
        homeTeam: String,
        awayTeam: String,
        competition: String,
        country: String,
        sport: SportType,
        matchDate: String,
        matchTime: String,
        primaryPick: String,
        secondaryPick: String,
        odds: Double,
        isPremium: Boolean,
        favorableFactors: List<String>,
        riskFactors: List<String>,
        explanation: String
    ) {
        val matchId = "custom_${System.currentTimeMillis()}"
        val probs = SportsAnalyticsEngine.calculateMatchProbabilities(1.4, 0.9, 1.0, 1.2)
        val entity = MatchEntity(
            id = matchId,
            homeTeam = homeTeam,
            awayTeam = awayTeam,
            competition = competition,
            country = country,
            matchDate = matchDate,
            matchTime = matchTime,
            sportName = sport.name,
            statusName = MatchStatus.UPCOMING.name,
            scoreHome = null,
            scoreAway = null,
            liveMinute = null,
            formHome = "V-N-V",
            formAway = "N-D-V",
            headToHead = "Historique en cours d'actualisation",
            hasPrediction = true,
            predictionTitle = "$primaryPick (Cote @$odds)",
            primaryPick = primaryPick,
            secondaryPick = secondaryPick,
            estimatedOdds = odds,
            probHomeWin = probs.probHomeWin,
            probDraw = probs.probDraw,
            probAwayWin = probs.probAwayWin,
            confidencePercent = probs.confidencePercent,
            confidenceLevel = probs.confidenceLevel,
            expectedGoalsHome = probs.expectedGoalsHome,
            expectedGoalsAway = probs.expectedGoalsAway,
            favorableFactors = favorableFactors,
            riskFactors = riskFactors,
            statisticalExplanation = explanation,
            modelSource = "FasoEngine v2.4 (Poisson & Elo)",
            lastUpdated = "À l'instant",
            isPremium = isPremium,
            outcomeName = PredictionOutcome.PENDING.name
        )
        dao.insertMatch(entity)
        logAudit("PREDICTION_PUBLISHED", "Pronostic publié: $homeTeam vs $awayTeam ($primaryPick)")
    }

    // Admin: Stats calculation
    suspend fun getAdminSummary(matches: List<MatchItem>, codes: List<ActivationCode>): AdminStatSummary {
        val predictions = matches.mapNotNull { it.prediction }
        val evaluated = predictions.filter { it.outcome == PredictionOutcome.WON || it.outcome == PredictionOutcome.LOST }
        val won = evaluated.count { it.outcome == PredictionOutcome.WON }
        val strikeRate = if (evaluated.isNotEmpty()) (won.toDouble() / evaluated.size) * 100 else 76.5
        val availableCodes = codes.count { !it.isUsed && !it.isRevoked }

        return AdminStatSummary(
            activeSubscribersCount = 142,
            totalRevenueFCFA = 1_285_000L,
            pendingRequestsCount = 4,
            totalPredictionsCount = predictions.size,
            wonPredictionsCount = won,
            globalStrikeRate = Math.round(strikeRate * 10.0) / 10.0,
            availableCodesCount = availableCodes
        )
    }

    // Community Poll Vote
    fun voteInPoll(optionIndex: Int) {
        val current = _dailyPoll.value
        if (current.userVotedOption != null) return
        val updated = when (optionIndex) {
            1 -> current.copy(votesHome = current.votesHome + 1, userVotedOption = 1)
            2 -> current.copy(votesDraw = current.votesDraw + 1, userVotedOption = 2)
            3 -> current.copy(votesAway = current.votesAway + 1, userVotedOption = 3)
            else -> current
        }
        _dailyPoll.value = updated
    }

    // Log administrative action
    private suspend fun logAudit(action: String, details: String) {
        val entity = AuditLogEntity(
            id = UUID.randomUUID().toString(),
            action = action,
            details = details,
            timestamp = System.currentTimeMillis()
        )
        dao.insertAuditLog(entity)
    }

    private fun generateRandomCodeString(length: Int): String {
        val chars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ" // sans 0, 1, I, O pour éviter les confusions
        return (1..length)
            .map { chars[Random.nextInt(chars.length)] }
            .joinToString("")
    }

    private fun formatDate(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH)
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            ""
        }
    }

    // Seed comprehensive multi-sport matches with statistical models
    private suspend fun seedInitialDataIfNeeded() {
        val existing = dao.getMatchById("bf_match_1")
        if (existing != null) return

        val initialMatches = mutableListOf<MatchEntity>()

        // 1. Burkina Faso Championnat National Football - ASFA Yennenga vs RC Bobo
        val p1 = SportsAnalyticsEngine.calculateMatchProbabilities(1.65, 0.85, 0.95, 1.25)
        initialMatches.add(
            MatchEntity(
                id = "bf_match_1",
                homeTeam = "ASFA Yennenga",
                awayTeam = "RC Bobo-Dioulasso",
                competition = "Ligue 1 Burkinabè",
                country = "Burkina Faso",
                matchDate = "Aujourd'hui",
                matchTime = "16:00",
                sportName = SportType.FOOTBALL.name,
                statusName = MatchStatus.UPCOMING.name,
                scoreHome = null,
                scoreAway = null,
                liveMinute = null,
                formHome = "V-V-N-V-D",
                formAway = "D-N-V-D-V",
                headToHead = "3 victoires ASFA, 1 nul, 1 victoire RCB",
                hasPrediction = true,
                predictionTitle = "ASFA Yennenga ou Nul & Moins de 2.5 buts",
                primaryPick = "1X & Moins de 2.5 buts",
                secondaryPick = "Score exact 1 - 0",
                estimatedOdds = 1.72,
                probHomeWin = p1.probHomeWin,
                probDraw = p1.probDraw,
                probAwayWin = p1.probAwayWin,
                confidencePercent = 78,
                confidenceLevel = "Élevé",
                expectedGoalsHome = p1.expectedGoalsHome,
                expectedGoalsAway = p1.expectedGoalsAway,
                favorableFactors = listOf(
                    "ASFA invaincu à domicile lors des 6 dernières réceptions à Ouagadougou",
                    "Défense de l'ASFA : seulement 4 buts concédés en 8 journées",
                    "RC Bobo en manque d'efficacité offensive en déplacement (0.7 but/m)"
                ),
                riskFactors = listOf(
                    "Choc historique équilibré avec forte intensité tactique",
                    "Possibilité de bloc bas de RCB cherchant le match nul"
                ),
                statisticalExplanation = "Le modèle de Poisson et le différentiel Elo accordent un net avantage à l'ASFA à domicile (62% de probabilité 1X). La moyenne de buts projetée est de 1.85, justifiant le marché combiné de sécurité.",
                modelSource = "FasoEngine v2.4 (Poisson & Elo)",
                lastUpdated = "Il y a 22 min",
                isPremium = false, // Free pick of the day!
                outcomeName = PredictionOutcome.PENDING.name
            )
        )

        // 2. Étoile Filante de Ouagadougou (EFO) vs Salitas FC (Burkina Faso) - PREMIUM
        val p2 = SportsAnalyticsEngine.calculateMatchProbabilities(1.4, 1.1, 1.35, 1.05)
        initialMatches.add(
            MatchEntity(
                id = "bf_match_2",
                homeTeam = "EFO Ouagadougou",
                awayTeam = "Salitas FC",
                competition = "Ligue 1 Burkinabè",
                country = "Burkina Faso",
                matchDate = "Aujourd'hui",
                matchTime = "18:15",
                sportName = SportType.FOOTBALL.name,
                statusName = MatchStatus.UPCOMING.name,
                scoreHome = null,
                scoreAway = null,
                liveMinute = null,
                formHome = "N-V-D-V-V",
                formAway = "V-V-V-D-N",
                headToHead = "2 victoires EFO, 2 victoires Salitas, 1 nul",
                hasPrediction = true,
                predictionTitle = "Les deux équipes marquent (BTTS - OUI)",
                primaryPick = "Les deux équipes marquent",
                secondaryPick = "Plus de 1.5 buts match",
                estimatedOdds = 1.95,
                probHomeWin = p2.probHomeWin,
                probDraw = p2.probDraw,
                probAwayWin = p2.probAwayWin,
                confidencePercent = 82,
                confidenceLevel = "Élevé",
                expectedGoalsHome = p2.expectedGoalsHome,
                expectedGoalsAway = p2.expectedGoalsAway,
                favorableFactors = listOf(
                    "Salitas FC a marqué lors de 9 de ses 10 derniers matchs officiels",
                    "EFO a trouvé le chemin des filets lors de chacun de ses 5 derniers matchs à Ouaga",
                    "Confrontations directes prolifiques : 4/5 ont vu les deux équipes marquer"
                ),
                riskFactors = listOf(
                    "Terrain lourd pouvant ralentir le jeu de transition de Salitas"
                ),
                statisticalExplanation = "Indice de prolifération offensive calculé à 74% de chances de BTTS. Le profil offensif des deux formations dépasse la moyenne de la ligue burkinabè.",
                modelSource = "FasoEngine v2.4 (Poisson Bivarié)",
                lastUpdated = "Il y a 35 min",
                isPremium = true, // Premium
                outcomeName = PredictionOutcome.PENDING.name
            )
        )

        // 3. CAF Champions League: Al Ahly vs TP Mazembe - PREMIUM
        val p3 = SportsAnalyticsEngine.calculateMatchProbabilities(1.85, 0.7, 0.8, 1.4)
        initialMatches.add(
            MatchEntity(
                id = "caf_match_1",
                homeTeam = "Al Ahly SC",
                awayTeam = "TP Mazembe",
                competition = "Ligue des Champions CAF",
                country = "Afrique",
                matchDate = "Aujourd'hui",
                matchTime = "19:00",
                sportName = SportType.FOOTBALL.name,
                statusName = MatchStatus.UPCOMING.name,
                scoreHome = null,
                scoreAway = null,
                liveMinute = null,
                formHome = "V-V-V-N-V",
                formAway = "N-V-D-V-N",
                headToHead = "Al Ahly domine au Caire (3V sur 3)",
                hasPrediction = true,
                predictionTitle = "Victoire Al Ahly & Plus de 1.5 buts",
                primaryPick = "Al Ahly gagne",
                secondaryPick = "Score 2 - 0",
                estimatedOdds = 1.68,
                probHomeWin = p3.probHomeWin,
                probDraw = p3.probDraw,
                probAwayWin = p3.probAwayWin,
                confidencePercent = 84,
                confidenceLevel = "Élevé",
                expectedGoalsHome = p3.expectedGoalsHome,
                expectedGoalsAway = p3.expectedGoalsAway,
                favorableFactors = listOf(
                    "Al Ahly impérial au Stade International du Caire devant 60 000 supporters",
                    "TP Mazembe historiquement en grande difficulté en déplacement maghrébin",
                    "Retour du maître à jouer égyptien au milieu de terrain"
                ),
                riskFactors = listOf(
                    "Vitesse de percussion des ailiers congolais en contre"
                ),
                statisticalExplanation = "Probabilité de victoire à domicile évaluée à 66%. L'indicateur xG d'Al Ahly au Caire est supérieur à 2.1 buts/match.",
                modelSource = "FasoEngine v2.4 (Elo Continental CAF)",
                lastUpdated = "Il y a 10 min",
                isPremium = true,
                outcomeName = PredictionOutcome.PENDING.name
            )
        )

        // 4. UEFA Champions League: Real Madrid vs Manchester City - PREMIUM
        val p4 = SportsAnalyticsEngine.calculateMatchProbabilities(1.7, 1.2, 1.6, 1.15)
        initialMatches.add(
            MatchEntity(
                id = "ucl_match_1",
                homeTeam = "Real Madrid",
                awayTeam = "Manchester City",
                competition = "UEFA Champions League",
                country = "Europe",
                matchDate = "Ce soir",
                matchTime = "20:00",
                sportName = SportType.FOOTBALL.name,
                statusName = MatchStatus.UPCOMING.name,
                scoreHome = null,
                scoreAway = null,
                liveMinute = null,
                formHome = "V-V-V-V-N",
                formAway = "V-V-N-V-V",
                headToHead = "Spectacles à hauts scores : 3-3, 1-1, 4-3",
                hasPrediction = true,
                predictionTitle = "Plus de 2.5 buts & Les 2 équipes marquent",
                primaryPick = "+2.5 buts & BTTS",
                secondaryPick = "Double chance 1X",
                estimatedOdds = 1.88,
                probHomeWin = p4.probHomeWin,
                probDraw = p4.probDraw,
                probAwayWin = p4.probAwayWin,
                confidencePercent = 80,
                confidenceLevel = "Élevé",
                expectedGoalsHome = p4.expectedGoalsHome,
                expectedGoalsAway = p4.expectedGoalsAway,
                favorableFactors = listOf(
                    "Attaques de classe mondiale : Vinicius Jr, Bellingham vs Haaland, De Bruyne",
                    "6 des 7 dernières confrontations ont dépassé les 2.5 buts"
                ),
                riskFactors = listOf(
                    "Gestion tactique prudente possible en début de première période"
                ),
                statisticalExplanation = "Modèle xG cumulé estimé à 3.4 buts. La probabilité d'over 2.5 buts ressort à 68% sur notre simulation de Monte-Carlo.",
                modelSource = "FasoEngine v2.4 (Monte-Carlo xG)",
                lastUpdated = "Il y a 1h",
                isPremium = true,
                outcomeName = PredictionOutcome.PENDING.name
            )
        )

        // 5. Basketball: Basketball Africa League (BAL) - Petro de Luanda vs US Monastir
        val p5 = SportsAnalyticsEngine.calculateMatchProbabilities(2.2, 1.8, 1.9, 2.0)
        initialMatches.add(
            MatchEntity(
                id = "bal_match_1",
                homeTeam = "Petro de Luanda",
                awayTeam = "US Monastir",
                competition = "Basketball Africa League (BAL)",
                country = "Afrique",
                matchDate = "Aujourd'hui",
                matchTime = "17:30",
                sportName = SportType.BASKETBALL.name,
                statusName = MatchStatus.UPCOMING.name,
                scoreHome = null,
                scoreAway = null,
                liveMinute = null,
                formHome = "V-V-V-D-V",
                formAway = "V-D-V-V-D",
                headToHead = "Petro 84 - 79 Monastir lors du tournoi de Dakar",
                hasPrediction = true,
                predictionTitle = "Victoire Petro de Luanda (Handicap -4.5)",
                primaryPick = "Petro de Luanda -4.5 pts",
                secondaryPick = "Plus de 158.5 points match",
                estimatedOdds = 1.78,
                probHomeWin = 64,
                probDraw = 0,
                probAwayWin = 36,
                confidencePercent = 75,
                confidenceLevel = "Élevé",
                expectedGoalsHome = 83.5,
                expectedGoalsAway = 77.0,
                favorableFactors = listOf(
                    "Domination de Petro sous le cercle avec une moyenne de 44 rebonds/match",
                    "Monastir privé de son meneur titulaire sur blessure cheville"
                ),
                riskFactors = listOf(
                    "Monastir très redoutable aux tirs extérieurs à 3 points si adresse > 40%"
                ),
                statisticalExplanation = "Analyse différentielle de rythme (Pace) et d'efficacité offensive : Petro affiche +6.8 pts de net rating dans la conférence.",
                modelSource = "FasoEngine Basket Pro v1.9",
                lastUpdated = "Il y a 40 min",
                isPremium = false, // Free basketball pick!
                outcomeName = PredictionOutcome.PENDING.name
            )
        )

        // 6. Basketball NBA: Boston Celtics vs Milwaukee Bucks - PREMIUM
        initialMatches.add(
            MatchEntity(
                id = "nba_match_1",
                homeTeam = "Boston Celtics",
                awayTeam = "Milwaukee Bucks",
                competition = "NBA",
                country = "USA",
                matchDate = "Cette nuit",
                matchTime = "00:30",
                sportName = SportType.BASKETBALL.name,
                statusName = MatchStatus.UPCOMING.name,
                scoreHome = null,
                scoreAway = null,
                liveMinute = null,
                formHome = "V-V-V-V-D",
                formAway = "V-D-V-N-V",
                headToHead = "Boston reste sur 3 victoires d'affilée au TD Garden",
                hasPrediction = true,
                predictionTitle = "Boston Celtics gagne de 6 points ou plus",
                primaryPick = "Boston Celtics -5.5",
                secondaryPick = "Plus de 224.5 points",
                estimatedOdds = 1.85,
                probHomeWin = 68,
                probDraw = 0,
                probAwayWin = 32,
                confidencePercent = 79,
                confidenceLevel = "Élevé",
                expectedGoalsHome = 118.0,
                expectedGoalsAway = 109.5,
                favorableFactors = listOf(
                    "Tatum et Brown en très grande forme au TD Garden",
                    "Défense de Milwaukee poreuse en transition"
                ),
                riskFactors = listOf(
                    "Giannis Antetokounmpo capable de scorer 40+ points"
                ),
                statisticalExplanation = "Modèle de possession NBA : Boston bénéficie d'une efficacité nette de +11.2 à domicile.",
                modelSource = "FasoEngine NBA Elo",
                lastUpdated = "Il y a 1h",
                isPremium = true,
                outcomeName = PredictionOutcome.PENDING.name
            )
        )

        // 7. Tennis: Carlos Alcaraz vs Jannik Sinner - PREMIUM
        initialMatches.add(
            MatchEntity(
                id = "tennis_match_1",
                homeTeam = "Carlos Alcaraz",
                awayTeam = "Jannik Sinner",
                competition = "ATP Masters 1000",
                country = "International",
                matchDate = "Demain",
                matchTime = "14:00",
                sportName = SportType.TENNIS.name,
                statusName = MatchStatus.UPCOMING.name,
                scoreHome = null,
                scoreAway = null,
                liveMinute = null,
                formHome = "V-V-V-V-V",
                formAway = "V-V-V-V-D",
                headToHead = "5-4 pour Alcaraz dans les duels directs",
                hasPrediction = true,
                predictionTitle = "Plus de 22.5 jeux disputés dans le match",
                primaryPick = "Plus de 22.5 jeux",
                secondaryPick = "Match en 3 sets",
                estimatedOdds = 1.82,
                probHomeWin = 52,
                probDraw = 0,
                probAwayWin = 48,
                confidencePercent = 81,
                confidenceLevel = "Élevé",
                expectedGoalsHome = 0.0,
                expectedGoalsAway = 0.0,
                favorableFactors = listOf(
                    "Deux des meilleurs relanceurs du circuit mondial",
                    "7 de leurs 9 duels précédents ont été disputés jusqu'au set décisif"
                ),
                riskFactors = listOf(
                    "Possibilité d'un break d'entrée conditionnant un set rapide 6-2"
                ),
                statisticalExplanation = "Le modèle de tenue de service accorde 78% de jeux tenus de part et d'autre, projetant un tie-break quasi certain.",
                modelSource = "FasoEngine ATP Service/Return Model",
                lastUpdated = "Il y a 2h",
                isPremium = true,
                outcomeName = PredictionOutcome.PENDING.name
            )
        )

        // 8. Handball: Paris SG vs FC Barcelone (EHF Champions League) - FREE
        initialMatches.add(
            MatchEntity(
                id = "handball_match_1",
                homeTeam = "Paris SG Handball",
                awayTeam = "FC Barcelone Handball",
                competition = "Ligue des Champions EHF",
                country = "Europe",
                matchDate = "Demain",
                matchTime = "19:45",
                sportName = SportType.HANDBALL.name,
                statusName = MatchStatus.UPCOMING.name,
                scoreHome = null,
                scoreAway = null,
                liveMinute = null,
                formHome = "V-V-D-V-V",
                formAway = "V-V-V-V-V",
                headToHead = "Barcelone mène 4 victoires à 2",
                hasPrediction = true,
                predictionTitle = "Plus de 62.5 buts dans le match",
                primaryPick = "Plus de 62.5 buts",
                secondaryPick = "Barcelone gagne",
                estimatedOdds = 1.75,
                probHomeWin = 42,
                probDraw = 10,
                probAwayWin = 48,
                confidencePercent = 74,
                confidenceLevel = "Élevé",
                expectedGoalsHome = 31.0,
                expectedGoalsAway = 33.0,
                favorableFactors = listOf(
                    "Attaques très rapides avec jeu de relance en moins de 8 secondes",
                    "Moyenne de tirs cadrés supérieure à 82% des deux côtés"
                ),
                riskFactors = listOf(
                    "Grosse performance possible des gardiens de but"
                ),
                statisticalExplanation = "Pace de jeu projeté à 66 possessions par équipe. Historique direct : 65 buts en moyenne.",
                modelSource = "FasoEngine Handball Pace Model",
                lastUpdated = "Il y a 3h",
                isPremium = false,
                outcomeName = PredictionOutcome.PENDING.name
            )
        )

        // 9. Historical evaluated match (WON) - Burkina Faso vs Mauritanie
        initialMatches.add(
            MatchEntity(
                id = "hist_match_1",
                homeTeam = "Burkina Faso (Étalons)",
                awayTeam = "Mauritanie",
                competition = "Coupe d'Afrique des Nations",
                country = "Afrique",
                matchDate = "16 Oct",
                matchTime = "Terminé",
                sportName = SportType.FOOTBALL.name,
                statusName = MatchStatus.FINISHED.name,
                scoreHome = 1,
                scoreAway = 0,
                liveMinute = "FT",
                formHome = "V-V-N-V-V",
                formAway = "D-N-V-D-D",
                headToHead = "Burkina Faso invaincu lors des 4 derniers duels",
                hasPrediction = true,
                predictionTitle = "Victoire des Étalons du Burkina Faso",
                primaryPick = "Burkina Faso gagne",
                secondaryPick = "Moins de 2.5 buts",
                estimatedOdds = 1.70,
                probHomeWin = 65,
                probDraw = 23,
                probAwayWin = 12,
                confidencePercent = 85,
                confidenceLevel = "Élevé",
                expectedGoalsHome = 1.5,
                expectedGoalsAway = 0.4,
                favorableFactors = listOf(
                    "Bertrand Traoré décisif dans les grands rendez-vous",
                    "Rigueur défensive d'Edmond Tapsoba"
                ),
                riskFactors = listOf("Solidité du bloc défensif mauritanien"),
                statisticalExplanation = "Domination territoriale et différentiel de valeur marchande confirmés.",
                modelSource = "FasoEngine CAN Special",
                lastUpdated = "Archivé",
                isPremium = true,
                outcomeName = PredictionOutcome.WON.name
            )
        )

        // 10. Historical evaluated match (WON) - Arsenal vs Chelsea
        initialMatches.add(
            MatchEntity(
                id = "hist_match_2",
                homeTeam = "Arsenal FC",
                awayTeam = "Chelsea FC",
                competition = "Premier League",
                country = "Angleterre",
                matchDate = "15 Oct",
                matchTime = "Terminé",
                sportName = SportType.FOOTBALL.name,
                statusName = MatchStatus.FINISHED.name,
                scoreHome = 2,
                scoreAway = 1,
                liveMinute = "FT",
                formHome = "V-V-V-D-V",
                formAway = "N-V-D-N-V",
                headToHead = "Arsenal reste sur 3 victoires à l'Emirates",
                hasPrediction = true,
                predictionTitle = "Victoire Arsenal & Plus de 1.5 buts",
                primaryPick = "Arsenal gagne",
                secondaryPick = "+2.5 buts match",
                estimatedOdds = 1.82,
                probHomeWin = 62,
                probDraw = 22,
                probAwayWin = 16,
                confidencePercent = 82,
                confidenceLevel = "Élevé",
                expectedGoalsHome = 2.1,
                expectedGoalsAway = 0.9,
                favorableFactors = listOf("Arsenal intraitable à l'Emirates"),
                riskFactors = listOf("Derby londonien engagé"),
                statisticalExplanation = "Indice de possession et de conversion d'occasions nettes nettement favorable.",
                modelSource = "FasoEngine Premier League",
                lastUpdated = "Archivé",
                isPremium = false,
                outcomeName = PredictionOutcome.WON.name
            )
        )

        // 11. Historical evaluated match (LOST) - Transparent, no hiding losses!
        initialMatches.add(
            MatchEntity(
                id = "hist_match_3",
                homeTeam = "Borussia Dortmund",
                awayTeam = "VfB Stuttgart",
                competition = "Bundesliga",
                country = "Allemagne",
                matchDate = "14 Oct",
                matchTime = "Terminé",
                sportName = SportType.FOOTBALL.name,
                statusName = MatchStatus.FINISHED.name,
                scoreHome = 1,
                scoreAway = 2,
                liveMinute = "FT",
                formHome = "V-V-N-V-D",
                formAway = "V-V-V-D-V",
                headToHead = "Stuttgart en forme cette saison",
                hasPrediction = true,
                predictionTitle = "Victoire Dortmund au Signal Iduna Park",
                primaryPick = "Dortmund gagne",
                secondaryPick = "+2.5 buts",
                estimatedOdds = 1.90,
                probHomeWin = 54,
                probDraw = 24,
                probAwayWin = 22,
                confidencePercent = 65,
                confidenceLevel = "Modéré",
                expectedGoalsHome = 1.8,
                expectedGoalsAway = 1.3,
                favorableFactors = listOf("Mur jaune et soutien des 80 000 supporters"),
                riskFactors = listOf("Contre-attaques rapides de Stuttgart"),
                statisticalExplanation = "Match serré avec forte variance offensive.",
                modelSource = "FasoEngine Bundesliga",
                lastUpdated = "Archivé",
                isPremium = true,
                outcomeName = PredictionOutcome.LOST.name
            )
        )

        dao.insertMatches(initialMatches)

        // Seed initial Activation Codes (Ready for testing by user!)
        val initialCodes = listOf(
            ActivationCodeEntity(
                code = "FASO-TEST-PREMIUM",
                planId = "pass_monthly",
                planName = "Pass Mensuel",
                durationDays = 30,
                isUsed = false,
                isRevoked = false,
                createdAt = System.currentTimeMillis() - 3600000,
                usedAt = null,
                usedBy = null
            ),
            ActivationCodeEntity(
                code = "FASO-WEEK-OUAGA",
                planId = "pass_weekly",
                planName = "Pass Hebdomadaire",
                durationDays = 7,
                isUsed = false,
                isRevoked = false,
                createdAt = System.currentTimeMillis() - 7200000,
                usedAt = null,
                usedBy = null
            ),
            ActivationCodeEntity(
                code = "FASO-VIP-BOBO",
                planId = "pass_annual",
                planName = "Pass Annuel",
                durationDays = 365,
                isUsed = false,
                isRevoked = false,
                createdAt = System.currentTimeMillis() - 10800000,
                usedAt = null,
                usedBy = null
            ),
            ActivationCodeEntity(
                code = "FASO-DAY-KDG",
                planId = "pass_daily",
                planName = "Pass Journalier",
                durationDays = 1,
                isUsed = true, // already used example
                isRevoked = false,
                createdAt = System.currentTimeMillis() - 86400000,
                usedAt = System.currentTimeMillis() - 40000000,
                usedBy = "DEMO_USER_99"
            )
        )
        dao.insertActivationCodes(initialCodes)

        // Seed initial config
        dao.setConfigValue(AppConfigEntity(KEY_WHATSAPP_PHONE, DEFAULT_WHATSAPP))
        dao.setConfigValue(AppConfigEntity(KEY_FAILED_ATTEMPTS, "0"))

        logAudit("SYSTEM_INIT", "Initialisation de la base de données FASO PRONOSTICS avec succès")
    }

    // Entity to Domain Mapper
    private fun MatchEntity.toDomain(): MatchItem {
        val pred = if (this.hasPrediction) {
            PredictionItem(
                id = "pred_${this.id}",
                matchId = this.id,
                title = this.predictionTitle ?: "Analyse disponible",
                primaryPick = this.primaryPick ?: "N/A",
                secondaryPick = this.secondaryPick ?: "",
                estimatedOdds = this.estimatedOdds ?: 1.50,
                probHomeWin = this.probHomeWin ?: 40,
                probDraw = this.probDraw ?: 30,
                probAwayWin = this.probAwayWin ?: 30,
                confidencePercent = this.confidencePercent ?: 70,
                confidenceLevel = this.confidenceLevel ?: "Modéré",
                expectedGoalsHome = this.expectedGoalsHome ?: 1.2,
                expectedGoalsAway = this.expectedGoalsAway ?: 1.0,
                favorableFactors = this.favorableFactors ?: emptyList(),
                riskFactors = this.riskFactors ?: emptyList(),
                statisticalExplanation = this.statisticalExplanation ?: "Données statistiques en cours d'actualisation.",
                modelSource = this.modelSource ?: "FasoEngine v2.4",
                lastUpdated = this.lastUpdated ?: "Récemment",
                isPremium = this.isPremium,
                outcome = try {
                    PredictionOutcome.valueOf(this.outcomeName)
                } catch (e: Exception) {
                    PredictionOutcome.PENDING
                },
                resultScore = if (this.scoreHome != null && this.scoreAway != null) "${this.scoreHome} - ${this.scoreAway}" else null
            )
        } else null

        val sport = try {
            SportType.valueOf(this.sportName)
        } catch (e: Exception) {
            SportType.FOOTBALL
        }

        val status = try {
            MatchStatus.valueOf(this.statusName)
        } catch (e: Exception) {
            MatchStatus.UPCOMING
        }

        return MatchItem(
            id = this.id,
            homeTeam = this.homeTeam,
            awayTeam = this.awayTeam,
            competition = this.competition,
            country = this.country,
            matchDate = this.matchDate,
            matchTime = this.matchTime,
            sport = sport,
            status = status,
            scoreHome = this.scoreHome,
            scoreAway = this.scoreAway,
            liveMinute = this.liveMinute,
            formHome = this.formHome,
            formAway = this.formAway,
            headToHead = this.headToHead,
            prediction = pred
        )
    }

    private fun ActivationCodeEntity.toDomain(): ActivationCode {
        return ActivationCode(
            code = this.code,
            planId = this.planId,
            planName = this.planName,
            durationDays = this.durationDays,
            isUsed = this.isUsed,
            isRevoked = this.isRevoked,
            createdAt = this.createdAt,
            usedAt = this.usedAt,
            usedBy = this.usedBy
        )
    }
}
