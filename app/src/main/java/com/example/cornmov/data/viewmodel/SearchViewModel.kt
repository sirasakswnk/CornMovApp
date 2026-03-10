package com.example.cornmov.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cornmov.data.model.Movie
import com.example.cornmov.data.repository.MovieRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// data/viewmodel/SearchViewModel.kt
class SearchViewModel : ViewModel() {

    private val repository = MovieRepository()

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    val searchState: StateFlow<SearchState> = _searchState

    private var searchJob: Job? = null

    sealed class SearchState {
        object Idle : SearchState()
        object Loading : SearchState()
        data class Success(val results: List<Movie>) : SearchState()
        object Empty : SearchState()
        data class Error(val message: String) : SearchState()
    }

    fun onQueryChange(query: String) {
        // ยกเลิก job เดิมก่อน (debounce)
        searchJob?.cancel()

        if (query.isBlank()) {
            _searchState.value = SearchState.Idle
            return
        }

        searchJob = viewModelScope.launch {
            delay(400) // รอ 400ms หลังพิมพ์หยุด
            _searchState.value = SearchState.Loading
            repository.searchMovies(query)
                .onSuccess { movies ->
                    _searchState.value = if (movies.isEmpty())
                        SearchState.Empty
                    else
                        SearchState.Success(movies)
                }
                .onFailure {
                    _searchState.value = SearchState.Error(it.message ?: "เกิดข้อผิดพลาด")
                }
        }
    }
}