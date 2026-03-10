package com.example.cornmov.show

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.cornmov.Screen
import com.example.cornmov.data.model.WatchSession
import com.example.cornmov.data.viewmodel.WatchlistViewModel

@Composable
fun WatchlistScreen(
    navController: NavController,
    viewModel: WatchlistViewModel = viewModel()
) {
    val watchlist by viewModel.watchlist.collectAsState()
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Want", "Watching", "Done")

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFFFE9E9))) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color(0xFFFFE9E9),
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = Color(0xFFE01C2E)
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title, color = if (selectedTabIndex == index) Color(0xFFE01C2E) else Color.Gray, fontWeight = FontWeight.Bold) }
                )
            }
        }

        val filteredMovies = watchlist.filter { it.status == tabs[selectedTabIndex] }

        if (filteredMovies.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("ไม่มีข้อมูล", color = Color.Gray)
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredMovies) { movie ->
                    WatchlistItemCard(
                        movie = movie,
                        onClick = { navController.navigate(Screen.MovieDetail.route + "/${movie.movieId}") },
                        onStatusChange = { newStatus -> viewModel.changeStatus(movie.movieId, newStatus) },
                        onSaveReview = { rating, review -> viewModel.saveReview(movie.movieId, rating, review) }, // ✅ ส่งคำสั่งเซฟรีวิว
                        onDelete = { viewModel.deleteMovie(movie.movieId) }
                    )
                }
            }
        }
    }
}

// ✅ สร้าง Component สำหรับกดให้ดาว
@Composable
fun StarRatingBar(rating: Float, onRatingChanged: (Float) -> Unit) {
    Row {
        for (i in 1..5) {
            val isSelected = i <= rating
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = if (isSelected) Color(0xFFFFC107) else Color(0xFFE0E0E0), // เหลือง(เลือก) / เทาอ่อน(ไม่เลือก)
                modifier = Modifier
                    .size(36.dp)
                    .clickable { onRatingChanged(i.toFloat()) }
                    .padding(2.dp)
            )
        }
    }
}

@Composable
fun WatchlistItemCard(
    movie: WatchSession,
    onClick: () -> Unit,
    onStatusChange: (String) -> Unit,
    onSaveReview: (Float, String) -> Unit, // ✅ รับฟังก์ชันรีวิว
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showReviewDialog by remember { mutableStateOf(false) } // ✅ สถานะเปิดปิด Dialog รีวิว
    val statusOptions = listOf("Want", "Watching", "Done")

    // ✅ Dialog สำหรับเขียนรีวิว
    if (showReviewDialog) {
        var tempRating by remember { mutableStateOf(movie.personalRating) }
        var tempReview by remember { mutableStateOf(movie.reviewText) }

        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            containerColor = Color.White,
            title = { Text("รีวิว ${movie.title}", fontWeight = FontWeight.Bold, color = Color(0xFFE01C2E)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("ให้คะแนนความประทับใจ", fontWeight = FontWeight.Bold)
                    StarRatingBar(rating = tempRating, onRatingChanged = { tempRating = it })

                    OutlinedTextField(
                        value = tempReview,
                        onValueChange = { tempReview = it },
                        label = { Text("ความรู้สึกหลังดูจบ...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE01C2E),
                            focusedLabelColor = Color(0xFFE01C2E)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveReview(tempRating, tempReview)
                        showReviewDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE01C2E))
                ) {
                    Text("บันทึก")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReviewDialog = false }) {
                    Text("ยกเลิก", color = Color.Gray)
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth().height(if (movie.personalRating > 0f) 140.dp else 120.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w200${movie.posterPath}",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.width(90.dp).fillMaxHeight().clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
            )
            Column(
                modifier = Modifier.weight(1f).padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = movie.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)

                    // ✅ ถ้าเคยให้คะแนนไว้ จะแสดงดาวและข้อความสั้นๆ บนการ์ด
                    if (movie.personalRating > 0f) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = "Rating", tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "${movie.personalRating.toInt()}/5", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                        }
                        if (movie.reviewText.isNotEmpty()) {
                            Text(text = "“${movie.reviewText}”", fontSize = 12.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        TextButton(onClick = { expanded = true }, contentPadding = PaddingValues(0.dp)) {
                            Text("สถานะ", color = Color(0xFFE01C2E), fontSize = 12.sp)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, containerColor = Color.White) {
                            statusOptions.forEach { status ->
                                if (status != movie.status) {
                                    DropdownMenuItem(
                                        text = { Text(text = status) },
                                        onClick = { onStatusChange(status); expanded = false }
                                    )
                                }
                            }
                        }
                    }

                    // ✅ ปุ่มกดเปิดหน้าต่างรีวิว
                    TextButton(onClick = { showReviewDialog = true }, contentPadding = PaddingValues(0.dp)) {
                        Text("รีวิว", color = Color(0xFFE01C2E), fontSize = 12.sp)
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                    }
                }
            }
        }
    }
}