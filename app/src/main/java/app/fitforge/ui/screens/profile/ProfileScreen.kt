package app.fitforge.ui.screens.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import app.fitforge.BuildConfig
import app.fitforge.data.images.ExerciseImageStore
import app.fitforge.data.prefs.AppSettings
import app.fitforge.data.prefs.ThemeMode
import app.fitforge.data.prefs.UserPrefsRepository
import app.fitforge.data.repo.ExerciseRepository
import app.fitforge.domain.model.Equipment
import app.fitforge.ui.components.SelectChip
import app.fitforge.ui.screens.library.toggle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DownloadState(val running: Boolean = false, val done: Int = 0, val total: Int = 0, val storedMb: Double = 0.0, val storedCount: Int = 0)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val prefs: UserPrefsRepository,
    private val repo: ExerciseRepository,
    private val images: ExerciseImageStore,
) : ViewModel() {
    val settings: StateFlow<AppSettings> = prefs.settings.stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())
    private val _download = MutableStateFlow(DownloadState())
    val download: StateFlow<DownloadState> = _download

    init { refreshStored() }

    private fun refreshStored() {
        _download.value = _download.value.copy(storedMb = images.storedBytes() / 1_048_576.0, storedCount = images.storedCount())
    }

    fun setTheme(m: ThemeMode) = viewModelScope.launch { prefs.setTheme(m) }

    fun toggleEquipment(e: Equipment) = viewModelScope.launch {
        prefs.setHomeEquipment(settings.value.homeEquipment.toggle(e.code))
    }

    fun downloadAll() {
        if (_download.value.running) return
        viewModelScope.launch {
            val urls = repo.all.first().flatMap { it.images }
            _download.value = _download.value.copy(running = true, done = 0, total = 0)
            images.download(urls) { d, t -> _download.value = _download.value.copy(done = d, total = t) }
            _download.value = _download.value.copy(running = false)
            refreshStored()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(vm: ProfileViewModel = hiltViewModel()) {
    val s by vm.settings.collectAsStateWithLifecycle()
    val d by vm.download.collectAsStateWithLifecycle()
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(20.dp),
    ) {
        Text("Perfil", style = MaterialTheme.typography.headlineLarge)
        Text(
            "Seus dados, objetivo e plano chegam na versão final. Por enquanto, ajuste o app:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Card("Tema") {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.entries.forEach { m ->
                    SelectChip(
                        m.label, s.themeMode == m, { vm.setTheme(m) },
                        icon = when (m) {
                            ThemeMode.ESCURO -> Icons.Rounded.DarkMode
                            ThemeMode.CLARO -> Icons.Rounded.LightMode
                            ThemeMode.SISTEMA -> Icons.Rounded.PhoneAndroid
                        },
                    )
                }
            }
        }

        Card("O que eu tenho em casa", "Usado no filtro \"Com o que tenho\" e, depois, no gerador de plano.") {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Equipment.entries.filter { it.homeFriendly && it != Equipment.NENHUM }.forEach { e ->
                    SelectChip(e.label, e.code in s.homeEquipment, { vm.toggleEquipment(e) })
                }
            }
        }

        Card(
            "Imagens offline",
            "As imagens vêm da internet e ficam em cache. Baixe todas para usar a biblioteca inteira sem sinal " +
                "(cerca de 1.700 imagens). Os exercícios dos seus treinos são baixados automaticamente.",
        ) {
            Text(
                "Guardadas: ${d.storedCount} imagens · %.1f MB".format(d.storedMb),
                style = MaterialTheme.typography.labelLarge,
            )
            AnimatedVisibility(d.running) {
                Column {
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { if (d.total == 0) 0f else d.done / d.total.toFloat() },
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(50)),
                    )
                    Text("${d.done} de ${d.total}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = vm::downloadAll, enabled = !d.running, shape = RoundedCornerShape(14.dp)) {
                Icon(Icons.Rounded.CloudDownload, null)
                Text("  Baixar todas as imagens")
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(
            "FitForge ${BuildConfig.VERSION_NAME}\nExercícios e imagens: free-exercise-db (domínio público).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Card(title: String, subtitle: String? = null, content: @Composable () -> Unit) {
    Column(
        Modifier
            .padding(top = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(18.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        if (subtitle != null) {
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(12.dp))
        content()
    }
}
