package dev.avles.gym.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.avles.gym.ui.screens.CoachScreen
import dev.avles.gym.ui.screens.FuelScreen
import dev.avles.gym.ui.screens.PatternsScreen
import dev.avles.gym.ui.screens.PhotosScreen
import dev.avles.gym.ui.screens.ProgressScreen
import dev.avles.gym.ui.screens.SettingsScreen
import dev.avles.gym.ui.screens.TodayScreen
import dev.avles.gym.ui.screens.TrainScreen
import dev.avles.gym.ui.theme.Bone
import dev.avles.gym.ui.theme.Brass
import dev.avles.gym.ui.theme.Faint
import dev.avles.gym.ui.theme.Ink
import dev.avles.gym.ui.theme.Mute
import dev.avles.gym.ui.theme.Sage

/**
 * Five destinations, each with a dot in its own accent rather than an icon.
 * Two colours already mean training and food everywhere else in the app, so the
 * bar can lean on that instead of introducing a second visual language of glyphs.
 */
private data class Destination(val route: String, val label: String, val accent: Color)

private val DESTINATIONS = listOf(
    Destination("today", "Today", Bone),
    Destination("train", "Train", Brass),
    Destination("fuel", "Fuel", Sage),
    Destination("progress", "Progress", Mute),
    Destination("settings", "Settings", Mute)
)

@Composable
fun GymNav(vm: GymViewModel) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination

    val day by vm.dayState.collectAsStateWithLifecycle()
    val dots by vm.calendar.collectAsStateWithLifecycle()
    val train by vm.trainState.collectAsStateWithLifecycle()
    val progress by vm.progressState.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val quickPicks by vm.quickPicks.collectAsStateWithLifecycle()
    val foods by vm.foods.collectAsStateWithLifecycle()
    val rest by vm.restRemaining.collectAsStateWithLifecycle()
    val prSetId by vm.lastPrSetId.collectAsStateWithLifecycle()
    val guidance by vm.guidance.collectAsStateWithLifecycle()
    val splits by vm.splitPlans.collectAsStateWithLifecycle()
    val activePlan by vm.activePlan.collectAsStateWithLifecycle()
    val summaries by vm.sessionSummaries.collectAsStateWithLifecycle()
    val openSession by vm.openSessionDetail.collectAsStateWithLifecycle()
    val liftPhotos by vm.exercisePhotoFiles.collectAsStateWithLifecycle()
    val markers by vm.dayMarkers.collectAsStateWithLifecycle()
    val coach by vm.coachState.collectAsStateWithLifecycle()
    val patterns by vm.patternState.collectAsStateWithLifecycle()

    fun go(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        containerColor = Ink,
        bottomBar = {
            NavigationBar(containerColor = Ink, tonalElevation = 0.dp) {
                DESTINATIONS.forEach { destination ->
                    val selected = currentRoute?.hierarchy?.any { it.route == destination.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = { go(destination.route) },
                        icon = {
                            Box(
                                Modifier
                                    .size(if (selected) 7.dp else 5.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) destination.accent else Faint)
                            )
                        },
                        label = {
                            Text(
                                destination.label,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedTextColor = destination.accent,
                            unselectedTextColor = Faint,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "today",
            modifier = Modifier.padding(padding)
        ) {
            composable("today") {
                TodayScreen(
                    day = day,
                    dots = dots,
                    onSelectDate = vm::selectDate,
                    onGoTrain = { go("train") },
                    onGoFuel = { go("fuel") },
                    dayKind = markers.firstOrNull { it.dateEpochDay == day.date.toEpochDay() }?.kind,
                    onMarkDay = { kind -> vm.markDay(day.date.toEpochDay(), kind) }
                )
            }
            composable("train") {
                TrainScreen(
                    state = train,
                    restRemaining = rest,
                    prSetId = prSetId,
                    guidance = guidance,
                    splits = splits,
                    activePlan = activePlan,
                    summaries = summaries,
                    openSession = openSession,
                    onSelectExercise = vm::selectExercise,
                    onSelectVariation = vm::selectVariation,
                    exercisePhotos = liftPhotos,
                    onAttachExercisePhoto = { id, uri -> vm.attachExercisePhoto(id, uri) },
                    onDetachExercisePhoto = vm::detachExercisePhoto,
                    onStartFromSplit = vm::startSessionFrom,
                    onStartBlank = { label -> vm.startSession(label) },
                    onSaveSplit = { id, name, ids -> vm.saveSplit(id, name, ids) },
                    onDeleteSplit = vm::deleteSplit,
                    onOpenSession = vm::openSession,
                    onRenameSession = { session, label -> vm.renameSession(session, label) },
                    onReopenSession = vm::reopenSession,
                    onDeleteSession = vm::deleteSession,
                    onFinish = vm::finishSession,
                    onLogSet = { exerciseId, weight, reps, warmup ->
                        vm.logSet(exerciseId, weight, reps, warmup)
                    },
                    onEditSet = { setId, weight, reps, warmup, variation ->
                        vm.editSet(setId, weight, reps, warmup, variation)
                    },
                    onDeleteSet = vm::deleteSet,
                    onSkipRest = vm::cancelRest,
                    onAddExercise = vm::addExercise
                )
            }
            composable("fuel") {
                FuelScreen(
                    day = day,
                    quickPicks = quickPicks,
                    catalogue = foods,
                    onLog = { foodId, meal, qty -> vm.logFood(foodId, meal, qty) },
                    onRemove = vm::removeFoodLog,
                    onEditEntry = { logId, qty, slot -> vm.editFoodLog(logId, qty, slot) },
                    onSaveFood = vm::saveFood
                )
            }
            composable("progress") {
                ProgressScreen(
                    state = progress,
                    onRecordWeight = vm::recordWeight,
                    onOpenPhotos = { go("photos") },
                    onOpenGoal = { go("goal") },
                    onOpenPatterns = { go("patterns") }
                )
            }
            // Not a sixth tab. Photos are about your body over time, which is what
            // Progress already is, and a five-item bar is already at the point where
            // the labels start truncating.
            composable("patterns") {
                PatternsScreen(state = patterns)
            }

            composable("goal") {
                CoachScreen(state = coach, onChooseGoal = vm::chooseGoal)
            }

            composable("photos") {
                val photos by vm.photoState.collectAsStateWithLifecycle()
                val photoError by vm.photoError.collectAsStateWithLifecycle()
                PhotosScreen(
                    state = photos,
                    error = photoError,
                    today = day.date,
                    onAdd = { uri, epochDay, label -> vm.addProgressPhoto(uri, epochDay, label) },
                    onDelete = vm::deleteProgressPhoto,
                    onEdit = { id, label, note -> vm.editProgressPhoto(id, label, note) },
                    onDismissError = vm::clearPhotoError
                )
            }

            composable("settings") {
                SettingsScreen(settings = settings, onSave = vm::saveSettings)
            }
        }
    }
}
