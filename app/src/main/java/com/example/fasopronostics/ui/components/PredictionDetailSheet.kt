package com.example.fasopronostics.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fasopronostics.data.model.MatchItem
import com.example.fasopronostics.ui.theme.DarkSurface
import com.example.fasopronostics.ui.theme.DarkSurfaceBorder
import com.example.fasopronostics.ui.theme.FasoGold
import com.example.fasopronostics.ui.theme.FasoGreen
import com.example.fasopronostics.ui.theme.FasoRed
import com.example.fasopronostics.ui.theme.WinGreen

@Composable
fun PredictionDetailSheet(
    match: MatchItem,
    onClose: () -> Unit,
    onShare: (MatchItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val pred = match.prediction
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = DarkSurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(scrollState)
        ) {
            // Drag Handle & Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp, 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF475569))
                )

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("close_detail_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fermer",
                        tint = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Match Title & Competition
            Text(
                text = "${match.competition} • ${match.country}",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = FasoGold
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${match.homeTeam} vs ${match.awayTeam}",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            )
            Text(
                text = "${match.matchDate} à ${match.matchTime}",
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Primary & Secondary Picks Card
            if (pred != null) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F241A)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FasoGreen.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "PRONOSTIC PRINCIPAL",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFA7F3D0),
                                    letterSpacing = 1.sp
                                )
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = FasoGreen
                            ) {
                                Text(
                                    text = "Cote @${pred.estimatedOdds}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = pred.primaryPick,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )

                        if (pred.secondaryPick.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Option alternative : ${pred.secondaryPick}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = FasoGold,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Confidence meter
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Indice de Confiance : ${pred.confidencePercent}% (${pred.confidenceLevel})",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { pred.confidencePercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (pred.confidencePercent >= 75) WinGreen else FasoGold,
                            trackColor = Color(0xFF1E293B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Probabilities 1X2 Breakdown
                Text(
                    text = "Probabilités Estimées (Bivariate Poisson & Elo)",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProbBox(label = "1 (${match.homeTeam})", prob = "${pred.probHomeWin}%", modifier = Modifier.weight(1f))
                    ProbBox(label = "Nul (X)", prob = "${pred.probDraw}%", modifier = Modifier.weight(1f))
                    ProbBox(label = "2 (${match.awayTeam})", prob = "${pred.probAwayWin}%", modifier = Modifier.weight(1f))
                }

                if (pred.expectedGoalsHome > 0.0 || pred.expectedGoalsAway > 0.0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Objectif buts attendus (xG) : ${match.homeTeam} ${pred.expectedGoalsHome} - ${pred.expectedGoalsAway} ${match.awayTeam}",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFA7F3D0))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Favorable Factors
                if (pred.favorableFactors.isNotEmpty()) {
                    Text(
                        text = "Facteurs Favorables",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = WinGreen
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    pred.favorableFactors.forEach { factor ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = WinGreen,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = factor,
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFE2E8F0))
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Risk Factors
                if (pred.riskFactors.isNotEmpty()) {
                    Text(
                        text = "Facteurs de Risque & Aléas",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = FasoRed
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    pred.riskFactors.forEach { risk ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = FasoRed,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = risk,
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFE2E8F0))
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Detailed Statistical Explanation
                Text(
                    text = "Synthèse d'Analyse du Modèle",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF13221B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = pred.statisticalExplanation,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFCBD5E1),
                            lineHeight = 20.sp
                        ),
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Source and Engine Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Moteur : ${pred.modelSource}",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B))
                    )
                    Text(
                        text = "Mis à jour : ${pred.lastUpdated}",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Legal Disclaimer
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E293B).copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Avertissement",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Avertissement Légal : Les prédictions sont des estimations statistiques objectives calculées sur les données officielles. Aucune méthode au monde ne peut garantir un résultat sportif à 100%. Jouez de manière responsable et mesurée.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Share Button
                Button(
                    onClick = { onShare(match) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("share_match_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = FasoGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Partager cette analyse (WhatsApp, SMS)",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ProbBox(label: String, prob: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF162A21),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF234737))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8)),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = prob,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = FasoGold
                )
            )
        }
    }
}
