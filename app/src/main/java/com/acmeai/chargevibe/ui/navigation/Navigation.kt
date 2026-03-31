package com.acmeai.chargevibe.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.acmeai.chargevibe.data.PreferencesManager
import com.acmeai.chargevibe.ui.dashboard.DashboardScreen
import com.acmeai.chargevibe.ui.gallery.GalleryScreen
import com.acmeai.chargevibe.ui.home.HomeScreen
import com.acmeai.chargevibe.ui.settings.SettingsScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Gallery : Screen("gallery", "Gallery", Icons.Filled.GridView)
    object Dashboard : Screen("dashboard", "Battery", Icons.Filled.BatteryChargingFull)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
}

val screens = listOf(Screen.Home, Screen.Gallery, Screen.Dashboard, Screen.Settings)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(prefs: PreferencesManager) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) { HomeScreen(prefs) }
            composable(Screen.Gallery.route) { GalleryScreen(prefs) }
            composable(Screen.Dashboard.route) { DashboardScreen() }
            composable(Screen.Settings.route) { SettingsScreen(prefs) }
        }
    }
}
