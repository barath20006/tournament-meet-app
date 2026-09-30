package com.sportmeet.app.core.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Register : Screen("register")
    object RoleSelection : Screen("role_selection")
    object PlayerHome : Screen("player_home")
    object OrganizerHome : Screen("organizer_home")
    object AdminDashboard : Screen("admin_dashboard")
}

data class BottomNavItem(
    val route: String,
    val title: String,
    val iconEmoji: String
) {
    companion object {
        // Player Navigation
        val PlayerHomeTab = BottomNavItem("player_tab_home", "Home", "🏠")
        val PlayerSearchTab = BottomNavItem("player_tab_search", "Search", "🔍")
        val PlayerSavedTab = BottomNavItem("player_tab_saved", "Saved", "❤️")
        val PlayerRegistrationsTab = BottomNavItem("player_tab_registrations", "Registered", "📝")
        val PlayerProfileTab = BottomNavItem("player_tab_profile", "Profile", "👤")

        // Organizer Navigation
        val OrganizerHomeTab = BottomNavItem("org_tab_home", "Home", "🏠")
        val OrganizerCreateTab = BottomNavItem("org_tab_create", "Create", "➕")
        val OrganizerTournamentsTab = BottomNavItem("org_tab_tournaments", "Tournaments", "🏆")
        val OrganizerRegistrationsTab = BottomNavItem("org_tab_registrations", "Teams", "📝")
        val OrganizerProfileTab = BottomNavItem("org_tab_profile", "Profile", "👤")

        val playerTabs: List<BottomNavItem>
            get() = listOf(
                PlayerHomeTab,
                PlayerSearchTab,
                PlayerSavedTab,
                PlayerRegistrationsTab,
                PlayerProfileTab
            )

        val organizerTabs: List<BottomNavItem>
            get() = listOf(
                OrganizerHomeTab,
                OrganizerCreateTab,
                OrganizerTournamentsTab,
                OrganizerRegistrationsTab,
                OrganizerProfileTab
            )
    }
}
