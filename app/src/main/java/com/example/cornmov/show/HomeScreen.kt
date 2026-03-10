package com.example.cornmov.show

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.cornmov.Screen
import com.example.cornmov.data.model.Movie
import com.example.cornmov.data.viewmodel.HomeViewModel
import com.example.cornmov.data.viewmodel.MovieDetailViewModel
import com.example.cornmov.data.viewmodel.NotificationViewModel
import com.example.cornmov.data.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel(),
    detailViewModel: MovieDetailViewModel = viewModel(),
    searchViewModel: SearchViewModel = viewModel()
) {
    val trendingState  by viewModel.trending.collectAsState()
    val userName       by viewModel.userName.collectAsState()
    val watchlistCount by viewModel.watchlistCount.collectAsState()
    val searchState    by searchViewModel.searchState.collectAsState()

    var searchQuery     by remember { mutableStateOf("") }
    var showSearchSheet by remember { mutableStateOf(false) }

    val pickOfWeek   = (trendingState as? HomeViewModel.HomeState.Success)?.data?.firstOrNull()
    val trendingList = (trendingState as? HomeViewModel.HomeState.Success)?.data?.drop(1) ?: emptyList()

    var selectedGenre by remember { mutableStateOf("ALL") }
    val genres        = listOf("ALL", "ACTION", "DRAMA", "HORROR", "SCI-FI")
    val genreIdMap    = mapOf("ACTION" to 28, "DRAMA" to 18, "HORROR" to 27, "SCI-FI" to 878)

    val filteredTrendingList = if (selectedGenre == "ALL") trendingList
    else trendingList.filter { movie ->
        val id = genreIdMap[selectedGenre]
        id != null && movie.genreIds.contains(id)
    }

    LazyColumn(
        modifier       = Modifier.fillMaxSize().background(Color(0xFFFFE9E9)),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {

        // ── Member Banner ──
        item {
            MemberBanner(
                navController  = navController,
                userName       = userName,
                watchlistCount = watchlistCount
            )
        }

        // ── Search Bar ──
        item {
            SearchBar(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                onClick  = { showSearchSheet = true }
            )
        }

        // ── Genre Chips ──
        item {
            GenreChips(
                genres          = genres,
                selectedGenre   = selectedGenre,
                onGenreSelected = { selectedGenre = it }
            )
        }

        // ── Pick of the Week ──
        item {
            when (trendingState) {
                is HomeViewModel.HomeState.Loading -> PickOfWeekSkeleton()
                is HomeViewModel.HomeState.Success -> {
                    pickOfWeek?.let { movie ->
                        LaunchedEffect(movie.id) {
                            detailViewModel.loadDetail(movie.id)
                        }
                        val isInWatchlist by detailViewModel.isInWatchlist.collectAsState()
                        PickOfWeekSection(
                            movie         = movie,
                            isInWatchlist = isInWatchlist,
                            onWatchlist   = {
                                detailViewModel.toggleWatchlist(
                                    com.example.cornmov.data.model.MovieDetail(
                                        id           = movie.id,
                                        title        = movie.title,
                                        overview     = movie.overview,
                                        posterPath   = movie.posterPath,
                                        backdropPath = movie.backdropPath,
                                        rating       = movie.rating,
                                        releaseDate  = movie.releaseDate,
                                        runtime      = 0,
                                        genres       = emptyList(),
                                        director     = "",
                                        cast         = emptyList(),
                                        voteCount    = 0
                                    )
                                )
                            },
                            onDetails = {
                                navController.navigate("${Screen.MovieDetail.route}/${movie.id}")
                            }
                        )
                    }
                }
                else -> {}
            }
        }

        // ── Trending Header ──
        item {
            TrendingSectionHeader(onSeeAll = { navController.navigate("all_movies") })
        }

        // ── Trending Grid ──
        item {
            when (trendingState) {
                is HomeViewModel.HomeState.Loading -> TrendingGridSkeleton()
                is HomeViewModel.HomeState.Success -> {
                    TrendingGrid(
                        movies       = filteredTrendingList.take(6),
                        onMovieClick = { movie ->
                            navController.navigate("${Screen.MovieDetail.route}/${movie.id}")
                        }
                    )
                }
                is HomeViewModel.HomeState.Error -> {
                    ErrorRow(
                        message = (trendingState as HomeViewModel.HomeState.Error).message,
                        onRetry = { viewModel.loadAll() }
                    )
                }
            }
        }
    }

    // ── Search Bottom Sheet ──
    if (showSearchSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showSearchSheet = false
                searchQuery = ""
                searchViewModel.onQueryChange("")
            },
            containerColor = Color(0xFFFFE9E9),
            shape          = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
            ) {
                // Search Field
                OutlinedTextField(
                    value         = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        searchViewModel.onQueryChange(it)
                    },
                    placeholder  = { Text("ค้นหาหนัง หรือ ซีรีย์", color = Color(0xFFAAAAAA)) },
                    leadingIcon  = { Icon(Icons.Default.Search, null, tint = Color(0xFFE01C2E)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                searchViewModel.onQueryChange("")
                            }) {
                                Icon(Icons.Default.Close, null, tint = Color(0xFFAAAAAA))
                            }
                        }
                    },
                    singleLine      = true,
                    modifier        = Modifier.fillMaxWidth(),
                    shape           = RoundedCornerShape(12.dp),
                    colors          = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor      = Color(0xFFE01C2E),
                        unfocusedBorderColor    = Color.White,
                        focusedContainerColor   = Color.White,
                        unfocusedContainerColor = Color.White,
                        cursorColor             = Color(0xFFE01C2E)
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        searchViewModel.onQueryChange(searchQuery)
                    })
                )

                Spacer(Modifier.height(12.dp))

                // Content
                when (val state = searchState) {
                    is SearchViewModel.SearchState.Idle -> {
                        Column(
                            modifier            = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🎬", fontSize = 48.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "ค้นหาหนังที่อยากดู",
                                fontSize   = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color      = Color(0xFF1A1A1A)
                            )
                            Text(
                                "พิมพ์ชื่อหนังเพื่อค้นหา",
                                fontSize = 14.sp,
                                color    = Color(0xFF999999)
                            )
                        }
                    }

                    is SearchViewModel.SearchState.Loading -> {
                        Box(
                            modifier         = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFFE01C2E))
                        }
                    }

                    is SearchViewModel.SearchState.Success -> {
                        val movies = state.results
                        Text(
                            "พบ ${movies.size} เรื่อง",
                            fontSize = 13.sp,
                            color    = Color(0xFF999999),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(movies) { movie ->
                                SearchMovieCard(
                                    movie   = movie,
                                    onClick = {
                                        showSearchSheet = false
                                        searchQuery = ""
                                        searchViewModel.onQueryChange("")
                                        navController.navigate("${Screen.MovieDetail.route}/${movie.id}")
                                    }
                                )
                            }
                        }
                    }

                    is SearchViewModel.SearchState.Empty -> {
                        Column(
                            modifier            = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🔍", fontSize = 48.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "ไม่พบหนัง \"$searchQuery\"",
                                fontSize   = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color      = Color(0xFF1A1A1A)
                            )
                            Text(
                                "ลองค้นหาด้วยคำอื่น",
                                fontSize = 13.sp,
                                color    = Color(0xFF999999)
                            )
                        }
                    }

                    is SearchViewModel.SearchState.Error -> {
                        Column(
                            modifier            = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("⚠️", fontSize = 40.sp)
                            Text("เกิดข้อผิดพลาด", fontSize = 15.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────
// Member Banner
// ──────────────────────────────────────────
@Composable
fun MemberBanner(navController: NavController, userName: String, watchlistCount: Int) {
    val notifViewModel: NotificationViewModel = viewModel()
    val unreadCount by notifViewModel.unreadCount.collectAsState()

    Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(16.dp),
            colors    = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(3.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column {
                        Text("OUR POP LIST", fontSize = 9.sp, letterSpacing = 1.5.sp, color = Color(0xFF999999))
                        Row {
                            Text("Corn", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                            Text("Mov",  fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE01C2E))
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BadgedBox(badge = {
                            if (unreadCount > 0) Badge { Text("$unreadCount") }
                        }) {
                            IconButton(onClick = { navController.navigate(Screen.Notifications.route) }) {
                                Icon(Icons.Default.Notifications, null, tint = Color(0xFF1A0508))
                            }
                        }
                        IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                            Box(
                                modifier         = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE01C2E)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text       = userName.firstOrNull { it.isLetter() }?.uppercase() ?: "?",
                                    fontSize   = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE01C2E))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.LocalMovies, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Text(
                            text       = "MEMBER $userName  ·  ค้าง $watchlistCount เรื่อง",
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────
// Search Bar
// ──────────────────────────────────────────
@Composable
fun SearchBar(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Default.Search, null, tint = Color(0xFFAAAAAA), modifier = Modifier.size(18.dp))
            Text("ค้นหาหนัง หรือ ซีรีย์", fontSize = 14.sp, color = Color(0xFFAAAAAA))
        }
    }
}

// ──────────────────────────────────────────
// Genre Chips
// ──────────────────────────────────────────
@Composable
fun GenreChips(genres: List<String>, selectedGenre: String, onGenreSelected: (String) -> Unit) {
    LazyRow(
        contentPadding        = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier              = Modifier.padding(vertical = 8.dp)
    ) {
        items(genres) { genre ->
            val isSelected = genre == selectedGenre
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) Color(0xFF7E1919) else Color.White)
                    .clickable { onGenreSelected(genre) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text       = genre,
                    fontSize   = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color      = if (isSelected) Color.White else Color(0xFF555555)
                )
            }
        }
    }
}

// ──────────────────────────────────────────
// Pick of the Week
// ──────────────────────────────────────────
@Composable
fun PickOfWeekSection(
    movie: Movie,
    isInWatchlist: Boolean,
    onWatchlist: () -> Unit,
    onDetails: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp))
    ) {
        AsyncImage(
            model              = movie.backdropUrl ?: movie.posterUrl,
            contentDescription = null,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier.fillMaxSize()
        )
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f)))
        Column(
            modifier            = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFFFC107))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("⭐", fontSize = 11.sp)
                    Text("pick of the week", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                }
            }
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = movie.title.uppercase(),
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White,
                        maxLines   = 2,
                        overflow   = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick        = onWatchlist,
                            shape          = RoundedCornerShape(8.dp),
                            border         = BorderStroke(1.dp, Color.White),
                            colors         = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isInWatchlist) Color.White.copy(alpha = 0.3f) else Color.Transparent,
                                contentColor   = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier       = Modifier.height(34.dp)
                        ) {
                            Text(
                                text       = if (isInWatchlist) "✓ IN WATCH LIST" else "+ WATCH LIST",
                                fontSize   = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick        = onDetails,
                            shape          = RoundedCornerShape(8.dp),
                            colors         = ButtonDefaults.buttonColors(containerColor = Color(0xFFE01C2E)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier       = Modifier.height(34.dp)
                        ) {
                            Text("DETAILS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                AsyncImage(
                    model              = movie.posterUrl,
                    contentDescription = null,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier
                        .width(70.dp).height(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        }
    }
}

// ──────────────────────────────────────────
// Trending Section Header
// ──────────────────────────────────────────
@Composable
fun TrendingSectionHeader(onSeeAll: () -> Unit) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("🔥", fontSize = 18.sp)
            Text("TRENDING", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFE01C2E))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text("HOT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        TextButton(onClick = onSeeAll) {
            Text("ดูทั้งหมด →", color = Color(0xFF1A1A1A), fontSize = 13.sp)
        }
    }
}

// ──────────────────────────────────────────
// Trending Grid
// ──────────────────────────────────────────
@Composable
fun TrendingGrid(movies: List<Movie>, onMovieClick: (Movie) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        movies.chunked(3).forEach { rowMovies ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowMovies.forEach { movie ->
                    TrendingMovieCard(movie = movie, onClick = { onMovieClick(movie) }, modifier = Modifier.weight(1f))
                }
                repeat(3 - rowMovies.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
fun TrendingMovieCard(movie: Movie, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(0.7f)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        AsyncImage(
            model              = movie.posterUrl,
            contentDescription = movie.title,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                    startY = 200f
                )
            )
        )
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)) {
            Text(movie.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text("★ ${movie.ratingFormatted}", fontSize = 10.sp, color = Color(0xFFFFD700))
        }
    }
}

// ──────────────────────────────────────────
// Skeleton
// ──────────────────────────────────────────
@Composable
fun PickOfWeekSkeleton() {
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFDDCCCC))
    )
}

@Composable
fun TrendingGridSkeleton() {
    Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(2) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.7f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFDDCCCC))
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorRow(message: String, onRetry: () -> Unit) {
    Column(
        modifier            = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("เกิดข้อผิดพลาด: $message", color = Color.Gray, fontSize = 13.sp)
        TextButton(onClick = onRetry) { Text("ลองใหม่", color = Color(0xFFE01C2E)) }
    }
}