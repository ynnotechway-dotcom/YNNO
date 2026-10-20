package com.example.fasopronostics.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fasopronostics.data.model.ActivationCode
import com.example.fasopronostics.data.model.AdminStatSummary
import com.example.fasopronostics.data.model.AuditLogItem
import com.example.fasopronostics.data.model.MatchItem
import com.example.fasopronostics.data.model.MatchStatus
import com.example.fasopronostics.data.model.PredictionOutcome
import com.example.fasopronostics.data.model.SportType
import com.example.fasopronostics.data.model.SubscriptionPlan
import com.example.fasopronostics.ui.theme.DarkBackground
import com.example.fasopronostics.ui.theme.DarkSurface
import com.example.fasopronostics.ui.theme.DarkSurfaceBorder
import com.example.fasopronostics.ui.theme.DarkSurfaceCard
import com.example.fasopronostics.ui.theme.FasoGold
import com.example.fasopronostics.ui.theme.FasoGreen
import com.example.fasopronostics.ui.theme.FasoRed
import com.example.fasopronostics.ui.theme.LossRed
import com.example.fasopronostics.ui.theme.WinGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminScreen(
    summary: AdminStatSummary,
    plans: List<SubscriptionPlan>,
    codes: List<ActivationCode>,
    matches: List<MatchItem>,
    auditLogs: List<AuditLogItem>,
    currentWhatsAppPhone: String,
    onGenerateCodes: (SubscriptionPlan, Int) -> Unit,
    onRevokeCode: (String) -> Unit,
    onUpdateMatchOutcome: (String, Int?, Int?, MatchStatus, PredictionOutcome) -> Unit,
    onPublishPrediction: (String, String, String, String, SportType, String, String, String, String, Double, Boolean, List<String>, List<String>, String) -> Unit,
    onUpdateWhatsAppPhone: (String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Codes, 1: Publier, 2: Résultats, 3: WhatsApp, 4: Logs

    val adminTabs = listOf("Générer Codes", "Publier Prono", "Valider Résultats", "Config WhatsApp", "Journal d'Audit")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_juju_logo),
                            contentDescription = "JuJu Dav",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "JUJU MULTI PRONOS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Administration Sécurisée • PIN : 2912",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFA7F3D0))
                        )
                    }
                }

                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Quitter", color = Color(0xFF94A3B8), fontSize = 11.sp)
                }
            }
        }

        // Summary KPI Stats
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F261B)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, FasoGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "VUE D'ENSEMBLE DE LA PLATEFORME",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = FasoGold,
                            letterSpacing = 1.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AdminKpiBox("Abonnés Actifs", "${summary.activeSubscribersCount}", FasoGold, Modifier.weight(1f))
                        AdminKpiBox("Revenus FCFA", "${summary.totalRevenueFCFA / 1000}k", Color.White, Modifier.weight(1f))
                        AdminKpiBox("Réussite Publique", "${summary.globalStrikeRate}%", WinGreen, Modifier.weight(1f))
                    }
                }
            }
        }

        // Admin Tabs
        item {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkBackground,
                contentColor = Color.White,
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = FasoGold,
                        height = 3.dp
                    )
                }
            ) {
                adminTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) FasoGold else Color(0xFF94A3B8)
                                )
                            )
                        }
                    )
                }
            }
        }

        // Tab Content
        when (selectedTab) {
            0 -> {
                // TAB 0: Code Generator & Code Manager
                item {
                    AdminCodeGeneratorSection(
                        plans = plans,
                        codes = codes,
                        onGenerateCodes = onGenerateCodes,
                        onRevokeCode = onRevokeCode
                    )
                }
            }
            1 -> {
                // TAB 1: Publish New Custom Prediction
                item {
                    AdminPublishPredictionSection(onPublishPrediction = onPublishPrediction)
                }
            }
            2 -> {
                // TAB 2: Outcome Validator (Won, Lost, Void)
                item {
                    AdminOutcomeValidatorSection(
                        matches = matches,
                        onUpdateMatchOutcome = onUpdateMatchOutcome
                    )
                }
            }
            3 -> {
                // TAB 3: WhatsApp Phone Config
                item {
                    AdminWhatsAppConfigSection(
                        currentPhone = currentWhatsAppPhone,
                        onSavePhone = onUpdateWhatsAppPhone
                    )
                }
            }
            4 -> {
                // TAB 4: Audit Logs
                items(auditLogs, key = { it.id }) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = log.action,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = FasoGold
                                    )
                                )
                                Text(
                                    text = formatAuditTime(log.timestamp),
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B))
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = log.details,
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminKpiBox(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF143023)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp))
            Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = color))
        }
    }
}

// -------------------------------------------------------------
// SUB-SECTIONS FOR ADMIN TABS
// -------------------------------------------------------------

@Composable
fun AdminCodeGeneratorSection(
    plans: List<SubscriptionPlan>,
    codes: List<ActivationCode>,
    onGenerateCodes: (SubscriptionPlan, Int) -> Unit,
    onRevokeCode: (String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var selectedPlanForCode by remember { mutableStateOf(plans.first()) }
    var quantity by remember { mutableIntStateOf(1) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Generator Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Générateur de Codes d'Activation",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Générez des codes uniques cryptographiques à usage unique avec durée associée.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Formula selector chips
                Text(
                    text = "Formule cible :",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFA7F3D0))
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    plans.forEach { plan ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedPlanForCode.id == plan.id) FasoGreen.copy(alpha = 0.3f) else Color(0xFF1E293B),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (selectedPlanForCode.id == plan.id) FasoGreen else Color(0xFF334155)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPlanForCode = plan }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${plan.name} (${plan.durationDays}j)",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = "${plan.priceFCFA} FCFA",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = FasoGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quantity selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Quantité à générer :", style = MaterialTheme.typography.bodySmall.copy(color = Color.White))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { quantity = 1 },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (quantity == 1) FasoGold else Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("1 Code", color = if (quantity == 1) Color.Black else Color.White)
                        }
                        Button(
                            onClick = { quantity = 5 },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (quantity == 5) FasoGold else Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Lot de 5", color = if (quantity == 5) Color.Black else Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        onGenerateCodes(selectedPlanForCode, quantity)
                        Toast.makeText(context, "$quantity code(s) généré(s) avec succès !", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FasoGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_generate_codes_button")
                ) {
                    Icon(imageVector = Icons.Default.Key, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Générer les codes maintenant", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Codes List
        Text(
            text = "Codes d'Activation Existants (${codes.size})",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )

        codes.forEach { code ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = DarkSurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = code.code,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = if (code.isUsed) Color(0xFF64748B) else if (code.isRevoked) LossRed else FasoGold,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "${code.planName} • ${code.durationDays}j",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                        )
                        val statusText = when {
                            code.isRevoked -> "Révoqué"
                            code.isUsed -> "Utilisé (par ${code.usedBy ?: "N/A"})"
                            else -> "Disponible ✓"
                        }
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (code.isUsed) Color(0xFF64748B) else if (code.isRevoked) LossRed else WinGreen,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Row {
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(code.code))
                                Toast.makeText(context, "Code ${code.code} copié !", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copier",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        if (!code.isUsed && !code.isRevoked) {
                            IconButton(onClick = { onRevokeCode(code.code) }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Révoquer",
                                    tint = FasoRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminPublishPredictionSection(
    onPublishPrediction: (String, String, String, String, SportType, String, String, String, String, Double, Boolean, List<String>, List<String>, String) -> Unit
) {
    val context = LocalContext.current
    var homeTeam by remember { mutableStateOf("") }
    var awayTeam by remember { mutableStateOf("") }
    var competition by remember { mutableStateOf("Ligue 1 Burkinabè") }
    var country by remember { mutableStateOf("Burkina Faso") }
    var matchTime by remember { mutableStateOf("16:30") }
    var primaryPick by remember { mutableStateOf("") }
    var secondaryPick by remember { mutableStateOf("") }
    var oddsStr by remember { mutableStateOf("1.80") }
    var isVipOnly by remember { mutableStateOf(true) }
    var factorInput by remember { mutableStateOf("Régularité offensive démontrée lors des 5 dernières journées") }
    var explanationInput by remember { mutableStateOf("Modèle statistique Poisson indiquant 64% de chances de succès.") }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Publier un Nouveau Pronostic",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = homeTeam,
                    onValueChange = { homeTeam = it },
                    label = { Text("Équipe Domicile") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = awayTeam,
                    onValueChange = { awayTeam = it },
                    label = { Text("Équipe Extérieur") },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = competition,
                    onValueChange = { competition = it },
                    label = { Text("Compétition") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = matchTime,
                    onValueChange = { matchTime = it },
                    label = { Text("Heure") },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = primaryPick,
                onValueChange = { primaryPick = it },
                label = { Text("Pronostic Principal (ex: Victoire Domicile & -3.5 buts)") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = secondaryPick,
                    onValueChange = { secondaryPick = it },
                    label = { Text("Pronostic Alternatif") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = oddsStr,
                    onValueChange = { oddsStr = it },
                    label = { Text("Cote Estimée") },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = factorInput,
                onValueChange = { factorInput = it },
                label = { Text("Facteur clé d'analyse") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = explanationInput,
                onValueChange = { explanationInput = it },
                label = { Text("Explication statistique") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Réservé aux Membres VIP :", style = MaterialTheme.typography.bodySmall.copy(color = Color.White))
                Switch(
                    checked = isVipOnly,
                    onCheckedChange = { isVipOnly = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = FasoGold)
                )
            }

            Button(
                onClick = {
                    if (homeTeam.isNotBlank() && awayTeam.isNotBlank() && primaryPick.isNotBlank()) {
                        val odds = oddsStr.toDoubleOrNull() ?: 1.70
                        onPublishPrediction(
                            homeTeam, awayTeam, competition, country, SportType.FOOTBALL,
                            "Aujourd'hui", matchTime, primaryPick, secondaryPick, odds, isVipOnly,
                            listOf(factorInput), listOf("Condition météo et turnover possible"), explanationInput
                        )
                        Toast.makeText(context, "Pronostic publié avec succès !", Toast.LENGTH_SHORT).show()
                        homeTeam = ""
                        awayTeam = ""
                        primaryPick = ""
                    } else {
                        Toast.makeText(context, "Veuillez remplir les équipes et le pronostic.", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FasoGreen),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Enregistrer et Diffuser", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AdminOutcomeValidatorSection(
    matches: List<MatchItem>,
    onUpdateMatchOutcome: (String, Int?, Int?, MatchStatus, PredictionOutcome) -> Unit
) {
    val context = LocalContext.current
    val pendingMatches = matches.filter { it.prediction?.outcome == PredictionOutcome.PENDING }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Validation des Résultats Officiels",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
        Text(
            text = "Sélectionnez une rencontre terminée pour saisir le score final et mettre à jour le bilan public instantanément.",
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
        )

        if (pendingMatches.isEmpty()) {
            Text(text = "Toutes les rencontres sont déjà validées.", style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFA7F3D0)))
        }

        pendingMatches.forEach { match ->
            var homeScoreInput by remember { mutableStateOf("1") }
            var awayScoreInput by remember { mutableStateOf("0") }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "${match.homeTeam} vs ${match.awayTeam}",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Prono : ${match.prediction?.primaryPick ?: "N/A"}",
                        style = MaterialTheme.typography.bodySmall.copy(color = FasoGold)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = homeScoreInput,
                            onValueChange = { homeScoreInput = it },
                            label = { Text("Score Dom") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = awayScoreInput,
                            onValueChange = { awayScoreInput = it },
                            label = { Text("Score Ext") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val sH = homeScoreInput.toIntOrNull() ?: 1
                                val sA = awayScoreInput.toIntOrNull() ?: 0
                                onUpdateMatchOutcome(match.id, sH, sA, MatchStatus.FINISHED, PredictionOutcome.WON)
                                Toast.makeText(context, "Validé comme GAGNÉ", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WinGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("✓ GAGNÉ", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val sH = homeScoreInput.toIntOrNull() ?: 0
                                val sA = awayScoreInput.toIntOrNull() ?: 1
                                onUpdateMatchOutcome(match.id, sH, sA, MatchStatus.FINISHED, PredictionOutcome.LOST)
                                Toast.makeText(context, "Validé comme PERDU", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LossRed),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("✗ PERDU", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminWhatsAppConfigSection(
    currentPhone: String,
    onSavePhone: (String) -> Unit
) {
    val context = LocalContext.current
    var phoneInput by remember { mutableStateOf(currentPhone) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Configuration du Support WhatsApp",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ce numéro est utilisé pour recevoir automatiquement les demandes d'abonnement générées par l'application.",
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = phoneInput,
                onValueChange = { phoneInput = it },
                label = { Text("Numéro WhatsApp officiel (ex: +226 70 12 34 56)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    onSavePhone(phoneInput.trim())
                    Toast.makeText(context, "Numéro WhatsApp mis à jour : $phoneInput", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = FasoGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Enregistrer le Numéro", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

fun formatAuditTime(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.FRENCH)
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        ""
    }
}
