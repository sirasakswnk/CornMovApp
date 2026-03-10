package com.example.cornmov.data.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.cornmov.data.model.WatchSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WatchlistViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _watchlist = MutableStateFlow<List<WatchSession>>(emptyList())
    val watchlist: StateFlow<List<WatchSession>> = _watchlist.asStateFlow()

    init {
        fetchWatchlist()
    }

    private fun fetchWatchlist() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).collection("watchlist")
            .orderBy("addedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val movies = snapshot.documents.mapNotNull {
                        it.toObject(WatchSession::class.java)
                    }
                    _watchlist.value = movies
                }
            }
    }

    fun changeStatus(movieId: Int, newStatus: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).collection("watchlist")
            .document(movieId.toString())
            .update("status", newStatus)
    }

    fun deleteMovie(movieId: Int) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).collection("watchlist")
            .document(movieId.toString())
            .delete()
    }

    fun saveReview(movieId: Int, rating: Float, review: String) {
        val uid = auth.currentUser?.uid ?: return

        // ดึง userName จาก Firestore
        db.collection("users").document(uid).get()
            .addOnSuccessListener { userDoc ->
                val name = userDoc.getString("name")?.takeIf { it.isNotBlank() }
                    ?: auth.currentUser?.displayName?.takeIf { it.isNotBlank() }
                    ?: auth.currentUser?.email?.substringBefore("@")
                    ?: "Anonymous"

                //บันทึกลง movies/{movieId}/reviews/{uid}
                db.collection("movies").document(movieId.toString())
                    .collection("reviews").document(uid)
                    .set(mapOf(
                        "uid"       to uid,
                        "userName"  to name,
                        "rating"    to rating.toInt(),  // ✅ เป็น Int เหมือน ReviewViewModel
                        "comment"   to review,
                        "createdAt" to System.currentTimeMillis(),
                        "likes"     to emptyList<String>()
                    ))

                // ✅ บันทึกลง watchlist
                db.collection("users").document(uid)
                    .collection("watchlist").document(movieId.toString())
                    .update(mapOf(
                        "personalRating" to rating,
                        "reviewText"     to review
                    ))
            }
    }
}