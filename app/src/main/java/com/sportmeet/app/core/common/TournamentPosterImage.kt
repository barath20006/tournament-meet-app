package com.sportmeet.app.core.common

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sportmeet.app.core.theme.SportBlue
import com.sportmeet.app.core.theme.SportGold
import com.sportmeet.app.data.remote.api.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

private val imageBitmapCache = mutableMapOf<String, Bitmap>()

@Composable
fun TournamentPosterImage(
    posterUrl: String? = null,
    posterUri: Uri? = null,
    sportEmoji: String = "🏆",
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    var bitmap by remember(posterUrl, posterUri) { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember(posterUrl, posterUri) { mutableStateOf(false) }

    LaunchedEffect(posterUrl, posterUri) {
        val targetUri = posterUri ?: if (!posterUrl.isNullOrBlank() && (posterUrl.startsWith("content://") || posterUrl.startsWith("file://"))) {
            try { Uri.parse(posterUrl) } catch (_: Exception) { null }
        } else null

        if (targetUri != null) {
            isLoading = true
            withContext(Dispatchers.IO) {
                try {
                    context.contentResolver.openInputStream(targetUri)?.use { input ->
                        val options = BitmapFactory.Options().apply {
                            inSampleSize = 2 // downsample for performance and memory
                        }
                        bitmap = BitmapFactory.decodeStream(input, null, options)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isLoading = false
                }
            }
        } else if (!posterUrl.isNullOrBlank()) {
            val resolvedUrl = if (posterUrl.startsWith("http://") || posterUrl.startsWith("https://")) {
                posterUrl
            } else {
                val base = ApiClient.getBaseUrl().removeSuffix("/")
                val path = if (posterUrl.startsWith("/")) posterUrl else "/$posterUrl"
                "$base$path"
            }

            val cached = imageBitmapCache[resolvedUrl]
            if (cached != null) {
                bitmap = cached
            } else {
                isLoading = true
                withContext(Dispatchers.IO) {
                    try {
                        val connection = URL(resolvedUrl).openConnection()
                        connection.connectTimeout = 6000
                        connection.readTimeout = 8000
                        connection.getInputStream().use { input ->
                            val decoded = BitmapFactory.decodeStream(input)
                            if (decoded != null) {
                                imageBitmapCache[resolvedUrl] = decoded
                                bitmap = decoded
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        isLoading = false
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        SportBlue.copy(alpha = 0.25f),
                        SportGold.copy(alpha = 0.20f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        val currentBitmap = bitmap
        if (currentBitmap != null) {
            Image(
                bitmap = currentBitmap.asImageBitmap(),
                contentDescription = "Tournament Poster",
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale
            )
        } else if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = SportBlue,
                strokeWidth = 2.dp
            )
        } else {
            // Placeholder badge when no image is uploaded
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = sportEmoji,
                    fontSize = 28.sp
                )
            }
        }
    }
}
