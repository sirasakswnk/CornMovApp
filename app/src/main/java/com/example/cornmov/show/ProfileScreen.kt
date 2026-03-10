package com.example.cornmov.show

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.cornmov.data.viewmodel.ProfileViewModel

// ── Design tokens ──────────────────────────────────────
private val CornRed     = Color(0xFFE01C2E)
private val CornRedDark = Color(0xFFB01020)
private val CornBg      = Color(0xFFFFE9E9)
private val CornCard    = Color.White
private val CornGray    = Color(0xFF9E9E9E)
private val CornText    = Color(0xFF1A0508)

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val user         by viewModel.user.collectAsState()
    val moviesCount  by viewModel.moviesWatchedCount.collectAsState()
    val archivesList by viewModel.archives.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CornBg)
            .verticalScroll(rememberScrollState())
    ) {

        // ════════════════════════════════════════
        // 1. HERO HEADER — gradient banner + avatar
        // ════════════════════════════════════════
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
        ) {
            // Gradient banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(145.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(CornRedDark, CornRed, Color(0xFFFF6B6B))
                        )
                    )
            )

            // Decorative circle overlay top-right
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 30.dp, y = (-30).dp)
                    .background(Color.White.copy(alpha = 0.08f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-20).dp, y = 10.dp)
                    .background(Color.White.copy(alpha = 0.06f), CircleShape)
            )

            // Avatar
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.BottomCenter)
                    .clip(CircleShape)
                    .background(CornRed)
                    .border(4.dp, CornBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (user.profileImageUrl.isNotEmpty()) {
                    AsyncImage(
                        model              = user.profileImageUrl,
                        contentDescription = null,
                        modifier           = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale       = ContentScale.Crop
                    )
                } else {
                    Text(
                        text       = user.name.firstOrNull { it.isLetter() }?.uppercase() ?: "?",
                        fontSize   = 40.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = Color.White
                    )
                }
            }
        }

        // ════════════════════════════════════════
        // 2. ชื่อ + สถิติ
        // ════════════════════════════════════════
        Spacer(Modifier.height(10.dp))

        Text(
            text       = user.name.ifEmpty { "Username" },
            color      = CornText,
            fontWeight = FontWeight.ExtraBold,
            fontSize   = 22.sp,
            modifier   = Modifier.fillMaxWidth(),
            textAlign  = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        // Stat pill
        Card(
            modifier  = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 48.dp)
                .fillMaxWidth(),
            shape     = RoundedCornerShape(20.dp),
            colors    = CardDefaults.cardColors(containerColor = CornCard),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Row(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment   = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text       = moviesCount.toString(),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 28.sp,
                        color      = CornRed
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text     = "Movies Watched",
                        color    = CornGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // ════════════════════════════════════════
        // 3. MY ARCHIVES
        // ════════════════════════════════════════
        Spacer(Modifier.height(28.dp))

        SectionHeader(title = "🎬  My Archives")

        Spacer(Modifier.height(10.dp))

        ArchivesCard(archivesList = archivesList)

        // ════════════════════════════════════════
        // 4. MY FRIENDS
        // ════════════════════════════════════════
        Spacer(Modifier.height(28.dp))

        SectionHeader(title = "👥  My Friends")

        Spacer(Modifier.height(10.dp))

        Card(
            modifier  = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape     = RoundedCornerShape(20.dp),
            colors    = CardDefaults.cardColors(containerColor = CornCard),
            elevation = CardDefaults.cardElevation(3.dp)
        ) {
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                ) {
                    repeat(4) { index ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier         = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(CornBg)
                                    .border(1.5.dp, CornRed.copy(alpha = 0.25f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint     = CornGray,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text     = "Friend ${index + 1}",
                                fontSize = 10.sp,
                                color    = CornGray
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { /* navigate */ },
                    shape   = RoundedCornerShape(20.dp),
                    border  = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.linearGradient(listOf(CornRed, CornRed))
                    ),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text(
                        "Manage Friends",
                        color      = CornRed,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

// ── Archives Card — preview 3 + expand ──────────────────
@Composable
private fun ArchivesCard(archivesList: List<com.example.cornmov.data.model.WatchSession>) {
    var expanded by remember { mutableStateOf(false) }
    val previewList  = archivesList.take(3)
    val hasMore      = archivesList.size > 3
    val displayList  = if (expanded) archivesList else previewList

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = CornCard),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (archivesList.isEmpty()) {
                // ── Empty state ──
                Spacer(Modifier.height(8.dp))
                Text("🍿", fontSize = 36.sp)
                Spacer(Modifier.height(8.dp))
                Text("ยังไม่มีประวัติการรับชม", color = CornGray, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
            } else {
                // ── Grid 3 columns ──
                val rows = displayList.chunked(3)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    rows.forEach { rowItems ->
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { movie ->
                                AsyncImage(
                                    model              = "https://image.tmdb.org/t/p/w200${movie.posterPath}",
                                    contentDescription = movie.title,
                                    contentScale       = ContentScale.Crop,
                                    modifier           = Modifier
                                        .weight(1f)
                                        .aspectRatio(2f / 3f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(CornBg)
                                )
                            }
                            // เติม placeholder ถ้าแถวสุดท้ายไม่เต็ม 3
                            repeat(3 - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                // ── ปุ่ม ดูทั้งหมด / ย่อ ──
                if (hasMore) {
                    Spacer(Modifier.height(14.dp))
                    OutlinedButton(
                        onClick        = { expanded = !expanded },
                        shape          = RoundedCornerShape(20.dp),
                        border         = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(listOf(CornRed, CornRed))
                        ),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector        = if (expanded) Icons.Default.KeyboardArrowUp
                            else          Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint               = CornRed,
                            modifier           = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text       = if (expanded) "ย่อลง"
                            else          "ดูทั้งหมด ${archivesList.size} เรื่อง",
                            color      = CornRed,
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ── Section header ──────────────────────────────────────
@Composable
private fun SectionHeader(title: String) {
    Text(
        text       = title,
        color      = CornText,
        fontWeight = FontWeight.ExtraBold,
        fontSize   = 16.sp,
        modifier   = Modifier.padding(horizontal = 24.dp)
    )
}