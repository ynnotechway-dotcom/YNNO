package com.example.fasopronostics.ui.components

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.fasopronostics.ui.theme.DarkSurface
import com.example.fasopronostics.ui.theme.FasoGold
import com.example.fasopronostics.ui.theme.FasoGreen
import com.example.fasopronostics.ui.viewmodel.FasoScreen

sealed class NavTab(
    val screen: FasoScreen,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    data object Pronos : NavTab(
        FasoScreen.PRONOS,
        "Pronos",
        Icons.Filled.SportsSoccer,
        Icons.Outlined.SportsSoccer,
        "nav_pronos"
    )
    data object Matches : NavTab(
        FasoScreen.MATCHES,
        "Matchs",
        Icons.Filled.CalendarMonth,
        Icons.Outlined.CalendarMonth,
        "nav_matches"
    )
    data object Premium : NavTab(
        FasoScreen.PREMIUM,
        "Premium",
        Icons.Filled.WorkspacePremium,
        Icons.Outlined.WorkspacePremium,
        "nav_premium"
    )
    data object History : NavTab(
        FasoScreen.HISTORY,
        "Historique",
        Icons.Filled.Assessment,
        Icons.Outlined.Assessment,
        "nav_history"
    )
    data object Profile : NavTab(
        FasoScreen.PROFILE,
        "Profil",
        Icons.Filled.Person,
        Icons.Outlined.Person,
        "nav_profile"
    )
}

@Composable
fun FasoBottomBar(
    currentScreen: FasoScreen,
    onTabSelected: (FasoScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        NavTab.Pronos,
        NavTab.Matches,
        NavTab.Premium,
        NavTab.History,
        NavTab.Profile
    )

    NavigationBar(
        modifier = modifier.navigationBarsPadding(),
        containerColor = DarkSurface,
        tonalElevation = 8.dp
    ) {
        tabs.forEach { tab ->
            val isSelected = currentScreen == tab.screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab.screen) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.title
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = FasoGold,
                    indicatorColor = FasoGreen,
                    unselectedIconColor = Color(0xFF94A3B8),
                    unselectedTextColor = Color(0xFF94A3B8)
                ),
                modifier = Modifier.testTag(tab.testTag)
            )
        }
    }
}
