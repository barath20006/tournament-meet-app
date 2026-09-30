package com.sportmeet.app.data.remote.dto

data class CreateTournamentRequest(
    val name: String,
    val sportId: String,
    val tournamentType: String = "Knockout",
    val location: String,
    val venueName: String,
    val address: String = "",
    val date: String,
    val startTime: String,
    val endTime: String = "",
    val registrationDeadline: String = "",
    val entryFee: Double = 0.0,
    val prizeMoney: Double = 0.0,
    val maxTeams: Int = 16,
    val playersPerTeam: Int = 11,
    val ageCategory: String = "Open",
    val contactNumber: String,
    val whatsappNumber: String,
    val description: String,
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
    val tournamentType: String = "Knockout",
    val location: String,
    val venueName: String,
    val address: String = "",
    val date: String,
    val startTime: String,
    val endTime: String = "",
    val registrationDeadline: String = "",
    val entryFee: Double = 0.0,
    val prizeMoney: Double = 0.0,
    val maxTeams: Int = 16,
    val registeredTeams: Int = 0,
    val playersPerTeam: Int = 11,
    val ageCategory: String = "Open",
    val organizerId: String = "",
    val organizerName: String,
    val contactNumber: String,
    val whatsappNumber: String,
    val description: String,
    val posterUrl: String? = null,
    val status: String = "APPROVED",
    val rejectionReason: String? = null,
    val isSaved: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class UpdateStatusRequest(
    val status: String,
    val reason: String? = null
)

data class SearchTournamentsResponse(
    val tournaments: List<TournamentResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int
)
