package com.example.cornmov.show

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import com.example.cornmov.data.viewmodel.SearchViewModel

// show/SearchScreen.kt
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = viewModel()
) {
    val searchState by viewModel.searchState.collectAsState()
    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current


    DisposableEffect(Unit) {
        onDispose {
            keyboardController?.hide()  // ✅ เพิ่ม
        }
    }
    LaunchedEffect(Unit) {
        //focusRequester.requestFocus() // auto focus keyboard
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE9E9))
    ) {

        // ── Search Bar ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    viewModel.onQueryChange(it)
                },
                placeholder = { Text("ค้นหาหนัง...", color = Color(0xFFAAAAAA)) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = Color(0xFFE01C2E)
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = {
                            query = ""
                            viewModel.onQueryChange("")
                        }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = Color(0xFFAAAAAA)
                            )
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE01C2E),
                    unfocusedBorderColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = Color(0xFFE01C2E)
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    viewModel.onQueryChange(query)
                })
            )
        }

        // ── Content ──
        when (searchState) {

            is SearchViewModel.SearchState.Idle -> {
                // หน้าแรกก่อนพิมพ์
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("🎬", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "ค้นหาหนังที่อยากดู",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Text(
                        text = "พิมพ์ชื่อหนังเพื่อค้นหา",
                        fontSize = 14.sp,
                        color = Color(0xFF999999),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            is SearchViewModel.SearchState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFE01C2E))
                }
            }

            is SearchViewModel.SearchState.Success -> {
                val movies = (searchState as SearchViewModel.SearchState.Success).results
                Text(
                    text = "พบ ${movies.size} เรื่อง",
                    fontSize = 13.sp,
                    color = Color(0xFF999999),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                LazyColumn(
                    contentPadding = PaddingValues(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(movies) { movie ->
                        SearchMovieCard(
                            movie = movie,
                            onClick = {
                                navController.navigate("${Screen.MovieDetail.route}/${movie.id}")
                            }
                        )
                    }
                }
            }

            is SearchViewModel.SearchState.Empty -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("🔍", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "ไม่พบหนัง \"$query\"",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Text(
                        text = "ลองค้นหาด้วยคำอื่น",
                        fontSize = 13.sp,
                        color = Color(0xFF999999),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            is SearchViewModel.SearchState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("⚠️", fontSize = 48.sp)
                    Text(
                        text = "เกิดข้อผิดพลาด",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

// ──────────────────────────────────────────
// Search Movie Card
// ──────────────────────────────────────────
@Composable
fun SearchMovieCard(movie: Movie, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Poster
            AsyncImage(
                model = movie.posterUrl,
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(60.dp)
                    .height(85.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFDDCCCC))
            )

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = movie.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = movie.releaseYear,
                    fontSize = 12.sp,
                    color = Color(0xFF999999)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("⭐", fontSize = 12.sp)
                    Text(
                        text = movie.ratingFormatted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE01C2E)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = movie.overview.take(80) + if (movie.overview.length > 80) "..." else "",
                    fontSize = 12.sp,
                    color = Color(0xFF777777),
                    lineHeight = 18.sp
                )
            }

            // Arrow
            Icon(
                Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint = Color(0xFFDDDDDD),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}