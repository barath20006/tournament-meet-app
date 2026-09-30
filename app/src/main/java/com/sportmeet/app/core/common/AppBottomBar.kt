package com.sportmeet.app.core.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sportmeet.app.core.navigation.BottomNavItem
import com.sportmeet.app.core.theme.SportBlue
import com.sportmeet.app.core.theme.SportGold
import com.sportmeet.app.domain.model.UserRole

@Composable
fun AppBottomBar(
    role: UserRole,
    selectedTabRoute: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = (if (role == UserRole.PLAYER) BottomNavItem.playerTabs else BottomNavItem.organizerTabs).filterNotNull()
    val activeColor = if (role == UserRole.ORGANIZER) SportGold else SportBlue

    Column(modifier = modifier) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            items.forEach { item ->
                val isSelected = selectedTabRoute == item.route
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onTabSelected(item.route) },
                    icon = {
                        Text(
                            text = item.iconEmoji,
                            fontSize = if (isSelected) 22.sp else 19.sp
                        )
                    },
                    label = {
                        Text(
                            text = item.title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = activeColor,
                        selectedTextColor = activeColor,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = activeColor.copy(alpha = 0.12f)
                    )
                )
            }
        }
    }
}
