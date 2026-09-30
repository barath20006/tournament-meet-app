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
@Table(name = "registrations")
data class Registration(
    @Id
    @Column(length = 36)
    var id: String = UUID.randomUUID().toString(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    var tournament: Tournament = Tournament(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User = User(),

    @Column(name = "team_name", nullable = false, length = 100)
    var teamName: String = "",

    @Column(name = "captain_name", nullable = false, length = 100)
    var captainName: String = "",

    @Column(nullable = false, length = 20)
    var phone: String = "",

    @Column(length = 100)
    var email: String = "",

    @Column(name = "number_of_players")
    var numberOfPlayers: Int = 11,

    @Column(columnDefinition = "TEXT")
    var notes: String = "",

    @Column(nullable = false, length = 30)
    var status: String = "PENDING",

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()
)
