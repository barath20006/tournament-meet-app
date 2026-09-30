package com.sportmeet.app.data.repository

import android.content.Context
import com.sportmeet.app.data.remote.api.ApiClient
import com.sportmeet.app.data.remote.dto.RegisterTeamRequest
import com.sportmeet.app.data.remote.dto.RegistrationResponse
import com.sportmeet.app.data.remote.dto.UpdateRegistrationStatusRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RegistrationRepository(context: Context) {
    private val api = ApiClient.getApi(context)

    // In-memory cache of registrations
    private val localRegistrations = mutableListOf(
        RegistrationResponse(
            id = "reg_001",
            tournamentId = "tour_001",
            tournamentName = "Erode Cricket Cup 2026",
            teamName = "Erode Strikers XI",
            captainName = "Arun Kumar",
            phone = "+91 98420 12345",
            email = "arun@sportmeet.com",
            numberOfPlayers = 11,
            notes = "Leather ball cricket team ready.",
            status = "APPROVED",
            registeredAt = "2026-09-25 10:30 AM"
        ),
        RegistrationResponse(
            id = "reg_002",
            tournamentId = "tour_001",
            tournamentName = "Erode Cricket Cup 2026",
            teamName = "Kongu Titans",
            captainName = "Vignesh S.",
            phone = "+91 98421 99887",
            email = "vignesh@sportmeet.com",
            numberOfPlayers = 11,
            notes = "College champion squad.",
            status = "PENDING",
            registeredAt = "2026-09-26 03:15 PM"
        )
    )

    suspend fun registerTeam(
        tournamentId: String,
        tournamentName: String,
        teamName: String,
        captainName: String,
        phone: String,
        email: String,
        numberOfPlayers: Int,
        notes: String
    ): Result<RegistrationResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.registerForTournament(
                tournamentId = tournamentId,
                request = RegisterTeamRequest(
                    teamName = teamName,
                    captainName = captainName,
                    phone = phone,
                    email = email,
                    numberOfPlayers = numberOfPlayers,
                    notes = notes
                )
            )

            if (response.isSuccessful && response.body() != null) {
                val reg = response.body()!!
                localRegistrations.add(0, reg)
                Result.success(reg)
            } else {
                val mock = RegistrationResponse(
                    id = "reg_${System.currentTimeMillis()}",
                    tournamentId = tournamentId,
                    tournamentName = tournamentName,
                    teamName = teamName,
                    captainName = captainName,
                    phone = phone,
                    email = email,
                    numberOfPlayers = numberOfPlayers,
                    notes = notes,
                    status = "PENDING",
                    registeredAt = "Just now"
                )
                localRegistrations.add(0, mock)
                Result.success(mock)
            }
        } catch (e: Exception) {
            val mock = RegistrationResponse(
                id = "reg_${System.currentTimeMillis()}",
                tournamentId = tournamentId,
                tournamentName = tournamentName,
                teamName = teamName,
                captainName = captainName,
                phone = phone,
                email = email,
                numberOfPlayers = numberOfPlayers,
                notes = notes,
                status = "PENDING",
                registeredAt = "Just now"
            )
            localRegistrations.add(0, mock)
            Result.success(mock)
        }
    }

    suspend fun getMyRegistrations(): Result<List<RegistrationResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getMyRegistrations()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.success(localRegistrations)
            }
        } catch (e: Exception) {
            Result.success(localRegistrations)
        }
    }

    suspend fun getTournamentRegistrations(tournamentId: String): Result<List<RegistrationResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getTournamentRegistrations(tournamentId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.success(localRegistrations.filter { it.tournamentId == tournamentId })
            }
        } catch (e: Exception) {
            Result.success(localRegistrations.filter { it.tournamentId == tournamentId })
        }
    }

    suspend fun updateRegistrationStatus(registrationId: String, newStatus: String): Result<RegistrationResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateRegistrationStatus(
                registrationId = registrationId,
                request = UpdateRegistrationStatusRequest(status = newStatus)
            )
            if (response.isSuccessful && response.body() != null) {
                val updated = response.body()!!
                updateLocalStatus(registrationId, newStatus)
                Result.success(updated)
            } else {
                updateLocalStatus(registrationId, newStatus)
            }
        } catch (e: Exception) {
            updateLocalStatus(registrationId, newStatus)
        }
    }

    private fun updateLocalStatus(id: String, status: String): Result<RegistrationResponse> {
        val index = localRegistrations.indexOfFirst { it.id == id }
        return if (index != -1) {
            val updated = localRegistrations[index].copy(status = status)
            localRegistrations[index] = updated
            Result.success(updated)
        } else {
            Result.failure(Exception("Registration not found"))
        }
    }
}
