package com.sportmeet.app.presentation.player

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.platform.testTag
import java.util.Locale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sportmeet.app.core.common.AppBottomBar
import com.sportmeet.app.core.common.FullScreenImageViewerDialog
import com.sportmeet.app.core.common.SearchFilterScope
import com.sportmeet.app.core.common.SportChip
import com.sportmeet.app.core.common.SportMeetTopBar
import com.sportmeet.app.core.common.StatusBadge
import com.sportmeet.app.core.common.TournamentCard
import com.sportmeet.app.core.common.TournamentSearchBar
import com.sportmeet.app.core.navigation.BottomNavItem
import com.sportmeet.app.core.theme.SportBlue
import com.sportmeet.app.core.theme.SportGold
import com.sportmeet.app.core.theme.SportGreen
import com.sportmeet.app.core.theme.SportRed
import com.sportmeet.app.data.repository.RegistrationRepository
import com.sportmeet.app.data.repository.TournamentRepository
import com.sportmeet.app.domain.model.Sport
import com.sportmeet.app.domain.model.Tournament
import com.sportmeet.app.domain.model.TournamentStatus
import com.sportmeet.app.domain.model.UserRole
import com.sportmeet.app.presentation.profile.ProfileScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerHomeScreen(
    onSwitchToOrganizer: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val tournamentRepo = remember { TournamentRepository(context) }
    val registrationRepo = remember { RegistrationRepository(context) }

    var currentTab by remember { mutableStateOf(BottomNavItem.PlayerHomeTab.route) }
    var searchQuery by remember { mutableStateOf("") }
    var searchScope by remember { mutableStateOf(SearchFilterScope.ALL) }
    var onlyActiveTournaments by remember { mutableStateOf(true) }
    var selectedSportId by remember { mutableStateOf<String?>(null) }
    var selectedLocation by remember { mutableStateOf<String?>(null) }
    var searchDateQuery by remember { mutableStateOf("") }

    // Tournaments state list
    val tournaments = remember { mutableStateListOf<Tournament>() }
    var selectedTournamentForDetail by remember { mutableStateOf<Tournament?>(null) }
    var fullScreenPosterTournament by remember { mutableStateOf<Tournament?>(null) }
    var showRegisterDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var reportReason by remember { mutableStateOf("Fake tournament") }
    var reportDescription by remember { mutableStateOf("") }

    // Quick cities
    val popularLocations = listOf("All", "Erode", "Salem", "Coimbatore", "Tiruppur", "Namakkal")

    fun reloadTournaments() {
        coroutineScope.launch {
            val result = tournamentRepo.searchTournaments(
                sport = selectedSportId,
                location = selectedLocation,
                date = searchDateQuery,
                query = searchQuery,
                onlyActive = onlyActiveTournaments
            )
            if (result.isSuccess) {
                tournaments.clear()
                tournaments.addAll(result.getOrDefault(emptyList()))
            }
        }
    }

    LaunchedEffect(selectedSportId, selectedLocation) {
        reloadTournaments()
    }

    // Filter logic for quick text search by Name or Location and Active status
    val displayedTournaments = tournaments.filter { tournament ->
        val isActive = tournament.status == TournamentStatus.APPROVED ||
                tournament.status == TournamentStatus.REGISTRATION_OPEN

        if (onlyActiveTournaments && !isActive) {
            return@filter false
        }

        val q = searchQuery.trim()
        val matchesText = if (q.isBlank()) {
            true
        } else {
            when (searchScope) {
                SearchFilterScope.NAME -> {
                    tournament.name.contains(q, ignoreCase = true)
                }
                SearchFilterScope.LOCATION -> {
                    tournament.location.contains(q, ignoreCase = true) ||
                    tournament.venueName.contains(q, ignoreCase = true)
                }
                SearchFilterScope.ALL -> {
                    tournament.name.contains(q, ignoreCase = true) ||
                    tournament.location.contains(q, ignoreCase = true) ||
                    tournament.venueName.contains(q, ignoreCase = true) ||
                    tournament.sport.name.contains(q, ignoreCase = true)
                }
            }
        }

        val matchesSport = selectedSportId == null || tournament.sport.id.equals(selectedSportId, ignoreCase = true)
        val matchesLocation = selectedLocation == null || selectedLocation == "All" || tournament.location.equals(selectedLocation, ignoreCase = true)

        matchesText && matchesSport && matchesLocation
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            if (currentTab == BottomNavItem.PlayerHomeTab.route || currentTab == BottomNavItem.PlayerSearchTab.route) {
                SportMeetTopBar(
                    currentRole = UserRole.PLAYER,
                    onSwitchRole = onSwitchToOrganizer
                )
            }
        },
        bottomBar = {
            AppBottomBar(
                role = UserRole.PLAYER,
                selectedTabRoute = currentTab,
                onTabSelected = { tab ->
                    currentTab = tab
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                BottomNavItem.PlayerSavedTab.route -> {
                    SavedTournamentsScreen(
                        onTournamentClick = { selectedTournamentForDetail = it }
                    )
                }

                BottomNavItem.PlayerRegistrationsTab.route -> {
                    MyRegistrationsScreen()
                }

                BottomNavItem.PlayerProfileTab.route -> {
                    ProfileScreen(
                        currentRole = UserRole.PLAYER,
                        onRoleSwitched = { newRole ->
                            if (newRole == UserRole.ORGANIZER) onSwitchToOrganizer()
                        },
                        onLogout = onLogout
                    )
                }

                BottomNavItem.PlayerSearchTab.route -> {
                    // Full dedicated search experience
                    PlayerDedicatedSearchView(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        searchScope = searchScope,
                        onSearchScopeChange = { searchScope = it },
                        onlyActive = onlyActiveTournaments,
                        onToggleOnlyActive = { onlyActiveTournaments = it },
                        selectedSportId = selectedSportId,
                        onSportSelected = {
                            selectedSportId = it
                            reloadTournaments()
                        },
                        selectedLocation = selectedLocation,
                        onLocationSelected = {
                            selectedLocation = it
                            reloadTournaments()
                        },
                        searchDate = searchDateQuery,
                        onSearchDateChange = {
                            searchDateQuery = it
                            reloadTournaments()
                        },
                        tournaments = displayedTournaments,
                        onTournamentClick = { selectedTournamentForDetail = it },
                        onSaveToggle = { tour ->
                            coroutineScope.launch {
                                tournamentRepo.toggleFavorite(tour.id, tour.isSaved)
                                reloadTournaments()
                            }
                        },
                        onResetFilters = {
                            searchQuery = ""
                            searchScope = SearchFilterScope.ALL
                            onlyActiveTournaments = true
                            selectedSportId = null
                            selectedLocation = null
                            searchDateQuery = ""
                            reloadTournaments()
                        }
                    )
                }

                else -> {
                    // Main Feed
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        // Hero Title & Search Header
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Find Your Next Match",
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 22.sp,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Grassroots tournaments with exact ground navigation",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 12.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Tournament Search Bar with Name/Location filter & Active status
                                TournamentSearchBar(
                                    searchQuery = searchQuery,
                                    onSearchQueryChange = { searchQuery = it },
                                    searchScope = searchScope,
                                    onSearchScopeChange = { searchScope = it },
                                    onlyActive = onlyActiveTournaments,
                                    onToggleOnlyActive = { onlyActiveTournaments = it },
                                    matchingResultsCount = displayedTournaments.size,
                                    onQuickLocationSelected = { loc ->
                                        searchScope = SearchFilterScope.LOCATION
                                        searchQuery = if (searchQuery.equals(loc, ignoreCase = true)) "" else loc
                                    }
                                )
                            }
                        }

                        // Popular Sports Selector Section
                        item {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Popular Sports",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    if (selectedSportId != null) {
                                        TextButton(onClick = { selectedSportId = null }) {
                                            Text(
                                                text = "Clear filter",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SportBlue
                                            )
                                        }
                                    }
                                }

                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    item {
                                        SportChip(
                                            sport = Sport("all", "All Sports", "🏅", true),
                                            isSelected = selectedSportId == null,
                                            onSelected = { selectedSportId = null }
                                        )
                                    }
                                    items(Sport.ALL_SPORTS) { sport ->
                                        SportChip(
                                            sport = sport,
                                            isSelected = selectedSportId == sport.id,
                                            onSelected = {
                                                selectedSportId = if (selectedSportId == sport.id) null else sport.id
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Popular Locations Filter Row
                        item {
                            Spacer(modifier = Modifier.height(14.dp))
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Filter by Location",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                        .padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    popularLocations.forEach { loc ->
                                        val isSelected = (selectedLocation == null && loc == "All") || selectedLocation == loc
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(18.dp))
                                                .background(
                                                    if (isSelected) SportBlue.copy(alpha = 0.14f)
                                                    else MaterialTheme.colorScheme.surface
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isSelected) SportBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                                    shape = RoundedCornerShape(18.dp)
                                                )
                                                .clickable {
                                                    selectedLocation = if (loc == "All") null else loc
                                                }
                                                .padding(horizontal = 13.dp, vertical = 7.dp)
                                        ) {
                                            Text(
                                                text = if (loc == "All") "📍 All Cities" else "📍 $loc",
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                                color = if (isSelected) SportBlue else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Upcoming Tournaments Header & Count
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Upcoming Tournaments",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "${displayedTournaments.size} Available",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Tournaments List
                        if (displayedTournaments.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = "🔍", fontSize = 42.sp)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = if (searchQuery.isNotBlank()) "No Tournaments Matching \"$searchQuery\"" else "No Active Tournaments Found",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = when {
                                                searchScope == SearchFilterScope.NAME -> "No active sports tournaments matched title \"$searchQuery\". Try checking the spelling or switching to 'All'."
                                                searchScope == SearchFilterScope.LOCATION -> "No active sports tournaments found in \"$searchQuery\". Try choosing Erode, Salem, or Coimbatore."
                                                searchQuery.isNotBlank() -> "No active tournaments match this name or location. Try clearing your search keyword."
                                                else -> "Try clearing your filters to see more tournaments."
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        OutlinedButton(
                                            onClick = {
                                                searchQuery = ""
                                                searchScope = SearchFilterScope.ALL
                                                onlyActiveTournaments = true
                                                selectedSportId = null
                                                selectedLocation = null
                                                searchDateQuery = ""
                                                reloadTournaments()
                                            }
                                        ) {
                                            Text("Clear Search & View Active Tournaments")
                                        }
                                    }
                                }
                            }
                        } else {
                            items(displayedTournaments, key = { it.id }) { tournament ->
                                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                    TournamentCard(
                                        tournament = tournament,
                                        onCardClick = { selectedTournamentForDetail = tournament },
                                        onSaveToggle = {
                                            coroutineScope.launch {
                                                tournamentRepo.toggleFavorite(tournament.id, tournament.isSaved)
                                                reloadTournaments()
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal BottomSheet for Tournament Details & Quick Actions
    selectedTournamentForDetail?.let { tournament ->
        ModalBottomSheet(
            onDismissRequest = { selectedTournamentForDetail = null },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (!tournament.posterUrl.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                fullScreenPosterTournament = tournament
                            }
                    ) {
                        com.sportmeet.app.core.common.TournamentPosterImage(
                            posterUrl = tournament.posterUrl,
                            sportEmoji = tournament.sport.iconEmoji,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Bottom gradient shadow for readable text badge
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(65.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                                    )
                                )
                        )

                        // Tap badge to view full poster
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(10.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.Black.copy(alpha = 0.8f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Fullscreen,
                                contentDescription = "View full poster",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "View Full Poster (9:16)",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Header: Emoji & Name
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = tournament.sport.iconEmoji, fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = tournament.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${tournament.sport.name} • ${tournament.tournamentType}",
                                fontSize = 12.sp,
                                color = SportBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    StatusBadge(status = tournament.status)
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(14.dp))

                // Schedule
                Text(
                    text = "SCHEDULE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📅 ${tournament.date}  •  ⏰ ${tournament.startTime}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 📍 VENUE SECTION
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("player_venue_location_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "📍", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Venue",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            if (tournament.latitude != null && tournament.longitude != null) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SportGreen.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "🎯 Exact Ground Pinned",
                                        color = SportGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // [Venue Name]
                        Text(
                            text = tournament.venueName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // [Ground Name]
                        if (tournament.groundName.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Place,
                                    contentDescription = null,
                                    tint = SportGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = tournament.groundName,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = SportGreen
                                )
                            }
                        }

                        // [Address]
                        val addressText = if (tournament.address.isNotBlank()) {
                            if (tournament.address.contains(tournament.location, ignoreCase = true)) {
                                tournament.address
                            } else {
                                "${tournament.address}, ${tournament.location}"
                            }
                        } else {
                            tournament.location
                        }
                        Text(
                            text = addressText,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Coordinates if available
                        if (tournament.latitude != null && tournament.longitude != null) {
                            Text(
                                text = "Coordinates: ${"%.6f".format(Locale.US, tournament.latitude)}, ${"%.6f".format(Locale.US, tournament.longitude)}",
                                fontSize = 11.sp,
                                color = SportBlue,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // [ 🗺️ VIEW LOCATION ] Button
                        Button(
                            onClick = {
                                if (tournament.latitude != null && tournament.longitude != null) {
                                    val lat = tournament.latitude
                                    val lng = tournament.longitude
                                    val groundLabel = (tournament.groundName.ifBlank { tournament.venueName }).ifBlank { "Tournament Ground" }
                                    val geoUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(groundLabel)})")
                                    val intent = Intent(Intent.ACTION_VIEW, geoUri).apply {
                                        setPackage("com.google.android.apps.maps")
                                    }
                                    try {
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        val fallbackUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
                                        try {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, fallbackUri))
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Could not open map: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    val query = listOf(tournament.venueName, tournament.address, tournament.location)
                                        .filter { it.isNotBlank() }
                                        .joinToString(", ")
                                    val fallbackUri = Uri.parse("geo:0,0?q=${Uri.encode(query)}")
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, fallbackUri))
                                    } catch (_: Exception) {
                                        val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(query)}")
                                        try { context.startActivity(Intent(Intent.ACTION_VIEW, webUri)) } catch (_: Exception) {}
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("view_location_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SportBlue)
                        ) {
                            Text(
                                text = "🗺️ VIEW LOCATION",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Prize & Entry
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = SportBlue.copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "ENTRY FEE", fontSize = 10.sp, color = SportBlue, fontWeight = FontWeight.Bold)
                            Text(text = tournament.formattedEntryFee, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = SportGold.copy(alpha = 0.12f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "PRIZE POOL", fontSize = 10.sp, color = SportGold, fontWeight = FontWeight.Bold)
                            Text(text = tournament.formattedPrizeMoney, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SportGold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Description
                Text(
                    text = "DESCRIPTION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tournament.description,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Organizer contact info
                Text(
                    text = "ORGANIZER CONTACT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Organizer: ${tournament.organizerName} (${tournament.contactNumber})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Contact Action Buttons: Call, WhatsApp, Map
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${tournament.contactNumber}"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Call", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val cleanNumber = tournament.whatsappNumber.replace(Regex("[^0-9]"), "")
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$cleanNumber&text=Hi, I am interested in ${tournament.name}"))
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SportGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("💬 WhatsApp", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            if (tournament.latitude != null && tournament.longitude != null) {
                                val lat = tournament.latitude
                                val lng = tournament.longitude
                                val groundLabel = (tournament.groundName.ifBlank { tournament.venueName }).ifBlank { "Tournament Ground" }
                                val geoUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(groundLabel)})")
                                val intent = Intent(Intent.ACTION_VIEW, geoUri).apply {
                                    setPackage("com.google.android.apps.maps")
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    val fallbackUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, fallbackUri))
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Could not open map: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                val query = listOf(tournament.venueName, tournament.address, tournament.location)
                                    .filter { it.isNotBlank() }
                                    .joinToString(", ")
                                val geoUri = Uri.parse("geo:0,0?q=${Uri.encode(query)}")
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, geoUri))
                                } catch (_: Exception) {}
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Map", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Primary Register Button
                Button(
                    onClick = {
                        showRegisterDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SportBlue)
                ) {
                    Text(
                        text = "📝 Register Team (${tournament.registeredTeams}/${tournament.maxTeams} Teams)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Favorite and Report secondary actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                tournamentRepo.toggleFavorite(tournament.id, tournament.isSaved)
                                reloadTournaments()
                                Toast.makeText(
                                    context,
                                    if (!tournament.isSaved) "Added to Favorites" else "Removed from Favorites",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (tournament.isSaved) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = null,
                            tint = if (tournament.isSaved) SportRed else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (tournament.isSaved) "Saved" else "Save Tournament")
                    }

                    TextButton(
                        onClick = { showReportDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Flag,
                            contentDescription = null,
                            tint = SportRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Report", color = SportRed)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Full Screen Poster Image Viewer
    fullScreenPosterTournament?.let { tournament ->
        FullScreenImageViewerDialog(
            posterUrl = tournament.posterUrl,
            title = tournament.name,
            sportEmoji = tournament.sport.iconEmoji,
            subtitle = "${tournament.location} • ${tournament.venueName} • 9:16 Full Poster",
            onDismiss = { fullScreenPosterTournament = null }
        )
    }

    // Register Team Dialog
    if (showRegisterDialog) {
        var teamName by remember { mutableStateOf("") }
        var captainName by remember { mutableStateOf("") }
        var captainPhone by remember { mutableStateOf("") }
        var playersCount by remember { mutableStateOf("11") }

        AlertDialog(
            onDismissRequest = { showRegisterDialog = false },
            title = {
                Text(
                    text = "Register for ${selectedTournamentForDetail?.name}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = teamName,
                        onValueChange = { teamName = it },
                        label = { Text("Team Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = captainName,
                        onValueChange = { captainName = it },
                        label = { Text("Captain Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = captainPhone,
                        onValueChange = { captainPhone = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = playersCount,
                        onValueChange = { playersCount = it },
                        label = { Text("Number of Players") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (teamName.isNotBlank() && captainName.isNotBlank() && captainPhone.isNotBlank()) {
                            coroutineScope.launch {
                                selectedTournamentForDetail?.let { tour ->
                                    registrationRepo.registerTeam(
                                        tournamentId = tour.id,
                                        tournamentName = tour.name,
                                        teamName = teamName,
                                        captainName = captainName,
                                        phone = captainPhone,
                                        email = "",
                                        numberOfPlayers = playersCount.toIntOrNull() ?: 11,
                                        notes = "Registered via Sports Arena Android"
                                    )
                                    Toast.makeText(context, "Registration submitted for $teamName!", Toast.LENGTH_LONG).show()
                                    showRegisterDialog = false
                                    selectedTournamentForDetail = null
                                }
                            }
                        } else {
                            Toast.makeText(context, "Please fill in team details", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SportBlue)
                ) {
                    Text("Submit Registration")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRegisterDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Report Tournament Dialog
    if (showReportDialog) {
        val reasons = listOf(
            "Fake tournament",
            "Incorrect information",
            "Wrong contact number",
            "Suspicious/fraudulent",
            "Tournament cancelled",
            "Other"
        )
        var dropdownOpen by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report Tournament", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Help keep Sports Arena safe. Admin reviews all reports.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    ExposedDropdownMenuBox(
                        expanded = dropdownOpen,
                        onExpandedChange = { dropdownOpen = !dropdownOpen }
                    ) {
                        OutlinedTextField(
                            value = reportReason,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Reason") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownOpen) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = dropdownOpen,
                            onDismissRequest = { dropdownOpen = false }
                        ) {
                            reasons.forEach { r ->
                                DropdownMenuItem(
                                    text = { Text(r) },
                                    onClick = {
                                        reportReason = r
                                        dropdownOpen = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = reportDescription,
                        onValueChange = { reportDescription = it },
                        label = { Text("Details") },
                        placeholder = { Text("Explain why this event is inappropriate or incorrect") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            selectedTournamentForDetail?.let { tour ->
                                tournamentRepo.reportTournament(tour.id, reportReason, reportDescription)
                                Toast.makeText(context, "Report submitted to admin for review", Toast.LENGTH_LONG).show()
                                showReportDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SportRed)
                ) {
                    Text("Submit Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerDedicatedSearchView(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchScope: SearchFilterScope,
    onSearchScopeChange: (SearchFilterScope) -> Unit,
    onlyActive: Boolean,
    onToggleOnlyActive: (Boolean) -> Unit,
    selectedSportId: String?,
    onSportSelected: (String?) -> Unit,
    selectedLocation: String?,
    onLocationSelected: (String?) -> Unit,
    searchDate: String,
    onSearchDateChange: (String) -> Unit,
    tournaments: List<Tournament>,
    onTournamentClick: (Tournament) -> Unit,
    onSaveToggle: (Tournament) -> Unit,
    onResetFilters: () -> Unit
) {
    var sportMenuExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Tournament Search Bar with Name & Location Scope
        item {
            TournamentSearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                searchScope = searchScope,
                onSearchScopeChange = onSearchScopeChange,
                onlyActive = onlyActive,
                onToggleOnlyActive = onToggleOnlyActive,
                matchingResultsCount = tournaments.size,
                onQuickLocationSelected = { loc ->
                    onLocationSelected(if (selectedLocation == loc) null else loc)
                    onSearchQueryChange(loc)
                    onSearchScopeChange(SearchFilterScope.LOCATION)
                }
            )
        }

        // Additional Refine Panel Card: Sport & Date
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "🎯 Refine Filters",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Sport Selector
                    ExposedDropdownMenuBox(
                        expanded = sportMenuExpanded,
                        onExpandedChange = { sportMenuExpanded = !sportMenuExpanded }
                    ) {
                        val currentSportName = Sport.ALL_SPORTS.find { it.id == selectedSportId }?.let { "${it.iconEmoji} ${it.name}" } ?: "All Sports"
                        OutlinedTextField(
                            value = currentSportName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Sport") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sportMenuExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = sportMenuExpanded,
                            onDismissRequest = { sportMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("🏅 All Sports") },
                                onClick = {
                                    onSportSelected(null)
                                    sportMenuExpanded = false
                                }
                            )
                            Sport.ALL_SPORTS.forEach { sport ->
                                DropdownMenuItem(
                                    text = { Text("${sport.iconEmoji} ${sport.name}") },
                                    onClick = {
                                        onSportSelected(sport.id)
                                        sportMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Date Input
                    OutlinedTextField(
                        value = searchDate,
                        onValueChange = onSearchDateChange,
                        label = { Text("Date (e.g. 15 October 2026)") },
                        leadingIcon = { Icon(Icons.Outlined.Event, contentDescription = null, tint = SportBlue) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Matching Active Tournaments",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${tournaments.size} Available",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (tournaments.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🔍", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "No active tournaments found", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try searching by another title, city (e.g. Erode, Salem), or resetting filters.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedButton(onClick = onResetFilters) {
                            Text("Reset Search Filters")
                        }
                    }
                }
            }
        } else {
            items(tournaments, key = { it.id }) { tour ->
                TournamentCard(
                    tournament = tour,
                    onCardClick = { onTournamentClick(tour) },
                    onSaveToggle = { onSaveToggle(tour) }
                )
            }
        }
    }
}
