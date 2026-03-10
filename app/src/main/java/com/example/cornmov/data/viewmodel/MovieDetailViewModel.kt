package com.example.cornmov.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cornmov.data.model.MovieDetail
import com.example.cornmov.data.repository.MovieRepository
import com.example.cornmov.data.repository.NotificationRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MovieDetailViewModel : ViewModel() {

    private val movieRepository = MovieRepository()
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    private val _detailState = MutableStateFlow<DetailState>(DetailState.Loading)
    val detailState: StateFlow<DetailState> = _detailState

    private val _isInWatchlist = MutableStateFlow(false)
    val isInWatchlist: StateFlow<Boolean> = _isInWatchlist

    private val notificationRepository = NotificationRepository()

    sealed class DetailState {
        object Loading : DetailState()
        data class Success(val data: MovieDetail) : DetailState()
        data class Error(val message: String) : DetailState()
    }

    fun loadDetail(movieId: Int) {
        viewModelScope.launch {
            _detailState.value = DetailState.Loading
            movieRepository.getMovieDetail(movieId)
                .onSuccess { _detailState.value = DetailState.Success(it) }
                .onFailure { _detailState.value = DetailState.Error(it.message ?: "เกิดข้อผิดพลาด") }
        }
        checkWatchlist(movieId)
    }

    private fun checkWatchlist(movieId: Int) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .collection("watchlist").document(movieId.toString())
            .get()
            .addOnSuccessListener { _isInWatchlist.value = it.exists() }
    }

    fun toggleWatchlist(movie: MovieDetail) {
        val uid = auth.currentUser?.uid ?: return
        val ref = db.collection("users").document(uid)
            .collection("watchlist").document(movie.id.toString())

        if (_isInWatchlist.value) {
            ref.delete().addOnSuccessListener {
                _isInWatchlist.value = false
            }
        } else {
            val item = hashMapOf(
                "movieId"     to movie.id,
                "title"       to movie.title,
                "posterPath"  to movie.posterPath,
                "rating"      to movie.rating,
                "releaseYear" to movie.releaseYear,
                "addedAt"     to com.google.firebase.Timestamp.now(),
                "isWatched"   to false,
                "status"      to "Want"
            )
            ref.set(item).addOnSuccessListener {
                _isInWatchlist.value = true
                // ✅ ส่ง notification เมื่อเพิ่มเข้า watchlist
                viewModelScope.launch {
                    notificationRepository.sendAddToWatchlistNotification(movie.title)
                }
            }
        }
    }
}