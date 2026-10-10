package com.sportmeet.app.core.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sportmeet.app.core.theme.SportBlue
import com.sportmeet.app.core.theme.SportGold
import com.sportmeet.app.core.theme.SportGreen

enum class SearchFilterScope(val displayName: String, val icon: ImageVector, val description: String) {
    ALL("All", Icons.Outlined.Search, "Name & Location"),
    NAME("By Name", Icons.Outlined.Title, "Tournament Name"),
    LOCATION("By Location", Icons.Outlined.LocationOn, "City or Venue")
}

@Composable
fun TournamentSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchScope: SearchFilterScope,
    onSearchScopeChange: (SearchFilterScope) -> Unit,
    onlyActive: Boolean,
    onToggleOnlyActive: (Boolean) -> Unit,
    matchingResultsCount: Int,
    onQuickLocationSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String? = null
) {
    val quickLocations = listOf("Erode", "Salem", "Coimbatore", "Tiruppur", "Namakkal")

    val currentPlaceholder = placeholderText ?: when (searchScope) {
        SearchFilterScope.NAME -> "Filter by tournament title (e.g. Cricket Cup)..."
        SearchFilterScope.LOCATION -> "Filter by city or venue (e.g. Erode, Nehru)..."
        SearchFilterScope.ALL -> "Search tournament by name or location..."
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Main Search Input Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = currentPlaceholder,
                        fontSize = 13.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = when (searchScope) {
                            SearchFilterScope.NAME -> Icons.Outlined.Title
                            SearchFilterScope.LOCATION -> Icons.Outlined.LocationOn
                            SearchFilterScope.ALL -> Icons.Outlined.Search
                        },
                        contentDescription = "Search icon",
                        tint = SportBlue,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Outlined.Clear,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                    focusedBorderColor = SportBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                ),
                singleLine = true
            )

            // Filter Scopes: By Name, By Location, All + Active Status Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Active Tournaments Only Toggle Chip
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (onlyActive) SportGreen.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (onlyActive) SportGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.clickable { onToggleOnlyActive(!onlyActive) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ElectricBolt,
                            contentDescription = null,
                            tint = if (onlyActive) SportGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (onlyActive) "Active Only" else "All Statuses",
                            fontSize = 11.5.sp,
                            fontWeight = if (onlyActive) FontWeight.Bold else FontWeight.Medium,
                            color = if (onlyActive) SportGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Scope: Name or Location Scope Buttons
                SearchFilterScope.values().forEach { scope ->
                    val isSelected = searchScope == scope
                    val chipBg by animateColorAsState(
                        targetValue = if (isSelected) SportBlue.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        label = "scopeBg"
                    )
                    val borderColor by animateColorAsState(
                        targetValue = if (isSelected) SportBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                        label = "scopeBorder"
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = chipBg,
                        border = androidx.compose.foundation.BorderStroke(width = 1.dp, color = borderColor),
                        modifier = Modifier.clickable { onSearchScopeChange(scope) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = scope.icon,
                                contentDescription = null,
                                tint = if (isSelected) SportBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = scope.displayName,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) SportBlue else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Quick City / Location Suggestions Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Locations:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )

                quickLocations.forEach { city ->
                    val isMatching = searchQuery.equals(city, ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isMatching) SportGold.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = if (isMatching) SportGold else Color.Transparent
                        ),
                        modifier = Modifier.clickable {
                            onQuickLocationSelected(city)
                        }
                    ) {
                        Text(
                            text = "📍 $city",
                            fontSize = 11.sp,
                            fontWeight = if (isMatching) FontWeight.Bold else FontWeight.Normal,
                            color = if (isMatching) SportGold else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Live Results Status & Active Filter Indicator
            if (searchQuery.isNotEmpty() || !onlyActive || searchScope != SearchFilterScope.ALL) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (matchingResultsCount > 0) {
                            "⚡ $matchingResultsCount active ${if (matchingResultsCount == 1) "tournament" else "tournaments"} found"
                        } else {
                            "No active tournaments match your search"
                        },
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (matchingResultsCount > 0) SportBlue else MaterialTheme.colorScheme.error
                    )

                    Text(
                        text = "Reset",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = SportBlue,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                onSearchQueryChange("")
                                onSearchScopeChange(SearchFilterScope.ALL)
                                onToggleOnlyActive(true)
                            }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
