package com.example.cornmov

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.Group
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cornmov.data.viewmodel.AuthViewModel
import com.example.cornmov.show.AllMoviesScreen
import com.example.cornmov.show.HomeScreen
import com.example.cornmov.show.LoginActivity
import com.example.cornmov.show.MovieDetailScreen
import com.example.cornmov.show.NotificationScreen
import com.example.cornmov.show.SearchScreen
import com.example.cornmov.ui.theme.CornmovTheme
import com.example.cornmov.show.WatchlistScreen
import com.example.cornmov.show.ProfileScreen

// MainActivity.kt

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CornmovTheme {
                LayoutScreen()
            }
        }
    }
}

// ──────────────────────────────────────────
// LayoutScreen — Scaffold หลักของแอป
// ──────────────────────────────────────────
@Composable
fun LayoutScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val keyboardController = LocalSoftwareKeyboardController.current

    // หน้าที่แสดง TopBar และ BottomBar
    val showBars = currentRoute in listOf(
        Screen.Home.route,
        Screen.Search.route,
        Screen.Watchlist.route,
        Screen.Profile.route
    )

    Scaffold(
        topBar = {
            if (showBars) {
                WatchListTopBar(currentRoute = currentRoute)
            }
        },
        bottomBar = {
            if (showBars) {
                WatchListBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        keyboardController?.hide()
                        if (route == Screen.Home.route) {
                            navController.navigate(route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        } else {
                            navController.navigate(route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        WatchListNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

// ──────────────────────────────────────────
// TopBar
// ──────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchListTopBar(currentRoute: String?,
                    authViewModel: AuthViewModel= viewModel()) {
    val context = LocalContext.current
    val title = when (currentRoute) {
        Screen.Home.route      -> "CornMov"
        Screen.Search.route    -> "ค้นหาหนัง"
        Screen.Watchlist.route -> "รายการของฉัน"
        Screen.Profile.route   -> "โปรไฟล์"
        else -> ""
    }

    TopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color(0xFFE01C2E)
            )
        },
        actions = {
            IconButton(
                onClick = {
                    authViewModel.logout()
                    // ไปหน้า Login แล้วลบ backstack ทิ้ง
                    val intent = Intent(context, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    context.startActivity(intent)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Logout",
                    tint = Color(0xFFE01C2E)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFFFFE9E9)
        )
    )
}

// ──────────────────────────────────────────
// BottomBar
// ──────────────────────────────────────────
@Composable
fun WatchListBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem(Screen.Home.route,      "หน้าหลัก", Icons.Default.Home),
        BottomNavItem(Screen.Search.route,    "ค้นหา",    Icons.Default.Search),
        BottomNavItem(Screen.Watchlist.route, "รายการ",   Icons.Default.Bookmarks),
        BottomNavItem(Screen.Profile.route,   "โปรไฟล์",  Icons.Default.Person)
    )

    NavigationBar(
        containerColor = Color(0xFFFFE9E9)
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(text = item.label, fontSize = 11.sp)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = Color(0xFFE01C2E),
                    selectedTextColor   = Color(0xFFE01C2E),
                    unselectedIconColor = Color(0xFFAAAAAA),
                    unselectedTextColor = Color(0xFFAAAAAA),
                    indicatorColor      = Color(0xFFFFD0D0)
                )
            )
        }
    }
}

// ──────────────────────────────────────────
// NavHost — เชื่อมทุกหน้า
// ──────────────────────────────────────────
@Composable
fun WatchListNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Search.route) {
            SearchScreen(navController = navController)
        }




        composable(
            route = "all_movies?genreId={genreId}&genreName={genreName}",
            arguments = listOf(
                navArgument("genreId") { type = NavType.IntType; defaultValue = -1 },
                navArgument("genreName") { type = NavType.StringType; defaultValue = "" }
            )
        ) { back ->
            val genreId = back.arguments?.getInt("genreId").takeIf { it != -1 }
            val genreName = back.arguments?.getString("genreName")?.ifEmpty { null }
            AllMoviesScreen(
                navController = navController,
                genreId = genreId,
                genreName = genreName
            )
        }

        composable(Screen.Watchlist.route) {
            WatchlistScreen(navController = navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
        composable(
            route = Screen.MovieDetail.route + "/{movieId}",
            arguments = listOf(navArgument("movieId") { type = NavType.IntType })
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getInt("movieId") ?: return@composable
            MovieDetailScreen(
                movieId = movieId,
                navController = navController
            )
        }

        composable(Screen.Notifications.route) {
            NotificationScreen(navController = navController)
        }

    }
}

// ──────────────────────────────────────────
// Screen Routes
// ──────────────────────────────────────────
sealed class Screen(val route: String) {
    object Home        : Screen("home")
    object Search      : Screen("search")

    object GroupDetail : Screen("group_detail")
    object Watchlist   : Screen("watchlist")
    object Profile     : Screen("profile")
    object MovieDetail : Screen("movie_detail")
    object AllMovies : Screen("all_movies")
    object Notifications : Screen("notifications")
}

// ──────────────────────────────────────────
// Data class BottomNavItem
// ──────────────────────────────────────────
data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)
