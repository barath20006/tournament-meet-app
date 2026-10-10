package com.sportmeet.backend.service

import com.sportmeet.backend.dto.AuthResponse
import com.sportmeet.backend.dto.CreateTournamentRequest
import com.sportmeet.backend.dto.LoginRequest
import com.sportmeet.backend.dto.RegisterRequest
import com.sportmeet.backend.dto.RegisterTeamRequest
import com.sportmeet.backend.dto.RegistrationResponse
import com.sportmeet.backend.dto.ReportResponse
import com.sportmeet.backend.dto.SearchTournamentsResponse
import com.sportmeet.backend.dto.TournamentResponse
import com.sportmeet.backend.entity.Favorite
import com.sportmeet.backend.entity.Registration
import com.sportmeet.backend.entity.Report
import com.sportmeet.backend.entity.Sport
import com.sportmeet.backend.entity.Tournament
import com.sportmeet.backend.entity.User
import com.sportmeet.backend.repository.FavoriteRepository
import com.sportmeet.backend.repository.RegistrationRepository
import com.sportmeet.backend.repository.ReportRepository
import com.sportmeet.backend.repository.SportRepository
import com.sportmeet.backend.repository.TournamentRepository
import com.sportmeet.backend.repository.UserRepository
import com.sportmeet.backend.security.JwtTokenProvider
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenProvider: JwtTokenProvider
) {
    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.existsByEmail(request.email.lowercase().trim())) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already registered")
        }

        // Public users can only register as PLAYER or ORGANIZER (section 8 of prompt)
        val role = if (request.role.equals("ORGANIZER", ignoreCase = true)) "ORGANIZER" else "PLAYER"

        val user = User(
            name = request.name.trim(),
            email = request.email.lowercase().trim(),
            phone = request.phone.trim(),
            passwordHash = passwordEncoder.encode(request.password),
            role = role
        )
        val saved = userRepository.save(user)
        val token = tokenProvider.generateToken(saved.id, saved.email, saved.role)

        return AuthResponse(
            token = token,
            userId = saved.id,
            name = saved.name,
            email = saved.email,
            phone = saved.phone,
            role = saved.role
        )
    }

    fun login(request: LoginRequest): AuthResponse {
        val input = request.emailOrPhone.trim().lowercase()
        val user = userRepository.findByEmail(input).or { userRepository.findByPhone(input) }
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email/phone or password") }

        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email/phone or password")
        }

        val token = tokenProvider.generateToken(user.id, user.email, user.role)
        return AuthResponse(
            token = token,
            userId = user.id,
            name = user.name,
            email = user.email,
            phone = user.phone,
            role = user.role
        )
    }
}

@Service
class TournamentService(
    private val tournamentRepository: TournamentRepository,
    private val sportRepository: SportRepository,
    private val userRepository: UserRepository
) {
    fun searchTournaments(
        sport: String?,
        location: String?,
        date: String?,
        page: Int,
        size: Int
    ): SearchTournamentsResponse {
        val pageable = PageRequest.of(page.coerceAtLeast(0), size.coerceIn(1, 100))
        val pageResult = tournamentRepository.searchTournaments(
            status = "APPROVED",
            sportId = if (sport.isNullOrBlank() || sport == "all") null else sport.trim(),
            location = if (location.isNullOrBlank() || location == "All") null else location.trim(),
            date = if (date.isNullOrBlank()) null else date.trim(),
            pageable = pageable
        )

        return SearchTournamentsResponse(
            tournaments = pageResult.content.map { mapToResponse(it) },
            totalElements = pageResult.totalElements,
            totalPages = pageResult.totalPages,
            currentPage = pageResult.number
        )
    }

    fun getTournamentById(id: String): TournamentResponse {
        val tournament = tournamentRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Tournament not found") }
        return mapToResponse(tournament)
    }

    @Transactional
    fun createTournament(organizerId: String, request: CreateTournamentRequest): TournamentResponse {
        val organizer = userRepository.findById(organizerId)
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "Organizer not found") }

        val sport = sportRepository.findById(request.sportId)
            .orElseGet {
                sportRepository.save(
                    Sport(
                        id = request.sportId.lowercase(),
                        name = request.sportId.replaceFirstChar { it.uppercase() },
                        iconEmoji = "🏆",
                        isPopular = true
                    )
                )
            }

        if (request.latitude != null && (request.latitude < -90.0 || request.latitude > 90.0)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid latitude: must be between -90 and 90")
        }
        if (request.longitude != null && (request.longitude < -180.0 || request.longitude > 180.0)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid longitude: must be between -180 and 180")
        }

        val tournament = Tournament(
            name = request.name.trim(),
            sport = sport,
            tournamentType = request.tournamentType,
            location = request.location.trim(),
            venueName = request.venueName.trim(),
            groundName = request.groundName.trim(),
            address = request.address.trim(),
            date = request.date.trim(),
            startTime = request.startTime.trim(),
            endTime = request.endTime.trim(),
            registrationDeadline = request.registrationDeadline.trim(),
            entryFee = request.entryFee,
            prizeMoney = request.prizeMoney,
            maxTeams = request.maxTeams,
            registeredTeams = 0,
            playersPerTeam = request.playersPerTeam,
            ageCategory = request.ageCategory,
            organizer = organizer,
            organizerName = organizer.name,
            contactNumber = request.contactNumber.trim(),
            whatsappNumber = if (request.whatsappNumber.isNotBlank()) request.whatsappNumber.trim() else request.contactNumber.trim(),
            description = request.description.trim(),
            posterUrl = request.posterUrl,
            status = "PENDING_APPROVAL", // Goes through Admin review as required by Section 13 of prompt
            latitude = request.latitude,
            longitude = request.longitude
        )

        val saved = tournamentRepository.save(tournament)
        return mapToResponse(saved)
    }

    fun getOrganizerTournaments(organizerId: String): List<TournamentResponse> {
        return tournamentRepository.findByOrganizerIdOrderByCreatedAtDesc(organizerId).map { mapToResponse(it) }
    }

    fun mapToResponse(t: Tournament): TournamentResponse {
        return TournamentResponse(
            id = t.id,
            name = t.name,
            sportId = t.sport.id,
            sportName = t.sport.name,
            sportEmoji = t.sport.iconEmoji,
            tournamentType = t.tournamentType,
            location = t.location,
            venueName = t.venueName,
            groundName = t.groundName,
            address = t.address,
            date = t.date,
            startTime = t.startTime,
            endTime = t.endTime,
            registrationDeadline = t.registrationDeadline,
            entryFee = t.entryFee,
            prizeMoney = t.prizeMoney,
            maxTeams = t.maxTeams,
            registeredTeams = t.registeredTeams,
            playersPerTeam = t.playersPerTeam,
            ageCategory = t.ageCategory,
            organizerId = t.organizer.id,
            organizerName = t.organizerName,
            contactNumber = t.contactNumber,
            whatsappNumber = t.whatsappNumber,
            description = t.description,
            posterUrl = t.posterUrl,
            status = t.status,
            rejectionReason = t.rejectionReason,
            latitude = t.latitude,
            longitude = t.longitude
        )
    }
}

@Service
class RegistrationService(
    private val registrationRepository: RegistrationRepository,
    private val tournamentRepository: TournamentRepository,
    private val userRepository: UserRepository
) {
    @Transactional
    fun registerTeam(userId: String, tournamentId: String, request: RegisterTeamRequest): RegistrationResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found") }

        val tournament = tournamentRepository.findById(tournamentId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Tournament not found") }

        if (tournament.status != "APPROVED" && tournament.status != "REGISTRATION_OPEN") {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Tournament is not open for registration")
        }

        if (registrationRepository.existsByTournamentIdAndUserId(tournamentId, userId)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "You have already registered a team for this tournament")
        }

        val registration = Registration(
            tournament = tournament,
            user = user,
            teamName = request.teamName.trim(),
            captainName = request.captainName.trim(),
            phone = request.phone.trim(),
            email = request.email.trim(),
            numberOfPlayers = request.numberOfPlayers,
            notes = request.notes.trim(),
            status = "PENDING"
        )
        val saved = registrationRepository.save(registration)

        // Increment registered teams
        tournament.registeredTeams += 1
        tournamentRepository.save(tournament)

        return mapToResponse(saved)
    }

    fun getMyRegistrations(userId: String): List<RegistrationResponse> {
        return registrationRepository.findByUserIdOrderByCreatedAtDesc(userId).map { mapToResponse(it) }
    }

    fun getTournamentRegistrations(organizerId: String, tournamentId: String): List<RegistrationResponse> {
        val tournament = tournamentRepository.findById(tournamentId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Tournament not found") }

        if (tournament.organizer.id != organizerId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to tournament entries")
        }

        return registrationRepository.findByTournamentIdOrderByCreatedAtDesc(tournamentId).map { mapToResponse(it) }
    }

    @Transactional
    fun updateRegistrationStatus(organizerId: String, registrationId: String, newStatus: String): RegistrationResponse {
        val registration = registrationRepository.findById(registrationId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Registration not found") }

        if (registration.tournament.organizer.id != organizerId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to update this registration")
        }

        registration.status = newStatus.uppercase().trim()
        val saved = registrationRepository.save(registration)
        return mapToResponse(saved)
    }

    private fun mapToResponse(r: Registration): RegistrationResponse {
        return RegistrationResponse(
            id = r.id,
            tournamentId = r.tournament.id,
            tournamentName = r.tournament.name,
            teamName = r.teamName,
            captainName = r.captainName,
            phone = r.phone,
            email = r.email,
            numberOfPlayers = r.numberOfPlayers,
            notes = r.notes,
            status = r.status,
            registeredAt = r.createdAt.toString()
        )
    }
}

@Service
class FavoriteService(
    private val favoriteRepository: FavoriteRepository,
    private val tournamentRepository: TournamentRepository,
    private val userRepository: UserRepository,
    private val tournamentService: TournamentService
) {
    @Transactional
    fun addFavorite(userId: String, tournamentId: String): Boolean {
        if (!favoriteRepository.existsByUserIdAndTournamentId(userId, tournamentId)) {
            val user = userRepository.findById(userId).orElseThrow()
            val tournament = tournamentRepository.findById(tournamentId).orElseThrow()
            favoriteRepository.save(Favorite(user = user, tournament = tournament))
        }
        return true
    }

    @Transactional
    fun removeFavorite(userId: String, tournamentId: String): Boolean {
        favoriteRepository.deleteByUserIdAndTournamentId(userId, tournamentId)
        return true
    }

    fun getFavorites(userId: String): List<TournamentResponse> {
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId)
            .map { tournamentService.mapToResponse(it.tournament).copy(isSaved = true) }
    }
}

@Service
class AdminService(
    private val tournamentRepository: TournamentRepository,
    private val userRepository: UserRepository,
    private val reportRepository: ReportRepository,
    private val tournamentService: TournamentService
) {
    fun getPendingTournaments(): List<TournamentResponse> {
        return tournamentRepository.findByStatusOrderByCreatedAtDesc("PENDING_APPROVAL")
            .map { tournamentService.mapToResponse(it) }
    }

    @Transactional
    fun approveTournament(tournamentId: String): TournamentResponse {
        val tournament = tournamentRepository.findById(tournamentId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Tournament not found") }
        tournament.status = "APPROVED"
        tournament.updatedAt = Instant.now()
        val saved = tournamentRepository.save(tournament)
        return tournamentService.mapToResponse(saved)
    }

    @Transactional
    fun rejectTournament(tournamentId: String, reason: String): TournamentResponse {
        val tournament = tournamentRepository.findById(tournamentId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Tournament not found") }
        tournament.status = "REJECTED"
        tournament.rejectionReason = reason
        tournament.updatedAt = Instant.now()
        val saved = tournamentRepository.save(tournament)
        return tournamentService.mapToResponse(saved)
    }

    fun getReports(): List<ReportResponse> {
        return reportRepository.findAllByOrderByCreatedAtDesc().map {
            ReportResponse(
                id = it.id,
                tournamentId = it.tournament.id,
                tournamentName = it.tournament.name,
                reporterName = it.reporterName,
                reason = it.reason,
                description = it.description,
                status = it.status,
                createdAt = it.createdAt.toString()
            )
        }
    }

    fun getStats(): Map<String, Long> {
        return mapOf(
            "totalUsers" to userRepository.count(),
            "totalTournaments" to tournamentRepository.count(),
            "pendingTournaments" to tournamentRepository.findByStatusOrderByCreatedAtDesc("PENDING_APPROVAL").size.toLong(),
            "reportsCount" to reportRepository.count()
        )
    }
}

@Service
class ReportService(
    private val reportRepository: ReportRepository,
    private val tournamentRepository: TournamentRepository,
    private val userRepository: UserRepository
) {
    @Transactional
    fun reportTournament(userId: String, tournamentId: String, reason: String, description: String): ReportResponse {
        val user = userRepository.findById(userId).orElseThrow()
        val tournament = tournamentRepository.findById(tournamentId).orElseThrow()

        val report = Report(
            tournament = tournament,
            reporter = user,
            reporterName = user.name,
            reason = reason,
            description = description
        )
        val saved = reportRepository.save(report)
        return ReportResponse(
            id = saved.id,
            tournamentId = tournament.id,
            tournamentName = tournament.name,
            reporterName = user.name,
            reason = saved.reason,
            description = saved.description,
            status = saved.status,
            createdAt = saved.createdAt.toString()
        )
    }
}
