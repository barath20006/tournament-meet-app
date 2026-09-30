package com.sportmeet.backend.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "sports")
data class Sport(
    @Id
    @Column(length = 50)
    var id: String = "",

    @Column(nullable = false, length = 50)
    var name: String = "",

    @Column(name = "icon_emoji", nullable = false, length = 10)
    var iconEmoji: String = "",

    @Column(name = "is_popular")
    var isPopular: Boolean = true
)
