package com.example.cornmov.data.repository

import com.example.cornmov.data.model.WatchSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class WatchlistRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    // ดึงข้อมูล Watchlist ของ User แบบ Real-time
    fun getWatchlist(): Flow<List<WatchSession>> = callbackFlow {
        val uid = currentUserId
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("users").document(uid)
            .collection("watchlist")
            .orderBy("addedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val movies = snapshot.documents.mapNotNull { it.toObject(WatchSession::class.java) }
                    trySend(movies)
                }
            }
        awaitClose { listener.remove() }
    }

    // เพิ่ม/ลบ หนังใน Watchlist
    fun toggleWatchlist(watchSession: WatchSession, isCurrentlyInWatchlist: Boolean) {
        val uid = currentUserId ?: return
        val docRef = firestore.collection("users").document(uid)
            .collection("watchlist").document(watchSession.movieId.toString())

        if (isCurrentlyInWatchlist) {
            docRef.delete() // ถ้ามีอยู่แล้วให้ลบออก
        } else {
            docRef.set(watchSession) // ถ้ายังไม่มีให้เพิ่มเข้าไป
        }
    }

    // เช็คสถานะว่าหนังเรื่องนี้อยู่ใน Watchlist หรือยัง (เอาไปเปลี่ยน UI ปุ่ม + WATCH LIST)
    fun isMovieInWatchlist(movieId: Int): Flow<Boolean> = callbackFlow {
        val uid = currentUserId ?: return@callbackFlow
        val listener = firestore.collection("users").document(uid)
            .collection("watchlist").document(movieId.toString())
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    trySend(snapshot.exists())
                } else {
                    trySend(false)
                }
            }
        awaitClose { listener.remove() }
    }

    // อัปเดตสถานะ (Want/Watching/Done)
    fun updateStatus(movieId: Int, newStatus: String) {
        val uid = currentUserId ?: return
        firestore.collection("users").document(uid)
            .collection("watchlist").document(movieId.toString())
            .update("status", newStatus)
    }
}