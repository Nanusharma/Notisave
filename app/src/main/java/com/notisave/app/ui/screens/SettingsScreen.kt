package com.notisave.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.notisave.app.viewmodel.NotificationViewModel
import com.notisave.app.viewmodel.SettingsViewModel
import com.notisave.app.viewmodel.ThemeMode

/**
 * Settings screen with listener status, data retention, theme,
 * battery optimization, and data management options.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = viewModel(),
    notificationViewModel: NotificationViewModel = viewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val settingsState by settingsViewModel.settingsState.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }
    var showRetentionDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    // Refresh status when the screen is displayed
    LaunchedEffect(Unit) {
        settingsViewModel.refreshStatus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // --- Listener Status ---
            SectionHeader("Service")
            SettingsItem(
                icon = Icons.Outlined.Notifications,
                title = "Listener Status",
                subtitle = if (settingsState.isListenerEnabled)
                    "Active — recording notifications"
                else
                    "Inactive — tap to enable",
                trailing = {
                    StatusDot(isActive = settingsState.isListenerEnabled)
                },
                onClick = {
                    if (!settingsState.isListenerEnabled) {
                        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    }
                }
            )

            Spacer(Modifier.height(8.dp))

            SettingsItem(
                icon = Icons.Outlined.BatteryChargingFull,
                title = "Battery Optimization",
                subtitle = if (!settingsState.isBatteryOptimized)
                    "Notisave is exempt from battery optimization"
                else
                    "Tap to exempt — improves reliability",
                trailing = {
                    StatusDot(isActive = !settingsState.isBatteryOptimized)
                },
                onClick = {
                    if (settingsState.isBatteryOptimized) {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                        intent.data = Uri.parse("package:${context.packageName}")
                        context.startActivity(intent)
                    }
                }
            )

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(Modifier.height(16.dp))

            // --- Appearance ---
            SectionHeader("Appearance")
            SettingsItem(
                icon = Icons.Outlined.DarkMode,
                title = "Theme",
                subtitle = when (settingsState.themeMode) {
                    ThemeMode.SYSTEM -> "System Default"
                    ThemeMode.LIGHT -> "Light"
                    ThemeMode.DARK -> "Dark"
                },
                onClick = { showThemeDialog = true }
            )

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(Modifier.height(16.dp))

            // --- Data Management ---
            SectionHeader("Data")
            SettingsItem(
                icon = Icons.Outlined.Schedule,
                title = "Data Retention",
                subtitle = if (settingsState.retentionDays <= 0)
                    "Keep notifications forever"
                else
                    "Keep for ${settingsState.retentionDays} days",
                onClick = { showRetentionDialog = true }
            )

            Spacer(Modifier.height(8.dp))

            SettingsItem(
                icon = Icons.Outlined.DeleteForever,
                title = "Clear All Data",
                subtitle = "Permanently delete all saved notifications",
                onClick = { showClearDialog = true },
                isDanger = true
            )

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(Modifier.height(16.dp))

            // --- About ---
            SectionHeader("About")
            SettingsItem(
                icon = Icons.Outlined.Info,
                title = "Notisave",
                subtitle = "Version 1.0.0\nAll notification data is stored locally on-device.",
                onClick = { }
            )

            Spacer(Modifier.height(40.dp))
        }
    }

    // --- Dialogs ---

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All Data?") },
            text = { Text("This will permanently delete all saved notifications. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        notificationViewModel.deleteAllNotifications()
                        showClearDialog = false
                    }
                ) {
                    Text("Delete All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showRetentionDialog) {
        val options = listOf(7, 14, 30, 90, 0)
        val labels = listOf("7 days", "14 days", "30 days", "90 days", "Forever")

        AlertDialog(
            onDismissRequest = { showRetentionDialog = false },
            title = { Text("Data Retention") },
            text = {
                Column {
                    options.forEachIndexed { index, days ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    settingsViewModel.setRetentionDays(days)
                                    showRetentionDialog = false
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settingsState.retentionDays == days,
                                onClick = {
                                    settingsViewModel.setRetentionDays(days)
                                    showRetentionDialog = false
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(labels[index])
                        }
                    }
                }
            },
            confirmButton = { }
        )
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Theme") },
            text = {
                Column {
                    ThemeMode.values().forEach { mode ->
                        val label = when (mode) {
                            ThemeMode.SYSTEM -> "System Default"
                            ThemeMode.LIGHT -> "Light"
                            ThemeMode.DARK -> "Dark"
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    settingsViewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settingsState.themeMode == mode,
                                onClick = {
                                    settingsViewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = { }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailing: @Composable (() -> Unit)? = null,
    onClick: () -> Unit,
    isDanger: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDanger) MaterialTheme.colorScheme.error
                   else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = if (isDanger) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }

        trailing?.invoke()
    }
}

@Composable
private fun StatusDot(isActive: Boolean) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(
                if (isActive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
            )
    )
}
