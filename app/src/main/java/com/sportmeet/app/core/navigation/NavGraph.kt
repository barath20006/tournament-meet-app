package com.sportmeet.app.core.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sportmeet.app.core.session.SessionManager
import com.sportmeet.app.domain.model.UserRole
import com.sportmeet.app.presentation.admin.AdminDashboardScreen
import com.sportmeet.app.presentation.auth.LoginScreen
import com.sportmeet.app.presentation.auth.RegisterScreen
import com.sportmeet.app.presentation.organizer.OrganizerHomeScreen
import com.sportmeet.app.presentation.player.PlayerHomeScreen
import com.sportmeet.app.presentation.role.RoleSelectionScreen
import com.sportmeet.app.presentation.splash.SplashScreen
import com.sportmeet.app.presentation.welcome.WelcomeScreen

@Composable
fun SportMeetNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route
) {
    val context = LocalContext.current
    val sessionManager = SessionManager.getInstance(context)

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = { fadeIn(animationSpec = tween(250)) },
        exitTransition = { fadeOut(animationSpec = tween(250)) }
    ) {
        // Splash Screen (Checks saved session)
        composable(Screen.Splash.route) {
            SplashScreen(
                onTimeout = {
                    try {
                        val loggedIn = try { sessionManager.isLoggedIn } catch (_: Throwable) { false }
                        if (loggedIn) {
                            val role = try { sessionManager.userRole } catch (_: Throwable) { UserRole.PLAYER }
                            val destination = when (role) {
                                UserRole.PLAYER -> Screen.PlayerHome.route
                                UserRole.ORGANIZER -> Screen.OrganizerHome.route
                                UserRole.ADMIN -> Screen.AdminDashboard.route
                            }
                            navController.navigate(destination) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        } else {
                            navController.navigate(Screen.Welcome.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    } catch (t: Throwable) {
                        try {
                            navController.navigate(Screen.Welcome.route) {
                                launchSingleTop = true
                            }
                        } catch (_: Throwable) {}
                    }
                }
            )
        }

        // Welcome / Onboarding Screen
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onGetStarted = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        // Login Screen
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { role ->
                    val destination = when (role) {
                        UserRole.PLAYER -> Screen.PlayerHome.route
                        UserRole.ORGANIZER -> Screen.OrganizerHome.route
                        UserRole.ADMIN -> Screen.AdminDashboard.route
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onContinueAsGuest = {
                    navController.navigate(Screen.PlayerHome.route)
                }
            )
        }

        // Register Screen
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { role ->
                    val destination = when (role) {
                        UserRole.PLAYER -> Screen.PlayerHome.route
                        UserRole.ORGANIZER -> Screen.OrganizerHome.route
                        UserRole.ADMIN -> Screen.AdminDashboard.route
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // Role Selection
        composable(Screen.RoleSelection.route) {
            RoleSelectionScreen(
                onRoleSelected = { role ->
                    val destination = when (role) {
                        UserRole.PLAYER -> Screen.PlayerHome.route
                        UserRole.ORGANIZER -> Screen.OrganizerHome.route
                        UserRole.ADMIN -> Screen.AdminDashboard.route
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.RoleSelection.route) { inclusive = true }
                    }
                }
            )
        }

        // Player Home Flow
        composable(Screen.PlayerHome.route) {
            PlayerHomeScreen(
                onSwitchToOrganizer = {
                    navController.navigate(Screen.OrganizerHome.route) {
                        popUpTo(Screen.PlayerHome.route) { inclusive = true }
                    }
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Organizer Home Flow
        composable(Screen.OrganizerHome.route) {
            OrganizerHomeScreen(
                onSwitchToPlayer = {
                    navController.navigate(Screen.PlayerHome.route) {
                        popUpTo(Screen.OrganizerHome.route) { inclusive = true }
                    }
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Admin Dashboard Flow
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
