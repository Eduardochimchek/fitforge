package app.fitforge.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import app.fitforge.ui.components.bounceClick
import app.fitforge.ui.theme.Ember
import app.fitforge.ui.theme.EmberDeep
import java.time.LocalTime

@Composable
fun HomeScreen(onOpenLibrary: () -> Unit, onOpenWorkouts: () -> Unit) {
    val hour = LocalTime.now().hour
    val greeting = when (hour) {
        in 5..11 -> "Bom dia"
        in 12..17 -> "Boa tarde"
        else -> "Boa noite"
    }
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(20.dp),
    ) {
        Text(greeting, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Bora treinar?", style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(20.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Brush.linearGradient(listOf(Ember, EmberDeep, Color(0xFFB8360B))))
                .bounceClick(onClick = onOpenWorkouts)
                .padding(22.dp),
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.LocalFireDepartment, null, tint = Color.White)
                    Spacer(Modifier.width(6.dp))
                    Text("Treino de hoje", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.85f))
                }
                Spacer(Modifier.height(10.dp))
                Text("Seu plano aparece aqui", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                Text(
                    "Monte um treino ou escolha um pronto para começar a sequência.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        ShortcutCard(
            icon = Icons.Rounded.Home,
            title = "Treino rápido em casa",
            subtitle = "20 minutos, sem equipamento",
            accent = MaterialTheme.colorScheme.secondary,
            onClick = onOpenWorkouts,
        )
        Spacer(Modifier.height(12.dp))
        ShortcutCard(
            icon = Icons.Rounded.MenuBook,
            title = "Biblioteca de exercícios",
            subtitle = "Busque por músculo, equipamento ou local",
            accent = MaterialTheme.colorScheme.tertiary,
            onClick = onOpenLibrary,
        )
        Spacer(Modifier.height(12.dp))
        ShortcutCard(
            icon = Icons.Rounded.Bolt,
            title = "HIIT para emagrecer",
            subtitle = "Tabata e EMOM com timer",
            accent = MaterialTheme.colorScheme.primary,
            onClick = onOpenWorkouts,
        )
    }
}

@Composable
private fun ShortcutCard(icon: ImageVector, title: String, subtitle: String, accent: Color, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .bounceClick(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            Modifier.size(48.dp).clip(CircleShape).background(accent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, null, tint = accent) }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
