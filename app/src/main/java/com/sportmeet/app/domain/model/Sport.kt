package com.sportmeet.app.domain.model

data class Sport(
    val id: String,
    val name: String,
    val iconEmoji: String,
    val isPopular: Boolean = true
) {
    companion object {
        val ALL_SPORTS = listOf(
            Sport("cricket", "Cricket", "🏏", true),
            Sport("football", "Football", "⚽", true),
            Sport("volleyball", "Volleyball", "🏐", true),
            Sport("badminton", "Badminton", "🏸", true),
            Sport("basketball", "Basketball", "🏀", true),
            Sport("table_tennis", "Table Tennis", "🏓", true),
            Sport("kabaddi", "Kabaddi", "🤼", true),
            Sport("tennis", "Tennis", "🎾", false),
            Sport("hockey", "Hockey", "🏑", false),
            Sport("athletics", "Athletics", "🏃", false)
        )
    }
}
