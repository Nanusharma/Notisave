package com.notisave.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.notisave.app.ui.navigation.NotisaveNavGraph
import com.notisave.app.ui.navigation.Routes
import com.notisave.app.ui.theme.NotisaveTheme
import com.notisave.app.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val settingsState by settingsViewModel.settingsState.collectAsState()

            NotisaveTheme(themeMode = settingsState.themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Start on onboarding if permission not granted, else main list
                    val startDestination = if (
                        SettingsViewModel.isNotificationServiceEnabled(this@MainActivity)
                    ) {
                        Routes.NOTIFICATION_LIST
                    } else {
                        Routes.ONBOARDING
                    }

                    NotisaveNavGraph(startDestination = startDestination)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Settings ViewModel refreshes status automatically via LaunchedEffect
    }
}
