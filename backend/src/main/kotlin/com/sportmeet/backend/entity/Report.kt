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
@Table(name = "reports")
data class Report(
    @Id
    @Column(length = 36)
    var id: String = UUID.randomUUID().toString(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    var tournament: Tournament = Tournament(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    var reporter: User = User(),

    @Column(name = "reporter_name", nullable = false, length = 100)
    var reporterName: String = "",

    @Column(nullable = false, length = 100)
    var reason: String = "",

    @Column(columnDefinition = "TEXT")
    var description: String = "",

    @Column(nullable = false, length = 30)
    var status: String = "PENDING_REVIEW",

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()
)
