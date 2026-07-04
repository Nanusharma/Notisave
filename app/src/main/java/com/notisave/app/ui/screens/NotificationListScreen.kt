package com.notisave.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.notisave.app.data.NotificationEntity
import com.notisave.app.ui.components.EmptyState
import com.notisave.app.ui.components.NotificationCard
import com.notisave.app.ui.components.ShimmerLoadingList
import com.notisave.app.viewmodel.NotificationViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Main notification list screen with search, app filter chips,
 * day-grouped notifications, and shimmer loading states.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationListScreen(
    viewModel: NotificationViewModel = viewModel(),
    onSettingsClick: () -> Unit,
    onNotificationClick: (NotificationEntity) -> Unit
) {
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedApp by viewModel.selectedApp.collectAsState()
    val availableApps by viewModel.availableApps.collectAsState()
    val notificationCount by viewModel.notificationCount.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- Top App Bar ---
        TopAppBar(
            title = {
                if (isSearchActive) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = {
                            Text(
                                "Search notifications…",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Column {
                        Text(
                            "Notisave",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (notificationCount > 0) {
                            Text(
                                "$notificationCount notifications",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            },
            actions = {
                IconButton(onClick = { viewModel.toggleSearch() }) {
                    Icon(
                        if (isSearchActive) Icons.Outlined.Close else Icons.Outlined.Search,
                        contentDescription = if (isSearchActive) "Close search" else "Search"
                    )
                }
                if (!isSearchActive) {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Settings")
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        // --- App filter chips ---
        AnimatedVisibility(
            visible = availableApps.isNotEmpty() && !isSearchActive,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedApp == null,
                    onClick = { viewModel.selectApp(null) },
                    label = { Text("All") },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                availableApps.forEach { appInfo ->
                    FilterChip(
                        selected = selectedApp == appInfo.packageName,
                        onClick = { viewModel.selectApp(appInfo.packageName) },
                        label = { Text(appInfo.appName, maxLines = 1) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }

        // --- Content ---
        when {
            isLoading -> {
                ShimmerLoadingList(
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            notifications.isEmpty() -> {
                EmptyState()
            }
            else -> {
                GroupedNotificationList(
                    notifications = notifications,
                    onNotificationClick = onNotificationClick,
                    onDelete = { viewModel.deleteNotification(it.id) }
                )
            }
        }
    }
}

/**
 * Groups notifications by day and renders them with sticky date headers.
 */
@Composable
private fun GroupedNotificationList(
    notifications: List<NotificationEntity>,
    onNotificationClick: (NotificationEntity) -> Unit,
    onDelete: (NotificationEntity) -> Unit
) {
    val grouped = notifications.groupBy { getDayKey(it.postedAt) }

    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        grouped.forEach { (dayLabel, dayNotifications) ->
            // Sticky day header
            item(key = "header_$dayLabel") {
                DayHeader(dayLabel)
            }

            // Notification cards for this day
            items(
                items = dayNotifications,
                key = { it.id }
            ) { notification ->
                NotificationCard(
                    item = notification,
                    onClick = { onNotificationClick(notification) },
                    onDelete = { onDelete(notification) },
                    modifier = Modifier.animateItem()
                )
            }
        }

        // Bottom spacing
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun DayHeader(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    )
}

/**
 * Returns a human-readable day label: "Today", "Yesterday",
 * or a formatted date like "Mon, Jun 24".
 */
private fun getDayKey(epochMillis: Long): String {
    val now = Calendar.getInstance()
    val then = Calendar.getInstance().apply { timeInMillis = epochMillis }

    return when {
        isSameDay(now, then) -> "Today"
        isYesterday(now, then) -> "Yesterday"
        else -> {
            val sdf = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
            sdf.format(Date(epochMillis))
        }
    }
}

private fun isSameDay(c1: Calendar, c2: Calendar): Boolean =
    c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
    c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)

private fun isYesterday(now: Calendar, then: Calendar): Boolean {
    val yesterday = Calendar.getInstance().apply {
        timeInMillis = now.timeInMillis
        add(Calendar.DAY_OF_YEAR, -1)
    }
    return isSameDay(yesterday, then)
}
