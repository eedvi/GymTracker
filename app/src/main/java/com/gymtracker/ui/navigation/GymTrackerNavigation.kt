package com.gymtracker.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.gymtracker.ui.screens.workout.WorkoutScreen
import com.gymtracker.ui.screens.workout.WorkoutViewModel

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Home : Screen(
        route = "home",
        title = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )
    data object Workout : Screen(
        route = "workout",
        title = "Workout",
        selectedIcon = Icons.Filled.FitnessCenter,
        unselectedIcon = Icons.Outlined.FitnessCenter
    )
    data object Routines : Screen(
        route = "routines",
        title = "Routines",
        selectedIcon = Icons.Filled.DateRange,
        unselectedIcon = Icons.Outlined.DateRange
    )
    data object Exercises : Screen(
        route = "exercises",
        title = "Exercises",
        selectedIcon = Icons.AutoMirrored.Filled.List,
        unselectedIcon = Icons.AutoMirrored.Outlined.List
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

    fun routineEditor(routineId: Long = 0) = "routine_editor/$routineId"
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
        "routine_editor"
    )
    if (hideOnRoutes.any { currentDestination?.route?.contains(it) == true }) return

    NavigationBar {
        bottomNavItems.forEach { screen ->
            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                        contentDescription = screen.title
                    )
                },
                label = { Text(screen.title) },
                selected = selected,
                onClick = {
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
    }
}
