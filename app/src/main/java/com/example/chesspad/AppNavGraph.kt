package com.example.chesspad

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun AppNavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val summaryViewModel: SummaryViewModel = viewModel()

    // Use the factory for GameNotesViewModel
    val gameNotesViewModel: GameNotesViewModel = viewModel(
        factory = GameNotesViewModel.GameNotesViewModelFactory(context.applicationContext as ChessPadApplication)
    )

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(onSplashFinished = {
                navController.navigate("sync") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }

        composable("sync") {
            ChessComSyncScreen(
                gameNotesViewModel = gameNotesViewModel,
                onUsernameEntered = { username, startY, startM, endY, endM, games ->
                    summaryViewModel.processGames(username, games)
                    navController.navigate("summary")
                },
                onGoToSummary = {
                    navController.navigate("summary")
                },
                onFilesClick = {
                    navController.navigate("notes")
                }
            )
        }

        composable("summary") {
            SummaryScreen(
                viewModel = summaryViewModel,
                onDone = { /* Optionally handle completion here */ },
                onTryAnotherUser = {
                    navController.navigate("sync") {
                        popUpTo("summary") { inclusive = true }
                    }
                }
            )
        }

        composable("notes") {
            // We use ViewModel to get saved games, so we don't need to pass them here
            val games = emptyList<ChessComGame>() // This will be replaced by data from viewModel
            GameNotesScreen(
                games = games,
                onDone = { /* Handle onDone action */ },
                navController = navController
            )
        }
    }
}