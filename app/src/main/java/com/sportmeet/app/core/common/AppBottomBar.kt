package com.sportmeet.app.core.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sportmeet.app.core.navigation.BottomNavItem
import com.sportmeet.app.core.theme.SportGold
import com.sportmeet.app.core.theme.SportPrimary
import com.sportmeet.app.domain.model.UserRole

@Composable
fun AppBottomBar(
    role: UserRole,
    selectedTabRoute: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = (if (role == UserRole.PLAYER) BottomNavItem.playerTabs else BottomNavItem.organizerTabs).filterNotNull()
    val activeColor = if (role == UserRole.ORGANIZER) SportGold else SportPrimary

    Surface(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
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
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = activeColor,
                        selectedTextColor = activeColor,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = activeColor.copy(alpha = 0.14f)
                    )
                )
            }
        }
    }
}
