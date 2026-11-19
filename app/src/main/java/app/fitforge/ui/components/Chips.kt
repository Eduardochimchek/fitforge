package app.fitforge.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun SelectChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accent: Color = MaterialTheme.colorScheme.primary,
) {
    val bg by animateColorAsState(if (selected) accent else MaterialTheme.colorScheme.surfaceContainerHigh, label = "bg")
    val fg by animateColorAsState(
        if (selected) Color.White else MaterialTheme.colorScheme.onSurface, label = "fg",
    )
    val border by animateDpAsState(if (selected) 0.dp else 1.dp, label = "border")
    Row(
        modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(border, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(50))
            .bounceClick(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, null, tint = fg, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(label, style = MaterialTheme.typography.labelLarge, color = fg)
    }
}

@Composable
fun Tag(
    label: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    icon: ImageVector? = null,
    filled: Boolean = false,
) {
    Row(
        modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (filled) color.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (icon != null) Icon(icon, null, tint = color, modifier = Modifier.size(13.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            maxLines = 1,
            softWrap = false,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun LevelBars(bars: Int, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.Bottom) {
        for (i in 1..3) {
            Box(
                Modifier
                    .width(4.dp)
                    .height((4 + i * 3).dp)
                    .clip(CircleShape)
                    .background(if (i <= bars) color else MaterialTheme.colorScheme.outline),
            )
        }
    }
}
