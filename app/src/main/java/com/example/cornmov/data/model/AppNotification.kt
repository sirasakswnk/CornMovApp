package com.example.cornmov.data.model

import com.google.firebase.Timestamp

data class AppNotification(
    val notifId  : String    = "",
    val type     : String    = "",   // invite | vote_start | vote_result | queue_add
    val title    : String    = "",
    val body     : String    = "",
    val groupId  : String    = "",
    val isRead   : Boolean   = false,
    val createdAt: Timestamp = Timestamp.now()
)