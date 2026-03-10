package com.example.cornmov.show

// show/HomeScreen.kt
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.cornmov.Screen
import com.example.cornmov.data.model.Movie
import com.example.cornmov.data.viewmodel.HomeViewModel
// show/HomeScreen.kt
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val trendingState by viewModel.trending.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val watchlistCount by viewModel.watchlistCount.collectAsState()

    // หนัง pick of the week = trending #1
    val pickOfWeek = (trendingState as? HomeViewModel.HomeState.Success)?.data?.firstOrNull()
    // trending list ตัดอันแรกออก (ใช้เป็น banner แล้ว)
    val trendingList = (trendingState as? HomeViewModel.HomeState.Success)?.data?.drop(1) ?: emptyList()

    // genre ที่เลือกอยู่
    var selectedGenre by remember { mutableStateOf("ALL") }
    val genres = listOf("ALL", "ACTION", "DRAMA", "HORROR", "SCI-FI")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE9E9)),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {

        // ── Member Banner ──
        item {
            MemberBanner(
                userName = userName,
                watchlistCount = watchlistCount
            )
        }

        // ── Search Bar ──
        item {
            SearchBar(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                onClick = { navController.navigate(Screen.Search.route) }
            )
        }

        // ── Genre Chips ──
        item {
            GenreChips(
                genres = genres,
                selectedGenre = selectedGenre,
                onGenreSelected = { selectedGenre = it }
            )
        }

        // ── Pick of the Week ──
        item {
            when (trendingState) {
                is HomeViewModel.HomeState.Loading -> PickOfWeekSkeleton()
                is HomeViewModel.HomeState.Success -> {
                    pickOfWeek?.let { movie ->
                        PickOfWeekSection(
                            movie = movie,
                            onWatchlist = { /* TODO */ },
                            onDetails = {
                                navController.navigate("${Screen.MovieDetail.route}/${movie.id}")
                            }
                        )
                    }
                }
                else -> {}
            }
        }

        // ── Trending Section ──
        item {
            TrendingSectionHeader(onSeeAll = {})
        }

        item {
            when (trendingState) {
                is HomeViewModel.HomeState.Loading -> TrendingGridSkeleton()
                is HomeViewModel.HomeState.Success -> {
                    TrendingGrid(
                        movies = trendingList.take(6),
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
}

// ──────────────────────────────────────────
// Member Banner
// ──────────────────────────────────────────
@Composable
fun MemberBanner(userName: String, watchlistCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Card พื้นหลัง
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(3.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // โลโก้ + ไอคอน
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "OUR POP LIST",
                            fontSize = 9.sp,
                            letterSpacing = 1.5.sp,
                            color = Color(0xFF999999)
                        )
                        Row {
                            Text(
                                text = "Corn",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A)
                            )
                            Text(
                                text = "Mov",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE01C2E)
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = {}) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Color(0xFF1A1A1A)
                            )
                        }
                        IconButton(onClick = {}) {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = "Profile",
                                tint = Color(0xFF1A1A1A)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Member tag
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE01C2E))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.LocalMovies,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "MEMBER $userName  ·  ค้าง $watchlistCount เรื่อง",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────
// Search Bar (กดแล้วไปหน้า Search)
// ──────────────────────────────────────────
@Composable
fun SearchBar(modifier: Modifier = Modifier, onClick: () -> Unit) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable {
                keyboardController?.hide()
                onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = Color(0xFFAAAAAA),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "ค้นหาหนัง หรือ ซีรีย์",
                fontSize = 14.sp,
                color = Color(0xFFAAAAAA)
            )
        }
    }
}

// ──────────────────────────────────────────
// Genre Chips
// ──────────────────────────────────────────
@Composable
fun GenreChips(
    genres: List<String>,
    selectedGenre: String,
    onGenreSelected: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        items(genres) { genre ->
            val isSelected = genre == selectedGenre
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) Color(0xFF1A1A1A) else Color.White
                    )
                    .clickable { onGenreSelected(genre) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = genre,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else Color(0xFF555555)
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
        // Backdrop
        AsyncImage(
            model = movie.backdropUrl ?: movie.posterUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Dark overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
        )

        // Badge + content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFFFD700))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("⭐", fontSize = 11.sp)
                    Text(
                        text = "pick of the week",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                }
            }

            // Title + Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = movie.title.uppercase(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // + WATCHLIST
                        OutlinedButton(
                            onClick = onWatchlist,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color.White),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("+ WATCH LIST", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        // DETAILS
                        Button(
                            onClick = onDetails,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE01C2E)
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("DETAILS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Poster เล็ก
                AsyncImage(
                    model = movie.posterUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(70.dp)
                        .height(100.dp)
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("🔥", fontSize = 18.sp)
            Text(
                text = "TRENDING",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
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
// Trending Grid 2 แถว 3 คอลัมน์
// ──────────────────────────────────────────
@Composable
fun TrendingGrid(movies: List<Movie>, onMovieClick: (Movie) -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        movies.chunked(3).forEach { rowMovies ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowMovies.forEach { movie ->
                    TrendingMovieCard(
                        movie = movie,
                        onClick = { onMovieClick(movie) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // padding ถ้าแถวไม่ครบ 3
                repeat(3 - rowMovies.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun TrendingMovieCard(
    movie: Movie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(0.7f)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = movie.posterUrl,
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // gradient bottom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                        startY = 200f
                    )
                )
        )
        // title + rating
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        ) {
            Text(
                text = movie.title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "★ ${movie.ratingFormatted}",
                fontSize = 10.sp,
                color = Color(0xFFFFD700)
            )
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
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "เกิดข้อผิดพลาด: $message",
            color = Color.Gray,
            fontSize = 13.sp
        )
        TextButton(onClick = onRetry) {
            Text("ลองใหม่", color = Color(0xFFE01C2E))
        }
    }
}