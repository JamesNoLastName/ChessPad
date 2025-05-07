package com.example.chesspad

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun AppNavGraph(navController: NavHostController) {
    val summaryViewModel: SummaryViewModel = viewModel()

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
            GameNotesScreen(
                games = listOf(), // Pass the required data
                onDone = { /* Handle onDone action */ },
                navController = navController // Pass navController to allow navigation
            )
        }
    }
}
