package com.gymtracker.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.gymtracker.ui.theme.Background
import com.gymtracker.ui.theme.CardBorder
import com.gymtracker.ui.theme.Surface
import com.gymtracker.ui.theme.TextPrimary
import com.gymtracker.ui.theme.TextSecondary
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gymtracker.domain.model.Exercise
import com.gymtracker.ui.screens.exercises.ExercisesScreen
import com.gymtracker.ui.screens.home.HomeScreen
import com.gymtracker.ui.screens.routines.RoutineEditorScreen
import com.gymtracker.ui.screens.routines.RoutinesScreen
import com.gymtracker.ui.screens.settings.SettingsScreen
import com.gymtracker.ui.screens.workout.WorkoutScreen
import com.gymtracker.ui.screens.workout.WorkoutViewModel
import com.gymtracker.ui.screens.workoutdetail.WorkoutDetailScreen

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Home : Screen(
        route = "home",
        title = "Home",
        icon = Icons.Outlined.GridView
    )
    data object Workout : Screen(
        route = "workout",
        title = "Workout",
        icon = Icons.Outlined.FitnessCenter
    )
    data object Routines : Screen(
        route = "routines",
        title = "Routines",
        icon = Icons.Outlined.CalendarMonth
    )
    data object Exercises : Screen(
        route = "exercises",
        title = "Exercises",
        icon = Icons.AutoMirrored.Outlined.List
    )
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Workout,
    Screen.Routines,
    Screen.Exercises
)

// Routes that are not in bottom nav
object Routes {
    const val EXERCISE_PICKER = "exercise_picker"
    const val ROUTINE_EDITOR = "routine_editor/{routineId}"
    const val ROUTINE_EXERCISE_PICKER = "routine_exercise_picker"
    const val SETTINGS = "settings"
    const val WORKOUT_DETAIL = "workout_detail/{workoutId}"

    fun routineEditor(routineId: Long = 0) = "routine_editor/$routineId"
    fun workoutDetail(workoutId: Long) = "workout_detail/$workoutId"
}

@Composable
fun GymTrackerNavHost(
    navController: NavHostController = rememberNavController()
) {
    Scaffold(
        bottomBar = {
            GymTrackerBottomBar(navController = navController)
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Home Screen
            composable(Screen.Home.route) {
                HomeScreen(
                    onStartWorkout = {
                        navController.navigate(Screen.Workout.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onContinueWorkout = {
                        navController.navigate(Screen.Workout.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToSettings = {
                        navController.navigate(Routes.SETTINGS)
                    },
                    onWorkoutClick = { workoutId ->
                        navController.navigate(Routes.workoutDetail(workoutId))
                    }
                )
            }

            // Workout Screen
            composable(Screen.Workout.route) { backStackEntry ->
                val selectedExercise = backStackEntry.savedStateHandle.get<Exercise>("selected_exercise")
                val routineIdToStart = backStackEntry.savedStateHandle.get<Long>("start_routine_id")
                val viewModel: WorkoutViewModel = hiltViewModel()

                // Handle starting from routine
                LaunchedEffect(routineIdToStart) {
                    if (routineIdToStart != null && routineIdToStart > 0) {
                        viewModel.startFromRoutine(routineIdToStart)
                        backStackEntry.savedStateHandle.remove<Long>("start_routine_id")
                    }
                }

                WorkoutScreen(
                    selectedExercise = selectedExercise,
                    onExerciseHandled = {
                        backStackEntry.savedStateHandle.remove<Exercise>("selected_exercise")
                    },
                    onNavigateToExercises = {
                        navController.navigate(Routes.EXERCISE_PICKER)
                    },
                    viewModel = viewModel
                )
            }

            // Routines List Screen
            composable(Screen.Routines.route) {
                RoutinesScreen(
                    onCreateRoutine = {
                        navController.navigate(Routes.routineEditor(0))
                    },
                    onEditRoutine = { routineId ->
                        navController.navigate(Routes.routineEditor(routineId))
                    },
                    onStartRoutine = { routineId ->
                        // Navigate to workout and pass routine ID
                        navController.navigate(Screen.Workout.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                        }
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("start_routine_id", routineId)
                    }
                )
            }

            // Routine Editor Screen
            composable(
                route = Routes.ROUTINE_EDITOR,
                arguments = listOf(
                    navArgument("routineId") {
                        type = NavType.LongType
                        defaultValue = 0L
                    }
                )
            ) { backStackEntry ->
                val selectedExercise = backStackEntry.savedStateHandle.get<Exercise>("selected_exercise")

                RoutineEditorScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onAddExercise = {
                        navController.navigate(Routes.ROUTINE_EXERCISE_PICKER)
                    },
                    selectedExercise = selectedExercise,
                    onExerciseHandled = {
                        backStackEntry.savedStateHandle.remove<Exercise>("selected_exercise")
                    }
                )
            }

            // Exercises Screen (browse)
            composable(Screen.Exercises.route) {
                ExercisesScreen(
                    onExerciseClick = { _ ->
                        // In browse mode, show details (not implemented yet)
                    }
                )
            }

            // Exercise Picker (for workout)
            composable(Routes.EXERCISE_PICKER) {
                ExercisePickerScreen(
                    onExerciseSelected = { exercise ->
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("selected_exercise", exercise)
                        navController.popBackStack()
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Exercise Picker (for routine editor)
            composable(Routes.ROUTINE_EXERCISE_PICKER) {
                ExercisePickerScreen(
                    onExerciseSelected = { exercise ->
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("selected_exercise", exercise)
                        navController.popBackStack()
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Settings Screen
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Workout Detail Screen
            composable(
                route = Routes.WORKOUT_DETAIL,
                arguments = listOf(
                    navArgument("workoutId") {
                        type = NavType.LongType
                    }
                )
            ) {
                WorkoutDetailScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
private fun GymTrackerBottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Hide bottom bar on picker/editor screens
    val hideOnRoutes = listOf(
        Routes.EXERCISE_PICKER,
        Routes.ROUTINE_EXERCISE_PICKER,
        Routes.SETTINGS,
        "routine_editor",
        "workout_detail"
    )
    if (hideOnRoutes.any { currentDestination?.route?.contains(it) == true }) return

    MinimalNavBar(
        items = bottomNavItems,
        currentRoute = currentDestination?.route,
        onItemClick = { screen ->
            navController.navigate(screen.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    )
}

@Composable
private fun MinimalNavBar(
    items: List<Screen>,
    currentRoute: String?,
    onItemClick: (Screen) -> Unit
) {
    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Background)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(Surface)
                .border(
                    width = 1.dp,
                    color = CardBorder,
                    shape = shape
                )
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { screen ->
                val selected = currentRoute == screen.route

                MinimalNavItem(
                    screen = screen,
                    selected = selected,
                    onClick = { onItemClick(screen) }
                )
            }
        }
    }
}

@Composable
private fun MinimalNavItem(
    screen: Screen,
    selected: Boolean,
    onClick: () -> Unit
) {
    val iconColor = if (selected) TextPrimary else TextSecondary

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = screen.icon,
            contentDescription = screen.title,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
    }
}
