package app.fitforge.ui.screens.workouts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.fitforge.ui.components.EmptyState

@Composable
fun WorkoutsScreen(onOpenLibrary: () -> Unit) {
    Column(Modifier.fillMaxSize().statusBarsPadding().padding(20.dp)) {
        Text("Treinos", style = MaterialTheme.typography.headlineLarge)
        EmptyState(
            icon = Icons.Rounded.FitnessCenter,
            title = "Montador e treinos prontos",
            message = "Chegam na próxima versão. Enquanto isso, explore a biblioteca e marque seus favoritos.",
            action = { Button(onClick = onOpenLibrary) { Text("Abrir biblioteca") } },
        )
    }
}
