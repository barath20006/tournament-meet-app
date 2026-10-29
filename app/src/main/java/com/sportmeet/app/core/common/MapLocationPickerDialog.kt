package com.sportmeet.app.core.common

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Stadium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.sportmeet.app.core.theme.SportBlue
import com.sportmeet.app.core.theme.SportGreen
import com.sportmeet.app.core.theme.SportRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

/**
 * Model representing a location autocomplete or search match.
 */
data class SearchLocationItem(
    val title: String,
    val subtitle: String,
    val latitude: Double,
    val longitude: Double,
    val isGround: Boolean = false
)

/**
 * Built-in rich catalog of stadiums, towns, areas, and sports grounds.
 * Guarantees instantaneous search & autocomplete even when offline or without Google Play Services.
 */
val ALL_KNOWN_PLACES = listOf(
    // Pallipalayam & Namakkal region (direct answer to user scenario)
    SearchLocationItem("Pallipalayam", "Namakkal District, Tamil Nadu", 11.3653, 77.7478),
    SearchLocationItem("Pallipalayam Agraharam", "Pallipalayam, Tamil Nadu", 11.3734, 77.7429),
    SearchLocationItem("SPB Colony Cricket Ground", "SPB Colony, Pallipalayam", 11.3850, 77.7550, isGround = true),
    SearchLocationItem("Cauvery River Sports Ground", "Pallipalayam, Namakkal", 11.3600, 77.7450, isGround = true),
    SearchLocationItem("Pallipalayam Bus Stand Ground", "Pallipalayam, Namakkal", 11.3670, 77.7460, isGround = true),
    SearchLocationItem("Komarapalayam", "Namakkal District, Tamil Nadu", 11.4397, 77.7058),
    SearchLocationItem("Tiruchengode", "Namakkal District, Tamil Nadu", 11.3789, 77.8936),
    SearchLocationItem("Namakkal City", "Namakkal District, Tamil Nadu", 11.2189, 78.1674),
    SearchLocationItem("Namakkal Sports Stadium", "Thillaipuram, Namakkal", 11.2215, 78.1630, isGround = true),

    // Erode region
    SearchLocationItem("Erode City", "Erode District, Tamil Nadu", 11.3410, 77.7172),
    SearchLocationItem("VOC Park Stadium", "Brough Road, Erode", 11.3398, 77.7289, isGround = true),
    SearchLocationItem("Kongu Engineering College", "Perundurai, Erode", 11.2742, 77.6070, isGround = true),
    SearchLocationItem("Perundurai Ground", "Perundurai, Erode", 11.2770, 77.5840, isGround = true),
    SearchLocationItem("Bhavani Town", "Erode District, Tamil Nadu", 11.4468, 77.6836),
    SearchLocationItem("Solar Ground", "Solar, Erode", 11.3094, 77.7533, isGround = true),
    SearchLocationItem("Chithode Sports Field", "Chithode, Erode", 11.4087, 77.6713, isGround = true),
    SearchLocationItem("Thindal Sports Ground", "Thindal, Erode", 11.3190, 77.6880, isGround = true),

    // Coimbatore region
    SearchLocationItem("Coimbatore City", "Coimbatore District, Tamil Nadu", 11.0168, 76.9558),
    SearchLocationItem("Nehru Stadium", "V.O.C. Park, Coimbatore", 11.0016, 76.9678, isGround = true),
    SearchLocationItem("PSG Tech Ground", "Peelamedu, Coimbatore", 11.0252, 77.0026, isGround = true),
    SearchLocationItem("Sri Krishna College Ground", "Kuniyamuthur, Coimbatore", 10.9385, 76.9554, isGround = true),
    SearchLocationItem("Gandhipuram", "Coimbatore, Tamil Nadu", 11.0183, 76.9644),
    SearchLocationItem("RS Puram", "Coimbatore, Tamil Nadu", 11.0088, 76.9489),
    SearchLocationItem("Saravanampatti Turf", "Saravanampatti, Coimbatore", 11.0805, 76.9960, isGround = true),

    // Salem & Tiruppur region
    SearchLocationItem("Salem City", "Salem District, Tamil Nadu", 11.6643, 78.1460),
    SearchLocationItem("Gandhi Stadium", "Hasthampatti, Salem", 11.6643, 78.1460, isGround = true),
    SearchLocationItem("Salem Cricket Foundation Ground", "Valapadi, Salem", 11.6428, 78.4116, isGround = true),
    SearchLocationItem("Tiruppur City", "Tiruppur District, Tamil Nadu", 11.1085, 77.3411),
    SearchLocationItem("Chikkanna Govt College Ground", "Tiruppur, Tamil Nadu", 11.1147, 77.3524, isGround = true),
    SearchLocationItem("Karur City", "Karur District, Tamil Nadu", 10.9601, 78.0766),
    SearchLocationItem("Dindigul Sports Stadium", "Dindigul, Tamil Nadu", 10.3673, 77.9803, isGround = true),

    // Major Tamil Nadu Hubs
    SearchLocationItem("Chennai City", "Tamil Nadu", 13.0827, 80.2707),
    SearchLocationItem("Marina Cricket Ground", "Marina Beach, Chennai", 13.0500, 80.2824, isGround = true),
    SearchLocationItem("Chepauk Stadium (M.A. Chidambaram)", "Chepauk, Chennai", 13.0628, 80.2792, isGround = true),
    SearchLocationItem("Madurai City", "Madurai District, Tamil Nadu", 9.9252, 78.1198),
    SearchLocationItem("Race Course Stadium", "Madurai, Tamil Nadu", 9.9272, 78.1408, isGround = true),
    SearchLocationItem("Tiruchirappalli (Trichy)", "Tamil Nadu", 10.7905, 78.7047)
)

val QUICK_GROUNDS = listOf(
    SearchLocationItem("Pallipalayam Ground", "Pallipalayam", 11.3653, 77.7478, isGround = true),
    SearchLocationItem("Kongu Engg College", "Perundurai", 11.2742, 77.6070, isGround = true),
    SearchLocationItem("VOC Park Ground", "Erode", 11.3398, 77.7289, isGround = true),
    SearchLocationItem("Nehru Stadium", "Coimbatore", 11.0016, 76.9678, isGround = true),
    SearchLocationItem("Gandhi Stadium", "Salem", 11.6643, 78.1460, isGround = true),
    SearchLocationItem("Marina Cricket Ground", "Chennai", 13.0500, 80.2824, isGround = true)
)

/**
 * Interactive Ground Location Picker Dialog.
 *
 * Allows tournament organizers to:
 * 1. Search for ANY city, town, street, village, landmark or cricket ground remotely without GPS restrictions.
 * 2. Autocomplete search results with instant selection.
 * 3. Pan and zoom the map to manually align the pin with exact playing fields.
 * 4. Draggable pin and center-aligned pin updates coordinates when map stops moving.
 * 5. Uses GPS ONLY on demand when user clicks the GPS location button.
 * 6. Confirm and save selected ground coordinates and address.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapLocationPickerDialog(
    initialLatitude: Double? = null,
    initialLongitude: Double? = null,
    initialVenueName: String = "",
    initialAddress: String = "",
    onDismissRequest: () -> Unit,
    onLocationConfirmed: (latitude: Double, longitude: Double, formattedAddress: String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Default coordinates: Kongu Engineering College / Erode sports region if none provided
    val defaultLat = 11.2742
    val defaultLng = 77.6070

    var currentLat by remember { mutableDoubleStateOf(initialLatitude ?: defaultLat) }
    var currentLng by remember { mutableDoubleStateOf(initialLongitude ?: defaultLng) }
    var resolvedAddress by remember { mutableStateOf(initialAddress.ifBlank { initialVenueName }) }
    var searchQuery by remember { mutableStateOf(initialVenueName) }
    var isSearching by remember { mutableStateOf(false) }
    var isReverseGeocoding by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isSatelliteView by remember { mutableStateOf(false) }

    // Autocomplete state
    var suggestions by remember { mutableStateOf<List<SearchLocationItem>>(emptyList()) }
    var showSuggestions by remember { mutableStateOf(false) }
    var searchJob by remember { mutableStateOf<Job?>(null) }

    // Reverse Geocoding helper using Nominatim with Geocoder fallback
    fun reverseGeocode(lat: Double, lng: Double) {
        coroutineScope.launch {
            isReverseGeocoding = true
            withContext(Dispatchers.IO) {
                var fullAddress: String? = null

                // 1. Try Nominatim reverse geocode
                try {
                    val url = URL(String.format(Locale.US, "https://nominatim.openstreetmap.org/reverse?format=json&lat=%.6f&lon=%.6f&zoom=18&addressdetails=1", lat, lng))
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "GET"
                    conn.setRequestProperty("User-Agent", "SportsArena-Android-App/1.0")
                    conn.connectTimeout = 3000
                    conn.readTimeout = 3000
                    conn.connect()

                    if (conn.responseCode == 200) {
                        val reader = BufferedReader(InputStreamReader(conn.inputStream))
                        val sb = StringBuilder()
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            sb.append(line)
                        }
                        reader.close()

                        val obj = JSONObject(sb.toString())
                        val addressObj = obj.optJSONObject("address")
                        if (addressObj != null) {
                            val parts = mutableListOf<String>()
                            listOf("road", "suburb", "neighbourhood", "village", "town", "city", "county", "state").forEach { key ->
                                val value = addressObj.optString(key)
                                if (value.isNotBlank() && !parts.contains(value)) {
                                    parts.add(value)
                                }
                            }
                            if (parts.isNotEmpty()) {
                                fullAddress = parts.joinToString(", ")
                            }
                        }
                        if (fullAddress.isNullOrBlank()) {
                            val displayName = obj.optString("display_name")
                            if (displayName.isNotBlank()) {
                                fullAddress = displayName.split(",").take(4).joinToString(", ").trim()
                            }
                        }
                    }
                } catch (_: Exception) {}

                // 2. Android Geocoder fallback
                if (fullAddress.isNullOrBlank()) {
                    try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(lat, lng, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val addr = addresses[0]
                            val parts = mutableListOf<String>()
                            if (!addr.featureName.isNullOrBlank() && addr.featureName != addr.subThoroughfare) {
                                parts.add(addr.featureName)
                            }
                            if (!addr.thoroughfare.isNullOrBlank()) parts.add(addr.thoroughfare)
                            if (!addr.subLocality.isNullOrBlank()) parts.add(addr.subLocality)
                            if (!addr.locality.isNullOrBlank()) parts.add(addr.locality)
                            else if (!addr.subAdminArea.isNullOrBlank()) parts.add(addr.subAdminArea)
                            if (!addr.adminArea.isNullOrBlank()) parts.add(addr.adminArea)
                            val combined = if (parts.isNotEmpty()) parts.distinct().joinToString(", ") else addr.getAddressLine(0)
                            if (!combined.isNullOrBlank()) fullAddress = combined
                        }
                    } catch (_: Exception) {}
                }

                // 3. Proximity check with known database
                if (fullAddress.isNullOrBlank()) {
                    val nearest = ALL_KNOWN_PLACES.minByOrNull {
                        val dLat = it.latitude - lat
                        val dLng = it.longitude - lng
                        dLat * dLat + dLng * dLng
                    }
                    if (nearest != null) {
                        val distSq = (nearest.latitude - lat) * (nearest.latitude - lat) + (nearest.longitude - lng) * (nearest.longitude - lng)
                        if (distSq < 0.005) { // very close ~ <5km
                            fullAddress = "${nearest.title}, ${nearest.subtitle}"
                        }
                    }
                }

                // 4. Default coordinates string if nothing else available
                if (fullAddress.isNullOrBlank()) {
                    fullAddress = String.format(Locale.US, "Match Ground (%.5f, %.5f)", lat, lng)
                }

                withContext(Dispatchers.Main) {
                    isReverseGeocoding = false
                    if (!fullAddress.isNullOrBlank()) {
                        resolvedAddress = fullAddress!!
                    }
                }
            }
        }
    }

    // Function to move map camera and pin to target coordinates
    fun moveToCoordinates(lat: Double, lng: Double, updateAddress: Boolean = true) {
        currentLat = lat
        currentLng = lng
        showSuggestions = false
        webViewRef?.evaluateJavascript("javascript:setMapLocation($lat, $lng, true);", null)
        if (updateAddress) {
            reverseGeocode(lat, lng)
        }
    }

    // Function to fetch device GPS location (ONLY called when user clicks the GPS FAB)
    fun fetchGpsLocation() {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (locationManager == null) {
                Toast.makeText(context, "Location service not available", Toast.LENGTH_SHORT).show()
                return
            }

            val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

            if (!hasFine && !hasCoarse) {
                return
            }

            var bestLocation: Location? = null
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                bestLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            }
            if (bestLocation == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                bestLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }
            if (bestLocation == null && locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
                bestLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
            }

            if (bestLocation != null) {
                moveToCoordinates(bestLocation.latitude, bestLocation.longitude, updateAddress = true)
                Toast.makeText(context, "Centered on your current GPS location", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Could not acquire current GPS location. You can search or position the pin manually.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Could not get location: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Runtime Permission Launcher for GPS FAB
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            fetchGpsLocation()
        } else {
            Toast.makeText(
                context,
                "Permission denied. You can search any location or drag the pin anywhere on the map.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Search query helper executing remote and local searches
    fun searchPlace(query: String) {
        val clean = query.trim()
        if (clean.isBlank()) return
        focusManager.clearFocus()
        showSuggestions = false
        isSearching = true

        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                var foundLocation: SearchLocationItem? = null

                // 1. Check local catalog first for immediate match
                val localMatch = ALL_KNOWN_PLACES.find {
                    it.title.contains(clean, ignoreCase = true) ||
                            it.subtitle.contains(clean, ignoreCase = true)
                }
                if (localMatch != null) {
                    foundLocation = localMatch
                }

                // 2. Query OpenStreetMap Nominatim for real-time worldwide places, streets, and fields
                if (foundLocation == null) {
                    try {
                        val encoded = URLEncoder.encode(clean, "UTF-8")
                        val url = URL("https://nominatim.openstreetmap.org/search?format=json&q=$encoded&addressdetails=1&limit=5")
                        val conn = url.openConnection() as HttpURLConnection
                        conn.requestMethod = "GET"
                        conn.setRequestProperty("User-Agent", "SportsArena-Android-App/1.0")
                        conn.connectTimeout = 3000
                        conn.readTimeout = 3000
                        conn.connect()

                        if (conn.responseCode == 200) {
                            val reader = BufferedReader(InputStreamReader(conn.inputStream))
                            val sb = StringBuilder()
                            var line: String?
                            while (reader.readLine().also { line = it } != null) {
                                sb.append(line)
                            }
                            reader.close()

                            val jsonArray = JSONArray(sb.toString())
                            if (jsonArray.length() > 0) {
                                val obj = jsonArray.getJSONObject(0)
                                val lat = obj.optDouble("lat", Double.NaN)
                                val lon = obj.optDouble("lon", Double.NaN)
                                val displayName = obj.optString("display_name", "")
                                if (!lat.isNaN() && !lon.isNaN()) {
                                    val parts = displayName.split(",")
                                    val title = parts.firstOrNull()?.trim() ?: clean
                                    val subtitle = parts.drop(1).take(3).joinToString(", ").trim()
                                    foundLocation = SearchLocationItem(title, subtitle.ifBlank { displayName }, lat, lon)
                                }
                            }
                        }
                    } catch (_: Exception) {}
                }

                // 3. Android Geocoder fallback
                if (foundLocation == null) {
                    try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        @Suppress("DEPRECATION")
                        val results = geocoder.getFromLocationName(clean, 3)
                        if (!results.isNullOrEmpty()) {
                            val top = results[0]
                            foundLocation = SearchLocationItem(
                                title = top.featureName ?: clean,
                                subtitle = listOfNotNull(top.subLocality, top.locality, top.adminArea).joinToString(", "),
                                latitude = top.latitude,
                                longitude = top.longitude
                            )
                        }
                    } catch (_: Exception) {}
                }

                withContext(Dispatchers.Main) {
                    isSearching = false
                    if (foundLocation != null) {
                        moveToCoordinates(foundLocation.latitude, foundLocation.longitude, updateAddress = true)
                        Toast.makeText(context, "📍 Moved to: ${foundLocation.title}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(
                            context,
                            "No places found for '$clean'. You can zoom and place the pin anywhere manually on the map.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    // Dynamic Autocomplete logic as user types
    LaunchedEffect(searchQuery) {
        val q = searchQuery.trim()
        if (q.length >= 2) {
            // Instant local filter
            val filtered = ALL_KNOWN_PLACES.filter {
                it.title.contains(q, ignoreCase = true) || it.subtitle.contains(q, ignoreCase = true)
            }.take(5)

            suggestions = filtered
            showSuggestions = filtered.isNotEmpty()

            // Debounced remote search for streets and unlisted grounds
            searchJob?.cancel()
            if (q.length >= 3) {
                searchJob = coroutineScope.launch {
                    delay(350)
                    withContext(Dispatchers.IO) {
                        try {
                            val encoded = URLEncoder.encode(q, "UTF-8")
                            val url = URL("https://nominatim.openstreetmap.org/search?format=json&q=$encoded&addressdetails=1&limit=4")
                            val conn = url.openConnection() as HttpURLConnection
                            conn.requestMethod = "GET"
                            conn.setRequestProperty("User-Agent", "SportsArena-Android-App/1.0")
                            conn.connectTimeout = 2500
                            conn.readTimeout = 2500
                            conn.connect()

                            if (conn.responseCode == 200) {
                                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                                val sb = StringBuilder()
                                var line: String?
                                while (reader.readLine().also { line = it } != null) {
                                    sb.append(line)
                                }
                                reader.close()

                                val jsonArray = JSONArray(sb.toString())
                                val remoteResults = mutableListOf<SearchLocationItem>()
                                for (i in 0 until jsonArray.length()) {
                                    val obj = jsonArray.getJSONObject(i)
                                    val lat = obj.optDouble("lat", Double.NaN)
                                    val lon = obj.optDouble("lon", Double.NaN)
                                    val displayName = obj.optString("display_name", "")
                                    if (!lat.isNaN() && !lon.isNaN() && displayName.isNotBlank()) {
                                        val parts = displayName.split(",")
                                        val title = parts.firstOrNull()?.trim() ?: q
                                        val subtitle = parts.drop(1).take(2).joinToString(", ").trim()
                                        val isGround = displayName.contains("ground", ignoreCase = true) ||
                                                displayName.contains("stadium", ignoreCase = true) ||
                                                displayName.contains("college", ignoreCase = true)
                                        remoteResults.add(SearchLocationItem(title, subtitle.ifBlank { displayName }, lat, lon, isGround))
                                    }
                                }

                                withContext(Dispatchers.Main) {
                                    val combined = (filtered + remoteResults).distinctBy { "${it.latitude},${it.longitude}" }
                                    suggestions = combined
                                    showSuggestions = combined.isNotEmpty()
                                }
                            }
                        } catch (_: Exception) {}
                    }
                }
            }
        } else {
            suggestions = emptyList()
            showSuggestions = false
        }
    }

    // Initial reverse geocode if no address yet
    LaunchedEffect(Unit) {
        if (resolvedAddress.isBlank()) {
            reverseGeocode(currentLat, currentLng)
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Interactive Map View with Leaflet & OpenStreetMap / Satellite layers
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("interactive_map_view"),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            @SuppressLint("SetJavaScriptEnabled")
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.cacheMode = WebSettings.LOAD_DEFAULT
                            settings.useWideViewPort = true
                            settings.loadWithOverviewMode = true

                            addJavascriptInterface(object {
                                @JavascriptInterface
                                fun onLocationUpdated(lat: Double, lng: Double) {
                                    post {
                                        currentLat = lat
                                        currentLng = lng
                                        reverseGeocode(lat, lng)
                                    }
                                }
                            }, "AndroidLocationBridge")

                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    view?.evaluateJavascript(
                                        "javascript:setMapLocation($currentLat, $currentLng, true);",
                                        null
                                    )
                                }
                            }

                            loadDataWithBaseURL(
                                "https://maps.sportmeet.local",
                                getMapHtml(currentLat, currentLng),
                                "text/html",
                                "UTF-8",
                                null
                            )
                            webViewRef = this
                        }
                    },
                    update = {
                        webViewRef = it
                    }
                )

                // Top Floating Control Bar: Back, Search, Autocomplete & Quick Ground Chips
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(12.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = onDismissRequest,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = {
                                        Text(
                                            "Search city, town, street, ground (e.g. Pallipalayam)...",
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("map_search_field"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        focusedBorderColor = SportBlue
                                    ),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(onSearch = { searchPlace(searchQuery) }),
                                    trailingIcon = {
                                        if (isSearching) {
                                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                        } else {
                                            IconButton(onClick = { searchPlace(searchQuery) }) {
                                                Icon(
                                                    imageVector = Icons.Default.Search,
                                                    contentDescription = "Search",
                                                    tint = SportBlue
                                                )
                                            }
                                        }
                                    }
                                )
                            }

                            // Autocomplete Suggestions Dropdown
                            if (showSuggestions && suggestions.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "SUGGESTED VENUES & LOCATIONS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SportBlue,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )

                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 200.dp)
                                ) {
                                    items(suggestions) { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    searchQuery = item.title
                                                    focusManager.clearFocus()
                                                    moveToCoordinates(item.latitude, item.longitude, updateAddress = true)
                                                }
                                                .padding(horizontal = 8.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (item.isGround) Icons.Outlined.Stadium else Icons.Outlined.Place,
                                                contentDescription = null,
                                                tint = if (item.isGround) SportGreen else SportRed,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = item.title,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = item.subtitle,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Quick Sports Ground Presets
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(QUICK_GROUNDS) { ground ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .clickable {
                                                searchQuery = ground.title
                                                focusManager.clearFocus()
                                                moveToCoordinates(ground.latitude, ground.longitude, updateAddress = true)
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "📍 ${ground.title}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Floating Action Buttons (GPS & Layer switch)
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // My Location GPS Button (Optional, only triggers when explicitly tapped)
                    FloatingActionButton(
                        onClick = {
                            val fineGranted = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                            val coarseGranted = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED

                            if (fineGranted || coarseGranted) {
                                fetchGpsLocation()
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = SportBlue,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(6.dp, CircleShape)
                            .testTag("my_location_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "My Location",
                            tint = SportBlue
                        )
                    }

                    // Map View Layer Switch (Street / Satellite)
                    FloatingActionButton(
                        onClick = {
                            isSatelliteView = !isSatelliteView
                            webViewRef?.evaluateJavascript(
                                "javascript:toggleMapLayer(${isSatelliteView});",
                                null
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(6.dp, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Toggle Layer"
                        )
                    }
                }

                // Bottom Location Details & Confirmation Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .testTag("location_confirm_card"),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Title and Instructions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(SportRed.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = null,
                                        tint = SportRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Exact Ground Location",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "Drag pin or pan map onto the playing field",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (isReverseGeocoding) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            }
                        }

                        // Resolved Address & Coordinates Display
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .padding(10.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = resolvedAddress.ifBlank { "Selected Tournament Ground" },
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SportBlue.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Lat: ${"%.6f".format(Locale.US, currentLat)}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SportBlue
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SportBlue.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Lng: ${"%.6f".format(Locale.US, currentLng)}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SportBlue
                                        )
                                    }
                                }
                            }
                        }

                        // Action Buttons: Cancel & Confirm
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismissRequest,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Cancel", fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = {
                                    // Validate coordinates
                                    if (currentLat < -90.0 || currentLat > 90.0 || currentLng < -180.0 || currentLng > 180.0) {
                                        Toast.makeText(context, "Invalid coordinates detected", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    onLocationConfirmed(currentLat, currentLng, resolvedAddress)
                                },
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(48.dp)
                                    .testTag("confirm_location_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SportBlue)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Confirm Location",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Builds HTML for interactive Leaflet map with draggable pin & satellite/street layers.
 * Pin stays fixed at the center when user pans, and updates coordinates on moveend and click.
 */
private fun getMapHtml(lat: Double, lng: Double): String {
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                html, body, #map {
                    height: 100%;
                    width: 100%;
                    margin: 0;
                    padding: 0;
                    background: #111827;
                }
                .custom-pin {
                    text-align: center;
                }
                .pin-wrapper {
                    position: relative;
                    width: 38px;
                    height: 38px;
                }
                .pin-pulse {
                    width: 36px;
                    height: 36px;
                    background: #ef4444;
                    border: 3px solid #ffffff;
                    border-radius: 50% 50% 50% 0;
                    transform: rotate(-45deg);
                    box-shadow: 0 4px 12px rgba(0,0,0,0.5);
                    margin: 0 auto;
                }
                .pin-dot {
                    width: 12px;
                    height: 12px;
                    background: #ffffff;
                    border-radius: 50%;
                    position: absolute;
                    top: 12px;
                    left: 12px;
                }
                .pin-label {
                    position: absolute;
                    top: -24px;
                    left: 50%;
                    transform: translateX(-50%);
                    background: rgba(17, 24, 39, 0.9);
                    color: #ffffff;
                    font-size: 10px;
                    font-weight: bold;
                    padding: 2px 6px;
                    border-radius: 4px;
                    white-space: nowrap;
                    border: 1px solid rgba(255,255,255,0.25);
                    pointer-events: none;
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map = L.map('map', {
                    zoomControl: false,
                    attributionControl: false
                }).setView([$lat, $lng], 16);

                var streetLayer = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    maxZoom: 19
                }).addTo(map);

                var satelliteLayer = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
                    maxZoom: 19
                });

                var pinIcon = L.divIcon({
                    className: 'custom-pin',
                    html: '<div class="pin-wrapper"><div class="pin-label">MATCH VENUE</div><div class="pin-pulse"><div class="pin-dot"></div></div></div>',
                    iconSize: [38, 38],
                    iconAnchor: [19, 38]
                });

                var marker = L.marker([$lat, $lng], {
                    draggable: true,
                    icon: pinIcon
                }).addTo(map);

                function notifyLocation(lat, lng) {
                    if (window.AndroidLocationBridge && window.AndroidLocationBridge.onLocationUpdated) {
                        window.AndroidLocationBridge.onLocationUpdated(lat, lng);
                    }
                }

                marker.on('dragend', function(e) {
                    var pos = marker.getLatLng();
                    notifyLocation(pos.lat, pos.lng);
                });

                map.on('click', function(e) {
                    marker.setLatLng(e.latlng);
                    notifyLocation(e.latlng.lat, e.latlng.lng);
                });

                map.on('move', function() {
                    var center = map.getCenter();
                    marker.setLatLng(center);
                });

                map.on('moveend', function() {
                    var center = map.getCenter();
                    marker.setLatLng(center);
                    notifyLocation(center.lat, center.lng);
                });

                window.setMapLocation = function(newLat, newLng, animate) {
                    if (animate) {
                        map.flyTo([newLat, newLng], 17);
                    } else {
                        map.setView([newLat, newLng], 17);
                    }
                    marker.setLatLng([newLat, newLng]);
                };

                window.toggleMapLayer = function(isSatellite) {
                    if (isSatellite) {
                        map.removeLayer(streetLayer);
                        map.addLayer(satelliteLayer);
                    } else {
                        map.removeLayer(satelliteLayer);
                        map.addLayer(streetLayer);
                    }
                };
            </script>
        </body>
        </html>
    """.trimIndent()
}
