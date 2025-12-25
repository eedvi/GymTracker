package com.gymtracker.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymtracker.ui.theme.Background
import com.gymtracker.ui.theme.Primary
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

    FloatingGlassNavBar(
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
private fun FloatingGlassNavBar(
    items: List<Screen>,
    currentRoute: String?,
    onItemClick: (Screen) -> Unit
) {
    val shape = RoundedCornerShape(28.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Background)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E1A3D),
                            Color(0xFF151030)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF4A4570).copy(alpha = 0.6f),
                            Color(0xFF2A2550).copy(alpha = 0.3f)
                        )
                    ),
                    shape = shape
                )
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { screen ->
                val selected = currentRoute == screen.route

                FloatingNavItem(
                    screen = screen,
                    selected = selected,
                    onClick = { onItemClick(screen) }
                )
            }
        }
    }
}

@Composable
private fun FloatingNavItem(
    screen: Screen,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val iconColor by animateColorAsState(
        targetValue = if (selected) Primary else Color.White.copy(alpha = 0.5f),
        label = "iconColor"
    )

    val textColor by animateColorAsState(
        targetValue = if (selected) Primary else Color.White.copy(alpha = 0.4f),
        label = "textColor"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .scale(scale)
    ) {
        Icon(
            imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
            contentDescription = screen.title,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = screen.title,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor
        )

        // Indicator dot
        if (selected) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Primary)
            )
        } else {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
