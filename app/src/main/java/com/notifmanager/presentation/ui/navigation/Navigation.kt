package com.notifmanager.presentation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.notifmanager.presentation.ui.screens.HomeScreen
import com.notifmanager.presentation.ui.screens.LLMSettingsScreen
import com.notifmanager.presentation.ui.screens.OnboardingScreen
import com.notifmanager.presentation.ui.screens.PreferencesScreen
import com.notifmanager.presentation.ui.screens.SettingsScreen

/**
 * NAVIGATION SETUP
 *
 * UPDATED: Added onRequestPermission callback
 */

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Settings : Screen("settings")
    object Preferences : Screen("preferences")
    object LLMSettings : Screen("llm_settings")
}

@Composable
fun AppNavigation(
    startDestination: String = Screen.Home.route,
    onRequestPermission: () -> Unit = {}  // NEW
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                },
                onRequestPermission = onRequestPermission  // NEW
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToPreferences = {  // ADD THIS
                    navController.navigate(Screen.Preferences.route)
                }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToLLMSettings = {  // ADD THIS
                    navController.navigate(Screen.LLMSettings.route)
                }
            )
        }

        // Preferences screen
        composable(Screen.Preferences.route) {
            PreferencesScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        // LLM Settings screen
        composable(Screen.LLMSettings.route) {
            LLMSettingsScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}
