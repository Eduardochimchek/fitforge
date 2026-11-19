package app.fitforge.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

@Serializable data object HomeRoute
@Serializable data object WorkoutsRoute
@Serializable data object LibraryRoute
@Serializable data object ProgressRoute
@Serializable data object ProfileRoute
@Serializable data class ExerciseRoute(val id: String)
@Serializable data class EditExerciseRoute(val id: String? = null)

enum class TopLevel(val route: Any, val label: String, val icon: ImageVector) {
    HOME(HomeRoute, "Início", Icons.Rounded.Home),
    WORKOUTS(WorkoutsRoute, "Treinos", Icons.Rounded.FitnessCenter),
    LIBRARY(LibraryRoute, "Biblioteca", Icons.Rounded.MenuBook),
    PROGRESS(ProgressRoute, "Progresso", Icons.Rounded.Insights),
    PROFILE(ProfileRoute, "Perfil", Icons.Rounded.AccountCircle),
}
