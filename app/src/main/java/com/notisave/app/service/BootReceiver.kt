package com.notisave.app.service

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log

/**
 * Receives BOOT_COMPLETED broadcast to ensure the notification listener
 * is re-bound after device restart.
 *
 * On some OEM ROMs (Xiaomi, Oppo, Vivo), the listener doesn't auto-restart
 * reliably. Toggling the component's enabled state forces a fresh bind.
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "NotisaveBootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            Log.d(TAG, "Boot completed — forcing listener rebind")

            val pm = context.packageManager
            val component = ComponentName(context, AppNotificationListenerService::class.java)

            // Toggle the component to force the system to rebind the listener
            pm.setComponentEnabledSetting(
                component,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
            pm.setComponentEnabledSetting(
                component,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )

            Log.d(TAG, "Listener component toggled for rebind")
        }
    }
}
