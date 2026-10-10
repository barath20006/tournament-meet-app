package com.sportmeet.backend.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank(message = "Name is required")
    val name: String,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "Phone number is required")
    val phone: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 6, message = "Password must be at least 6 characters")
    val password: String,

    @field:NotBlank(message = "Role is required")
    val role: String // PLAYER, ORGANIZER
)

data class LoginRequest(
    @field:NotBlank(message = "Email or phone is required")
    val emailOrPhone: String,

    @field:NotBlank(message = "Password is required")
    val password: String
)

data class AuthResponse(
    val token: String,
    val tokenType: String = "Bearer",
    val userId: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: String
)

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)

data class CreateTournamentRequest(
    @field:NotBlank(message = "Tournament name is required")
    val name: String,

    @field:NotBlank(message = "Sport ID is required")
    val sportId: String,

    val tournamentType: String = "Knockout",

    @field:NotBlank(message = "Location/City is required")
    val location: String,

    @field:NotBlank(message = "Venue name is required")
    val venueName: String,

    val groundName: String = "",

    val address: String = "",

    @field:NotBlank(message = "Tournament date is required")
    val date: String,

    @field:NotBlank(message = "Start time is required")
    val startTime: String,

    val endTime: String = "",
    val registrationDeadline: String = "",
    val entryFee: Double = 0.0,
    val prizeMoney: Double = 0.0,
    val maxTeams: Int = 16,
    val playersPerTeam: Int = 11,
    val ageCategory: String = "Open",

    @field:NotBlank(message = "Contact number is required")
    val contactNumber: String,

    val whatsappNumber: String = "",
    val description: String = "",
    val posterUrl: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class TournamentResponse(
    val id: String,
    val name: String,
    val sportId: String,
    val sportName: String,
    val sportEmoji: String,
    val tournamentType: String,
    val location: String,
    val venueName: String,
    val groundName: String = "",
    val address: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val registrationDeadline: String,
    val entryFee: Double,
    val prizeMoney: Double,
    val maxTeams: Int,
    val registeredTeams: Int,
    val playersPerTeam: Int,
    val ageCategory: String,
    val organizerId: String,
    val organizerName: String,
    val contactNumber: String,
    val whatsappNumber: String,
    val description: String,
    val posterUrl: String?,
    val status: String,
    val rejectionReason: String?,
    val isSaved: Boolean = false,
    val latitude: Double?,
    val longitude: Double?
)

data class SearchTournamentsResponse(
    val tournaments: List<TournamentResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int
)

data class UpdateStatusRequest(
    val status: String,
    val reason: String? = null
)

data class RegisterTeamRequest(
    @field:NotBlank(message = "Team name is required")
    val teamName: String,

    @field:NotBlank(message = "Captain name is required")
    val captainName: String,

    @field:NotBlank(message = "Contact phone is required")
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
    val description: String = ""
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
