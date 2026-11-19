package app.fitforge.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.fitforge.R

private fun sora(weight: FontWeight) = Font(
    R.font.sora,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

val Sora = FontFamily(
    sora(FontWeight.Normal),
    sora(FontWeight.Medium),
    sora(FontWeight.SemiBold),
    sora(FontWeight.Bold),
    sora(FontWeight.ExtraBold),
)

private val base = Typography()

val AppTypography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = Sora, fontWeight = FontWeight.ExtraBold, letterSpacing = (-1.5).sp),
    displayMedium = base.displayMedium.copy(fontFamily = Sora, fontWeight = FontWeight.ExtraBold, letterSpacing = (-1).sp),
    displaySmall = base.displaySmall.copy(fontFamily = Sora, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
    headlineLarge = base.headlineLarge.copy(fontFamily = Sora, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
    headlineMedium = base.headlineMedium.copy(fontFamily = Sora, fontWeight = FontWeight.Bold, letterSpacing = (-0.25).sp),
    headlineSmall = base.headlineSmall.copy(fontFamily = Sora, fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.copy(fontFamily = Sora, fontWeight = FontWeight.Bold),
    titleMedium = base.titleMedium.copy(fontFamily = Sora, fontWeight = FontWeight.SemiBold),
    titleSmall = base.titleSmall.copy(fontFamily = Sora, fontWeight = FontWeight.SemiBold),
    bodyLarge = base.bodyLarge,
    bodyMedium = base.bodyMedium,
    bodySmall = base.bodySmall,
    labelLarge = base.labelLarge.copy(fontFamily = Sora, fontWeight = FontWeight.SemiBold),
    labelMedium = base.labelMedium.copy(fontFamily = Sora, fontWeight = FontWeight.Medium),
    labelSmall = base.labelSmall.copy(fontFamily = Sora, fontWeight = FontWeight.Medium),
)

val NumberStyle = TextStyle(fontFamily = Sora, fontWeight = FontWeight.ExtraBold, letterSpacing = (-1).sp)
