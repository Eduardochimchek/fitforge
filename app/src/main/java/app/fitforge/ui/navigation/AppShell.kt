package app.fitforge.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import app.fitforge.ui.components.bounceClick
import app.fitforge.ui.screens.editexercise.EditExerciseScreen
import app.fitforge.ui.screens.exercise.ExerciseDetailScreen
import app.fitforge.ui.screens.home.HomeScreen
import app.fitforge.ui.screens.library.LibraryScreen
import app.fitforge.ui.screens.profile.ProfileScreen
import app.fitforge.ui.screens.progress.ProgressScreen
import app.fitforge.ui.screens.workouts.WorkoutsScreen

@Composable
fun AppShell() {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val dest = entry?.destination
    val current = TopLevel.entries.firstOrNull { t -> dest?.hasRoute(t.route::class) == true }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AnimatedVisibility(
                visible = current != null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
            ) {
                BottomBar(current) { target -> nav.navigateTopLevel(target) }
            }
        },
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = HomeRoute,
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding()),
            enterTransition = { slideInHorizontally(tween(320, easing = FastOutSlowInEasing)) { it / 5 } + fadeIn(tween(320)) },
            exitTransition = { fadeOut(tween(200)) },
            popEnterTransition = { fadeIn(tween(250)) },
            popExitTransition = { slideOutHorizontally(tween(280)) { it / 5 } + fadeOut(tween(280)) },
        ) {
            topLevel<HomeRoute> {
                HomeScreen(
                    onOpenLibrary = { nav.navigateTopLevel(TopLevel.LIBRARY) },
                    onOpenWorkouts = { nav.navigateTopLevel(TopLevel.WORKOUTS) },
                )
            }
            topLevel<WorkoutsRoute> { WorkoutsScreen(onOpenLibrary = { nav.navigateTopLevel(TopLevel.LIBRARY) }) }
            topLevel<LibraryRoute> {
                LibraryScreen(
                    onOpenExercise = { nav.navigate(ExerciseRoute(it)) },
                    onCreateExercise = { nav.navigate(EditExerciseRoute()) },
                )
            }
            topLevel<ProgressRoute> { ProgressScreen() }
            topLevel<ProfileRoute> { ProfileScreen() }
            composable<ExerciseRoute> { e ->
                val id = e.toRoute<ExerciseRoute>().id
                ExerciseDetailScreen(
                    onBack = { nav.popBackStack() },
                    onEdit = { nav.navigate(EditExerciseRoute(id)) },
                )
            }
            composable<EditExerciseRoute> {
                EditExerciseScreen(
                    onBack = { nav.popBackStack() },
                    onSaved = { id ->
                        nav.popBackStack()
                        if (it.toRoute<EditExerciseRoute>().id == null) nav.navigate(ExerciseRoute(id))
                    },
                    onDeleted = { nav.popBackStack(LibraryRoute, inclusive = false) },
                )
            }
        }
    }
}

private inline fun <reified T : Any> androidx.navigation.NavGraphBuilder.topLevel(
    noinline content: @Composable () -> Unit,
) = composable<T>(
    enterTransition = { fadeIn(tween(220)) + scaleIn(tween(220), initialScale = 0.98f) },
    exitTransition = { fadeOut(tween(150)) },
    popEnterTransition = { fadeIn(tween(220)) },
    popExitTransition = { fadeOut(tween(150)) },
) { content() }

private fun NavHostController.navigateTopLevel(target: TopLevel) {
    navigate(target.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun BottomBar(current: TopLevel?, onSelect: (TopLevel) -> Unit) {
    Column(Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow)) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
        Row(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(68.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TopLevel.entries.forEach { item ->
                val selected = item == current
                val scale by animateFloatAsState(
                    if (selected) 1.08f else 1f,
                    spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
                    label = "s",
                )
                val pill by animateColorAsState(
                    if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                    label = "pill",
                )
                val tint by animateColorAsState(
                    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "tint",
                )
                Column(
                    Modifier
                        .weight(1f)
                        .bounceClick { onSelect(item) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        Modifier
                            .graphicsLayer { scaleX = scale; scaleY = scale }
                            .clip(RoundedCornerShape(50))
                            .background(pill)
                            .padding(horizontal = 18.dp, vertical = 5.dp),
                    ) {
                        Icon(item.icon, contentDescription = item.label, tint = tint, modifier = Modifier.size(24.dp))
                    }
                    Text(
                        item.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = tint,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
            }
        }
    }
}
