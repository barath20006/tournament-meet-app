package com.sportmeet.app.data.mapper

import com.sportmeet.app.data.remote.dto.TournamentResponse
import com.sportmeet.app.domain.model.Sport
import com.sportmeet.app.domain.model.Tournament
import com.sportmeet.app.domain.model.TournamentStatus

object TournamentMapper {
    fun toDomain(dto: TournamentResponse): Tournament {
        val sport = Sport.ALL_SPORTS.find { it.id.equals(dto.sportId, ignoreCase = true) }
            ?: Sport(
                id = dto.sportId,
                name = dto.sportName.ifBlank { "Sport" },
                iconEmoji = dto.sportEmoji.ifBlank { "🏅" },
                isPopular = true
            )

        val status = try {
            TournamentStatus.valueOf(dto.status)
        } catch (e: Exception) {
            TournamentStatus.APPROVED
        }

        return Tournament(
            id = dto.id,
            name = dto.name,
            sport = sport,
            tournamentType = dto.tournamentType,
            location = dto.location,
            venueName = dto.venueName,
            date = dto.date,
            startTime = dto.startTime,
            endTime = dto.endTime,
            registrationDeadline = dto.registrationDeadline,
            entryFee = dto.entryFee,
            prizeMoney = dto.prizeMoney,
            maxTeams = dto.maxTeams,
            registeredTeams = dto.registeredTeams,
            playersPerTeam = dto.playersPerTeam,
            ageCategory = dto.ageCategory,
            organizerName = dto.organizerName,
            contactNumber = dto.contactNumber,
            whatsappNumber = dto.whatsappNumber,
            description = dto.description,
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
