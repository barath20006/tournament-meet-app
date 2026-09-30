package com.sportmeet.backend.repository

import com.sportmeet.backend.entity.Favorite
import com.sportmeet.backend.entity.Registration
import com.sportmeet.backend.entity.Report
import com.sportmeet.backend.entity.Sport
import com.sportmeet.backend.entity.Tournament
import com.sportmeet.backend.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<User, String> {
    fun findByEmail(email: String): Optional<User>
    fun findByPhone(phone: String): Optional<User>
    fun existsByEmail(email: String): Boolean
}

@Repository
interface SportRepository : JpaRepository<Sport, String>

@Repository
interface TournamentRepository : JpaRepository<Tournament, String> {
    fun findByStatus(status: String, pageable: Pageable): Page<Tournament>

    fun findByOrganizerIdOrderByCreatedAtDesc(organizerId: String): List<Tournament>

    fun findByStatusOrderByCreatedAtDesc(status: String): List<Tournament>

    @Query(
        """
        SELECT t FROM Tournament t 
        WHERE (:status IS NULL OR t.status = :status)
          AND (:sportId IS NULL OR LOWER(t.sport.id) = LOWER(:sportId) OR LOWER(t.sport.name) = LOWER(:sportId))
          AND (:location IS NULL OR LOWER(t.location) = LOWER(:location))
          AND (:date IS NULL OR LOWER(t.date) LIKE LOWER(CONCAT('%', :date, '%')))
        ORDER BY t.createdAt DESC
        """
    )
    fun searchTournaments(
        @Param("status") status: String?,
        @Param("sportId") sportId: String?,
        @Param("location") location: String?,
        @Param("date") date: String?,
        pageable: Pageable
    ): Page<Tournament>
}

@Repository
interface RegistrationRepository : JpaRepository<Registration, String> {
    fun findByTournamentIdOrderByCreatedAtDesc(tournamentId: String): List<Registration>
    fun findByUserIdOrderByCreatedAtDesc(userId: String): List<Registration>
    fun existsByTournamentIdAndUserId(tournamentId: String, userId: String): Boolean
}

@Repository
interface FavoriteRepository : JpaRepository<Favorite, String> {
    fun findByUserIdOrderByCreatedAtDesc(userId: String): List<Favorite>
    fun findByUserIdAndTournamentId(userId: String, tournamentId: String): Optional<Favorite>
    fun existsByUserIdAndTournamentId(userId: String, tournamentId: String): Boolean
    fun deleteByUserIdAndTournamentId(userId: String, tournamentId: String)
}

@Repository
interface ReportRepository : JpaRepository<Report, String> {
    fun findAllByOrderByCreatedAtDesc(): List<Report>
}
