package com.example.cornmov.show

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.cornmov.data.model.AppNotification
import com.example.cornmov.data.viewmodel.NotificationViewModel

@Composable
fun NotificationScreen(
    navController: NavController,
    viewModel: NotificationViewModel = viewModel()
) {
    val notifications by viewModel.notifications.collectAsState()

    LaunchedEffect(Unit) { viewModel.markAllAsRead() }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFFFE9E9))
    ) {
        // TopBar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, null, tint = Color(0xFF1A0508))
            }
            Text(
                "การแจ้งเตือน",
                fontWeight = FontWeight.ExtraBold,
                fontSize   = 18.sp,
                color      = Color(0xFF1A0508),
                modifier   = Modifier.weight(1f)
            )

            if (notifications.isNotEmpty()) {
                TextButton(onClick = { viewModel.clearAll() }) {
                    Text("ลบทั้งหมด", color = Color(0xFFE01C2E), fontSize = 13.sp)
                }
            }
        }

        if (notifications.isEmpty()) {
            Column(
                modifier            = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🔔", fontSize = 48.sp)
                Spacer(Modifier.height(12.dp))
                Text("ยังไม่มีการแจ้งเตือน", fontWeight = FontWeight.Bold, color = Color(0xFF1A0508))
                Text("การแจ้งเตือนจะแสดงที่นี่", fontSize = 13.sp, color = Color(0xFF999999))
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(notifications) { notif ->
                    NotificationCard(
                        notif    = notif,
                        onClick  = {
                            viewModel.markAsRead(notif.notifId)
                            if (notif.groupId.isNotEmpty()) {
                                navController.navigate("group_detail/${notif.groupId}")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(notif: AppNotification, onClick: () -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(
            containerColor = if (notif.isRead) Color.White else Color(0xFFFFF0F0)
        ),
        elevation = CardDefaults.cardElevation(if (notif.isRead) 1.dp else 3.dp)
    ) {
        Row(
            modifier          = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon ตาม type
            Text(
                text = when (notif.type) {
                    "invite"      -> "👋"
                    "vote_start"  -> "🗳️"
                    "vote_result" -> "🏆"
                    "queue_add"   -> "🎬"
                    else          -> "🔔"
                },
                fontSize = 28.sp
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A0508))
                Text(notif.body, fontSize = 12.sp, color = Color(0xFF555555), maxLines = 2,
                    overflow = TextOverflow.Ellipsis)
            }
            // Unread dot
            if (!notif.isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE01C2E))
                )
            }
        }
    }
}