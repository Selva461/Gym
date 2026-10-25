package dev.avles.gym.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.CompositionLocalProvider
import dev.avles.gym.core.Dates
import dev.avles.gym.ui.i18n.LocalLanguage
import dev.avles.gym.ui.i18n.tr
import dev.avles.gym.core.epochDay
import dev.avles.gym.data.DayKinds
import dev.avles.gym.domain.Weekly
import dev.avles.gym.ui.components.Motion
import dev.avles.gym.anim.Avatar
import dev.avles.gym.ui.components.EmptyNote
import dev.avles.gym.ui.components.LocalDemoAvatar
import dev.avles.gym.ui.screens.CoachScreen
import dev.avles.gym.ui.screens.LearnDetailScreen
import dev.avles.gym.ui.screens.LearnScreen
import dev.avles.gym.ui.screens.FuelScreen
import dev.avles.gym.ui.screens.PatternsScreen
import dev.avles.gym.ui.screens.PhotosScreen
import dev.avles.gym.ui.screens.MeasurementsScreen
import dev.avles.gym.ui.screens.ProgressScreen
import dev.avles.gym.ui.screens.RecordsScreen
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
 * Six destinations, each with a dot in its own accent rather than an icon.
 * Two colours already mean training and food everywhere else in the app, so the
 * bar can lean on that instead of introducing a second visual language of glyphs.
 */
private data class Destination(val route: String, val label: String, val accent: Color)

private val DESTINATIONS = listOf(
    Destination("today", "Today", Bone),
    Destination("train", "Train", Brass),
    Destination("learn", "Learn", Brass),
    Destination("fuel", "Fuel", Sage),
    Destination("progress", "Progress", Mute),
    Destination("settings", "Settings", Mute)
)

private val TABS = DESTINATIONS.map { it.route }.toSet()

/**
 * Tabs fade through each other: they are siblings, so neither should look like it
 * sits on top. Screens opened from a tab (photos, goal, patterns) slide in from
 * the side and back out the way they came, because those do sit on top.
 */
private fun AnimatedContentTransitionScope<NavBackStackEntry>.isPush(): Boolean =
    targetState.destination.route !in TABS && initialState.destination.route in TABS

private fun AnimatedContentTransitionScope<NavBackStackEntry>.isPop(): Boolean =
    initialState.destination.route !in TABS && targetState.destination.route in TABS

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
    val recentFoods by vm.recentFoods.collectAsStateWithLifecycle()
    val savedMeals by vm.savedMeals.collectAsStateWithLifecycle()
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
    val workoutReport by vm.workoutReport.collectAsStateWithLifecycle()
    val records by vm.records.collectAsStateWithLifecycle()
    val superset by vm.superset.collectAsStateWithLifecycle()
    val needsWelcome by vm.needsWelcome.collectAsStateWithLifecycle()
    val cardio by vm.cardio.collectAsStateWithLifecycle()

    val haptics = LocalHapticFeedback.current

    fun go(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    // The lift opened from Learn. Kept here rather than in the route so a custom lift
    // with any name opens the same way, and survives rotation with the back stack.
    var learning by rememberSaveable { mutableStateOf<Long?>(null) }

    CompositionLocalProvider(
        LocalLanguage provides settings.language,
        LocalDemoAvatar provides Avatar.chosen(settings.demoAvatarId, settings.isFemale)
    ) {
    Scaffold(
        containerColor = Ink,
        bottomBar = {
            NavigationBar(containerColor = Ink, tonalElevation = 0.dp) {
                DESTINATIONS.forEach { destination ->
                    val selected = currentRoute?.hierarchy?.any { it.route == destination.route } == true
                    // The dot stretches into a short bar when its tab is chosen, so the
                    // selection travels visibly instead of jumping between items.
                    val tabWidth by animateDpAsState(if (selected) 18.dp else 5.dp, Motion.press(), label = "tab-width")
                    val tabHeight by animateDpAsState(if (selected) 6.dp else 5.dp, Motion.press(), label = "tab-height")
                    val dot by animateColorAsState(
                        if (selected) destination.accent else Faint, Motion.quick(), label = "tab-colour"
                    )
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (!selected) haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            go(destination.route)
                        },
                        icon = {
                            Box(
                                Modifier
                                    .width(tabWidth)
                                    .height(tabHeight)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(dot)
                            )
                        },
                        label = {
                            Text(
                                tr(destination.label),
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
            modifier = Modifier.padding(padding),
            enterTransition = {
                when {
                    isPush() -> slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Start, Motion.standard()
                    ) + fadeIn(Motion.enter())
                    else -> fadeIn(Motion.enter()) + scaleIn(Motion.enter(), initialScale = 0.98f)
                }
            },
            exitTransition = {
                if (isPush()) fadeOut(Motion.quick()) + scaleOut(Motion.quick(), targetScale = 0.98f)
                else fadeOut(Motion.quick())
            },
            popEnterTransition = {
                fadeIn(Motion.enter()) + scaleIn(Motion.enter(), initialScale = 0.98f)
            },
            popExitTransition = {
                if (isPop()) slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End, Motion.standard()
                ) + fadeOut(Motion.standard())
                else fadeOut(Motion.quick())
            }
        ) {
            composable("today") {
                val week = remember(progress, patterns) {
                    Weekly.compare(
                        today = Dates.todayEpochDay(),
                        trained = progress.trained,
                        volumeByDay = progress.volume.associate { it.dateEpochDay to it.volume },
                        protein = patterns.protein,
                        kcal = patterns.kcal
                    )
                }
                TodayScreen(
                    week = week,
                    latestWeightKg = progress.weights.maxByOrNull { it.dateEpochDay }?.weightKg,
                    onWelcomeTargets = { vm.applyWelcomeTargets(it) },
                    onDismissWelcome = { vm.dismissWelcome() },
                    showWelcome = needsWelcome,
                    onRecordWeight = vm::recordWeight,
                    day = day,
                    dots = dots,
                    onSelectDate = vm::selectDate,
                    onGoTrain = { go("train") },
                    onGoFuel = { go("fuel") },
                    dayKind = markers.firstOrNull { it.dateEpochDay == day.date.epochDay }?.kind,
                    onMarkDay = { kind -> vm.markDay(day.date.epochDay, kind) },
                    onAddWater = vm::addWater,
                    onRemoveWater = { vm.removeWater() }
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
                    onAttachExercisePhoto = { id, image -> vm.attachExercisePhoto(id, image) },
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
                    onLogSet = { exerciseId, weight, reps, warmup, kind, rpe ->
                        vm.logSet(exerciseId, weight, reps, warmup, kind = kind, rpe = rpe)
                    },
                    onEditSet = { setId, weight, reps, warmup, variation, kind, rpe ->
                        vm.editSet(setId, weight, reps, warmup, variation, kind, rpe)
                    },
                    onDeleteSet = vm::deleteSet,
                    onSkipRest = vm::cancelRest,
                    onAddExercise = vm::addExercise,
                    onAdjustRest = vm::adjustRest,
                    workoutReport = workoutReport,
                    onDismissReport = vm::dismissWorkoutReport,
                    liftHistory = vm::liftHistory,
                    onSetNotes = { id, notes -> vm.setExerciseNotes(id, notes) },
                    superset = superset,
                    onStartSuperset = vm::startSuperset,
                    onEndSuperset = vm::endSuperset,
                    cardio = cardio,
                    bodyKg = progress.weights.maxByOrNull { it.dateEpochDay }?.weightKg ?: 0.0,
                    onLogCardio = { a, m, km -> vm.logCardio(a, m, km) },
                    onDeleteCardio = { vm.deleteCardio(it) }
                )
            }
            composable("learn") {
                LearnScreen(
                    exercises = train.exercises,
                    onOpen = { exercise ->
                        learning = exercise.id
                        navController.navigate("learn-lift") { launchSingleTop = true }
                    }
                )
            }
            composable("learn-lift") {
                val exercise = train.exercises.firstOrNull { it.id == learning }
                if (exercise == null) {
                    EmptyNote("That lift is no longer in the catalogue.", Modifier.padding(horizontal = 20.dp))
                } else {
                    LearnDetailScreen(
                        exercise = exercise,
                        avatar = LocalDemoAvatar.current,
                        onChooseAvatar = { chosen -> vm.chooseDemoAvatar(chosen.id) }
                    )
                }
            }
            composable("fuel") {
                FuelScreen(
                    day = day,
                    quickPicks = quickPicks,
                    catalogue = foods,
                    onLog = { foodId, meal, qty -> vm.logFood(foodId, meal, qty) },
                    onRemove = vm::removeFoodLog,
                    onEditEntry = { logId, qty, slot -> vm.editFoodLog(logId, qty, slot) },
                    onSaveFood = vm::saveFood,
                    onAddWater = vm::addWater,
                    onRemoveWater = { vm.removeWater() },
                    onCopyMeal = { meal, done -> vm.copyMealFromDayBefore(meal, done) },
                    recentFoods = recentFoods,
                    onStartFast = { vm.startFast(it) },
                    onEndFast = { vm.endFast() },
                    savedMeals = savedMeals,
                    onLogSavedMeal = { m, slot -> vm.logSavedMeal(m, slot) },
                    onDeleteSavedMeal = { vm.deleteSavedMeal(it) },
                    onSaveMeal = { name, slot -> vm.saveMeal(name, slot) },
                    onQuickAdd = { k, p, c, f, slot -> vm.quickAdd(k, p, c, f, slot) },
                    favouriteIds = settings.favouriteFoodIds,
                    onToggleFavourite = { vm.toggleFavourite(it) }
                )
            }
            composable("progress") {
                ProgressScreen(
                    state = progress,
                    onRecordWeight = vm::recordWeight,
                    onOpenPhotos = { go("photos") },
                    onOpenGoal = { go("goal") },
                    onOpenPatterns = { go("patterns") },
                    onOpenRecords = { go("records") },
                    onOpenMeasurements = { go("measurements") },
                    liftsWithRecords = records.size,
                    goalWeightKg = settings.goalWeightKg,
                    restDays = markers.filter { it.kind == DayKinds.REST }.map { it.dateEpochDay }.toSet()
                )
            }
            // Not another tab. Photos are about your body over time, which is what
            // Progress already is, and a six-item bar is already at the point where
            // the labels start truncating.
            composable("measurements") {
                val measurements by vm.measurements.collectAsStateWithLifecycle()
                MeasurementsScreen(
                    measurements = measurements,
                    settings = settings,
                    onSave = { vm.saveMeasurements(it) },
                    onSaveBody = { h, f -> vm.saveBody(h, f) }
                )
            }
            composable("records") {
                RecordsScreen(records = records, exercises = train.exercises, liftHistory = vm::liftHistory)
            }
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
                    onAdd = { image, epochDay, label -> vm.addProgressPhoto(image, epochDay, label) },
                    onDelete = vm::deleteProgressPhoto,
                    onEdit = { id, label, note -> vm.editProgressPhoto(id, label, note) },
                    onDismissError = vm::clearPhotoError
                )
            }

            composable("settings") {
                SettingsScreen(
                    settings = settings,
                    onSave = { vm.saveSettings(it) },
                    latestWeightKg = progress.weights.maxByOrNull { it.dateEpochDay }?.weightKg,
                    onExport = { kind, share -> vm.export(kind, share) }
                )
            }
        }
    }
    }
}
