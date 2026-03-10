package com.example.cornmov.show

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.cornmov.data.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val user by viewModel.user.collectAsState()

    // ดึงข้อมูลสถิติและ Archive มาจาก ViewModel
    val moviesCount by viewModel.moviesWatchedCount.collectAsState()
    val seriesCount by viewModel.seriesWatchedCount.collectAsState()
    val archivesList by viewModel.archives.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE9E9))
    ) {
        // --- 1. ส่วนหัว (แถบสีเทา + รูปโปรไฟล์) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(Color(0xFFD9D9D9))
            )
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.BottomCenter)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, Color(0xFFE01C2E), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (user.profileImageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = user.profileImageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 2. การ์ดแสดงชื่อและสถิติ ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = user.name.ifEmpty { "Username" },
                    color = Color(0xFFE01C2E),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Black, thickness = 1.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Movies Watched", color = Color(0xFFE01C2E), fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = moviesCount.toString(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    VerticalDivider(modifier = Modifier.height(40.dp), color = Color.Black, thickness = 1.dp)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Series Watched", color = Color(0xFFE01C2E), fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = seriesCount.toString(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 3. ส่วน My Archives (รูปโปสเตอร์หนังที่ดูจบแล้ว) ---
        Text(
            text = "My Archives",
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (archivesList.isEmpty()) {
                        Text("ยังไม่มีประวัติการรับชม", color = Color.Gray, modifier = Modifier.padding(vertical = 32.dp))
                    } else {
                        // ดึงมาแสดงมากสุดแค่ 3 รูป
                        archivesList.take(3).forEach { movie ->
                            AsyncImage(
                                model = "https://image.tmdb.org/t/p/w200${movie.posterPath}",
                                contentDescription = movie.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(75.dp, 110.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.LightGray)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Manage Archives", color = Color.Gray, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 4. ส่วน My Friends (กลับมาแล้วครับ!) ---
        Text(
            text = "My Friends",
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // วาดวงกลมเพื่อน 4 คน
                    repeat(4) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color.Gray, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.Black)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Manage Friends", color = Color.Gray, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp)) // เว้นระยะล่างสุดให้สวยงาม
    }
}