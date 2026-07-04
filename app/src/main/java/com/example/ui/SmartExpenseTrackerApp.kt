package com.example.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

@Composable
fun SmartExpenseTrackerApp(
    viewModel: MainViewModel
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val themeSetting by viewModel.themeSetting.collectAsStateWithLifecycle()

    // Map theme settings to boolean
    val darkTheme = when (themeSetting) {
        "LIGHT" -> false
        "DARK" -> true
        else -> isSystemInDarkTheme()
    }

    MyApplicationTheme(darkTheme = darkTheme) {
        if (currentUser == null) {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    // Handled automatically as user state updates in ViewModel
                }
            )
        } else {
            val navController = rememberNavController()
            
            NavHost(
                navController = navController,
                startDestination = "dashboard"
            ) {
                // Dashboard
                composable("dashboard") {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToAddTransaction = { type ->
                            navController.navigate("add_transaction/$type")
                        },
                        onNavigateToCharts = { navController.navigate("charts") },
                        onNavigateToReports = { navController.navigate("reports") },
                        onNavigateToBudgets = { navController.navigate("budgets") },
                        onNavigateToNotifications = { navController.navigate("notifications") },
                        onNavigateToProfile = { navController.navigate("profile") },
                        onNavigateToSettings = { navController.navigate("settings") }
                    )
                }

                // Add Transaction (Income or Expense)
                composable(
                    route = "add_transaction/{type}",
                    arguments = listOf(navArgument("type") { type = NavType.StringType })
                ) { backStackEntry ->
                    val type = backStackEntry.arguments?.getString("type") ?: "EXPENSE"
                    AddTransactionScreen(
                        viewModel = viewModel,
                        initialType = type,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // Custom charts
                composable("charts") {
                    ChartsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // Historical Monthly reports
                composable("reports") {
                    MonthlyReportsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // Categories list manager
                composable("categories") {
                    CategoriesScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // Budgets planning list manager
                composable("budgets") {
                    BudgetPlanningScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // Alerts list logger
                composable("notifications") {
                    NotificationsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // Profile and currency preferences
                composable("profile") {
                    UserProfileScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onLogoutClick = { viewModel.logout() }
                    )
                }

                // App configs and back-up integrations
                composable("settings") {
                    SettingsScreen(
                        viewModel = viewModel,
                        onNavigateToCategories = { navController.navigate("categories") },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
