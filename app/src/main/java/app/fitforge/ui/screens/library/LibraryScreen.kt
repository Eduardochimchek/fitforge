package app.fitforge.ui.screens.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Backpack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.fitforge.domain.model.Category
import app.fitforge.domain.model.Equipment
import app.fitforge.domain.model.Exercise
import app.fitforge.domain.model.Level
import app.fitforge.domain.model.Muscle
import app.fitforge.domain.model.Place
import app.fitforge.domain.model.muscleLabel
import app.fitforge.ui.components.EmptyState
import app.fitforge.ui.components.ExerciseImage
import app.fitforge.ui.components.LevelBars
import app.fitforge.ui.components.SelectChip
import app.fitforge.ui.components.Tag
import app.fitforge.ui.components.bounceClick
import app.fitforge.ui.theme.CategoryColors

@Composable
fun LibraryScreen(
    onOpenExercise: (String) -> Unit,
    onCreateExercise: () -> Unit,
    vm: LibraryViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val query by vm.rawQuery.collectAsStateWithLifecycle()
    var showSheet by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val expandedFab by remember { androidx.compose.runtime.derivedStateOf { listState.firstVisibleItemIndex == 0 } }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(bottom = 96.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
                Column(Modifier.statusBarsPadding().padding(start = 20.dp, end = 20.dp, top = 16.dp)) {
                    Text("Biblioteca", style = MaterialTheme.typography.headlineLarge)
                    Text(
                        if (state.loading) "Carregando…" else "${state.total} exercícios",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(14.dp))
                    SearchField(query, vm::setQuery)
                }
            }
            item {
                QuickFilters(
                    filters = state.filters,
                    onUpdate = vm::updateFilters,
                    onOpenSheet = { showSheet = true },
                )
            }
            item {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "${state.results.size} resultado" + if (state.results.size == 1) "" else "s",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    AnimatedVisibility(!state.filters.isEmpty) {
                        TextButton(onClick = vm::clearFilters) { Text("Limpar filtros") }
                    }
                }
            }
            if (!state.loading && state.results.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Rounded.SearchOff,
                        title = "Nada por aqui",
                        message = "Tente outro termo ou tire algum filtro.",
                    )
                }
            }
            items(state.results, key = { it.id }) { e ->
                ExerciseRow(
                    e,
                    onClick = { onOpenExercise(e.id) },
                    onFavorite = { vm.toggleFavorite(e) },
                    modifier = Modifier.animateItem(),
                )
            }
        }

        ExtendedFloatingActionButton(
            onClick = onCreateExercise,
            expanded = expandedFab,
            icon = { Icon(Icons.Rounded.Add, null) },
            text = { Text("Criar exercício") },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
        )
    }

    if (showSheet) {
        FilterSheet(
            filters = state.filters,
            resultCount = state.results.size,
            onUpdate = vm::updateFilters,
            onClear = vm::clearFilters,
            onDismiss = { showSheet = false },
        )
    }
}

@Composable
private fun SearchField(value: String, onChange: (String) -> Unit) {
    TextField(
        value = value,
        onValueChange = onChange,
        placeholder = { Text("Buscar exercício ou músculo", maxLines = 1) },
        leadingIcon = { Icon(Icons.Rounded.Search, null) },
        trailingIcon = {
            AnimatedVisibility(value.isNotEmpty(), enter = scaleIn() + fadeIn(), exit = scaleOut() + fadeOut()) {
                IconButton(onClick = { onChange("") }) { Icon(Icons.Rounded.Close, "Limpar") }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        shape = RoundedCornerShape(18.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun QuickFilters(
    filters: LibraryFilters,
    onUpdate: ((LibraryFilters) -> LibraryFilters) -> Unit,
    onOpenSheet: () -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            SelectChip(
                label = if (filters.sheetCount > 0) "Filtros · ${filters.sheetCount}" else "Filtros",
                selected = filters.sheetCount > 0,
                onClick = onOpenSheet,
                icon = Icons.Rounded.Tune,
            )
        }
        item {
            SelectChip("Em casa", filters.place == Place.CASA, {
                onUpdate { it.copy(place = if (it.place == Place.CASA) null else Place.CASA) }
            }, icon = Icons.Rounded.Home)
        }
        item {
            SelectChip("Com o que tenho", filters.withMyEquipment, {
                onUpdate { it.copy(withMyEquipment = !it.withMyEquipment) }
            }, icon = Icons.Rounded.Backpack)
        }
        item {
            SelectChip("Academia", filters.place == Place.ACADEMIA, {
                onUpdate { it.copy(place = if (it.place == Place.ACADEMIA) null else Place.ACADEMIA) }
            })
        }
        item {
            SelectChip("Favoritos", filters.favorites, {
                onUpdate { it.copy(favorites = !it.favorites) }
            }, icon = Icons.Rounded.Favorite)
        }
        item {
            SelectChip("Meus", filters.customOnly, {
                onUpdate { it.copy(customOnly = !it.customOnly) }
            }, icon = Icons.Rounded.Person)
        }
        items(Category.entries) { c ->
            SelectChip(
                c.label,
                c in filters.categories,
                { onUpdate { it.copy(categories = it.categories.toggle(c)) } },
                icon = c.icon,
                accent = CategoryColors[c.code] ?: MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
fun ExerciseRow(
    e: Exercise,
    onClick: () -> Unit,
    onFavorite: (() -> Unit)?,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    val catColor = CategoryColors[e.category.code] ?: MaterialTheme.colorScheme.primary
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .bounceClick(pressedScale = 0.98f, onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ExerciseImage(
            images = e.images,
            category = e.category,
            animate = false,
            modifier = Modifier.size(72.dp).clip(RoundedCornerShape(14.dp)),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                e.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (e.translated && !e.custom && e.nameEn != e.name) {
                Text(
                    e.nameEn,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Tag(e.category.label, color = catColor, filled = true)
                LevelBars(e.level.bars, color = catColor)
                e.primaryMuscles.firstOrNull()?.let { Tag(muscleLabel(it), modifier = Modifier.weight(1f, fill = false)) }
            }
        }
        if (trailing != null) trailing()
        else if (onFavorite != null) FavoriteButton(e.favorite, onFavorite)
    }
}

@Composable
fun FavoriteButton(favorite: Boolean, onToggle: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val scale by animateFloatAsState(if (favorite) 1.15f else 1f, label = "fav")
    IconButton(onClick = {
        haptic.performHapticFeedback(if (favorite) HapticFeedbackType.ToggleOff else HapticFeedbackType.ToggleOn)
        onToggle()
    }) {
        Icon(
            if (favorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
            contentDescription = "Favorito",
            tint = if (favorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun FilterSheet(
    filters: LibraryFilters,
    resultCount: Int,
    onUpdate: ((LibraryFilters) -> LibraryFilters) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .animateContentSize(),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Filtros", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                TextButton(onClick = onClear) { Text("Limpar") }
            }
            SheetSection("Categoria") {
                Category.entries.forEach { c ->
                    SelectChip(
                        c.label, c in filters.categories,
                        { onUpdate { it.copy(categories = it.categories.toggle(c)) } },
                        icon = c.icon,
                        accent = CategoryColors[c.code] ?: MaterialTheme.colorScheme.primary,
                    )
                }
            }
            SheetSection("Músculo") {
                Muscle.entries.sortedBy { it.group.ordinal }.forEach { m ->
                    SelectChip(m.label, m in filters.muscles, { onUpdate { it.copy(muscles = it.muscles.toggle(m)) } })
                }
            }
            SheetSection("Equipamento") {
                Equipment.entries.forEach { eq ->
                    SelectChip(eq.label, eq in filters.equipment, { onUpdate { it.copy(equipment = it.equipment.toggle(eq)) } })
                }
            }
            SheetSection("Nível") {
                Level.entries.forEach { l ->
                    SelectChip(l.label, l in filters.levels, { onUpdate { it.copy(levels = it.levels.toggle(l)) } })
                }
            }
            SheetSection("Idioma") {
                SelectChip("Só os em português", filters.translatedOnly, {
                    onUpdate { it.copy(translatedOnly = !it.translatedOnly) }
                })
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text("Ver $resultCount exercícios", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SheetSection(title: String, content: @Composable () -> Unit) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
    )
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        content()
    }
}

fun <T> Set<T>.toggle(item: T): Set<T> = if (item in this) this - item else this + item
