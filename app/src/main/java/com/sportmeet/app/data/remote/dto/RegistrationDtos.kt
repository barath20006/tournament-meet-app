package com.sportmeet.app.data.remote.dto

data class RegisterTeamRequest(
    val teamName: String,
    val captainName: String,
    val phone: String,
    val email: String = "",
    val numberOfPlayers: Int = 11,
    val notes: String = ""
)

data class RegistrationResponse(
    val id: String,
    val tournamentId: String,
    val tournamentName: String,
    val teamName: String,
    val captainName: String,
    val phone: String,
    val email: String,
    val numberOfPlayers: Int,
    val notes: String,
    val status: String,
    val registeredAt: String
)

data class UpdateRegistrationStatusRequest(
    val status: String
)

data class ReportTournamentRequest(
    val tournamentId: String,
    val reason: String,
    val description: String
)

data class ReportResponse(
    val id: String,
    val tournamentId: String,
    val tournamentName: String,
    val reporterName: String,
    val reason: String,
    val description: String,
    val status: String,
    val createdAt: String
)
