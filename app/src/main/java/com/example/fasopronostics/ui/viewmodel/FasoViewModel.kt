package com.example.fasopronostics.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fasopronostics.data.local.AppDatabase
import com.example.fasopronostics.data.model.ActivationCode
import com.example.fasopronostics.data.model.AdminStatSummary
import com.example.fasopronostics.data.model.AuditLogItem
import com.example.fasopronostics.data.model.CommunityPoll
import com.example.fasopronostics.data.model.MatchItem
import com.example.fasopronostics.data.model.MatchStatus
import com.example.fasopronostics.data.model.PredictionOutcome
import com.example.fasopronostics.data.model.ReferralProfile
import com.example.fasopronostics.data.model.SportType
import com.example.fasopronostics.data.model.SubscriptionPlan
import com.example.fasopronostics.data.model.UserSubscription
import com.example.fasopronostics.data.repository.FasoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class FasoScreen {
    PRONOS,
    MATCHES,
    PREMIUM,
    HISTORY,
    PROFILE,
    ADMIN
}

data class FasoUiState(
    val currentScreen: FasoScreen = FasoScreen.PRONOS,
    val selectedSport: SportType = SportType.ALL,
    val filterPremiumOnly: Boolean = false,
    val selectedMatchForDetail: MatchItem? = null,
    val showActivationDialog: Boolean = false,
    val showShareDialog: Boolean = false,
    val shareMatchItem: MatchItem? = null,
    val activationInputCode: String = "",
    val activationLoading: Boolean = false,
    val activationErrorMessage: String? = null,
    val activationSuccessMessage: String? = null,
    val lockoutSeconds: Long = 0,
    val adminPinInput: String = "",
    val isAdminAuthenticated: Boolean = false,
    val adminWhatsAppPhone: String = FasoRepository.DEFAULT_WHATSAPP
)

class FasoViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = FasoRepository(db.fasoDao())

    private val _uiState = MutableStateFlow(FasoUiState())
    val uiState: StateFlow<FasoUiState> = _uiState.asStateFlow()

    // Screen backstack for standard navigation
    private val screenBackstack = mutableListOf(FasoScreen.PRONOS)

    val matches: StateFlow<List<MatchItem>> = repository.allMatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subscription: StateFlow<UserSubscription> = repository.getUserSubscription()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            UserSubscription(isPremium = false, planName = null, expiryTimestamp = null, daysRemaining = 0)
        )

    val activationCodes: StateFlow<List<ActivationCode>> = repository.allActivationCodes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLogItem>> = repository.auditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyPoll: StateFlow<CommunityPoll> = repository.dailyPoll
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            CommunityPoll(
                id = "poll-1",
                matchTitle = "ASFA vs RCB",
                question = "Votre pronostic ?",
                optionHome = "1", votesHome = 10,
                optionDraw = "X", votesDraw = 5,
                optionAway = "2", votesAway = 8
            )
        )

    val referralProfile: StateFlow<ReferralProfile> = repository.referralProfile
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            ReferralProfile("FASO-7842", 2, 2, 3, 0)
        )

    val adminSummary: StateFlow<AdminStatSummary> = combine(matches, activationCodes) { m, c ->
        repository.getAdminSummary(m, c)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AdminStatSummary(142, 1285000L, 4, 11, 8, 72.7, 3)
    )

    init {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                adminWhatsAppPhone = repository.getWhatsAppPhone()
            )
        }
    }

    // Navigation
    fun navigateTo(screen: FasoScreen) {
        if (_uiState.value.currentScreen != screen) {
            screenBackstack.add(screen)
            _uiState.value = _uiState.value.copy(
                currentScreen = screen,
                selectedMatchForDetail = null
            )
        }
    }

    fun navigateBack(): Boolean {
        if (_uiState.value.selectedMatchForDetail != null) {
            _uiState.value = _uiState.value.copy(selectedMatchForDetail = null)
            return true
        }
        if (screenBackstack.size > 1) {
            screenBackstack.removeAt(screenBackstack.lastIndex)
            val previous = screenBackstack.last()
            _uiState.value = _uiState.value.copy(currentScreen = previous)
            return true
        }
        return false
    }

    fun selectSport(sport: SportType) {
        _uiState.value = _uiState.value.copy(selectedSport = sport)
    }

    fun setFilterPremiumOnly(premiumOnly: Boolean) {
        _uiState.value = _uiState.value.copy(filterPremiumOnly = premiumOnly)
    }

    fun openMatchDetail(match: MatchItem) {
        _uiState.value = _uiState.value.copy(selectedMatchForDetail = match)
    }

    fun closeMatchDetail() {
        _uiState.value = _uiState.value.copy(selectedMatchForDetail = null)
    }

    fun openActivationDialog() {
        _uiState.value = _uiState.value.copy(
            showActivationDialog = true,
            activationErrorMessage = null,
            activationSuccessMessage = null,
            activationInputCode = ""
        )
    }

    fun closeActivationDialog() {
        _uiState.value = _uiState.value.copy(showActivationDialog = false)
    }

    fun openShareDialog(match: MatchItem) {
        _uiState.value = _uiState.value.copy(
            showShareDialog = true,
            shareMatchItem = match
        )
    }

    fun closeShareDialog() {
        _uiState.value = _uiState.value.copy(
            showShareDialog = false,
            shareMatchItem = null
        )
    }

    fun updateActivationInput(code: String) {
        _uiState.value = _uiState.value.copy(
            activationInputCode = code,
            activationErrorMessage = null
        )
    }

    // Activate Code
    fun submitActivationCode() {
        val code = _uiState.value.activationInputCode.trim()
        if (code.isBlank()) {
            _uiState.value = _uiState.value.copy(activationErrorMessage = "Veuillez saisir un code d'activation.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(activationLoading = true, activationErrorMessage = null)
            val result = repository.activateCode(code)
            when (result) {
                is FasoRepository.ActivationResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        activationLoading = false,
                        activationSuccessMessage = "Félicitations ! Votre formule ${result.planName} (+${result.durationDays} jours) est maintenant active.",
                        activationErrorMessage = null,
                        activationInputCode = ""
                    )
                }
                is FasoRepository.ActivationResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        activationLoading = false,
                        activationErrorMessage = result.message
                    )
                }
                is FasoRepository.ActivationResult.Lockout -> {
                    _uiState.value = _uiState.value.copy(
                        activationLoading = false,
                        lockoutSeconds = result.secondsRemaining,
                        activationErrorMessage = "Trop de tentatives infructueuses. Sécurité anti-bruteforce active. Réessayez dans ${result.secondsRemaining} secondes."
                    )
                }
            }
        }
    }

    // Community Poll Vote
    fun votePoll(option: Int) {
        repository.voteInPoll(option)
    }

    // WhatsApp Contact for Plan Subscription
    fun contactWhatsAppForSubscription(context: Context, plan: SubscriptionPlan) {
        val userName = "Supporter Faso #226"
        val userId = "BF-USER-7842"
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH)
        val currentDate = sdf.format(Date())
        val phone = _uiState.value.adminWhatsAppPhone.replace(" ", "").replace("+", "")

        val message = """
Bonjour JUJU MULTI PRONOS,

Je souhaite souscrire à votre abonnement VIP.

Nom du compte : $userName
Identifiant utilisateur : $userId
Formule choisie : ${plan.name}
Montant : ${plan.priceFCFA} FCFA (${plan.durationDays} jours)
Date de la demande : $currentDate

Code Promo BetWinner utilisé : JUJUDAV12
Merci de m'indiquer la procédure de paiement et d'activation de mon abonnement.
        """.trimIndent()

        val encoded = Uri.encode(message)
        val whatsappUri = Uri.parse("https://api.whatsapp.com/send?phone=$phone&text=$encoded")
        val intent = Intent(Intent.ACTION_VIEW, whatsappUri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to generic share / SMS if WhatsApp isn't installed
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
                putExtra(Intent.EXTRA_SUBJECT, "Demande Abonnement JUJU MULTI PRONOS")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(Intent.createChooser(shareIntent, "Contacter JUJU MULTI PRONOS via :"))
            } catch (err: Exception) {
                Toast.makeText(context, "Impossible d'ouvrir l'application de contact.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Native Match Share
    fun shareMatchPrediction(context: Context, match: MatchItem) {
        val pred = match.prediction
        val shareText = if (pred != null) {
            """
🔥 JUJU MULTI PRONOS — Par JuJu Dav
🏆 ${match.competition} (${match.country})
⚽ ${match.homeTeam} vs ${match.awayTeam}
📅 ${match.matchDate} à ${match.matchTime}

🎯 Pronostic : ${pred.primaryPick} (Cote estimée : @${pred.estimatedOdds})
📊 Indice de confiance : ${pred.confidencePercent}% (${pred.confidenceLevel})
🔍 Facteur clé : ${pred.favorableFactors.firstOrNull() ?: "Analyse statistique Poisson & Elo"}

🎁 CODE PROMO BETWINNER : JUJUDAV12 (Jusqu'à 200% de bonus 1er dépôt !)

⚠️ Mention légale : Estimation statistique basée sur l'historique officiel. Aucun gain n'est garanti à 100%. Jouez responsablement.

📲 Rejoins la communauté JUJU MULTI PRONOS dès maintenant !
            """.trimIndent()
        } else {
            """
🔥 JUJU MULTI PRONOS — Par JuJu Dav
🏆 ${match.competition}
⚽ ${match.homeTeam} vs ${match.awayTeam}
📅 ${match.matchDate} à ${match.matchTime}
🎁 Code Promo BetWinner : JUJUDAV12
📲 Rejoins JUJU MULTI PRONOS !
            """.trimIndent()
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareChooser = Intent.createChooser(sendIntent, "Partager le pronostic via :")
        shareChooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareChooser)
        closeShareDialog()
    }

    // Admin authentication
    fun authenticateAdmin(pin: String): Boolean {
        if (pin.trim() == "2912") {
            _uiState.value = _uiState.value.copy(
                isAdminAuthenticated = true,
                adminPinInput = ""
            )
            return true
        }
        return false
    }

    fun logoutAdmin() {
        _uiState.value = _uiState.value.copy(isAdminAuthenticated = false)
        navigateTo(FasoScreen.PRONOS)
    }

    // Admin Actions
    fun generateAdminCodes(plan: SubscriptionPlan, count: Int = 1) {
        viewModelScope.launch {
            repository.generateActivationCodes(plan, count)
        }
    }

    fun revokeAdminCode(code: String) {
        viewModelScope.launch {
            repository.revokeCode(code)
        }
    }

    fun updateMatchOutcome(matchId: String, scoreHome: Int?, scoreAway: Int?, status: MatchStatus, outcome: PredictionOutcome) {
        viewModelScope.launch {
            repository.updateMatchOutcome(matchId, scoreHome, scoreAway, status, outcome)
        }
    }

    fun updateWhatsAppPhone(newPhone: String) {
        viewModelScope.launch {
            repository.setWhatsAppPhone(newPhone)
            _uiState.value = _uiState.value.copy(adminWhatsAppPhone = newPhone)
        }
    }

    fun publishCustomPrediction(
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
        viewModelScope.launch {
            repository.publishNewPrediction(
                homeTeam = homeTeam,
                awayTeam = awayTeam,
                competition = competition,
                country = country,
                sport = sport,
                matchDate = matchDate,
                matchTime = matchTime,
                primaryPick = primaryPick,
                secondaryPick = secondaryPick,
                odds = odds,
                isPremium = isPremium,
                favorableFactors = favorableFactors,
                riskFactors = riskFactors,
                explanation = explanation
            )
        }
    }
}
