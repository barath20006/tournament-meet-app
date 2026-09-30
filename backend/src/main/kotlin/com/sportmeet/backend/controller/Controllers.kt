package com.sportmeet.backend.controller

import com.sportmeet.backend.dto.ApiResponse
import com.sportmeet.backend.dto.AuthResponse
import com.sportmeet.backend.dto.CreateTournamentRequest
import com.sportmeet.backend.dto.LoginRequest
import com.sportmeet.backend.dto.RegisterRequest
import com.sportmeet.backend.dto.RegisterTeamRequest
import com.sportmeet.backend.dto.RegistrationResponse
import com.sportmeet.backend.dto.ReportResponse
import com.sportmeet.backend.dto.ReportTournamentRequest
import com.sportmeet.backend.dto.SearchTournamentsResponse
import com.sportmeet.backend.dto.TournamentResponse
import com.sportmeet.backend.dto.UpdateRegistrationStatusRequest
import com.sportmeet.backend.dto.UpdateStatusRequest
import com.sportmeet.backend.entity.Sport
import com.sportmeet.backend.repository.SportRepository
import com.sportmeet.backend.service.AdminService
import com.sportmeet.backend.service.AuthService
import com.sportmeet.backend.service.FavoriteService
import com.sportmeet.backend.service.RegistrationService
import com.sportmeet.backend.service.ReportService
import com.sportmeet.backend.service.TournamentService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {
    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.register(request))
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.login(request))
    }
}

@RestController
@RequestMapping("/api/tournaments")
class TournamentController(
    private val tournamentService: TournamentService
) {
    @GetMapping
    fun getApprovedTournaments(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<SearchTournamentsResponse> {
        return ResponseEntity.ok(tournamentService.searchTournaments(null, null, null, page, size))
    }

    @GetMapping("/search")
    fun searchTournaments(
        @RequestParam(required = false) sport: String?,
        @RequestParam(required = false) location: String?,
        @RequestParam(required = false) date: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<SearchTournamentsResponse> {
        return ResponseEntity.ok(tournamentService.searchTournaments(sport, location, date, page, size))
    }

    @GetMapping("/{id}")
    fun getTournamentById(@PathVariable id: String): ResponseEntity<TournamentResponse> {
        return ResponseEntity.ok(tournamentService.getTournamentById(id))
    }

    @PostMapping
    fun createTournament(
        @Valid @RequestBody request: CreateTournamentRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<TournamentResponse> {
        val organizerId = httpRequest.getAttribute("authenticatedUserId") as? String ?: ""
        return ResponseEntity.ok(tournamentService.createTournament(organizerId, request))
    }
}

@RestController
@RequestMapping("/api/organizer")
class OrganizerController(
    private val tournamentService: TournamentService,
    private val registrationService: RegistrationService
) {
    @GetMapping("/tournaments")
    fun getMyTournaments(httpRequest: HttpServletRequest): ResponseEntity<List<TournamentResponse>> {
        val organizerId = httpRequest.getAttribute("authenticatedUserId") as? String ?: ""
        return ResponseEntity.ok(tournamentService.getOrganizerTournaments(organizerId))
    }

    @GetMapping("/tournaments/{id}/registrations")
    fun getTournamentRegistrations(
        @PathVariable id: String,
        httpRequest: HttpServletRequest
    ): ResponseEntity<List<RegistrationResponse>> {
        val organizerId = httpRequest.getAttribute("authenticatedUserId") as? String ?: ""
        return ResponseEntity.ok(registrationService.getTournamentRegistrations(organizerId, id))
    }

    @PutMapping("/registrations/{id}/status")
    fun updateRegistrationStatus(
        @PathVariable id: String,
        @RequestBody request: UpdateRegistrationStatusRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<RegistrationResponse> {
        val organizerId = httpRequest.getAttribute("authenticatedUserId") as? String ?: ""
        return ResponseEntity.ok(registrationService.updateRegistrationStatus(organizerId, id, request.status))
    }
}

@RestController
@RequestMapping("/api")
class PlayerController(
    private val registrationService: RegistrationService,
    private val favoriteService: FavoriteService,
    private val reportService: ReportService
) {
    @PostMapping("/tournaments/{id}/register")
    fun registerTeam(
        @PathVariable id: String,
        @Valid @RequestBody request: RegisterTeamRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<RegistrationResponse> {
        val userId = httpRequest.getAttribute("authenticatedUserId") as? String ?: ""
        return ResponseEntity.ok(registrationService.registerTeam(userId, id, request))
    }

    @GetMapping("/player/registrations")
    fun getMyRegistrations(httpRequest: HttpServletRequest): ResponseEntity<List<RegistrationResponse>> {
        val userId = httpRequest.getAttribute("authenticatedUserId") as? String ?: ""
        return ResponseEntity.ok(registrationService.getMyRegistrations(userId))
    }

    @PostMapping("/tournaments/{id}/favorite")
    fun addFavorite(
        @PathVariable id: String,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ApiResponse<Boolean>> {
        val userId = httpRequest.getAttribute("authenticatedUserId") as? String ?: ""
        val result = favoriteService.addFavorite(userId, id)
        return ResponseEntity.ok(ApiResponse(true, "Added to favorites", result))
    }

    @DeleteMapping("/tournaments/{id}/favorite")
    fun removeFavorite(
        @PathVariable id: String,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ApiResponse<Boolean>> {
        val userId = httpRequest.getAttribute("authenticatedUserId") as? String ?: ""
        val result = favoriteService.removeFavorite(userId, id)
        return ResponseEntity.ok(ApiResponse(true, "Removed from favorites", result))
    }

    @GetMapping("/favorites")
    fun getMyFavorites(httpRequest: HttpServletRequest): ResponseEntity<List<TournamentResponse>> {
        val userId = httpRequest.getAttribute("authenticatedUserId") as? String ?: ""
        return ResponseEntity.ok(favoriteService.getFavorites(userId))
    }

    @PostMapping("/tournaments/{id}/report")
    fun reportTournament(
        @PathVariable id: String,
        @RequestBody request: ReportTournamentRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ReportResponse> {
        val userId = httpRequest.getAttribute("authenticatedUserId") as? String ?: ""
        return ResponseEntity.ok(reportService.reportTournament(userId, id, request.reason, request.description))
    }
}

@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val adminService: AdminService
) {
    @GetMapping("/tournaments/pending")
    fun getPendingTournaments(): ResponseEntity<List<TournamentResponse>> {
        return ResponseEntity.ok(adminService.getPendingTournaments())
    }

    @PutMapping("/tournaments/{id}/approve")
    fun approveTournament(@PathVariable id: String): ResponseEntity<TournamentResponse> {
        return ResponseEntity.ok(adminService.approveTournament(id))
    }

    @PutMapping("/tournaments/{id}/reject")
    fun rejectTournament(
        @PathVariable id: String,
        @RequestBody request: UpdateStatusRequest
    ): ResponseEntity<TournamentResponse> {
        return ResponseEntity.ok(adminService.rejectTournament(id, request.reason ?: "Tournament rejected by admin"))
    }

    @GetMapping("/reports")
    fun getReports(): ResponseEntity<List<ReportResponse>> {
        return ResponseEntity.ok(adminService.getReports())
    }

    @GetMapping("/stats")
    fun getStats(): ResponseEntity<Map<String, Long>> {
        return ResponseEntity.ok(adminService.getStats())
    }
}

@RestController
@RequestMapping("/api/sports")
class SportController(
    private val sportRepository: SportRepository
) {
    @GetMapping
    fun getAllSports(): ResponseEntity<List<Sport>> {
        return ResponseEntity.ok(sportRepository.findAll())
    }
}
