package com.example.cornmov.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cornmov.data.model.MovieDetail  // ✅ แก้ import ตรงนี้
import com.example.cornmov.data.repository.MovieRepository
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

    sealed class DetailState {
        object Loading : DetailState()
        data class Success(val data: MovieDetail) : DetailState()  // ✅ แก้ตรงนี้
        data class Error(val message: String) : DetailState()
    }

    fun loadDetail(movieId: Int) {
        viewModelScope.launch {
            _detailState.value = DetailState.Loading
            movieRepository.getMovieDetail(movieId)
                .onSuccess { _detailState.value = DetailState.Success(it) }  // ✅ หายแดงแล้ว
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
                "movieId" to movie.id,
                "title" to movie.title,
                "posterPath" to (movie.posterPath ?: ""), // กันค่า null
                "rating" to movie.rating,
                "releaseYear" to movie.releaseYear,
                "addedAt" to System.currentTimeMillis(),
                "status" to "Want" // ✅ เปลี่ยนจาก "isWatched" เป็น "status" เพื่อให้ตรงกับเงื่อนไข 3 แท็บ
            )
            ref.set(item).addOnSuccessListener {
                _isInWatchlist.value = true
            }
        }
    }
}