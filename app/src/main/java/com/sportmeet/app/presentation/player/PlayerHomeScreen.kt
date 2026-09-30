package com.sportmeet.app.presentation.player

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Search
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sportmeet.app.core.common.AppBottomBar
import com.sportmeet.app.core.common.SportChip
import com.sportmeet.app.core.common.SportMeetTopBar
import com.sportmeet.app.core.common.StatusBadge
import com.sportmeet.app.core.common.TournamentCard
import com.sportmeet.app.core.navigation.BottomNavItem
import com.sportmeet.app.core.theme.SportBlue
import com.sportmeet.app.core.theme.SportGold
import com.sportmeet.app.core.theme.SportGreen
import com.sportmeet.app.core.theme.SportRed
import com.sportmeet.app.data.repository.RegistrationRepository
import com.sportmeet.app.data.repository.TournamentRepository
import com.sportmeet.app.domain.data.SampleData
import com.sportmeet.app.domain.model.Sport
import com.sportmeet.app.domain.model.Tournament
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
    var selectedSportId by remember { mutableStateOf<String?>(null) }
    var selectedLocation by remember { mutableStateOf<String?>(null) }
    var searchDateQuery by remember { mutableStateOf("") }

    // Tournaments state list
    val tournaments = remember { mutableStateListOf(*SampleData.sampleTournaments.toTypedArray()) }
    var selectedTournamentForDetail by remember { mutableStateOf<Tournament?>(null) }
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
                date = searchDateQuery
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

    // Filter logic for quick text search
    val displayedTournaments = tournaments.filter { tournament ->
        searchQuery.isBlank() ||
                tournament.name.contains(searchQuery, ignoreCase = true) ||
                tournament.location.contains(searchQuery, ignoreCase = true) ||
                tournament.sport.name.contains(searchQuery, ignoreCase = true)
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
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Find Your Next Tournament",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Browse grassroots tournaments or search by sport, city & date.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Search input box
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = {
                                        Text(
                                            text = "Search cricket, football, Erode, Salem...",
                                            fontSize = 14.sp
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Search,
                                            contentDescription = "Search",
                                            tint = SportBlue
                                        )
                                    },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { searchQuery = "" }) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Clear,
                                                    contentDescription = "Clear",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                        focusedBorderColor = SportBlue,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                    ),
                                    singleLine = true
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
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    if (selectedSportId != null) {
                                        TextButton(onClick = { selectedSportId = null }) {
                                            Text(
                                                text = "Clear filter",
                                                fontSize = 12.sp,
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
                                    fontWeight = FontWeight.Bold,
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
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(
                                                    if (isSelected) SportBlue.copy(alpha = 0.15f)
                                                    else MaterialTheme.colorScheme.surface
                                                )
                                                .clickable {
                                                    selectedLocation = if (loc == "All") null else loc
                                                }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = if (loc == "All") "📍 All Cities" else "📍 $loc",
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
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
                                            text = "No Tournaments Found",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Try clearing your filters or search keywords.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        OutlinedButton(
                                            onClick = {
                                                searchQuery = ""
                                                selectedSportId = null
                                                selectedLocation = null
                                                searchDateQuery = ""
                                                reloadTournaments()
                                            }
                                        ) {
                                            Text("Reset All Filters")
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

                // Schedule & Location
                Text(
                    text = "SCHEDULE & VENUE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "📅 ${tournament.date}  •  ⏰ ${tournament.startTime}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📍 ${tournament.venueName}, ${tournament.location}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

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
                            val geoUri = if (tournament.latitude != null && tournament.longitude != null) {
                                Uri.parse("geo:${tournament.latitude},${tournament.longitude}?q=${Uri.encode(tournament.venueName + ", " + tournament.location)}")
                            } else {
                                Uri.parse("geo:0,0?q=${Uri.encode(tournament.venueName + ", " + tournament.location)}")
                            }
                            val intent = Intent(Intent.ACTION_VIEW, geoUri)
                            context.startActivity(intent)
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
                                        notes = "Registered via SportMeet Android"
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
                        text = "Help keep SportMeet safe. Admin reviews all reports.",
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
    selectedSportId: String?,
    onSportSelected: (String?) -> Unit,
    selectedLocation: String?,
    onLocationSelected: (String?) -> Unit,
    searchDate: String,
    onSearchDateChange: (String) -> Unit,
    tournaments: List<Tournament>,
    onTournamentClick: (Tournament) -> Unit,
    onSaveToggle: (Tournament) -> Unit
) {
    var sportMenuExpanded by remember { mutableStateOf(false) }
    val cities = listOf("All", "Erode", "Salem", "Coimbatore", "Tiruppur", "Namakkal")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Dedicated Search Panel Card (Prompt Section 38 Search UX)
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
                        text = "🔍 Search Tournaments",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Keyword
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        label = { Text("Keyword / Title") },
                        placeholder = { Text("e.g. Cricket Cup, Trophy") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
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

                    // Location / City
                    Text(text = "Location", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        cities.forEach { city ->
                            val isSel = (selectedLocation == null && city == "All") || selectedLocation == city
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSel) SportBlue else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { onLocationSelected(if (city == "All") null else city) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = city,
                                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
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
                    text = "Matching Tournaments",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${tournaments.size} Found",
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
                        Text(text = "No tournaments found", fontWeight = FontWeight.Bold)
                        Text(
                            text = "Try:\n• Another sport\n• Another location\n• Another date",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
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
