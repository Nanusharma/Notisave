package com.notisave.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.notisave.app.data.NotificationEntity
import com.notisave.app.ui.screens.NotificationDetailSheet
import com.notisave.app.ui.screens.NotificationListScreen
import com.notisave.app.ui.screens.OnboardingScreen
import com.notisave.app.ui.screens.SettingsScreen
import com.notisave.app.viewmodel.NotificationViewModel
import com.notisave.app.viewmodel.SettingsViewModel

object Routes {
    const val ONBOARDING = "onboarding"
    const val NOTIFICATION_LIST = "notification_list"
    const val SETTINGS = "settings"
}

@Composable
fun NotisaveNavGraph(
    startDestination: String
) {
    val navController = rememberNavController()

    // Shared ViewModels scoped to the nav graph
    val notificationViewModel: NotificationViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()

    // Detail sheet state — managed here to overlay on any screen
    var selectedNotification by remember { mutableStateOf<NotificationEntity?>(null) }

    val transitionDuration = 300

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                tween(transitionDuration)
            ) + fadeIn(tween(transitionDuration))
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                tween(transitionDuration)
            ) + fadeOut(tween(transitionDuration))
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                tween(transitionDuration)
            ) + fadeIn(tween(transitionDuration))
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                tween(transitionDuration)
            ) + fadeOut(tween(transitionDuration))
        }
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onPermissionGranted = {
                    navController.navigate(Routes.NOTIFICATION_LIST) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.NOTIFICATION_LIST) {
            NotificationListScreen(
                viewModel = notificationViewModel,
                onSettingsClick = {
                    navController.navigate(Routes.SETTINGS)
                },
                onNotificationClick = { notification ->
                    selectedNotification = notification
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                settingsViewModel = settingsViewModel,
                notificationViewModel = notificationViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }

    // Detail bottom sheet overlay
    selectedNotification?.let { notification ->
        NotificationDetailSheet(
            notification = notification,
            onDismiss = { selectedNotification = null }
        )
    }
}
