package com.sportmeet.app.domain.model

data class Tournament(
    val id: String,
    val name: String,
    val sport: Sport,
    val tournamentType: String = "Knockout",
    val location: String,
    val venueName: String,
    val groundName: String = "",
    val address: String = "",
    val date: String,
    val startTime: String,
    val endTime: String = "",
    val registrationDeadline: String = "",
    val entryFee: Double,
    val prizeMoney: Double,
    val maxTeams: Int = 16,
    val registeredTeams: Int = 0,
    val playersPerTeam: Int = 11,
    val ageCategory: String = "Open",
    val organizerName: String,
    val contactNumber: String,
    val whatsappNumber: String,
    val description: String,
    val posterUrl: String? = null,
    val status: TournamentStatus = TournamentStatus.APPROVED,
    val isSaved: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    val formattedEntryFee: String
        get() = if (entryFee > 0) "₹${entryFee.toInt().toString().replace(Regex("(\\d)(?=(\\d{3})+$)"), "$1,")}" else "Free"

    val formattedPrizeMoney: String
        get() = if (prizeMoney > 0) "₹${prizeMoney.toInt().toString().replace(Regex("(\\d)(?=(\\d{3})+$)"), "$1,")}" else "Trophy Only"
}
