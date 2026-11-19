package app.fitforge.ui.screens.editexercise

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import app.fitforge.data.db.ExerciseEntity
import app.fitforge.data.images.ExerciseImageStore
import app.fitforge.data.repo.ExerciseRepository
import app.fitforge.domain.model.Category
import app.fitforge.domain.model.Equipment
import app.fitforge.domain.model.Level
import app.fitforge.domain.model.Muscle
import app.fitforge.domain.model.fromCode
import app.fitforge.ui.components.SelectChip
import app.fitforge.ui.navigation.EditExerciseRoute
import app.fitforge.ui.screens.library.toggle
import app.fitforge.ui.theme.CategoryColors
import coil3.compose.AsyncImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class EditState(
    val loaded: Boolean = false,
    val isNew: Boolean = true,
    val id: String = "c_" + UUID.randomUUID().toString().take(8),
    val name: String = "",
    val category: Category = Category.CALISTENIA,
    val level: Level = Level.INICIANTE,
    val equipment: Equipment = Equipment.NENHUM,
    val primary: Set<Muscle> = emptySet(),
    val secondary: Set<Muscle> = emptySet(),
    val homeFriendly: Boolean = true,
    val timeBased: Boolean = false,
    val instructions: String = "",
    val images: List<String> = emptyList(),
    val videoUrl: String = "",
    val tip: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val favorite: Boolean = false,
) {
    val canSave: Boolean get() = name.isNotBlank() && primary.isNotEmpty()
}

@HiltViewModel
class EditExerciseViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val repo: ExerciseRepository,
    private val images: ExerciseImageStore,
) : ViewModel() {
    private val editId = savedState.toRoute<EditExerciseRoute>().id
    private val _state = MutableStateFlow(EditState())
    val state: StateFlow<EditState> = _state

    init {
        viewModelScope.launch {
            val e = editId?.let { repo.getEntity(it) }
            _state.value = if (e == null) EditState(loaded = true) else EditState(
                loaded = true,
                isNew = false,
                id = e.id,
                name = e.name,
                category = fromCode(e.category, Category.CALISTENIA),
                level = fromCode(e.level, Level.INICIANTE),
                equipment = fromCode(e.equipment, Equipment.NENHUM),
                primary = Muscle.entries.filter { it.code in e.primaryMuscles }.toSet(),
                secondary = Muscle.entries.filter { it.code in e.secondaryMuscles }.toSet(),
                homeFriendly = e.homeFriendly,
                timeBased = e.timeBased,
                instructions = e.instructions.joinToString("\n"),
                images = e.images,
                videoUrl = e.videoUrl.orEmpty(),
                tip = e.tip.orEmpty(),
                createdAt = e.createdAt,
                favorite = e.favorite,
            )
        }
    }

    fun update(block: (EditState) -> EditState) = _state.update(block)

    fun addImages(uris: List<Uri>) {
        viewModelScope.launch {
            val id = _state.value.id
            val stored = uris.map { images.importFromGallery(it, id) }
            _state.update { it.copy(images = (it.images + stored).take(4)) }
        }
    }

    fun save(onDone: (String) -> Unit) {
        val s = _state.value
        if (!s.canSave) return
        viewModelScope.launch {
            repo.saveCustom(
                ExerciseEntity(
                    id = s.id,
                    name = s.name.trim(),
                    nameEn = s.name.trim(),
                    category = s.category.code,
                    level = s.level.code,
                    equipment = s.equipment.code,
                    primaryMuscles = s.primary.map { it.code },
                    secondaryMuscles = (s.secondary - s.primary).map { it.code },
                    homeFriendly = s.homeFriendly,
                    timeBased = s.timeBased,
                    instructions = s.instructions.lines().map { it.trim().trimStart('-', '•', ' ') }.filter { it.isNotBlank() },
                    images = s.images,
                    tip = s.tip.trim().ifBlank { null },
                    translated = true,
                    custom = true,
                    videoUrl = s.videoUrl.trim().ifBlank { null },
                    favorite = s.favorite,
                    createdAt = s.createdAt,
                ),
            )
            onDone(s.id)
        }
    }

    fun delete(onDone: () -> Unit) {
        viewModelScope.launch {
            repo.deleteCustom(_state.value.id)
            onDone()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditExerciseScreen(
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
    onDeleted: () -> Unit,
    vm: EditExerciseViewModel = hiltViewModel(),
) {
    val s by vm.state.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(4)) { uris ->
        if (uris.isNotEmpty()) vm.addImages(uris)
    }
    LaunchedEffect(s.equipment) {
        if (s.loaded && s.isNew) vm.update { it.copy(homeFriendly = it.equipment.homeFriendly) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (s.isNew) "Novo exercício" else "Editar exercício") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Voltar") } },
                actions = {
                    if (!s.isNew) IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Rounded.Delete, "Excluir") }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { pad ->
        if (!s.loaded) return@Scaffold
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Label("Fotos")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                s.images.forEach { img ->
                    Box(Modifier.size(96.dp)) {
                        AsyncImage(
                            model = img,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                        )
                        Box(
                            Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            IconButton(onClick = { vm.update { it.copy(images = it.images - img) } }) {
                                Icon(Icons.Rounded.Close, "Remover", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
                if (s.images.size < 4) {
                    Box(
                        Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        IconButton(
                            onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            Icon(Icons.Rounded.AddPhotoAlternate, "Adicionar foto", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
            Text(
                "Com 2 fotos (início e fim do movimento) a imagem fica animada, igual aos outros exercícios.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )

            Label("Nome")
            OutlinedTextField(
                value = s.name,
                onValueChange = { v -> vm.update { it.copy(name = v) } },
                placeholder = { Text("Ex.: Flexão com mochila") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            )

            Label("Categoria")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Category.entries.forEach { c ->
                    SelectChip(c.label, s.category == c, { vm.update { it.copy(category = c) } }, icon = c.icon,
                        accent = CategoryColors[c.code] ?: MaterialTheme.colorScheme.primary)
                }
            }

            Label("Músculos principais")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Muscle.entries.forEach { m ->
                    SelectChip(m.label, m in s.primary, { vm.update { it.copy(primary = it.primary.toggle(m)) } })
                }
            }

            Label("Músculos secundários")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Muscle.entries.filter { it !in s.primary }.forEach { m ->
                    SelectChip(m.label, m in s.secondary, { vm.update { it.copy(secondary = it.secondary.toggle(m)) } },
                        accent = MaterialTheme.colorScheme.tertiary)
                }
            }

            Label("Equipamento")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Equipment.entries.forEach { eq ->
                    SelectChip(eq.label, s.equipment == eq, { vm.update { it.copy(equipment = eq) } })
                }
            }

            Label("Nível")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Level.entries.forEach { l -> SelectChip(l.label, s.level == l, { vm.update { it.copy(level = l) } }) }
            }

            Spacer(Modifier.height(12.dp))
            SwitchRow("Dá pra fazer em casa", s.homeFriendly) { v -> vm.update { it.copy(homeFriendly = v) } }
            SwitchRow("Faço por tempo (não por repetições)", s.timeBased) { v -> vm.update { it.copy(timeBased = v) } }

            Label("Como fazer (um passo por linha)")
            OutlinedTextField(
                value = s.instructions,
                onValueChange = { v -> vm.update { it.copy(instructions = v) } },
                minLines = 4,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            )

            Label("Dica (opcional)")
            OutlinedTextField(
                value = s.tip,
                onValueChange = { v -> vm.update { it.copy(tip = v) } },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            )

            Label("Link do vídeo (opcional)")
            OutlinedTextField(
                value = s.videoUrl,
                onValueChange = { v -> vm.update { it.copy(videoUrl = v) } },
                placeholder = { Text("https://youtube.com/…") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { vm.save(onSaved) },
                enabled = s.canSave,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                Text("Salvar exercício", fontWeight = FontWeight.Bold)
            }
            if (!s.canSave) {
                Text(
                    "Preencha o nome e pelo menos um músculo principal.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            Spacer(Modifier.navigationBarsPadding().height(32.dp))
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Excluir exercício?") },
            text = { Text("\"${s.name}\" vai sumir da biblioteca. Isso não dá para desfazer.") },
            confirmButton = { TextButton(onClick = { confirmDelete = false; vm.delete(onDeleted) }) { Text("Excluir") } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun Label(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
    )
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
