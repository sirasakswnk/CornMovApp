package com.example.cornmov.show

import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.cornmov.data.model.MovieDetail
import com.example.cornmov.data.model.CastMember
import com.example.cornmov.data.viewmodel.MovieDetailViewModel

// show/MovieDetailScreen.kt
@Composable
fun MovieDetailScreen(
    movieId: Int,
    navController: NavController,
    viewModel: MovieDetailViewModel = viewModel()
) {
    val detailState by viewModel.detailState.collectAsState()
    val isInWatchlist by viewModel.isInWatchlist.collectAsState()

    LaunchedEffect(movieId) {
        viewModel.loadDetail(movieId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE9E9))
    ) {
        when (detailState) {
            is MovieDetailViewModel.DetailState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFFE01C2E)
                )
            }
            is MovieDetailViewModel.DetailState.Success -> {
                val movie = (detailState as MovieDetailViewModel.DetailState.Success).data
                MovieDetailContent(
                    movie = movie,
                    isInWatchlist = isInWatchlist,
                    onBack = { navController.popBackStack() },
                    onToggleWatchlist = { viewModel.toggleWatchlist(movie) }
                )
            }
            is MovieDetailViewModel.DetailState.Error -> {
                val msg = (detailState as MovieDetailViewModel.DetailState.Error).message
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("เกิดข้อผิดพลาด: $msg", color = Color.Gray)
                    TextButton(onClick = { viewModel.loadDetail(movieId) }) {
                        Text("ลองใหม่", color = Color(0xFFE01C2E))
                    }
                }
            }
        }
    }
}

@Composable
fun MovieDetailContent(
    movie: com.example.cornmov.data.model.MovieDetail,
    isInWatchlist: Boolean,
    onBack: () -> Unit,
    onToggleWatchlist: () -> Unit
) {
    var isOverviewExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {

        // ── TopBar ──
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFE9E9))
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF1A1A1A)
                    )
                }
                Text(
                    text = "DETAIL",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        // ── Backdrop + Poster ──
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                // Backdrop
                AsyncImage(
                    model = movie.backdropUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.3f),
                                    Color(0xFF1A0505).copy(alpha = 0.85f)
                                )
                            )
                        )
                )
                // Poster กลาง
                AsyncImage(
                    model = movie.posterUrl,
                    contentDescription = movie.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(130.dp)
                        .height(190.dp)
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(12.dp))
                )
                // ชื่อหนังด้านล่าง poster
                Text(
                    text = movie.title.uppercase(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp, start = 16.dp, end = 16.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // ── Genre Chips ──
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(movie.genres) { genre ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(genreColor(genre))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = genre.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // ── ชื่อหนัง + Share ──
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = movie.title.uppercase(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {}) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color(0xFF1A1A1A)
                    )
                }
            }
        }

        // ── Info Row (ปี / runtime / ภาษา) ──
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoItem(icon = "🎬", text = movie.releaseYear)
                InfoItem(icon = "⏱", text = movie.runtimeFormatted)
                InfoItem(icon = "🌐", text = movie.languageFormatted)
            }
        }

        // ── + WATCH LIST Button ──
        item {
            Button(
                onClick = onToggleWatchlist,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isInWatchlist) Color(0xFF444444)
                    else Color(0xFF1A1A1A)
                )
            ) {
                Text(
                    text = if (isInWatchlist) "✓ IN WATCH LIST" else "+ WATCH LIST",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // ── Stats Row ──
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    value = movie.ratingFormatted,
                    label = "IMDB",
                    valueColor = Color(0xFFFFD700)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    value = "${movie.rottenScore}%",
                    label = "ROTTEN TOMATOES",
                    valueColor = Color(0xFFE01C2E)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    value = "${movie.voteCount / 1000.0}K",
                    label = "รีวิวทั้งหมด",
                    valueColor = Color(0xFF1A1A1A)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    value = "PG-13",
                    label = "RATING",
                    valueColor = Color(0xFFFFD700)
                )
            }
        }

        // ── เนื้อเรื่อง ──
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "เนื้อเรื่อง",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = movie.overview.ifEmpty { "ไม่มีเนื้อเรื่อง" },
                        fontSize = 14.sp,
                        color = Color(0xFF555555),
                        lineHeight = 22.sp,
                        maxLines = if (isOverviewExpanded) Int.MAX_VALUE else 4,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (movie.overview.length > 200) {
                        TextButton(
                            onClick = { isOverviewExpanded = !isOverviewExpanded },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = if (isOverviewExpanded) "ย่อลง" else "อ่านต่อ",
                                color = Color(0xFFE01C2E),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // ── ผู้กำกับ + ประเทศ ──
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // ผู้กำกับ
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFE9E9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Movie,
                                contentDescription = null,
                                tint = Color(0xFFE01C2E),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "ผู้กำกับ",
                                fontSize = 11.sp,
                                color = Color(0xFF999999)
                            )
                            Text(
                                text = movie.director.ifEmpty { "N/A" },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A)
                            )
                        }
                    }

                    // ประเทศ
                    Column {
                        Text(
                            text = "ประเทศ",
                            fontSize = 11.sp,
                            color = Color(0xFF999999)
                        )
                        Text(
                            text = movie.productionCountry.ifEmpty { "N/A" },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                    }
                }
            }
        }

        // ── นักแสดง ──
        if (movie.cast.isNotEmpty()) {
            item {
                Text(
                    text = "นักแสดง",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(movie.cast) { cast ->
                        CastCard(cast = cast)
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────
// Reusable Composables
// ──────────────────────────────────────────

@Composable
fun InfoItem(icon: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(icon, fontSize = 14.sp)
        Text(text, fontSize = 14.sp, color = Color(0xFF555555))
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    valueColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            Text(
                text = label,
                fontSize = 8.sp,
                color = Color(0xFF999999),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CastCard(cast: CastMember) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        AsyncImage(
            model = cast.photoUrl ?: "https://via.placeholder.com/185x185",
            contentDescription = cast.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFFDDCCCC))
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = cast.name,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1A1A1A),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

// สีตาม genre
fun genreColor(genre: String): Color = when (genre.lowercase()) {
    "romance"   -> Color(0xFFE91E8C)
    "drama"     -> Color(0xFF3F51B5)
    "comedy"    -> Color(0xFFFF9800)
    "action"    -> Color(0xFFE01C2E)
    "horror"    -> Color(0xFF212121)
    "sci-fi",
    "science fiction" -> Color(0xFF009688)
    "thriller"  -> Color(0xFF795548)
    "animation" -> Color(0xFF4CAF50)
    else        -> Color(0xFF9E9E9E)
}