package com.sportmeet.app.presentation.organizer

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PostAdd
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sportmeet.app.core.common.AppBottomBar
import com.sportmeet.app.core.common.SportMeetTopBar
import com.sportmeet.app.core.common.StatusBadge
import com.sportmeet.app.core.navigation.BottomNavItem
import com.sportmeet.app.core.theme.SportBlue
import com.sportmeet.app.core.theme.SportGold
import com.sportmeet.app.core.theme.SportGreen
import com.sportmeet.app.core.theme.SportRed
import com.sportmeet.app.data.remote.dto.CreateTournamentRequest
import com.sportmeet.app.data.remote.dto.RegistrationResponse
import com.sportmeet.app.data.repository.RegistrationRepository
import com.sportmeet.app.data.repository.TournamentRepository
import com.sportmeet.app.domain.data.SampleData
import com.sportmeet.app.domain.model.Sport
import com.sportmeet.app.domain.model.Tournament
import com.sportmeet.app.domain.model.TournamentStatus
import com.sportmeet.app.domain.model.UserRole
import com.sportmeet.app.presentation.profile.ProfileScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizerHomeScreen(
    onSwitchToPlayer: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val tournamentRepo = remember { TournamentRepository(context) }
    val registrationRepo = remember { RegistrationRepository(context) }

    var currentTab by remember { mutableStateOf(BottomNavItem.OrganizerHomeTab.route) }

    // Organizer Tournaments state
    val myTournaments = remember { mutableStateListOf(*SampleData.sampleOrganizerTournaments.toTypedArray()) }
    val allRegistrations = remember { mutableStateListOf<RegistrationResponse>() }

    var showCreateTournamentDialog by remember { mutableStateOf(false) }
    var selectedTournamentForTeams by remember { mutableStateOf<Tournament?>(null) }

    fun refreshOrganizerData() {
        coroutineScope.launch {
            val tourResult = tournamentRepo.getOrganizerTournaments()
            if (tourResult.isSuccess) {
                myTournaments.clear()
                myTournaments.addAll(tourResult.getOrDefault(emptyList()))
            }
            val regResult = registrationRepo.getMyRegistrations()
            if (regResult.isSuccess) {
                allRegistrations.clear()
                allRegistrations.addAll(regResult.getOrDefault(emptyList()))
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshOrganizerData()
    }

    val activeCount = myTournaments.count { it.status == TournamentStatus.APPROVED || it.status == TournamentStatus.REGISTRATION_OPEN }
    val pendingCount = myTournaments.count { it.status == TournamentStatus.PENDING_APPROVAL }
    val totalRegisteredTeams = myTournaments.sumOf { it.registeredTeams }

    Scaffold(
        topBar = {
            if (currentTab != BottomNavItem.OrganizerProfileTab.route) {
                SportMeetTopBar(
                    currentRole = UserRole.ORGANIZER,
                    onSwitchRole = onSwitchToPlayer
                )
            }
        },
        bottomBar = {
            AppBottomBar(
                role = UserRole.ORGANIZER,
                selectedTabRoute = currentTab,
                onTabSelected = { tab ->
                    currentTab = tab
                    if (tab == BottomNavItem.OrganizerCreateTab.route) {
                        showCreateTournamentDialog = true
                    }
                }
            )
        },
        floatingActionButton = {
            if (currentTab == BottomNavItem.OrganizerHomeTab.route || currentTab == BottomNavItem.OrganizerTournamentsTab.route) {
                ExtendedFloatingActionButton(
                    onClick = { showCreateTournamentDialog = true },
                    containerColor = SportGold,
                    contentColor = Color.Black,
                    icon = { Icon(imageVector = Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Post Tournament", fontWeight = FontWeight.Bold) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                BottomNavItem.OrganizerProfileTab.route -> {
                    ProfileScreen(
                        currentRole = UserRole.ORGANIZER,
                        onRoleSwitched = { newRole ->
                            if (newRole == UserRole.PLAYER) onSwitchToPlayer()
                        },
                        onLogout = onLogout
                    )
                }

                BottomNavItem.OrganizerRegistrationsTab.route -> {
                    // Organizer Team Registrations Management View
                    OrganizerRegistrationsView(
                        registrations = allRegistrations,
                        onUpdateStatus = { regId, newStatus ->
                            coroutineScope.launch {
                                registrationRepo.updateRegistrationStatus(regId, newStatus)
                                refreshOrganizerData()
                                Toast.makeText(context, "Registration status updated to $newStatus", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }

                else -> {
                    // Home & My Tournaments
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        // Organizer Greeting Banner
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
                                        .padding(20.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Organizer Hub 👋",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Host tournaments & accept squad entries",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(SportGold.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = "📢", fontSize = 22.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(18.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Stats Grid (Active, Pending, Teams)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        StatBox(
                                            title = "Active",
                                            value = "$activeCount",
                                            icon = Icons.Outlined.CheckCircleOutline,
                                            color = SportGreen
                                        )
                                        StatBox(
                                            title = "Pending",
                                            value = "$pendingCount",
                                            icon = Icons.Outlined.HourglassEmpty,
                                            color = SportGold
                                        )
                                        StatBox(
                                            title = "Teams",
                                            value = "$totalRegisteredTeams",
                                            icon = Icons.Outlined.Groups,
                                            color = SportBlue
                                        )
                                    }
                                }
                            }
                        }

                        // Quick Create Card Banner
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = SportBlue.copy(alpha = 0.08f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(SportBlue),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.PostAdd,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Host a New Tournament",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "List sports event in 2 minutes",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { showCreateTournamentDialog = true },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SportBlue),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Post Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // My Hosted Tournaments Section
                        item {
                            Spacer(modifier = Modifier.height(20.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "My Hosted Tournaments",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "${myTournaments.size} Events",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Tournaments List
                        items(myTournaments, key = { it.id }) { tour ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = tour.sport.iconEmoji, fontSize = 24.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = tour.name,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "📍 ${tour.location} • 📅 ${tour.date}",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        StatusBadge(status = tour.status)
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "REGISTRATIONS",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "${tour.registeredTeams} / ${tour.maxTeams} Teams",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (tour.registeredTeams >= tour.maxTeams) SportGreen else SportBlue
                                            )
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = {
                                                    selectedTournamentForTeams = tour
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = SportBlue),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text("Manage Teams", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(60.dp))
                        }
                    }
                }
            }
        }
    }

    // Dialog for Quick Organizer Post Tournament
    if (showCreateTournamentDialog) {
        var tourName by remember { mutableStateOf("") }
        var selectedSport by remember { mutableStateOf(Sport.ALL_SPORTS[0]) }
        var locationCity by remember { mutableStateOf("Erode") }
        var venueName by remember { mutableStateOf("") }
        var tourDate by remember { mutableStateOf("15 November 2026") }
        var tourTime by remember { mutableStateOf("06:00 PM") }
        var entryFeeText by remember { mutableStateOf("2000") }
        var prizeText by remember { mutableStateOf("25000") }
        var contactPhone by remember { mutableStateOf("+91 98765 43210") }
        var sportDropdownExpanded by remember { mutableStateOf(false) }

        // Poster image state
        var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
        var uploadedPosterUrl by remember { mutableStateOf<String?>(null) }
        var isUploadingImage by remember { mutableStateOf(false) }

        val imagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                isUploadingImage = true
                coroutineScope.launch {
                    val uploadResult = tournamentRepo.uploadPoster(context, it)
                    uploadResult.onSuccess { url ->
                        uploadedPosterUrl = url
                        isUploadingImage = false
                        Toast.makeText(context, "Tournament poster attached!", Toast.LENGTH_SHORT).show()
                    }.onFailure { err ->
                        isUploadingImage = false
                        Toast.makeText(context, "Upload note: Local preview ready (${err.message})", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        AlertDialog(
            onDismissRequest = { showCreateTournamentDialog = false },
            title = {
                Text(
                    text = "🏆 Post New Tournament",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Enter details for your sports event. It will be submitted for admin approval.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = tourName,
                        onValueChange = { tourName = it },
                        label = { Text("Tournament Name") },
                        placeholder = { Text("e.g. Erode Premier Cricket Cup") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Sport Dropdown
                    ExposedDropdownMenuBox(
                        expanded = sportDropdownExpanded,
                        onExpandedChange = { sportDropdownExpanded = !sportDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = "${selectedSport.iconEmoji} ${selectedSport.name}",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Sport") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sportDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = sportDropdownExpanded,
                            onDismissRequest = { sportDropdownExpanded = false }
                        ) {
                            Sport.ALL_SPORTS.forEach { sport ->
                                DropdownMenuItem(
                                    text = { Text("${sport.iconEmoji} ${sport.name}") },
                                    onClick = {
                                        selectedSport = sport
                                        sportDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Tournament Poster Upload Section
                    Text(
                        text = "TOURNAMENT POSTER / BANNER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (selectedImageUri != null || !uploadedPosterUrl.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, SportBlue.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        ) {
                            com.sportmeet.app.core.common.TournamentPosterImage(
                                posterUri = selectedImageUri,
                                posterUrl = uploadedPosterUrl,
                                sportEmoji = selectedSport.iconEmoji,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Actions overlay: Change / Remove
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.Black.copy(alpha = 0.7f),
                                    modifier = Modifier.clickable {
                                        imagePickerLauncher.launch("image/*")
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.PhotoCamera,
                                            contentDescription = "Change",
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Change", color = Color.White, fontSize = 11.sp)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SportRed.copy(alpha = 0.85f),
                                    modifier = Modifier.clickable {
                                        selectedImageUri = null
                                        uploadedPosterUrl = null
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Delete,
                                            contentDescription = "Remove",
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Remove", color = Color.White, fontSize = 11.sp)
                                    }
                                }
                            }

                            if (isUploadingImage) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                        Text(
                                            text = "Uploading image...",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { imagePickerLauncher.launch("image/*") },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp, horizontal = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(SportBlue.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CloudUpload,
                                        contentDescription = "Upload Poster",
                                        tint = SportBlue,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "📸 Upload Tournament Poster / Banner",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SportBlue
                                )
                                Text(
                                    text = "Tap to choose photo from gallery (JPEG, PNG)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = locationCity,
                            onValueChange = { locationCity = it },
                            label = { Text("City") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = tourDate,
                            onValueChange = { tourDate = it },
                            label = { Text("Date") },
                            singleLine = true,
                            modifier = Modifier.weight(1.3f)
                        )
                    }

                    OutlinedTextField(
                        value = venueName,
                        onValueChange = { venueName = it },
                        label = { Text("Venue Name") },
                        placeholder = { Text("e.g. VOC Sports Ground") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = entryFeeText,
                            onValueChange = { entryFeeText = it },
                            label = { Text("Entry Fee (₹)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = prizeText,
                            onValueChange = { prizeText = it },
                            label = { Text("Prize (₹)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = contactPhone,
                        onValueChange = { contactPhone = it },
                        label = { Text("Contact Phone") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tourName.isNotBlank() && venueName.isNotBlank()) {
                            coroutineScope.launch {
                                tournamentRepo.createTournament(
                                    CreateTournamentRequest(
                                        name = tourName,
                                        sportId = selectedSport.id,
                                        location = locationCity,
                                        venueName = venueName,
                                        date = tourDate,
                                        startTime = tourTime,
                                        entryFee = entryFeeText.toDoubleOrNull() ?: 0.0,
                                        prizeMoney = prizeText.toDoubleOrNull() ?: 0.0,
                                        contactNumber = contactPhone,
                                        whatsappNumber = contactPhone,
                                        description = "Grassroots tournament hosted in $locationCity at $venueName.",
                                        posterUrl = uploadedPosterUrl
                                    )
                                )
                                refreshOrganizerData()
                                Toast.makeText(context, "Tournament '$tourName' submitted for approval!", Toast.LENGTH_LONG).show()
                                showCreateTournamentDialog = false
                            }
                        } else {
                            Toast.makeText(context, "Please enter Tournament Name and Venue", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SportGold, contentColor = Color.Black)
                ) {
                    Text("Submit for Approval", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateTournamentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal Dialog to view Registered Teams for a Tournament
    selectedTournamentForTeams?.let { tournament ->
        val teamsForTour = allRegistrations.filter { it.tournamentId == tournament.id || tournament.name.contains(it.tournamentName) }

        AlertDialog(
            onDismissRequest = { selectedTournamentForTeams = null },
            title = {
                Text(
                    text = "Teams for ${tournament.name}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Total Entries: ${teamsForTour.size} / ${tournament.maxTeams}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SportBlue
                    )

                    if (teamsForTour.isEmpty()) {
                        Text(
                            text = "No squads have registered for this tournament yet.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        teamsForTour.forEach { reg ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = reg.teamName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(text = reg.status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (reg.status == "APPROVED") SportGreen else SportGold)
                                    }
                                    Text(text = "Captain: ${reg.captainName} (${reg.phone})", fontSize = 11.sp)

                                    if (reg.status != "APPROVED") {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        registrationRepo.updateRegistrationStatus(reg.id, "APPROVED")
                                                        refreshOrganizerData()
                                                        Toast.makeText(context, "${reg.teamName} Approved!", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = SportGreen),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text("Approve", fontSize = 11.sp)
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        registrationRepo.updateRegistrationStatus(reg.id, "REJECTED")
                                                        refreshOrganizerData()
                                                        Toast.makeText(context, "${reg.teamName} Rejected.", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text("Reject", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedTournamentForTeams = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun OrganizerRegistrationsView(
    registrations: List<RegistrationResponse>,
    onUpdateStatus: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Team Entries Management",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Review, confirm or reject squad submissions",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (registrations.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "📝", fontSize = 42.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "No registrations yet", fontWeight = FontWeight.Bold)
                    Text(
                        text = "Teams registered by players will appear here for your confirmation.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(registrations, key = { it.id }) { reg ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = reg.teamName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                StatusBadgeText(status = reg.status)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Tournament: ${reg.tournamentName}", fontSize = 12.sp, color = SportBlue, fontWeight = FontWeight.Medium)
                            Text(text = "Captain: ${reg.captainName}  •  Phone: ${reg.phone}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            if (reg.status != "APPROVED") {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = { onUpdateStatus(reg.id, "REJECTED") },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SportRed)
                                    ) {
                                        Icon(imageVector = Icons.Outlined.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Reject", fontSize = 12.sp)
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = { onUpdateStatus(reg.id, "APPROVED") },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SportGreen)
                                    ) {
                                        Icon(imageVector = Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Confirm Team", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadgeText(status: String) {
    val (bg, textColor) = when (status) {
        "APPROVED", "CONFIRMED" -> Pair(SportGreen.copy(alpha = 0.15f), SportGreen)
        "PENDING" -> Pair(SportGold.copy(alpha = 0.18f), Color(0xFFB45309))
        else -> Pair(SportRed.copy(alpha = 0.15f), SportRed)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text = status.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}

@Composable
private fun StatBox(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = title,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
