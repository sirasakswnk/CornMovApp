package com.example.cornmov.data.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class Review(
    val reviewId: String = "",
    val uid: String = "",
    val userName: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val createdAt: Long = 0L,
    val likes: List<String> = emptyList()
) {
    val likeCount get() = likes.size
    val timeAgo: String get() {
        val diff = System.currentTimeMillis() - createdAt
        val days = diff / (1000 * 60 * 60 * 24)
        val weeks = days / 7
        return when {
            days < 1   -> "วันนี้"
            days < 7   -> "${days} วันที่แล้ว"
            weeks < 4  -> "${weeks} สัปดาห์ที่แล้ว"
            else       -> "${weeks / 4} เดือนที่แล้ว"
        }
    }
}

sealed class ReviewSubmitState {
    object Idle : ReviewSubmitState()
    object Loading : ReviewSubmitState()
    object Success : ReviewSubmitState()
    data class Error(val message: String) : ReviewSubmitState()
}

class ReviewViewModel : ViewModel() {

    private val auth = Firebase.auth
    private val db   = Firebase.firestore

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews

    private val _submitState = MutableStateFlow<ReviewSubmitState>(ReviewSubmitState.Idle)
    val submitState: StateFlow<ReviewSubmitState> = _submitState

    private val _myReview = MutableStateFlow<Review?>(null)
    val myReview: StateFlow<Review?> = _myReview

    fun listenReviews(movieId: Int) {
        db.collection("movies").document(movieId.toString())
            .collection("reviews")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                if (snap == null) return@addSnapshotListener
                val list = snap.documents.mapNotNull { doc ->
                    Review(
                        reviewId  = doc.id,
                        uid       = doc.getString("uid") ?: "",
                        userName  = doc.getString("userName") ?: "Anonymous",
                        rating    = (doc.getLong("rating") ?: 0L).toInt(),
                        comment   = doc.getString("comment") ?: "",
                        createdAt = doc.getLong("createdAt") ?: 0L,
                        likes     = (doc.get("likes") as? List<*>)
                            ?.filterIsInstance<String>() ?: emptyList()
                    )
                }
                _reviews.value = list
                // เช็ค review ของ user ปัจจุบัน
                val uid = auth.currentUser?.uid
                _myReview.value = list.find { it.uid == uid }
            }
    }

    fun submitReview(movieId: Int, rating: Int, comment: String) {
        val uid = auth.currentUser?.uid ?: return
        _submitState.value = ReviewSubmitState.Loading

        //ดึงชื่อจาก Firestore แล้ว submit
        db.collection("users").document(uid).get()
            .addOnSuccessListener { userDoc ->
                val name = userDoc.getString("name")?.takeIf { it.isNotBlank() }
                    ?: auth.currentUser?.displayName?.takeIf { it.isNotBlank() }
                    ?: auth.currentUser?.email?.substringBefore("@")
                    ?: "Anonymous"

                val ref = db.collection("movies").document(movieId.toString())
                    .collection("reviews").document(uid)

                val data = hashMapOf(
                    "uid"       to uid,
                    "userName"  to name,
                    "rating"    to rating,
                    "comment"   to comment,
                    "createdAt" to System.currentTimeMillis(),
                    "likes"     to emptyList<String>()
                )

                ref.set(data)
                    .addOnSuccessListener {
                        db.collection("users").document(uid)
                            .collection("watchlist").document(movieId.toString())
                            .update(mapOf(
                                "personalRating" to rating.toFloat(),
                                "reviewText"     to comment
                            ))
                        _submitState.value = ReviewSubmitState.Success
                    }
                    .addOnFailureListener {
                        _submitState.value = ReviewSubmitState.Error(it.message ?: "Error")
                    }
            }
            .addOnFailureListener {
                _submitState.value = ReviewSubmitState.Error(it.message ?: "Error")
            }
    }

    fun toggleLike(movieId: Int, reviewId: String) {
        val uid = auth.currentUser?.uid ?: return
        val ref = db.collection("movies").document(movieId.toString())
            .collection("reviews").document(reviewId)

        val review = _reviews.value.find { it.reviewId == reviewId } ?: return
        if (review.likes.contains(uid)) {
            ref.update("likes", FieldValue.arrayRemove(uid))
        } else {
            ref.update("likes", FieldValue.arrayUnion(uid))
        }
    }

    fun resetSubmitState() {
        _submitState.value = ReviewSubmitState.Idle
    }
}