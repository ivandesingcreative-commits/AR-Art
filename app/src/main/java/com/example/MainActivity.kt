package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.ArSculptScreen
import com.example.ui.screens.CloudSyncScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LightboxScreen
import com.example.ui.screens.ProjectDetailScreen
import com.example.ui.screens.TimelapseScreen
import com.example.ui.screens.PrintableMarkersScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StudioDarkBg
import com.example.ui.viewmodel.ProjectViewModel

class MainActivity : ComponentActivity() {

    private val projectViewModel: ProjectViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = StudioDarkBg
                ) {
                    ClayStudioAppNavHost(viewModel = projectViewModel)
                }
            }
        }
    }
}

@Composable
fun ClayStudioAppNavHost(viewModel: ProjectViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToLightbox = { navController.navigate("lightbox") },
                onNavigateToAr = { navController.navigate("ar") },
                onNavigateToTimelapse = { navController.navigate("timelapse") },
                onNavigateToProjectDetail = { id -> navController.navigate("project_detail/$id") },
                onNavigateToCloudSync = { navController.navigate("cloud_sync") },
                onNavigateToPrintableMarkers = { navController.navigate("printable_markers") }
            )
        }

        composable("lightbox") {
            LightboxScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNavigateToPrintableMarkers = { navController.navigate("printable_markers") }
            )
        }

        composable("ar") {
            ArSculptScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNavigateToPrintableMarkers = { navController.navigate("printable_markers") }
            )
        }

        composable("printable_markers") {
            PrintableMarkersScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("timelapse") {
            TimelapseScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "project_detail/{projectId}",
            arguments = listOf(navArgument("projectId") { type = NavType.LongType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
            ProjectDetailScreen(
                projectId = projectId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onOpenLightbox = { navController.navigate("lightbox") },
                onOpenAr = { navController.navigate("ar") }
            )
        }

        composable("cloud_sync") {
            CloudSyncScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
