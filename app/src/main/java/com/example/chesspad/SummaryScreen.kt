package com.example.chesspad

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SummaryScreen(
    username: String,
    gamesPlayed: Int,
    winRate: Int,
    favoriteOpening: String?,
    topOpponent: String?,
    onDone: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF6650A4), Color(0xFFEFB8C8))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(24.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Your ChessPad Wrapped, $username!",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF6650A4),
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                Text("Games Played: $gamesPlayed", fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Win Rate: $winRate%", fontSize = 22.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Favorite Opening: ${favoriteOpening ?: "N/A"}", fontSize = 20.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Top Opponent: ${topOpponent ?: "N/A"}", fontSize = 20.sp)
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onDone) {
                    Text("Done")
                }
            }
        }
    }
}
