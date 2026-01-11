package com.notifmanager.presentation.ui.navigation
import com.notifmanager.presentation.ui.screens.HomeScreen


import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.notifmanager.presentation.ui.screens.OnboardingScreen
import com.notifmanager.presentation.ui.screens.SettingsScreen

/**
 * NAVIGATION SETUP
 *
 * Defines all app screens and navigation routes
 */

// Screen routes
sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Settings : Screen("settings")
}

/**
 * Main navigation host
 */
@Composable
fun AppNavigation(
    startDestination: String = Screen.Home.route
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Onboarding screen
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // Home screen
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        // Settings screen
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
    }
}