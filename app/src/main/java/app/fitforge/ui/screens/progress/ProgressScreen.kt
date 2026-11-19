package app.fitforge.ui.screens.progress

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.fitforge.ui.components.EmptyState

@Composable
fun ProgressScreen() {
    Column(Modifier.fillMaxSize().statusBarsPadding().padding(20.dp)) {
        Text("Progresso", style = MaterialTheme.typography.headlineLarge)
        EmptyState(
            icon = Icons.Rounded.Insights,
            title = "Sem treinos registrados",
            message = "Quando você concluir os primeiros treinos, a evolução aparece aqui.",
        )
    }
}
