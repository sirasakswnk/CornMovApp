package com.example.cornmov.data.viewmodel

import androidx.lifecycle.ViewModel
import com.example.cornmov.data.model.User
import com.example.cornmov.data.model.WatchSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _user = MutableStateFlow(User())
    val user: StateFlow<User> = _user.asStateFlow()

    // State สำหรับเก็บยอดที่ดูจบแล้ว (แยก Movie / Series)
    private val _moviesWatchedCount = MutableStateFlow(0)
    val moviesWatchedCount: StateFlow<Int> = _moviesWatchedCount.asStateFlow()

    private val _seriesWatchedCount = MutableStateFlow(0)
    val seriesWatchedCount: StateFlow<Int> = _seriesWatchedCount.asStateFlow()

    // State สำหรับเก็บรายการ Archive (เฉพาะที่ status = "Done")
    private val _archives = MutableStateFlow<List<WatchSession>>(emptyList())
    val archives: StateFlow<List<WatchSession>> = _archives.asStateFlow()

    init {
        fetchUserData()
        fetchArchivesAndStats() // เรียกดึงสถิติ
    }

    private fun fetchUserData() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get().addOnSuccessListener { doc ->
            doc.toObject(User::class.java)?.let { _user.value = it }
        }
    }

    private fun fetchArchivesAndStats() {
        val uid = auth.currentUser?.uid ?: return
        // ดึง Watchlist เฉพาะที่ดูจบแล้ว (status == "Done")
        db.collection("users").document(uid).collection("watchlist")
            .whereEqualTo("status", "Done")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                // แปลงข้อมูลเป็นลิสต์
                val doneList = snapshot.documents.mapNotNull { it.toObject(WatchSession::class.java) }

                // นับจำนวน
                _moviesWatchedCount.value = doneList.count { it.type == "Movie" }

                // เรียงลำดับจากเพิ่มล่าสุด และเก็บเข้า State สำหรับโชว์หน้า Archive
                _archives.value = doneList.sortedByDescending { it.addedAt.seconds }
            }
    }
}