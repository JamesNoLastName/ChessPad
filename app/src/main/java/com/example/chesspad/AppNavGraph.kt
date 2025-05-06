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
                onUsernameEntered = { username, startYear, startMonth, endYear, endMonth ->
                    // TODO: Replace these mock values with actual API results
                    summaryViewModel.setSummaryData(
                        username = username,
                        gamesPlayed = 42,
                        winRate = 65.0f,
                        favoriteOpening = "Queen's Gambit",
                        topOpponent = "chessMaster123"
                    )
                    navController.navigate("summary")
                },
                onGoToSummary = {
                    navController.navigate("summary")
                }
            )
        }

        composable("summary") {
            SummaryScreen(
                username = summaryViewModel.username.collectAsState().value,
                gamesPlayed = summaryViewModel.gamesPlayed.collectAsState().value,
                winRate = summaryViewModel.winRate.collectAsState().value.toInt(),
                favoriteOpening = summaryViewModel.favoriteOpening.collectAsState().value,
                topOpponent = summaryViewModel.topOpponent.collectAsState().value,
                onDone = {
                    // You could navigate somewhere or close the app
                },
                onTryAnotherUser = {
                    summaryViewModel.setSummaryData("", 0, 0f, null, null)
                    navController.navigate("sync") {
                        popUpTo("sync") { inclusive = true }
                    }
                }
            )
        }
    }
}
