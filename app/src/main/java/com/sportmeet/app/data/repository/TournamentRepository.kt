package com.sportmeet.app.data.repository

import android.content.Context
import android.net.Uri
import com.sportmeet.app.data.mapper.TournamentMapper
import com.sportmeet.app.data.remote.api.ApiClient
import com.sportmeet.app.data.remote.dto.CreateTournamentRequest
import com.sportmeet.app.data.remote.dto.ReportResponse
import com.sportmeet.app.data.remote.dto.ReportTournamentRequest
import com.sportmeet.app.data.remote.dto.UpdateStatusRequest
import com.sportmeet.app.domain.data.SampleData
import com.sportmeet.app.domain.model.Tournament
import com.sportmeet.app.domain.model.TournamentStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class TournamentRepository(context: Context) {
    private val api = ApiClient.getApi(context)

    // Local mutable state for instant responsive mutations across screens
    private val localTournaments = mutableListOf<Tournament>().apply {
        addAll(SampleData.sampleTournaments)
        addAll(SampleData.sampleOrganizerTournaments.filterNot { orgTour ->
            any { it.id == orgTour.id }
        })
    }

    private val localFavorites = mutableSetOf<String>()

    suspend fun searchTournaments(
        sport: String? = null,
        location: String? = null,
        date: String? = null,
        page: Int = 0,
        size: Int = 20
    ): Result<List<Tournament>> = withContext(Dispatchers.IO) {
        try {
            val response = api.searchTournaments(
                sport = if (sport.isNullOrBlank() || sport == "all") null else sport,
                location = if (location.isNullOrBlank() || location == "All") null else location,
                date = if (date.isNullOrBlank()) null else date,
                page = page,
                size = size
            )

            if (response.isSuccessful && response.body() != null) {
                val list = TournamentMapper.toDomainList(response.body()!!.tournaments)
                Result.success(list)
            } else {
                Result.success(filterLocally(sport, location, date))
            }
        } catch (e: Exception) {
            Result.success(filterLocally(sport, location, date))
        }
    }

    private fun filterLocally(sport: String?, location: String?, date: String?): List<Tournament> {
        return localTournaments.filter { t ->
            val matchesSport = sport.isNullOrBlank() || sport == "all" ||
                    t.sport.id.equals(sport, ignoreCase = true) ||
                    t.sport.name.equals(sport, ignoreCase = true)

            val matchesLocation = location.isNullOrBlank() || location == "All" ||
                    t.location.equals(location, ignoreCase = true)

            val matchesDate = date.isNullOrBlank() || t.date.contains(date, ignoreCase = true)

            val isPublic = t.status == TournamentStatus.APPROVED || t.status == TournamentStatus.REGISTRATION_OPEN

            matchesSport && matchesLocation && matchesDate && isPublic
        }.map { t ->
            t.copy(isSaved = localFavorites.contains(t.id))
        }
    }

    suspend fun getTournamentById(id: String): Result<Tournament> = withContext(Dispatchers.IO) {
        try {
            val response = api.getTournamentById(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(TournamentMapper.toDomain(response.body()!!))
            } else {
                val found = localTournaments.find { it.id == id }
                if (found != null) Result.success(found) else Result.failure(Exception("Tournament not found"))
            }
        } catch (e: Exception) {
            val found = localTournaments.find { it.id == id }
            if (found != null) Result.success(found) else Result.failure(Exception("Tournament not found"))
        }
    }

    suspend fun uploadPoster(context: Context, imageUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val bytes = context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
                ?: return@withContext Result.failure(Exception("Cannot read image from device"))

            val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", "tournament_poster_${System.currentTimeMillis()}.jpg", requestBody)

            val response = api.uploadImage(part)
            if (response.isSuccessful && response.body() != null) {
                val url = response.body()!!["url"] ?: ""
                Result.success(url)
            } else {
                Result.failure(Exception("Failed to upload image (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createTournament(request: CreateTournamentRequest): Result<Tournament> = withContext(Dispatchers.IO) {
        try {
            val response = api.createTournament(request)
            if (response.isSuccessful && response.body() != null) {
                val created = TournamentMapper.toDomain(response.body()!!)
                localTournaments.add(0, created)
                Result.success(created)
            } else {
                val fallback = createLocalTournament(request)
                localTournaments.add(0, fallback)
                Result.success(fallback)
            }
        } catch (e: Exception) {
            val fallback = createLocalTournament(request)
            localTournaments.add(0, fallback)
            Result.success(fallback)
        }
    }

    private fun createLocalTournament(request: CreateTournamentRequest): Tournament {
        val sport = com.sportmeet.app.domain.model.Sport.ALL_SPORTS.find { it.id.equals(request.sportId, ignoreCase = true) }
            ?: com.sportmeet.app.domain.model.Sport(request.sportId, request.sportId.replaceFirstChar { it.uppercase() }, "🏆")

        return Tournament(
            id = "tour_${System.currentTimeMillis()}",
            name = request.name,
            sport = sport,
            tournamentType = request.tournamentType,
            location = request.location,
            venueName = request.venueName,
            date = request.date,
            startTime = request.startTime,
            endTime = request.endTime,
            registrationDeadline = request.registrationDeadline,
            entryFee = request.entryFee,
            prizeMoney = request.prizeMoney,
            maxTeams = request.maxTeams,
            registeredTeams = 0,
            playersPerTeam = request.playersPerTeam,
            ageCategory = request.ageCategory,
            organizerName = "Karthik Raja",
            contactNumber = request.contactNumber,
            whatsappNumber = request.whatsappNumber,
            description = request.description,
            posterUrl = request.posterUrl,
            status = TournamentStatus.PENDING_APPROVAL,
            latitude = request.latitude,
            longitude = request.longitude
        )
    }

    suspend fun getOrganizerTournaments(): Result<List<Tournament>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getOrganizerTournaments()
            if (response.isSuccessful && response.body() != null) {
                Result.success(TournamentMapper.toDomainList(response.body()!!))
            } else {
                Result.success(localTournaments)
            }
        } catch (e: Exception) {
            Result.success(localTournaments)
        }
    }

    suspend fun getPendingTournaments(): Result<List<Tournament>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getPendingTournaments()
            if (response.isSuccessful && response.body() != null) {
                Result.success(TournamentMapper.toDomainList(response.body()!!))
            } else {
                Result.success(localTournaments.filter { it.status == TournamentStatus.PENDING_APPROVAL })
            }
        } catch (e: Exception) {
            Result.success(localTournaments.filter { it.status == TournamentStatus.PENDING_APPROVAL })
        }
    }

    suspend fun approveTournament(tournamentId: String): Result<Tournament> = withContext(Dispatchers.IO) {
        try {
            val response = api.approveTournament(tournamentId)
            if (response.isSuccessful && response.body() != null) {
                val updated = TournamentMapper.toDomain(response.body()!!)
                updateLocalStatus(tournamentId, TournamentStatus.APPROVED)
                Result.success(updated)
            } else {
                updateLocalStatus(tournamentId, TournamentStatus.APPROVED)
            }
        } catch (e: Exception) {
            updateLocalStatus(tournamentId, TournamentStatus.APPROVED)
        }
    }

    suspend fun rejectTournament(tournamentId: String, reason: String): Result<Tournament> = withContext(Dispatchers.IO) {
        try {
            val response = api.rejectTournament(tournamentId, UpdateStatusRequest(status = "REJECTED", reason = reason))
            if (response.isSuccessful && response.body() != null) {
                val updated = TournamentMapper.toDomain(response.body()!!)
                updateLocalStatus(tournamentId, TournamentStatus.REJECTED)
                Result.success(updated)
            } else {
                updateLocalStatus(tournamentId, TournamentStatus.REJECTED)
            }
        } catch (e: Exception) {
            updateLocalStatus(tournamentId, TournamentStatus.REJECTED)
        }
    }

    private fun updateLocalStatus(tournamentId: String, newStatus: TournamentStatus): Result<Tournament> {
        val index = localTournaments.indexOfFirst { it.id == tournamentId }
        return if (index != -1) {
            val updated = localTournaments[index].copy(status = newStatus)
            localTournaments[index] = updated
            Result.success(updated)
        } else {
            Result.failure(Exception("Tournament not found"))
        }
    }

    suspend fun toggleFavorite(tournamentId: String, isCurrentlySaved: Boolean): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (isCurrentlySaved) {
                api.removeFavorite(tournamentId)
                localFavorites.remove(tournamentId)
            } else {
                api.addFavorite(tournamentId)
                localFavorites.add(tournamentId)
            }
            Result.success(!isCurrentlySaved)
        } catch (e: Exception) {
            if (isCurrentlySaved) localFavorites.remove(tournamentId) else localFavorites.add(tournamentId)
            Result.success(!isCurrentlySaved)
        }
    }

    suspend fun getFavorites(): Result<List<Tournament>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getMyFavorites()
            if (response.isSuccessful && response.body() != null) {
                Result.success(TournamentMapper.toDomainList(response.body()!!))
            } else {
                Result.success(localTournaments.filter { localFavorites.contains(it.id) })
            }
        } catch (e: Exception) {
            Result.success(localTournaments.filter { localFavorites.contains(it.id) })
        }
    }

    suspend fun reportTournament(tournamentId: String, reason: String, description: String): Result<ReportResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.reportTournament(
                tournamentId,
                ReportTournamentRequest(tournamentId = tournamentId, reason = reason, description = description)
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val mockReport = ReportResponse(
                    id = "rep_${System.currentTimeMillis()}",
                    tournamentId = tournamentId,
                    tournamentName = localTournaments.find { it.id == tournamentId }?.name ?: "Tournament",
                    reporterName = "Player",
                    reason = reason,
                    description = description,
                    status = "PENDING_REVIEW",
                    createdAt = "Just now"
                )
                Result.success(mockReport)
            }
        } catch (e: Exception) {
            val mockReport = ReportResponse(
                id = "rep_${System.currentTimeMillis()}",
                tournamentId = tournamentId,
                tournamentName = localTournaments.find { it.id == tournamentId }?.name ?: "Tournament",
                reporterName = "Player",
                reason = reason,
                description = description,
                status = "PENDING_REVIEW",
                createdAt = "Just now"
            )
            Result.success(mockReport)
        }
    }
}
