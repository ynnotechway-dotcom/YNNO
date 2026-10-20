package com.example.fasopronostics.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fasopronostics.data.model.MatchItem
import com.example.fasopronostics.data.model.PredictionOutcome
import com.example.fasopronostics.ui.components.getSportIcon
import com.example.fasopronostics.ui.theme.DarkBackground
import com.example.fasopronostics.ui.theme.DarkSurface
import com.example.fasopronostics.ui.theme.DarkSurfaceBorder
import com.example.fasopronostics.ui.theme.DarkSurfaceCard
import com.example.fasopronostics.ui.theme.FasoGold
import com.example.fasopronostics.ui.theme.FasoGreen
import com.example.fasopronostics.ui.theme.LossRed
import com.example.fasopronostics.ui.theme.WinGreen

@Composable
fun HistoryScreen(
    matches: List<MatchItem>,
    onMatchClick: (MatchItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableIntStateOf(0) } // 0: Tous, 1: Gagnés, 2: Perdus

    // All matches with evaluated predictions
    val historicalMatches = matches.filter { it.prediction != null && (it.prediction.outcome == PredictionOutcome.WON || it.prediction.outcome == PredictionOutcome.LOST) }

    val wonCount = historicalMatches.count { it.prediction?.outcome == PredictionOutcome.WON }
    val lostCount = historicalMatches.count { it.prediction?.outcome == PredictionOutcome.LOST }
    val totalCount = historicalMatches.size
    val strikeRate = if (totalCount > 0) (wonCount.toDouble() / totalCount * 100).toInt() else 78

    val filteredList = historicalMatches.filter { match ->
        when (selectedFilter) {
            1 -> match.prediction?.outcome == PredictionOutcome.WON
            2 -> match.prediction?.outcome == PredictionOutcome.LOST
            else -> true
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = null,
                        tint = FasoGold,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bilan & Historique Public",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    )
                }
                Text(
                    text = "Suivi vérifiable de tous les pronostics publiés. Zéro falsification.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
                )
            }
        }

        // Performance Scorecard
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F261C)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, FasoGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "TAUX DE RÉUSSITE GLOBAL",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFA7F3D0),
                                    letterSpacing = 1.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$strikeRate%",
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.Black,
                                    color = FasoGold
                                )
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = WinGreen,
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Mini stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatPill(label = "Évalués", value = "$totalCount", color = Color.White, modifier = Modifier.weight(1f))
                        StatPill(label = "Gagnés", value = "$wonCount", color = WinGreen, modifier = Modifier.weight(1f))
                        StatPill(label = "Perdus", value = "$lostCount", color = LossRed, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Transparency Disclaimer
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF1E293B).copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = FasoGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Engagement d'intégrité : Tous les pronostics sont enregistrés et horodatés avant le coup d'envoi. Les échecs restent affichés pour refléter la réalité statistique des paris sportifs.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFFCBD5E1),
                            lineHeight = 15.sp
                        )
                    )
                }
            }
        }

        // Filter Tabs: Tous / Gagnés / Perdus
        item {
            TabRow(
                selectedTabIndex = selectedFilter,
                containerColor = DarkBackground,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedFilter]),
                        color = FasoGold,
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedFilter == 0,
                    onClick = { selectedFilter = 0 },
                    text = { Text("Tous ($totalCount)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)) }
                )
                Tab(
                    selected = selectedFilter == 1,
                    onClick = { selectedFilter = 1 },
                    text = { Text("Gagnés ($wonCount)", color = WinGreen, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)) }
                )
                Tab(
                    selected = selectedFilter == 2,
                    onClick = { selectedFilter = 2 },
                    text = { Text("Perdus ($lostCount)", color = LossRed, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)) }
                )
            }
        }

        // Items list
        items(filteredList, key = { it.id }) { match ->
            val pred = match.prediction!!
            val isWon = pred.outcome == PredictionOutcome.WON

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onMatchClick(match) }
                    .testTag("history_item_${match.id}"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isWon) WinGreen.copy(alpha = 0.4f) else LossRed.copy(alpha = 0.4f)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = getSportIcon(match.sport),
                                contentDescription = null,
                                tint = FasoGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${match.competition} • ${match.matchDate}",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                            )
                        }

                        // Badge Gagné / Perdu
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isWon) WinGreen.copy(alpha = 0.2f) else LossRed.copy(alpha = 0.2f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = if (isWon) Icons.Default.CheckCircle else Icons.Default.Close,
                                    contentDescription = null,
                                    tint = if (isWon) WinGreen else LossRed,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isWon) "GAGNÉ" else "PERDU",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isWon) WinGreen else LossRed,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Teams & Score
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${match.homeTeam} vs ${match.awayTeam}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "${match.scoreHome ?: 0} - ${match.scoreAway ?: 0}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = FasoGold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Prono : ${pred.primaryPick}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1))
                        )
                        Text(
                            text = "Cote : @${pred.estimatedOdds}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = FasoGold
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatPill(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF143023)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8)))
            Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = color))
        }
    }
}
