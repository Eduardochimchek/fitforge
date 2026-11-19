package app.fitforge.ui.screens.exercise

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import app.fitforge.data.repo.ExerciseRepository
import app.fitforge.domain.model.Exercise
import app.fitforge.domain.model.muscleLabel
import app.fitforge.ui.components.ExerciseImage
import app.fitforge.ui.components.LevelBars
import app.fitforge.ui.components.Tag
import app.fitforge.ui.components.bounceClick
import app.fitforge.ui.navigation.ExerciseRoute
import app.fitforge.ui.screens.library.FavoriteButton
import app.fitforge.ui.theme.CategoryColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseDetailViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val repo: ExerciseRepository,
) : ViewModel() {
    private val id = savedState.toRoute<ExerciseRoute>().id
    val exercise: StateFlow<Exercise?> = repo.observe(id).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun toggleFavorite() {
        val e = exercise.value ?: return
        viewModelScope.launch { repo.setFavorite(e.id, !e.favorite) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExerciseDetailScreen(
    onBack: () -> Unit,
    onEdit: () -> Unit,
    vm: ExerciseDetailViewModel = hiltViewModel(),
) {
    val e by vm.exercise.collectAsStateWithLifecycle()
    val ex = e ?: return Box(Modifier.fillMaxSize())
    val context = LocalContext.current
    val catColor = CategoryColors[ex.category.code] ?: MaterialTheme.colorScheme.primary

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(bottom = 96.dp),
        ) {
            ExerciseImage(
                images = ex.images,
                category = ex.category,
                iconSize = 72.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.1f)
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)),
            )
            Column(Modifier.padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(20.dp))
                Text(ex.name, style = MaterialTheme.typography.headlineMedium)
                if (ex.nameEn != ex.name && !ex.custom) {
                    Text(ex.nameEn, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(14.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Tag(ex.category.label, color = catColor, icon = ex.category.icon, filled = true)
                    Row(
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        LevelBars(ex.level.bars, color = catColor)
                        Text(ex.level.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Tag(ex.equipment.label)
                    if (ex.homeFriendly) Tag("Dá pra fazer em casa", icon = Icons.Rounded.Home, color = MaterialTheme.colorScheme.secondary, filled = true)
                    if (ex.timeBased) Tag("Por tempo", icon = Icons.Rounded.Timer)
                }

                SectionTitle("Músculos")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ex.primaryMuscles.forEach { MuscleChip(muscleLabel(it), primary = true) }
                    ex.secondaryMuscles.forEach { MuscleChip(muscleLabel(it), primary = false) }
                }

                if (ex.instructions.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SectionTitle("Como fazer", Modifier.weight(1f))
                        if (!ex.translated) {
                            Tag("em inglês", icon = Icons.Rounded.Translate, modifier = Modifier.padding(top = 18.dp))
                        }
                    }
                    ex.instructions.forEachIndexed { i, step -> Step(i + 1, step, catColor) }
                }

                ex.tip?.let { tip ->
                    Spacer(Modifier.height(12.dp))
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(14.dp),
                    ) {
                        Icon(Icons.Rounded.Lightbulb, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        Spacer(Modifier.width(10.dp))
                        Text(tip, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            RoundIcon(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Voltar", tint = Color.White) }
            Row {
                if (ex.custom) {
                    RoundIcon(onClick = onEdit) { Icon(Icons.Rounded.Edit, "Editar", tint = Color.White) }
                    Spacer(Modifier.width(8.dp))
                }
                Box(Modifier.clip(CircleShape).background(Color.Black.copy(alpha = 0.45f))) {
                    FavoriteButton(ex.favorite, vm::toggleFavorite)
                }
            }
        }

        Button(
            onClick = {
                val url = ex.videoUrl?.takeIf { it.isNotBlank() } ?: ex.youtubeSearchUrl
                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri())) }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(20.dp)
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Icon(Icons.Rounded.PlayCircle, null)
            Spacer(Modifier.width(8.dp))
            Text(if (ex.videoUrl.isNullOrBlank()) "Ver vídeo no YouTube" else "Ver meu vídeo", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RoundIcon(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.45f))
            .bounceClick(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier.padding(top = 24.dp, bottom = 10.dp),
    )
}

@Composable
private fun MuscleChip(label: String, primary: Boolean) {
    val c = MaterialTheme.colorScheme.primary
    Text(
        label,
        style = MaterialTheme.typography.labelLarge,
        color = if (primary) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (primary) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 12.dp, vertical = 7.dp),
    )
}

@Composable
private fun Step(n: Int, text: String, color: Color) {
    Row(Modifier.padding(vertical = 6.dp)) {
        Box(
            Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Text("$n", style = MaterialTheme.typography.labelLarge, color = color)
        }
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 3.dp))
    }
}
