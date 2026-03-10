package com.example.cornmov.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cornmov.data.model.AppNotification
import com.example.cornmov.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {
    private val repo = NotificationRepository()

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications

    val unreadCount: StateFlow<Int> = _notifications
        .map { it.count { n -> !n.isRead } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)

    init {
        viewModelScope.launch {
            repo.getNotificationsFlow().collect {
                _notifications.value = it
            }
        }
    }

    fun markAsRead(notifId: String) = repo.markAsRead(notifId)
    fun markAllAsRead() = repo.markAllAsRead(_notifications.value)

    fun clearAll() {
        viewModelScope.launch {
            repo.clearAllNotifications()
        }
    }

}