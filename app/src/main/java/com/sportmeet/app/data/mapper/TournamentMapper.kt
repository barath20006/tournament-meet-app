package com.sportmeet.app.data.mapper

import com.sportmeet.app.data.remote.dto.TournamentResponse
import com.sportmeet.app.domain.model.Sport
import com.sportmeet.app.domain.model.Tournament
import com.sportmeet.app.domain.model.TournamentStatus

object TournamentMapper {
    fun toDomain(dto: TournamentResponse): Tournament {
        val sportId = dto.sportId.ifBlank { "cricket" }
        val sport = Sport.ALL_SPORTS.find { it.id.equals(sportId, ignoreCase = true) }
            ?: Sport(
                id = sportId,
                name = (dto.sportName ?: "").ifBlank { "Sport" },
                iconEmoji = (dto.sportEmoji ?: "").ifBlank { "🏅" },
                isPopular = true
            )

        val status = try {
            TournamentStatus.valueOf(dto.status ?: "APPROVED")
        } catch (_: Exception) {
            TournamentStatus.APPROVED
        }

        return Tournament(
            id = (dto.id ?: "").ifBlank { "tour_${System.currentTimeMillis()}" },
            name = (dto.name ?: "").ifBlank { "Grassroots Tournament" },
            sport = sport,
            tournamentType = (dto.tournamentType ?: "").ifBlank { "Knockout" },
            location = (dto.location ?: "").ifBlank { "Erode" },
            venueName = (dto.venueName ?: "").ifBlank { "Sports Ground" },
            groundName = dto.groundName ?: "",
            address = dto.address ?: "",
            date = (dto.date ?: "").ifBlank { "TBD" },
            startTime = (dto.startTime ?: "").ifBlank { "09:00 AM" },
            endTime = dto.endTime ?: "",
            registrationDeadline = dto.registrationDeadline ?: "",
            entryFee = dto.entryFee,
            prizeMoney = dto.prizeMoney,
            maxTeams = if (dto.maxTeams > 0) dto.maxTeams else 16,
            registeredTeams = dto.registeredTeams,
            playersPerTeam = if (dto.playersPerTeam > 0) dto.playersPerTeam else 11,
            ageCategory = (dto.ageCategory ?: "").ifBlank { "Open" },
            organizerName = (dto.organizerName ?: "").ifBlank { "Organizer" },
            contactNumber = dto.contactNumber ?: "",
            whatsappNumber = dto.whatsappNumber ?: "",
            description = dto.description ?: "",
            posterUrl = dto.posterUrl,
            status = status,
            isSaved = dto.isSaved,
            latitude = dto.latitude,
            longitude = dto.longitude
        )
    }

    fun toDomainList(dtos: List<TournamentResponse>): List<Tournament> {
        return dtos.map { toDomain(it) }
    }
}
