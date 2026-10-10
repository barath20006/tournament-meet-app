package com.sportmeet.backend.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "tournaments")
data class Tournament(
    @Id
    @Column(length = 36)
    var id: String = UUID.randomUUID().toString(),

    @Column(nullable = false, length = 150)
    var name: String = "",

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sport_id", nullable = false)
    var sport: Sport = Sport(),

    @Column(name = "tournament_type", length = 50)
    var tournamentType: String = "Knockout",

    @Column(nullable = false, length = 100)
    var location: String = "",

    @Column(name = "venue_name", nullable = false, length = 150)
    var venueName: String = "",

    @Column(name = "ground_name", length = 150)
    var groundName: String = "",

    @Column(length = 255)
    var address: String = "",

    @Column(name = "tournament_date", nullable = false, length = 50)
    var date: String = "",

    @Column(name = "start_time", nullable = false, length = 20)
    var startTime: String = "",

    @Column(name = "end_time", length = 20)
    var endTime: String = "",

    @Column(name = "registration_deadline", length = 50)
    var registrationDeadline: String = "",

    @Column(name = "entry_fee")
    var entryFee: Double = 0.0,

    @Column(name = "prize_money")
    var prizeMoney: Double = 0.0,

    @Column(name = "max_teams")
    var maxTeams: Int = 16,

    @Column(name = "registered_teams")
    var registeredTeams: Int = 0,

    @Column(name = "players_per_team")
    var playersPerTeam: Int = 11,

    @Column(name = "age_category", length = 50)
    var ageCategory: String = "Open",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    var organizer: User = User(),

    @Column(name = "organizer_name", nullable = false, length = 100)
    var organizerName: String = "",

    @Column(name = "contact_number", nullable = false, length = 20)
    var contactNumber: String = "",

    @Column(name = "whatsapp_number", nullable = false, length = 20)
    var whatsappNumber: String = "",

    @Column(columnDefinition = "TEXT")
    var description: String = "",

    @Column(name = "poster_url", length = 1000)
    var posterUrl: String? = null,

    @Column(nullable = false, length = 30)
    var status: String = "APPROVED",

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    var rejectionReason: String? = null,

    var latitude: Double? = null,
    var longitude: Double? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)
