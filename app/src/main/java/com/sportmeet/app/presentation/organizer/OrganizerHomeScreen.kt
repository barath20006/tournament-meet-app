package com.sportmeet.app.presentation.organizer

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.PostAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sportmeet.app.core.common.AppBottomBar
import com.sportmeet.app.core.common.FullScreenImageViewerDialog
import com.sportmeet.app.core.common.SportMeetTopBar
import com.sportmeet.app.core.common.StatusBadge
import com.sportmeet.app.core.common.TournamentPosterImage
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
    val myTournaments = remember { mutableStateListOf<Tournament>() }
    val allRegistrations = remember { mutableStateListOf<RegistrationResponse>() }

    var selectedTournamentForTeams by remember { mutableStateOf<Tournament?>(null) }
    var fullScreenTournamentPoster by remember { mutableStateOf<Tournament?>(null) }

    // Poster image state for creating tournaments (registered at root Activity level)
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
                try {
                    val uploadResult = tournamentRepo.uploadPoster(context, it)
                    uploadResult.onSuccess { url ->
                        uploadedPosterUrl = url
                        isUploadingImage = false
                        Toast.makeText(context, "Tournament poster attached!", Toast.LENGTH_SHORT).show()
                    }.onFailure { err ->
                        isUploadingImage = false
                        Toast.makeText(context, "Poster preview ready", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    isUploadingImage = false
                }
            }
        }
    }

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

    if (currentTab != BottomNavItem.OrganizerHomeTab.route) {
        BackHandler {
            currentTab = BottomNavItem.OrganizerHomeTab.route
        }
    }

    val activeCount = myTournaments.count { it.status == TournamentStatus.APPROVED || it.status == TournamentStatus.REGISTRATION_OPEN }
    val pendingCount = myTournaments.count { it.status == TournamentStatus.PENDING_APPROVAL }
    val totalRegisteredTeams = myTournaments.sumOf { it.registeredTeams }

    Scaffold(
        topBar = {
            if (currentTab != BottomNavItem.OrganizerProfileTab.route && currentTab != BottomNavItem.OrganizerCreateTab.route) {
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
                }
            )
        },
        floatingActionButton = {
            if (currentTab == BottomNavItem.OrganizerHomeTab.route || currentTab == BottomNavItem.OrganizerTournamentsTab.route) {
                ExtendedFloatingActionButton(
                    onClick = { currentTab = BottomNavItem.OrganizerCreateTab.route },
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

                BottomNavItem.OrganizerCreateTab.route -> {
                    // Dedicated Full-Screen Organizer Create Tournament View
                    OrganizerCreateTournamentView(
                        tournamentRepo = tournamentRepo,
                        selectedImageUri = selectedImageUri,
                        uploadedPosterUrl = uploadedPosterUrl,
                        isUploadingImage = isUploadingImage,
                        onPickImage = { imagePickerLauncher.launch("image/*") },
                        onRemoveImage = {
                            selectedImageUri = null
                            uploadedPosterUrl = null
                        },
                        onPostSuccess = {
                            selectedImageUri = null
                            uploadedPosterUrl = null
                            refreshOrganizerData()
                            currentTab = BottomNavItem.OrganizerTournamentsTab.route
                        },
                        onCancel = {
                            currentTab = BottomNavItem.OrganizerHomeTab.route
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
                                                text = "Manage tournaments & accept squad entries",
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
                        if (myTournaments.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = "🏆", fontSize = 40.sp)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "No Tournaments Posted Yet",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "You haven't posted any tournaments yet. Tap '+ Post Tournament' to create your first event.",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = { currentTab = BottomNavItem.OrganizerCreateTab.route },
                                            colors = ButtonDefaults.buttonColors(containerColor = SportGold),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text(
                                                text = "+ Post New Tournament",
                                                color = Color.Black,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            itemsIndexed(myTournaments, key = { index, tour -> "${tour.id}_$index" }) { _, tour ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        if (!tour.posterUrl.isNullOrBlank()) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(140.dp)
                                                    .clickable { fullScreenTournamentPoster = tour }
                                            ) {
                                                TournamentPosterImage(
                                                    posterUrl = tour.posterUrl,
                                                    sportEmoji = tour.sport.iconEmoji,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )

                                                // Pill badge to view full poster
                                                Row(
                                                    modifier = Modifier
                                                        .align(Alignment.BottomEnd)
                                                        .padding(8.dp)
                                                        .clip(RoundedCornerShape(16.dp))
                                                        .background(Color.Black.copy(alpha = 0.75f))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.Fullscreen,
                                                        contentDescription = "View full poster",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        text = "View Full Poster (9:16)",
                                                        color = Color.White,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }

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
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = tour.sport.iconEmoji, fontSize = 24.sp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = tour.name,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    val groundDesc = listOf(tour.groundName, tour.venueName, tour.location).filter { it.isNotBlank() }.joinToString(" • ")
                                                    Text(
                                                        text = "📍 $groundDesc  •  📅 ${tour.date}",
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
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

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
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

                                                if (tour.latitude != null && tour.longitude != null) {
                                                    IconButton(
                                                        onClick = {
                                                            val lat = tour.latitude
                                                            val lng = tour.longitude
                                                            val groundLabel = (tour.groundName.ifBlank { tour.venueName }).ifBlank { "Tournament Ground" }
                                                            val geoUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(groundLabel)})")
                                                            val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
                                                                setPackage("com.google.android.apps.maps")
                                                            }
                                                            try {
                                                                context.startActivity(mapIntent)
                                                            } catch (_: Exception) {
                                                                val fallbackUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
                                                                try { context.startActivity(Intent(Intent.ACTION_VIEW, fallbackUri)) } catch (_: Exception) {}
                                                            }
                                                        },
                                                        modifier = Modifier.size(36.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Outlined.Place,
                                                            contentDescription = "View Ground Location",
                                                            tint = SportRed,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                }

                                                IconButton(
                                                    onClick = {
                                                        coroutineScope.launch {
                                                            tournamentRepo.deleteTournament(tour.id)
                                                            refreshOrganizerData()
                                                            Toast.makeText(context, "Tournament '${tour.name}' removed", Toast.LENGTH_SHORT).show()
                                                        }
                                                    },
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.Delete,
                                                        contentDescription = "Delete Tournament",
                                                        tint = SportRed.copy(alpha = 0.85f),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
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

    fullScreenTournamentPoster?.let { tournament ->
        FullScreenImageViewerDialog(
            posterUrl = tournament.posterUrl,
            title = tournament.name,
            sportEmoji = tournament.sport.iconEmoji,
            subtitle = "Organizer Poster View • 9:16 Full Poster",
            onDismiss = { fullScreenTournamentPoster = null }
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
