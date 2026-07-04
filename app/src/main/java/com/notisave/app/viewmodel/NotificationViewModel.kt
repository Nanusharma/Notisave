package com.notisave.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.notisave.app.data.AppInfo
import com.notisave.app.data.NotificationEntity
import com.notisave.app.data.NotificationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class NotificationViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = NotificationRepository(app)

    // --- Search state ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    // --- App filter state ---
    private val _selectedApp = MutableStateFlow<String?>(null)
    val selectedApp: StateFlow<String?> = _selectedApp.asStateFlow()

    // --- Loading state ---
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // --- Available apps for filter chips ---
    val availableApps: StateFlow<List<AppInfo>> = repository.getDistinctApps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Notification count ---
    val notificationCount: StateFlow<Int> = repository.getNotificationCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /**
     * Main notifications flow that reacts to search query and app filter changes.
     * Debounces search input by 300ms for smooth typing experience.
     */
    val notifications: StateFlow<List<NotificationEntity>> =
        combine(
            _searchQuery.debounce(300),
            _selectedApp
        ) { query, app ->
            Pair(query, app)
        }.flatMapLatest { (query, app) ->
            when {
                query.isNotBlank() -> repository.searchNotifications(query)
                app != null -> repository.getNotificationsByApp(app)
                else -> repository.getAllNotifications()
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    init {
        // Mark loading as false once we get the first emission
        viewModelScope.launch {
            notifications.collect {
                _isLoading.value = false
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSearch() {
        _isSearchActive.value = !_isSearchActive.value
        if (!_isSearchActive.value) {
            _searchQuery.value = ""
        }
    }

    fun selectApp(packageName: String?) {
        _selectedApp.value = if (_selectedApp.value == packageName) null else packageName
    }

    fun deleteNotification(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun cleanupOlderThan(days: Int) {
        viewModelScope.launch {
            val cutoff = System.currentTimeMillis() - days * 24L * 60L * 60L * 1000L
            repository.deleteOlderThan(cutoff)
        }
    }

    fun deleteAllNotifications() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }
}
