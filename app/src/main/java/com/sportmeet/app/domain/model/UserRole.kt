package com.sportmeet.app.domain.model

enum class UserRole(val title: String, val subtitle: String, val badge: String) {
    PLAYER(
        title = "Player / Team",
        subtitle = "Discover tournaments, join teams, and compete locally",
        badge = "Player"
    ),
    ORGANIZER(
        title = "Tournament Organizer",
        subtitle = "Host tournaments, manage registrations, and post fixtures",
        badge = "Organizer"
    ),
    ADMIN(
        title = "Administrator",
        subtitle = "Approve tournaments, review reports, and manage platform",
        badge = "Admin"
    )
}
