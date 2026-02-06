package com.glia.exampleapp.ui.navigation

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.glia.exampleapp.LegacyActivity
import com.glia.exampleapp.data.AppState
import com.glia.exampleapp.ui.screens.main.MainScreen
import com.glia.exampleapp.ui.screens.main.MainViewModel
import com.glia.exampleapp.ui.screens.settings.SettingsScreen
import com.glia.exampleapp.ui.screens.settings.SettingsViewModel
import com.glia.exampleapp.ui.screens.visitorinfo.VisitorInfoScreen
import com.glia.exampleapp.ui.screens.visitorinfo.VisitorInfoViewModel
import com.glia.exampleapp.ui.screens.sensitivedata.SensitiveDataScreen

/**
 * Navigation routes for the app
 */
sealed class Screen(val route: String) {
    data object Main : Screen("main")
    data object Settings : Screen("settings")
    data object VisitorInfo : Screen("visitor_info")
    data object SensitiveData : Screen("sensitive_data")
}

@Composable
fun AppNavigation(
    onPushNotificationCheck: ((MainViewModel) -> Unit)? = null
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val appState = remember { AppState.getInstance(context) }

    // Push notification permission launcher
    val pushPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Permission result handled by SettingsViewModel */ }

    NavHost(navController = navController, startDestination = Screen.Main.route) {
        composable(Screen.Main.route) {
            val viewModel: MainViewModel = viewModel(
                factory = MainViewModel.Factory(context.applicationContext, appState)
            )

            // Check for push notification launch
            androidx.compose.runtime.LaunchedEffect(Unit) {
                onPushNotificationCheck?.invoke(viewModel)
            }

            MainScreen(
                viewModel = viewModel,
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToVisitorInfo = { navController.navigate(Screen.VisitorInfo.route) },
                onNavigateToSensitiveData = {
                    navController.navigate(Screen.SensitiveData.route)
                },
                onNavigateToLegacyActivity = {
                    context.startActivity(Intent(context, LegacyActivity::class.java))
                }
            )
        }

        composable(Screen.Settings.route) {
            val viewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.Factory(context.applicationContext, appState)
            )
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onRequestPushPermission = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        pushPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            )
        }

        composable(Screen.VisitorInfo.route) {
            val viewModel: VisitorInfoViewModel = viewModel(
                factory = VisitorInfoViewModel.Factory(context.applicationContext)
            )
            VisitorInfoScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.SensitiveData.route) {
            SensitiveDataScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
