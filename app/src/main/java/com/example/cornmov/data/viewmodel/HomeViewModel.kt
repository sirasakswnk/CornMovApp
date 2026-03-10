package com.example.cornmov.data.viewmodel

// data/viewmodel/HomeViewModel.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cornmov.data.model.Movie
import com.example.cornmov.data.repository.MovieRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
// data/viewmodel/HomeViewModel.kt
class HomeViewModel : ViewModel() {

    private val movieRepository = MovieRepository()
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    private val _trending = MutableStateFlow<HomeState<List<Movie>>>(HomeState.Loading)
    val trending: StateFlow<HomeState<List<Movie>>> = _trending

    private val _popular = MutableStateFlow<HomeState<List<Movie>>>(HomeState.Loading)
    val popular: StateFlow<HomeState<List<Movie>>> = _popular

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName

    private val _watchlistCount = MutableStateFlow(0)
    val watchlistCount: StateFlow<Int> = _watchlistCount

    sealed class HomeState<out T> {
        object Loading : HomeState<Nothing>()
        data class Success<T>(val data: T) : HomeState<T>()
        data class Error(val message: String) : HomeState<Nothing>()
    }

    init {
        loadAll()
        loadUserInfo()
    }

    fun loadAll() {
        loadTrending()
        loadPopular()
    }

    private fun loadTrending() = viewModelScope.launch {
        _trending.value = HomeState.Loading
        movieRepository.getTrending()
            .onSuccess { _trending.value = HomeState.Success(it) }
            .onFailure { _trending.value = HomeState.Error(it.message ?: "เกิดข้อผิดพลาด") }
    }

    private fun loadPopular() = viewModelScope.launch {
        _popular.value = HomeState.Loading
        movieRepository.getPopular()
            .onSuccess { _popular.value = HomeState.Success(it) }
            .onFailure { _popular.value = HomeState.Error(it.message ?: "เกิดข้อผิดพลาด") }
    }

    private fun loadUserInfo() = viewModelScope.launch {
        val uid = auth.currentUser?.uid ?: return@launch

        // ดึงชื่อ user จาก Firestore
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                _userName.value = doc.getString("name")
                    ?: auth.currentUser?.displayName
                            ?: "Member"
            }

        // ดึงจำนวน Watchlist
        db.collection("users").document(uid)
            .collection("watchlist")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                _watchlistCount.value = snapshot?.documents?.count {
                    it.getString("status") != "Done"
                } ?: 0
            }
    }
}