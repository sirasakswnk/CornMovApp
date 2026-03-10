package com.example.cornmov.data.repository

import com.example.cornmov.data.model.AppNotification
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NotificationRepository {
    private val db   = Firebase.firestore
    private val auth = Firebase.auth

    private val currentUid get() = auth.currentUser?.uid ?: ""

    fun getNotificationsFlow(): Flow<List<AppNotification>> = callbackFlow {
        val uid = currentUid
        if (uid.isEmpty()) { trySend(emptyList()); close(); return@callbackFlow }

        val listener = db.collection("users").document(uid)
            .collection("notifications")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snap, _ ->
                if (snap == null) return@addSnapshotListener
                val list = snap.documents.mapNotNull { doc ->
                    AppNotification(
                        notifId   = doc.id,
                        type      = doc.getString("type") ?: "",
                        title     = doc.getString("title") ?: "",
                        body      = doc.getString("body") ?: "",
                        groupId   = doc.getString("groupId") ?: "",
                        isRead    = doc.getBoolean("isRead") ?: false,
                        createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now()
                    )
                }
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun markAsRead(notifId: String) {
        val uid = currentUid
        db.collection("users").document(uid)
            .collection("notifications").document(notifId)
            .update("isRead", true)
    }

    fun markAllAsRead(notifications: List<AppNotification>) {
        val uid = currentUid
        val batch = db.batch()
        notifications.filter { !it.isRead }.forEach { notif ->
            batch.update(
                db.collection("users").document(uid)
                    .collection("notifications").document(notif.notifId),
                "isRead", true
            )
        }
        batch.commit()
    }

    suspend fun clearAllNotifications() {
        val uid = currentUid
        val docs = db.collection("users").document(uid)
            .collection("notifications").get().await()
        val batch = db.batch()
        docs.documents.forEach { batch.delete(it.reference) }
        batch.commit().await()
    }

    suspend fun sendAddToWatchlistNotification(movieTitle: String) {
        val uid = currentUid
        if (uid.isEmpty()) return

        val ref = db.collection("users").document(uid)
            .collection("notifications").document()

        ref.set(mapOf(
            "type"      to "watchlist_add",
            "title"     to "เพิ่มหนังสำเร็จ 🎬",
            "body"      to "\"$movieTitle\" ถูกเพิ่มเข้า Watchlist แล้ว",
            "groupId"   to "",
            "isRead"    to false,
            "createdAt" to Timestamp.now()
        )).await()
    }

}