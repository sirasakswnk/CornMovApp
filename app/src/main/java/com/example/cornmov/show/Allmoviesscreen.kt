package com.example.cornmov.show

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.cornmov.data.model.Movie
import com.example.cornmov.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ──────────────────────────────────────────
// ViewModel
// ──────────────────────────────────────────
class AllMoviesViewModel : ViewModel() {

    private val repository = MovieRepository()

    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    val movies: StateFlow<List<Movie>> = _movies

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var currentPage = 1
    private var hasMore = true
    private var currentGenreId: Int? = null

    fun init(genreId: Int?) {
        currentGenreId = genreId
        loadFirstPage()
    }

    private fun loadFirstPage() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = if (currentGenreId != null)
                repository.discoverByGenre(currentGenreId!!, 1)
            else
                repository.getTrendingPaged(1)
            result.onSuccess { list ->
                _movies.value = list
                currentPage = 1
                hasMore = list.size == 20
            }.onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun loadNextPage() {
        if (_isLoadingMore.value || !hasMore) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            val nextPage = currentPage + 1
            val result = if (currentGenreId != null)
                repository.discoverByGenre(currentGenreId!!, nextPage)
            else
                repository.getTrendingPaged(nextPage)
            result.onSuccess { list ->
                _movies.value = _movies.value + list
                currentPage = nextPage
                hasMore = list.size == 20
            }.onFailure { _error.value = it.message }
            _isLoadingMore.value = false
        }
    }
}

// ──────────────────────────────────────────
// Screen
// ──────────────────────────────────────────
@Composable
fun AllMoviesScreen(
    navController: NavController,
    genreId: Int? = null,
    genreName: String? = null,
    viewModel: AllMoviesViewModel = viewModel()
) {
    val movies by viewModel.movies.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val gridState = rememberLazyGridState()

    LaunchedEffect(genreId) {
        viewModel.init(genreId)
    }

    // ตรวจจับ scroll ถึงท้าย → โหลดหน้าถัดไป
    LaunchedEffect(gridState) {
        snapshotFlow {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = gridState.layoutInfo.totalItemsCount
            lastVisible >= total - 4
        }.collect { nearEnd ->
            if (nearEnd) viewModel.loadNextPage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A0000),
                        Color(0xFF790C0C),
                        Color(0xFF7E1919),
                        Color(0xFF1A0000)
                    ),
                    endY = 2800f
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── TopBar ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    text = genreName?.uppercase() ?: "TRENDING",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // ── Content ──
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFE01C2E))
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        state = gridState,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(movies) { movie ->
                            MovieGridItem(
                                movie = movie,
                                onClick = { navController.navigate("movie_detail/${movie.id}") }
                            )
                        }

                        // loading indicator ท้าย grid
                        if (isLoadingMore) {
                            item(span = { GridItemSpan(3) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = Color(0xFFE01C2E),
                                        modifier = Modifier.size(28.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────
// MovieGridItem
// ──────────────────────────────────────────
@Composable
fun MovieGridItem(movie: Movie, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
    ) {
        Box {
            AsyncImage(
                model = movie.posterUrl,
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF2A0A0A))
            )
            // Rating badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("★", fontSize = 9.sp, color = Color(0xFFFFD700))
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = movie.ratingFormatted,
                        fontSize = 9.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = movie.title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
        Text(
            text = movie.releaseYear,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp)
        )
    }
}