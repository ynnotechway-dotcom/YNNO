package com.example.fasopronostics.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import com.example.R
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fasopronostics.data.model.CommunityPoll
import com.example.fasopronostics.data.model.MatchItem
import com.example.fasopronostics.data.model.SportType
import com.example.fasopronostics.data.model.UserSubscription
import com.example.fasopronostics.ui.components.MatchCard
import com.example.fasopronostics.ui.components.getSportIcon
import com.example.fasopronostics.ui.theme.DarkBackground
import com.example.fasopronostics.ui.theme.DarkSurface
import com.example.fasopronostics.ui.theme.DarkSurfaceBorder
import com.example.fasopronostics.ui.theme.DarkSurfaceCard
import com.example.fasopronostics.ui.theme.FasoGold
import com.example.fasopronostics.ui.theme.FasoGreen
import com.example.fasopronostics.ui.theme.FasoRed

@Composable
fun PredictionsScreen(
    matches: List<MatchItem>,
    subscription: UserSubscription,
    selectedSport: SportType,
    onSportSelected: (SportType) -> Unit,
    onMatchClick: (MatchItem) -> Unit,
    onUnlockClick: () -> Unit,
    onShareClick: (MatchItem) -> Unit,
    onOpenActivation: () -> Unit,
    poll: CommunityPoll,
    onVotePoll: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Tous, 1: VIP Uniquement, 2: Gratuits

    val sports = listOf(
        SportType.ALL,
        SportType.FOOTBALL,
        SportType.BASKETBALL,
        SportType.TENNIS,
        SportType.HANDBALL,
        SportType.VOLLEYBALL
    )

    // Filtered matches
    val filteredMatches = matches.filter { match ->
        val sportMatch = (selectedSport == SportType.ALL || match.sport == selectedSport)
        val tabMatch = when (selectedTab) {
            1 -> match.prediction?.isPremium == true
            2 -> match.prediction?.isPremium == false
            else -> true
        }
        sportMatch && tabMatch
    }

    // Top pick of the day (Hot Match)
    val hotMatch = matches.find { it.id == "bf_match_1" } ?: matches.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // BetWinner Official Promo Card by JuJu Dav
        item {
            val context = LocalContext.current
            val clipboardManager = LocalClipboardManager.current
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("betwinner_promo_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF140D0F)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE50914))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
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
                                    text = "PARTENAIRE OFFICIEL BETWINNER",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        color = FasoGold,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                                Text(
                                    text = "Offre Exclusive JuJu Dav",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1))
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFE50914)
                        ) {
                            Text(
                                text = "BONUS 200%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Profitez de jusqu'à 200% de bonus sur votre 1er dépôt avec mon code promo officiel BetWinner :",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFE2E8F0))
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF261014),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE50914)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "CODE PROMO BETWINNER",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFFFCA5A5),
                                        fontSize = 9.sp
                                    )
                                )
                                Text(
                                    text = "JUJUDAV12",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = FasoGold,
                                        letterSpacing = 2.sp
                                    )
                                )
                            }

                            Button(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString("JUJUDAV12"))
                                    Toast.makeText(context, "Code Promo JUJUDAV12 copié !", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE50914)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copier", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Hot Match of the Day Hero Banner
        if (hotMatch != null && selectedTab != 1) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMatchClick(hotMatch) }
                            .testTag("hot_match_banner"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D281D)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, FasoGold)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = null,
                                        tint = FasoRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "CHOC DU FASO DU JOUR",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Black,
                                            color = FasoGold,
                                            letterSpacing = 1.sp
                                        )
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = FasoGreen
                                ) {
                                    Text(
                                        text = "GRATUIT",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "${hotMatch.homeTeam} vs ${hotMatch.awayTeam}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "${hotMatch.competition} • ${hotMatch.matchTime}",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFA7F3D0))
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            hotMatch.prediction?.let { pred ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF143B2A),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = "Recommandation :",
                                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                                            )
                                            Text(
                                                text = pred.primaryPick,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            )
                                        }
                                        Text(
                                            text = "@${pred.estimatedOdds}",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Black,
                                                color = FasoGold
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Activation Code Quick Bar (If user is not premium)
        if (!subscription.isPremium) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { onOpenActivation() }
                        .testTag("quick_activation_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = FasoGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Vous avez un code d'activation ?",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = "Cliquez ici pour débloquer votre abonnement",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                        Text(
                            text = "ACTIVER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = FasoGold
                            )
                        )
                    }
                }
            }
        }

        // Sport Category Horizontal Chips
        item {
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Spacer(modifier = Modifier.width(8.dp))
                sports.forEach { sport ->
                    val isSelected = selectedSport == sport
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSportSelected(sport) },
                        label = {
                            Text(
                                text = sport.displayName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = getSportIcon(sport),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FasoGreen,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White,
                            containerColor = DarkSurface,
                            labelColor = Color(0xFFCBD5E1),
                            iconColor = Color(0xFF94A3B8)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = DarkSurfaceBorder,
                            selectedBorderColor = FasoGreen
                        )
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
        }

        // Tabs: Tous / VIP / Gratuits
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkBackground,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = FasoGold,
                        height = 3.dp
                    )
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "Tous (${matches.size})",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = FasoGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "VIP (${matches.count { it.prediction?.isPremium == true }})",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = FasoGold
                                )
                            )
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            text = "Gratuits (${matches.count { it.prediction?.isPremium == false }})",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                )
            }
        }

        // Match list
        items(filteredMatches, key = { it.id }) { match ->
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                MatchCard(
                    match = match,
                    isUserPremium = subscription.isPremium,
                    onMatchClick = onMatchClick,
                    onUnlockClick = onUnlockClick,
                    onShareClick = onShareClick
                )
            }
        }

        // Community Daily Poll Challenge
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF234435)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HowToVote,
                                contentDescription = null,
                                tint = FasoGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DÉFI DE LA COMMUNAUTÉ",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    color = FasoGold,
                                    letterSpacing = 1.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = poll.question,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val totalVotes = (poll.votesHome + poll.votesDraw + poll.votesAway).coerceAtLeast(1)
                        val pctHome = (poll.votesHome * 100) / totalVotes
                        val pctDraw = (poll.votesDraw * 100) / totalVotes
                        val pctAway = (poll.votesAway * 100) / totalVotes

                        // 3 Options
                        PollButton(
                            label = "${poll.optionHome} ($pctHome%)",
                            isSelected = poll.userVotedOption == 1,
                            onClick = { onVotePoll(1) }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        PollButton(
                            label = "${poll.optionDraw} ($pctDraw%)",
                            isSelected = poll.userVotedOption == 2,
                            onClick = { onVotePoll(2) }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        PollButton(
                            label = "${poll.optionAway} ($pctAway%)",
                            isSelected = poll.userVotedOption == 3,
                            onClick = { onVotePoll(3) }
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$totalVotes supporters ont voté aujourd'hui au Faso.",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PollButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) FasoGreen.copy(alpha = 0.25f) else Color(0xFF1E293B),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) FasoGreen else Color(0xFF334155)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color(0xFFA7F3D0) else Color.White
                )
            )
            if (isSelected) {
                Text(
                    text = "Voté ✓",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = FasoGreen,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
