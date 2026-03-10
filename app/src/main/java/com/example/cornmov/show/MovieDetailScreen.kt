package com.example.cornmov.show

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.example.cornmov.data.model.CastMember
import com.example.cornmov.data.model.MovieDetail
import com.example.cornmov.data.model.Review
import com.example.cornmov.data.model.ReviewSubmitState
import com.example.cornmov.data.viewmodel.MovieDetailViewModel
import com.example.cornmov.data.viewmodel.ReviewViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun MovieDetailScreen(
    movieId: Int,
    navController: NavController,
    viewModel: MovieDetailViewModel = viewModel(),
    reviewViewModel: ReviewViewModel = viewModel()
) {
    val detailState by viewModel.detailState.collectAsState()
    val isInWatchlist by viewModel.isInWatchlist.collectAsState()

    LaunchedEffect(movieId) {
        viewModel.loadDetail(movieId)
        reviewViewModel.listenReviews(movieId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A0000),
                        Color(0xFF1A0000),
                        Color(0xFF790C0C),
                        Color(0xFF7E1919),
                        Color(0xFF1A0000),
                    ),
                    startY = 0f,
                    endY = 2800f
                )
            )
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
                    onToggleWatchlist = { viewModel.toggleWatchlist(movie) },
                    navController = navController,
                    reviewViewModel = reviewViewModel,
                    movieId = movieId,
                )
            }
            is MovieDetailViewModel.DetailState.Error -> {
                val msg = (detailState as MovieDetailViewModel.DetailState.Error).message
                Column(
                    modifier            = Modifier.align(Alignment.Center),
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
    onToggleWatchlist: () -> Unit,
    navController: NavController,
    reviewViewModel: ReviewViewModel = viewModel(),
    movieId: Int = 0,
) {
    var isOverviewExpanded by remember { mutableStateOf(false) }
    val reviews     by reviewViewModel.reviews.collectAsState()
    val submitState by reviewViewModel.submitState.collectAsState()
    val myReview    by reviewViewModel.myReview.collectAsState()
    val currentUid  = Firebase.auth.currentUser?.uid

    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage   by remember { mutableStateOf("") }

    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(snackbarMessage)
            snackbarMessage = ""
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier       = Modifier
                .fillMaxSize()
                .background(Color.Transparent),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {

            // ── TopBar ──
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                ) {
                    IconButton(
                        onClick  = onBack,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text(
                        text       = "DETAIL",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White,
                        modifier   = Modifier.align(Alignment.Center)
                    )
                }
            }

            // ── Backdrop + Poster ──
            item {
                Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
                    AsyncImage(
                        model              = movie.backdropUrl,
                        contentDescription = null,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.3f),
                                    Color(0xFF1A0505).copy(alpha = 0.85f)
                                )
                            )
                        )
                    )
                    AsyncImage(
                        model              = movie.posterUrl,
                        contentDescription = movie.title,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier
                            .width(130.dp).height(190.dp)
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Text(
                        text     = movie.title.uppercase(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color    = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp, start = 16.dp, end = 16.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }


            item {
                LazyRow(
                    contentPadding        = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(movie.genres) { genre ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(genreColor(genre))
                                .clickable {
                                    val gId = genreIdOf(genre)
                                    if (gId != null) {
                                        navController.navigate("all_movies?genreId=$gId&genreName=$genre")
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text       = genre.uppercase(),
                                fontSize   = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color      = Color.White
                            )
                        }
                    }
                }
            }

            // ── ชื่อหนัง + Share ──
            item {
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text       = movie.title.uppercase(),
                        fontSize   = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White,
                        modifier   = Modifier.weight(1f)
                    )
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                    }
                }
            }

            // ── Info Row ──
            item {
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    InfoItem(icon = "🎬", text = movie.releaseYear)
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Timer,
                            contentDescription = null,
                            tint               = Color(0xFFFFD700),
                            modifier           = Modifier.size(16.dp)
                        )
                        Text(text = movie.runtimeFormatted, fontSize = 14.sp, color = Color(0xFFFDFDFD))
                    }
                    InfoItem(icon = "🌐", text = movie.languageFormatted)
                }
            }

            // ── WATCH LIST Button ──
            item {
                Button(
                    onClick  = onToggleWatchlist,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(50.dp),
                    shape  = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isInWatchlist) Color(0xFF444444) else Color(0xFF1A1A1A)
                    )
                ) {
                    Text(
                        text       = if (isInWatchlist) "✓ IN WATCH LIST" else "+ WATCH LIST",
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White
                    )
                }
            }




            // ── Stats Row ──
            item {
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(modifier = Modifier.weight(1f), value = movie.ratingFormatted,        label = "IMDB",            valueColor = Color(0xFFFFD700))
                    StatCard(modifier = Modifier.weight(1f), value = "${movie.rottenScore}%",       label = "ROTTEN TOMATOES", valueColor = Color(0xFFE01C2E))
                    StatCard(modifier = Modifier.weight(1f), value = "${movie.voteCount / 1000.0}K",label = "รีวิวทั้งหมด",      valueColor = Color(0xFFFCD4D4))
                    StatCard(modifier = Modifier.weight(1f), value = "PG-13",                       label = "RATING",          valueColor = Color(0xFFFFD700))
                }
            }

            // ── เนื้อเรื่อง ──
            item {
                Card(
                    modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text       = "เนื้อเรื่อง",
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color(0xFF1A1A1A),
                            modifier   = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text     = movie.overview.ifEmpty { "ไม่มีเนื้อเรื่อง" },
                            fontSize = 14.sp,
                            color    = Color(0xFF555555),
                            lineHeight = 22.sp,
                            maxLines = if (isOverviewExpanded) Int.MAX_VALUE else 4,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (movie.overview.length > 200) {
                            TextButton(
                                onClick        = { isOverviewExpanded = !isOverviewExpanded },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text     = if (isOverviewExpanded) "ย่อลง" else "อ่านต่อ",
                                    color    = Color(0xFFE01C2E),
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
                    modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier         = Modifier.size(44.dp).clip(CircleShape).background(Color(0xFFFFE9E9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Movie, null, tint = Color(0xFFE01C2E), modifier = Modifier.size(22.dp))
                            }
                            Column {
                                Text(text = "ผู้กำกับ", fontSize = 11.sp, color = Color(0xFF999999))
                                Text(
                                    text       = movie.director.ifEmpty { "N/A" },
                                    fontSize   = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = Color(0xFF1A1A1A)
                                )
                            }
                        }
                        Column {
                            Text(text = "ประเทศ", fontSize = 11.sp, color = Color(0xFF999999))
                            Text(
                                text       = movie.productionCountry.ifEmpty { "N/A" },
                                fontSize   = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color      = Color(0xFF1A1A1A)
                            )
                        }
                    }
                }
            }

            // ── นักแสดง ──
            if (movie.cast.isNotEmpty()) {
                item {
                    Text(
                        text     = "นักแสดง",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color    = Color(0xFFFDFCFC),
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                    )
                }
                item {
                    LazyRow(
                        contentPadding        = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(movie.cast) { cast -> CastCard(cast = cast) }
                    }
                }
            }

            // ── ให้คะแนน & รีวิว ──
            item {
                ReviewSection(
                    movieId     = movieId,
                    myReview    = myReview,
                    submitState = submitState,
                    onSubmit    = { rating, comment -> reviewViewModel.submitReview(movieId, rating, comment) },
                    onResetState = { reviewViewModel.resetSubmitState() }
                )
            }

            // ── รีวิวจากผู้ชม ──
            if (reviews.isNotEmpty()) {
                item {
                    Row(
                        modifier          = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp).height(18.dp)
                                .background(Color(0xFFE01C2E), RoundedCornerShape(2.dp))
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text       = "รีวิวจากผู้ชม",
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color(0xFFFCFCFC)
                        )
                    }
                }
                items(reviews) { review ->
                    ReviewCard(
                        review     = review,
                        currentUid = currentUid,
                        onLike     = { reviewViewModel.toggleLike(movieId, review.reviewId) }
                    )
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier  = Modifier.align(Alignment.BottomCenter)
        )
    }

}

// ──────────────────────────────────────────
// Reusable Composables
// ──────────────────────────────────────────

@Composable
fun InfoItem(icon: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(icon, fontSize = 14.sp)
        Text(text, fontSize = 14.sp, color = Color.White.copy(alpha = 0.85f))
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, value: String, label: String, valueColor: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = valueColor)
            Text(text = label, fontSize = 8.sp, color = Color.White.copy(alpha = 0.6f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun CastCard(cast: CastMember) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(72.dp)) {
        AsyncImage(
            model = cast.photoUrl ?: "https://via.placeholder.com/185x185",
            contentDescription = cast.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(64.dp).clip(CircleShape).background(Color(0xFFDDCCCC))
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = cast.name, fontSize = 11.sp, fontWeight = FontWeight.Medium,
            color = Color(0xFFFAF7F7), maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center
        )
    }
}

// ──────────────────────────────────────────
// ReviewSection — ฟอร์มให้คะแนน
// ──────────────────────────────────────────
@Composable
fun ReviewSection(
    movieId: Int,
    myReview: Review?,
    submitState: ReviewSubmitState,
    onSubmit: (rating: Int, comment: String) -> Unit,
    onResetState: () -> Unit
) {
    var selectedRating by remember(myReview) { mutableStateOf(myReview?.rating ?: 0) }
    var comment by remember(myReview) { mutableStateOf(myReview?.comment ?: "") }

    LaunchedEffect(submitState) {
        if (submitState is ReviewSubmitState.Success) onResetState()
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(4.dp).height(18.dp).background(Color(0xFFE01C2E), RoundedCornerShape(2.dp)))
                Spacer(Modifier.width(8.dp))
                Text(text = "ให้คะแนน & รีวิว", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
            }

            Spacer(Modifier.height(14.dp))
            Text(text = "คะแนนของคุณ", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
            Spacer(Modifier.height(8.dp))

            // Star rating
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                (1..5).forEach { star ->
                    Text(
                        text = "★",
                        fontSize = 32.sp,
                        color = if (star <= selectedRating) Color(0xFFFFD700) else Color(0xFFDDDDDD),
                        modifier = Modifier.clickable { selectedRating = star }
                    )
                }
            }
            if (selectedRating == 0) {
                Text(text = "แตะดาวเพื่อให้คะแนน", fontSize = 12.sp, color = Color(0xFF999999), modifier = Modifier.padding(top = 4.dp))
            }

            Spacer(Modifier.height(12.dp))

            // Comment field
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                placeholder = { Text("เขียนรีวิวของคุณ...(ไม่บังคับ)", color = Color(0xFFAAAAAA), fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                minLines = 3,
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE01C2E),
                    unfocusedBorderColor = Color(0xFFEEEEEE),
                    focusedContainerColor = Color(0xFFFFF5F5),
                    unfocusedContainerColor = Color(0xFFFFF5F5)
                )
            )

            Spacer(Modifier.height(12.dp))

            // Submit button
            Button(
                onClick = { if (selectedRating > 0) onSubmit(selectedRating, comment) },
                enabled = selectedRating > 0 && submitState !is ReviewSubmitState.Loading,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE01C2E),
                    disabledContainerColor = Color(0xFFCCCCCC)
                )
            ) {
                if (submitState is ReviewSubmitState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        text = if (myReview != null) "แก้ไขรีวิว" else "ส่งรีวิว",
                        fontWeight = FontWeight.Bold, color = Color.White
                    )
                }
            }
        }
    }
}

// ──────────────────────────────────────────
// ReviewCard — แสดงรีวิวแต่ละคน
// ──────────────────────────────────────────
@Composable
fun ReviewCard(review: Review, currentUid: String?, onLike: () -> Unit) {
    val hasLiked = currentUid != null && review.likes.contains(currentUid)

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Avatar
                    Box(
                        modifier = Modifier.size(38.dp).clip(CircleShape).background(Color(0xFFE01C2E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = review.userName.firstOrNull()?.uppercase() ?: "?",
                            color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp
                        )
                    }
                    Column {
                        Text(text = review.userName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A1A1A))
                        Row {
                            (1..5).forEach { star ->
                                Text(text = "★", fontSize = 13.sp, color = if (star <= review.rating) Color(0xFFFFD700) else Color(0xFFDDDDDD))
                            }
                        }
                    }
                }
                Text(text = review.timeAgo, fontSize = 11.sp, color = Color(0xFF999999))
            }

            if (review.comment.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(text = review.comment, fontSize = 13.sp, color = Color(0xFF555555), lineHeight = 20.sp)
            }

            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onLike, modifier = Modifier.size(28.dp)) {
                    Text(text = if (hasLiked) "❤️" else "🤍", fontSize = 18.sp)
                }
                if (review.likeCount > 0) {
                    Spacer(Modifier.width(4.dp))
                    Text(text = "${review.likeCount} คนเห็นด้วย", fontSize = 12.sp, color = Color(0xFF999999))
                }
            }
        }
    }
}

// สีตาม genre
fun genreColor(genre: String): Color = when (genre.lowercase()) {
    "romance"          -> Color(0xFFE91E8C)
    "drama"            -> Color(0xFF3F51B5)
    "comedy"           -> Color(0xFFFF9800)
    "action"           -> Color(0xFFE01C2E)
    "horror"           -> Color(0xFF212121)
    "sci-fi",
    "science fiction"  -> Color(0xFF009688)
    "thriller"         -> Color(0xFF795548)
    "animation"        -> Color(0xFF4CAF50)
    "family"           -> Color(0xFF96C9F4)
    else               -> Color(0xFF9E9E9E)
}

// แปลง genre name → TMDb genre ID
fun genreIdOf(genre: String): Int? = when (genre.lowercase()) {
    "action"                    -> 28
    "adventure"                 -> 12
    "animation"                 -> 16
    "comedy"                    -> 35
    "crime"                     -> 80
    "documentary"               -> 99
    "drama"                     -> 18
    "family"                    -> 10751
    "fantasy"                   -> 14
    "history"                   -> 36
    "horror"                    -> 27
    "music"                     -> 10402
    "mystery"                   -> 9648
    "romance"                   -> 10749
    "science fiction", "sci-fi" -> 878
    "thriller"                  -> 53
    "war"                       -> 10752
    "western"                   -> 37
    else                        -> null
}