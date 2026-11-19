package app.fitforge.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.fitforge.data.images.ExerciseImageStore
import app.fitforge.domain.model.Category
import app.fitforge.ui.theme.CategoryColors
import coil3.compose.SubcomposeAsyncImage
import kotlinx.coroutines.delay

val LocalImageStore = staticCompositionLocalOf<ExerciseImageStore?> { null }

@Composable
fun ExerciseImage(
    images: List<String>,
    category: Category,
    modifier: Modifier = Modifier,
    animate: Boolean = true,
    intervalMs: Long = 1100,
    iconSize: Dp = 32.dp,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val store = LocalImageStore.current
    var index by remember(images) { mutableIntStateOf(0) }
    if (animate && images.size > 1) {
        LaunchedEffect(images) {
            while (true) {
                delay(intervalMs)
                index = (index + 1) % images.size
            }
        }
    }
    val bg = Color.White
    Box(modifier.background(bg), contentAlignment = Alignment.Center) {
        if (images.isEmpty()) {
            Placeholder(category, iconSize)
        } else {
            Crossfade(targetState = index, animationSpec = tween(350), label = "frame") { i ->
                val url = images[i.coerceIn(0, images.lastIndex)]
                val model = remember(url) { store?.resolve(url) ?: url }
                SubcomposeAsyncImage(
                    model = model,
                    contentDescription = null,
                    contentScale = contentScale,
                    modifier = Modifier.fillMaxSize(),
                    loading = { Placeholder(category, iconSize, loading = true) },
                    error = { Placeholder(category, iconSize) },
                )
            }
        }
    }
}

@Composable
private fun Placeholder(category: Category, iconSize: Dp, loading: Boolean = false) {
    val c = CategoryColors[category.code] ?: MaterialTheme.colorScheme.primary
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(c.copy(alpha = 0.35f), c.copy(alpha = 0.08f)))),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            category.icon,
            contentDescription = null,
            tint = c.copy(alpha = if (loading) 0.4f else 0.9f),
            modifier = Modifier.fillMaxWidth(0.4f),
        )
    }
}
