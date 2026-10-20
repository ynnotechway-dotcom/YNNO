package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fasopronostics.ui.components.ActivationDialog
import com.example.fasopronostics.ui.components.FasoBottomBar
import com.example.fasopronostics.ui.components.FasoTopAppBar
import com.example.fasopronostics.ui.components.PredictionDetailSheet
import com.example.fasopronostics.ui.components.ShareCardDialog
import com.example.fasopronostics.ui.screens.AdminScreen
import com.example.fasopronostics.ui.screens.HistoryScreen
import com.example.fasopronostics.ui.screens.MatchesScreen
import com.example.fasopronostics.ui.screens.PredictionsScreen
import com.example.fasopronostics.ui.screens.PremiumScreen
import com.example.fasopronostics.ui.screens.ProfileScreen
import com.example.fasopronostics.ui.theme.DarkBackground
import com.example.fasopronostics.ui.theme.DarkSurface
import com.example.fasopronostics.ui.theme.FasoPronosticsTheme
import com.example.fasopronostics.ui.viewmodel.FasoScreen
import com.example.fasopronostics.ui.viewmodel.FasoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FasoPronosticsTheme(darkTheme = true) {
                FasoPronosticsApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FasoPronosticsApp(
    viewModel: FasoViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val matches by viewModel.matches.collectAsStateWithLifecycle()
    val subscription by viewModel.subscription.collectAsStateWithLifecycle()
    val activationCodes by viewModel.activationCodes.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
    val adminSummary by viewModel.adminSummary.collectAsStateWithLifecycle()
    val dailyPoll by viewModel.dailyPoll.collectAsStateWithLifecycle()
    val referralProfile by viewModel.referralProfile.collectAsStateWithLifecycle()

    // Back button handling
    BackHandler(enabled = true) {
        val handled = viewModel.navigateBack()
        if (!handled) {
            // Already at root of backstack
            (context as? ComponentActivity)?.finish()
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        topBar = {
            if (uiState.currentScreen != FasoScreen.ADMIN) {
                FasoTopAppBar(
                    subscription = subscription,
                    isAdminAuthenticated = uiState.isAdminAuthenticated,
                    onOpenAdmin = { viewModel.navigateTo(FasoScreen.ADMIN) },
                    onAdminPinSubmit = { pin -> viewModel.authenticateAdmin(pin) },
                    onOpenPremium = { viewModel.navigateTo(FasoScreen.PREMIUM) }
                )
            }
        },
        bottomBar = {
            if (uiState.currentScreen != FasoScreen.ADMIN) {
                FasoBottomBar(
                    currentScreen = uiState.currentScreen,
                    onTabSelected = { screen -> viewModel.navigateTo(screen) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.currentScreen) {
                FasoScreen.PRONOS -> {
                    PredictionsScreen(
                        matches = matches,
                        subscription = subscription,
                        selectedSport = uiState.selectedSport,
                        onSportSelected = { viewModel.selectSport(it) },
                        onMatchClick = { viewModel.openMatchDetail(it) },
                        onUnlockClick = { viewModel.navigateTo(FasoScreen.PREMIUM) },
                        onShareClick = { viewModel.openShareDialog(it) },
                        onOpenActivation = { viewModel.openActivationDialog() },
                        poll = dailyPoll,
                        onVotePoll = { viewModel.votePoll(it) }
                    )
                }
                FasoScreen.MATCHES -> {
                    MatchesScreen(
                        matches = matches,
                        onMatchClick = { viewModel.openMatchDetail(it) }
                    )
                }
                FasoScreen.PREMIUM -> {
                    PremiumScreen(
                        plans = viewModel.repository.defaultPlans,
                        subscription = subscription,
                        adminWhatsAppPhone = uiState.adminWhatsAppPhone,
                        onContactWhatsApp = { ctx, plan -> viewModel.contactWhatsAppForSubscription(ctx, plan) },
                        onOpenActivation = { viewModel.openActivationDialog() }
                    )
                }
                FasoScreen.HISTORY -> {
                    HistoryScreen(
                        matches = matches,
                        onMatchClick = { viewModel.openMatchDetail(it) }
                    )
                }
                FasoScreen.PROFILE -> {
                    ProfileScreen(
                        subscription = subscription,
                        referral = referralProfile,
                        adminWhatsAppPhone = uiState.adminWhatsAppPhone,
                        onOpenActivation = { viewModel.openActivationDialog() },
                        onOpenAdmin = {
                            if (uiState.isAdminAuthenticated) {
                                viewModel.navigateTo(FasoScreen.ADMIN)
                            } else {
                                // Pin dialog handled from TopBar or direct navigate
                                viewModel.navigateTo(FasoScreen.ADMIN)
                            }
                        }
                    )
                }
                FasoScreen.ADMIN -> {
                    AdminScreen(
                        summary = adminSummary,
                        plans = viewModel.repository.defaultPlans,
                        codes = activationCodes,
                        matches = matches,
                        auditLogs = auditLogs,
                        currentWhatsAppPhone = uiState.adminWhatsAppPhone,
                        onGenerateCodes = { plan, qty -> viewModel.generateAdminCodes(plan, qty) },
                        onRevokeCode = { code -> viewModel.revokeAdminCode(code) },
                        onUpdateMatchOutcome = { mId, sH, sA, st, out -> viewModel.updateMatchOutcome(mId, sH, sA, st, out) },
                        onPublishPrediction = { h, a, c, ctry, sp, dt, tm, pp, spk, od, vip, ff, rf, ex ->
                            viewModel.publishCustomPrediction(h, a, c, ctry, sp, dt, tm, pp, spk, od, vip, ff, rf, ex)
                        },
                        onUpdateWhatsAppPhone = { viewModel.updateWhatsAppPhone(it) },
                        onLogout = { viewModel.logoutAdmin() }
                    )
                }
            }
        }
    }

    // Match Detail Bottom Sheet
    uiState.selectedMatchForDetail?.let { match ->
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeMatchDetail() },
            sheetState = sheetState,
            containerColor = DarkSurface
        ) {
            PredictionDetailSheet(
                match = match,
                onClose = { viewModel.closeMatchDetail() },
                onShare = {
                    viewModel.closeMatchDetail()
                    viewModel.shareMatchPrediction(context, it)
                }
            )
        }
    }

    // Activation Code Input Dialog
    if (uiState.showActivationDialog) {
        ActivationDialog(
            inputCode = uiState.activationInputCode,
            onCodeChange = { viewModel.updateActivationInput(it) },
            isLoading = uiState.activationLoading,
            errorMessage = uiState.activationErrorMessage,
            successMessage = uiState.activationSuccessMessage,
            lockoutSeconds = uiState.lockoutSeconds,
            onSubmit = { viewModel.submitActivationCode() },
            onDismiss = { viewModel.closeActivationDialog() }
        )
    }

    // Share Match Dialog
    uiState.shareMatchItem?.let { matchToShare ->
        ShareCardDialog(
            match = matchToShare,
            onShareNative = { viewModel.shareMatchPrediction(context, it) },
            onDismiss = { viewModel.closeShareDialog() }
        )
    }
}
