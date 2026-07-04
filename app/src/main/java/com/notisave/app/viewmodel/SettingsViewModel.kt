package com.notisave.app.viewmodel

import android.app.Application
import android.content.Context
import android.os.PowerManager
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.notisave.app.service.AppNotificationListenerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class SettingsState(
    val isListenerEnabled: Boolean = false,
    val isBatteryOptimized: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val retentionDays: Int = 30 // 0 = keep forever
)

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()

    init {
        refreshStatus()
    }

    /**
     * Re-checks system states (listener enabled, battery optimization).
     * Called when returning to the settings screen.
     */
    fun refreshStatus() {
        viewModelScope.launch {
            val context = getApplication<Application>()
            _settingsState.value = _settingsState.value.copy(
                isListenerEnabled = isNotificationServiceEnabled(context),
                isBatteryOptimized = !isIgnoringBatteryOptimizations(context)
            )
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _settingsState.value = _settingsState.value.copy(themeMode = mode)
    }

    fun setRetentionDays(days: Int) {
        _settingsState.value = _settingsState.value.copy(retentionDays = days)
    }

    companion object {
        /**
         * Checks if the notification listener service is enabled in system settings.
         */
        fun isNotificationServiceEnabled(context: Context): Boolean {
            val enabledListeners = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            ) ?: return false
            return enabledListeners.contains(context.packageName)
        }

        /**
         * Checks if the app is exempt from battery optimization.
         */
        fun isIgnoringBatteryOptimizations(context: Context): Boolean {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            return pm.isIgnoringBatteryOptimizations(context.packageName)
        }
    }
}
