package com.sportmeet.backend.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "users")
data class User(
    @Id
    @Column(length = 36)
    var id: String = UUID.randomUUID().toString(),

    @Column(nullable = false, length = 100)
    var name: String = "",

    @Column(nullable = false, unique = true, length = 100)
    var email: String = "",

    @Column(nullable = false, length = 20)
    var phone: String = "",

    @Column(name = "password_hash", nullable = false)
    var passwordHash: String = "",

    @Column(nullable = false, length = 30)
    var role: String = "PLAYER", // PLAYER, ORGANIZER, ADMIN

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)
