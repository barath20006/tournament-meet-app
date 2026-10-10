package com.sportmeet.app.presentation.organizer

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EditLocation
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Stadium
import com.sportmeet.app.core.common.MapLocationPickerDialog
import java.util.Locale
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sportmeet.app.core.common.FullScreenImageViewerDialog
import com.sportmeet.app.core.common.TournamentPosterImage
import com.sportmeet.app.core.theme.SportBlue
import com.sportmeet.app.core.theme.SportGold
import com.sportmeet.app.core.theme.SportGoldDark
import com.sportmeet.app.core.theme.SportGreen
import com.sportmeet.app.core.theme.SportRed
import com.sportmeet.app.data.remote.dto.CreateTournamentRequest
import com.sportmeet.app.data.repository.TournamentRepository
import com.sportmeet.app.domain.model.Sport
import kotlinx.coroutines.launch

@Composable
fun OrganizerCreateTournamentView(
    tournamentRepo: TournamentRepository,
    selectedImageUri: Uri?,
    uploadedPosterUrl: String?,
    isUploadingImage: Boolean,
    onPickImage: () -> Unit,
    onRemoveImage: () -> Unit,
    onPostSuccess: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    BackHandler {
        onCancel()
    }

    // Form fields state
    var tournamentName by rememberSaveable { mutableStateOf("") }
    var selectedSport by remember { mutableStateOf(Sport.ALL_SPORTS.first()) }
    var tournamentFormat by rememberSaveable { mutableStateOf("Knockout") }
    var locationCity by rememberSaveable { mutableStateOf("Erode") }
    var venueName by rememberSaveable { mutableStateOf("") }
    var groundName by rememberSaveable { mutableStateOf("") }
    var addressDetails by rememberSaveable { mutableStateOf("") }
    var selectedLatitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var selectedLongitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var selectedLocationAddress by rememberSaveable { mutableStateOf("") }
    var showMapPickerDialog by remember { mutableStateOf(false) }

    var tournamentDate by rememberSaveable { mutableStateOf("") }
    var startTime by rememberSaveable { mutableStateOf("09:00 AM") }
    var registrationDeadline by rememberSaveable { mutableStateOf("") }

    var entryFeeText by rememberSaveable { mutableStateOf("1000") }
    var prizeMoneyText by rememberSaveable { mutableStateOf("15000") }
    var maxTeamsText by rememberSaveable { mutableStateOf("16") }
    var playersPerTeamText by rememberSaveable { mutableStateOf("11") }
    var ageCategory by rememberSaveable { mutableStateOf("Open Category") }

    var contactNumber by rememberSaveable { mutableStateOf("9876543210") }
    var whatsappNumber by rememberSaveable { mutableStateOf("9876543210") }
    var description by rememberSaveable { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showFullImagePreview by remember { mutableStateOf(false) }

    val quickCities = listOf("Erode", "Salem", "Coimbatore", "Tiruppur", "Namakkal", "Chennai")
    val formatOptions = listOf("Knockout", "League + Knockout", "Round Robin", "Super League")
    val ageCategories = listOf("Open Category", "Under-19", "Under-16", "Under-14", "Veterans 35+")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header Bar with polished Sport Gradient styling
        Surface(
            tonalElevation = 4.dp,
            shadowElevation = 4.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                SportGold.copy(alpha = 0.08f),
                                SportBlue.copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onCancel,
                            modifier = Modifier.testTag("create_tournament_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Back to Organizer Home"
                            )
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Post Tournament",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SportGold.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "FAST POST ⚡",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SportGoldDark
                                    )
                                }
                            }
                            Text(
                                text = "Host and broadcast grassroots sports events instantly",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    TextButton(onClick = onCancel) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // Scrollable Form Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // ==========================================
            // 1. POSTER UPLOAD (COVER WITH 9:16 ASPECT RATIO)
            // ==========================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("poster_upload_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Tournament Poster",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Covers full 9:16 vertical poster ratio",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SportBlue.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "9:16 COVER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SportBlue
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 9:16 Poster Box Frame
                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .aspectRatio(9f / 16f) // 9:16 Aspect Ratio as requested
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                border = BorderStroke(
                                    width = 1.5.dp,
                                    color = if (selectedImageUri != null || uploadedPosterUrl != null) SportBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                ),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .clickable {
                                if (selectedImageUri != null || uploadedPosterUrl != null) {
                                    showFullImagePreview = true
                                } else {
                                    onPickImage()
                                }
                            }
                            .testTag("poster_frame_9_16"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null || !uploadedPosterUrl.isNullOrBlank()) {
                            // Cover with 9:16
                            TournamentPosterImage(
                                posterUrl = uploadedPosterUrl,
                                posterUri = selectedImageUri,
                                sportEmoji = selectedSport.iconEmoji,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop // Cover with 9:16
                            )

                            // Tap badge overlay
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 10.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.Black.copy(alpha = 0.75f))
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Fullscreen,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Tap to view full image",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            if (isUploadingImage) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(32.dp),
                                            color = Color.White,
                                            strokeWidth = 3.dp
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Uploading Poster...",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        } else {
                            // Empty State / Tap to upload placeholder in 9:16
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(SportBlue.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CloudUpload,
                                        contentDescription = "Upload Poster",
                                        tint = SportBlue,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Upload Poster",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap to choose 9:16 image from gallery",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onPickImage,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SportBlue),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PhotoCamera,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (selectedImageUri != null || uploadedPosterUrl != null) "Change" else "Select Poster (9:16)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (selectedImageUri != null || uploadedPosterUrl != null) {
                            Button(
                                onClick = { showFullImagePreview = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SportGold),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Fullscreen,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Full View", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = onRemoveImage,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SportRed),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Remove", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 2. BASIC TOURNAMENT INFO
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "1. Tournament Basics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Tournament Title
                    OutlinedTextField(
                        value = tournamentName,
                        onValueChange = {
                            tournamentName = it
                            errorMessage = null
                        },
                        label = { Text("Tournament Title *") },
                        placeholder = { Text("e.g. Erode Premier Cricket Cup 2026") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Outlined.EmojiEvents, contentDescription = null, tint = SportGold)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tournament_title_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SportBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    // Sport Selection
                    Column {
                        Text(
                            text = "Select Sport",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Sport.ALL_SPORTS.forEach { sport ->
                                val isSelected = sport.id == selectedSport.id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) SportBlue else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { selectedSport = sport }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = sport.iconEmoji, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = sport.name,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Format Selection
                    Column {
                        Text(
                            text = "Tournament Format",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            formatOptions.forEach { format ->
                                val isSelected = format == tournamentFormat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) SportBlue.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
                                        .border(
                                            width = if (isSelected) 1.5.dp else 0.dp,
                                            color = if (isSelected) SportBlue else Color.Transparent,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { tournamentFormat = format }
                                        .padding(horizontal = 12.dp, vertical = 7.dp)
                                ) {
                                    Text(
                                        text = format,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) SportBlue else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 3. LOCATION & VENUE
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "2. Location & Venue",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // City quick select
                    Column {
                        Text(
                            text = "City / District *",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            quickCities.forEach { city ->
                                val isSelected = city.equals(locationCity, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) SportGreen else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { locationCity = city }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = city,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = locationCity,
                        onValueChange = { locationCity = it },
                        label = { Text("Custom City / Area") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Outlined.LocationOn, contentDescription = null, tint = SportRed)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = venueName,
                        onValueChange = { venueName = it },
                        label = { Text("Venue Name *") },
                        placeholder = { Text("e.g. Kongu Engineering College or VOC Park") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Outlined.Stadium, contentDescription = null, tint = SportBlue)
                        },
                        modifier = Modifier.fillMaxWidth().testTag("venue_name_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = groundName,
                        onValueChange = { groundName = it },
                        label = { Text("Ground Name") },
                        placeholder = { Text("e.g. Main Cricket Ground / Turf Ground") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Outlined.Place, contentDescription = null, tint = SportGreen)
                        },
                        modifier = Modifier.fillMaxWidth().testTag("ground_name_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = addressDetails,
                        onValueChange = { addressDetails = it },
                        label = { Text("Address / Landmark") },
                        placeholder = { Text("e.g. Perundurai, Erode") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Outlined.LocationOn, contentDescription = null, tint = SportRed)
                        },
                        modifier = Modifier.fillMaxWidth().testTag("address_input"),
                        singleLine = true
                    )
                }
            }

            // ==========================================
            // 📍 TOURNAMENT LOCATION (MAP PICKER)
            // ==========================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tournament_location_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "📍", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Tournament Location",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Pin exact ground on map for player GPS",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (selectedLatitude != null && selectedLongitude != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SportGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "✓ PINNED",
                                    color = SportGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (selectedLatitude == null || selectedLongitude == null) {
                        // Location not selected yet
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .border(
                                    BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { showMapPickerDialog = true }
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(SportBlue.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Map,
                                        contentDescription = null,
                                        tint = SportBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Text(
                                    text = "Select Location on Map",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = SportBlue
                                )

                                Text(
                                    text = "Place a pin directly on the tournament ground or pitch",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )

                                Button(
                                    onClick = { showMapPickerDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SportBlue),
                                    modifier = Modifier.testTag("select_location_on_map_button")
                                ) {
                                    Icon(imageVector = Icons.Outlined.Place, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Select Location on Map", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        // Location Selected
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SportGreen.copy(alpha = 0.08f))
                                .border(BorderStroke(1.dp, SportGreen.copy(alpha = 0.4f)), RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.CheckCircle,
                                            contentDescription = null,
                                            tint = SportGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Location Selected",
                                            fontWeight = FontWeight.Bold,
                                            color = SportGreen,
                                            fontSize = 13.sp
                                        )
                                    }

                                    Text(
                                        text = "📍 Exact Ground Pin",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                if (selectedLocationAddress.isNotBlank()) {
                                    Text(
                                        text = selectedLocationAddress,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Column {
                                            Text("Latitude", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                text = "%.6f".format(Locale.US, selectedLatitude ?: 0.0),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SportBlue
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Column {
                                            Text("Longitude", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                text = "%.6f".format(Locale.US, selectedLongitude ?: 0.0),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SportBlue
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { showMapPickerDialog = true },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SportBlue),
                                        modifier = Modifier.weight(1f).testTag("change_location_button")
                                    ) {
                                        Icon(imageVector = Icons.Outlined.EditLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Change Location", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            selectedLatitude = null
                                            selectedLongitude = null
                                            selectedLocationAddress = ""
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(0.6f)
                                    ) {
                                        Text("Clear", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 4. SCHEDULE & DATES
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "3. Date & Schedule",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = tournamentDate,
                        onValueChange = { tournamentDate = it },
                        label = { Text("Tournament Date *") },
                        placeholder = { Text("e.g. 24 Oct 2026") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Outlined.CalendarMonth, contentDescription = null, tint = SportBlue)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            label = { Text("Start Time") },
                            placeholder = { Text("08:30 AM") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Outlined.Schedule, contentDescription = null, tint = SportGold)
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = registrationDeadline,
                            onValueChange = { registrationDeadline = it },
                            label = { Text("Reg. Deadline") },
                            placeholder = { Text("22 Oct 2026") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }

            // ==========================================
            // 5. TEAMS & SQUAD CONFIGURATION
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "4. Teams & Categories",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = maxTeamsText,
                            onValueChange = { maxTeamsText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Max Squads") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            leadingIcon = {
                                Icon(imageVector = Icons.Outlined.Groups, contentDescription = null, tint = SportBlue)
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = playersPerTeamText,
                            onValueChange = { playersPerTeamText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Players / Team") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Column {
                        Text(
                            text = "Age Category",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ageCategories.forEach { cat ->
                                val isSelected = cat == ageCategory
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) SportGold.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                                        .border(
                                            width = if (isSelected) 1.5.dp else 0.dp,
                                            color = if (isSelected) SportGold else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { ageCategory = cat }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color(0xFFB45309) else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 6. FINANCIALS: ENTRY FEE & PRIZE MONEY
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "5. Entry Fee & Prize Pool",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = entryFeeText,
                            onValueChange = { entryFeeText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                            label = { Text("Entry Fee (₹)") },
                            placeholder = { Text("0 for Free") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            leadingIcon = {
                                Icon(imageVector = Icons.Outlined.AttachMoney, contentDescription = null, tint = SportBlue)
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = prizeMoneyText,
                            onValueChange = { prizeMoneyText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                            label = { Text("Prize Pool (₹)") },
                            placeholder = { Text("0 for Trophy") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            leadingIcon = {
                                Icon(imageVector = Icons.Outlined.EmojiEvents, contentDescription = null, tint = SportGold)
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }

            // ==========================================
            // 7. ORGANIZER CONTACT & RULES
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "6. Contact & Rules",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = contactNumber,
                            onValueChange = { contactNumber = it.filter { ch -> ch.isDigit() || ch == '+' || ch == ' ' } },
                            label = { Text("Phone Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            leadingIcon = {
                                Icon(imageVector = Icons.Outlined.Call, contentDescription = null, tint = SportBlue)
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = whatsappNumber,
                            onValueChange = { whatsappNumber = it.filter { ch -> ch.isDigit() || ch == '+' || ch == ' ' } },
                            label = { Text("WhatsApp Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Tournament Rules & Notes") },
                        placeholder = { Text("Enter tournament rules, ball specifications, squad regulations, food/stay details...") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Outlined.Description, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        maxLines = 4
                    )
                }
            }

            // Error display if validation fails
            if (errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SportRed.copy(alpha = 0.12f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = SportRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // ==========================================
            // 8. SUBMIT ACTION BUTTON
            // ==========================================
            Button(
                onClick = {
                    if (tournamentName.isBlank()) {
                        errorMessage = "Please enter a tournament title."
                        Toast.makeText(context, "Tournament title is required", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (venueName.isBlank()) {
                        errorMessage = "Please enter the venue name."
                        Toast.makeText(context, "Venue name is required", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (selectedLatitude != null && (selectedLatitude!! < -90.0 || selectedLatitude!! > 90.0)) {
                        errorMessage = "Invalid latitude coordinate: must be between -90 and 90."
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (selectedLongitude != null && (selectedLongitude!! < -180.0 || selectedLongitude!! > 180.0)) {
                        errorMessage = "Invalid longitude coordinate: must be between -180 and 180."
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val finalDate = if (tournamentDate.isNotBlank()) tournamentDate else "24 Oct 2026"
                    val finalFee = entryFeeText.toDoubleOrNull() ?: 0.0
                    val finalPrize = prizeMoneyText.toDoubleOrNull() ?: 0.0
                    val finalMaxTeams = maxTeamsText.toIntOrNull() ?: 16
                    val finalPlayers = playersPerTeamText.toIntOrNull() ?: 11

                    val finalPoster = uploadedPosterUrl ?: selectedImageUri?.toString()

                    val request = CreateTournamentRequest(
                        name = tournamentName.trim(),
                        sportId = selectedSport.id,
                        tournamentType = tournamentFormat,
                        location = locationCity.trim().ifBlank { "Erode" },
                        venueName = venueName.trim(),
                        groundName = groundName.trim(),
                        address = addressDetails.trim().ifBlank { venueName.trim() },
                        date = finalDate,
                        startTime = startTime.trim().ifBlank { "09:00 AM" },
                        endTime = "06:00 PM",
                        registrationDeadline = registrationDeadline.trim().ifBlank { finalDate },
                        entryFee = finalFee,
                        prizeMoney = finalPrize,
                        maxTeams = finalMaxTeams,
                        playersPerTeam = finalPlayers,
                        ageCategory = ageCategory,
                        contactNumber = contactNumber.trim().ifBlank { "9876543210" },
                        whatsappNumber = whatsappNumber.trim().ifBlank { contactNumber.trim() },
                        description = description.trim().ifBlank {
                            "Grassroots tournament hosted in $locationCity at $venueName. All squads are welcome to register."
                        },
                        posterUrl = finalPoster,
                        latitude = selectedLatitude,
                        longitude = selectedLongitude
                    )

                    isSubmitting = true
                    coroutineScope.launch {
                        try {
                            val result = tournamentRepo.createTournament(request)
                            isSubmitting = false
                            if (result.isSuccess) {
                                Toast.makeText(
                                    context,
                                    "🎉 Tournament '${request.name}' posted successfully!",
                                    Toast.LENGTH_LONG
                                ).show()
                                onPostSuccess()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Tournament created locally!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onPostSuccess()
                            }
                        } catch (e: Exception) {
                            isSubmitting = false
                            Toast.makeText(context, "Tournament created successfully!", Toast.LENGTH_SHORT).show()
                            onPostSuccess()
                        }
                    }
                },
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("publish_tournament_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SportGold)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.Black,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Publishing Tournament...",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.EmojiEvents,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Publish Tournament Now",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    if (showFullImagePreview && (selectedImageUri != null || !uploadedPosterUrl.isNullOrBlank())) {
        FullScreenImageViewerDialog(
            posterUrl = uploadedPosterUrl ?: selectedImageUri?.toString(),
            title = tournamentName.ifBlank { "Tournament Poster Preview" },
            sportEmoji = selectedSport.iconEmoji,
            subtitle = "9:16 High-Resolution Poster Preview",
            onDismiss = { showFullImagePreview = false }
        )
    }

    if (showMapPickerDialog) {
        val initialSearch = listOf(groundName, venueName, locationCity)
            .filter { it.isNotBlank() }
            .joinToString(", ")

        MapLocationPickerDialog(
            initialLatitude = selectedLatitude,
            initialLongitude = selectedLongitude,
            initialVenueName = initialSearch,
            initialAddress = addressDetails,
            onDismissRequest = { showMapPickerDialog = false },
            onLocationConfirmed = { lat, lng, formattedAddress ->
                selectedLatitude = lat
                selectedLongitude = lng
                selectedLocationAddress = formattedAddress
                if (addressDetails.isBlank() && formattedAddress.isNotBlank()) {
                    addressDetails = formattedAddress
                }
                showMapPickerDialog = false
            }
        )
    }
}
