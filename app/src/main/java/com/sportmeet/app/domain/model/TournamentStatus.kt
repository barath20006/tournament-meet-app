package com.sportmeet.app.domain.model

enum class TournamentStatus(val displayName: String) {
    DRAFT("Draft"),
    PENDING_APPROVAL("Pending Approval"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    REGISTRATION_OPEN("Registration Open"),
    REGISTRATION_CLOSED("Registration Closed"),
    CANCELLED("Cancelled"),
    COMPLETED("Completed")
}
