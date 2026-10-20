package dev.avles.gym.domain

import dev.avles.gym.data.SetRow
import kotlin.math.max

/**
 * One lift's story, and one session's, as the logging apps tell them: the best set,
 * how the estimated max moved session by session, what weight each rep count should
 * be, and what a workout just achieved.
 *
 * Pure functions over [SetRow], so the screens only draw and the tests can check
 * every number without a database.
 */
object LiftHistory {

    /** One session's best on a lift: the point a strength chart is drawn through. */
    data class Point(val sessionId: Long, val loggedAt: Long, val bestE1rm: Double, val topSet: SetRow)

    /**
     * Each session's best estimated max, oldest first. One point per session rather
     * than per set, because five sets on one evening are one data point about
     * strength, not five.
     */
    fun trend(history: List<SetRow>): List<Point> =
        history.filter { !it.isWarmup && !it.isAssisted && it.weightKg > 0 && it.reps > 0 }
            .groupBy { it.sessionId }
            .map { (sessionId, sets) ->
                val top = sets.maxByOrNull { Training.estimatedOneRepMax(it.weightKg, it.reps) }!!
                Point(
                    sessionId = sessionId,
                    loggedAt = sets.minOf { it.loggedAt },
                    bestE1rm = Training.estimatedOneRepMax(top.weightKg, top.reps),
                    topSet = top
                )
            }
            .sortedBy { it.loggedAt }

    /** The records every lifting app keeps per exercise. Null fields mean "no working sets yet". */
    data class Records(
        val bestSet: SetRow?,
        val bestE1rm: Double,
        val heaviest: SetRow?,
        val mostReps: SetRow?,
        val bestSessionVolume: Double,
        val sessions: Int,
        val workingSets: Int
    )

    fun records(history: List<SetRow>): Records {
        val working = history.filter { !it.isWarmup && it.reps > 0 }
        val loaded = working.filter { !it.isAssisted }
        val best = loaded.maxByOrNull { Training.estimatedOneRepMax(it.weightKg, it.reps) }
        return Records(
            bestSet = best,
            bestE1rm = best?.let { Training.estimatedOneRepMax(it.weightKg, it.reps) } ?: 0.0,
            // Ties go to more reps: 100 × 5 is a better heaviest set than 100 × 1.
            heaviest = loaded.maxWithOrNull(compareBy<SetRow>({ it.weightKg }, { it.reps })),
            mostReps = working.maxWithOrNull(compareBy<SetRow>({ it.reps }, { if (it.isAssisted) -it.weightKg else it.weightKg })),
            bestSessionVolume = loaded.groupBy { it.sessionId }.values
                .maxOfOrNull { Training.volume(it) } ?: 0.0,
            sessions = working.map { it.sessionId }.distinct().size,
            workingSets = working.size
        )
    }

    /**
     * What you should manage for each rep count, from an estimated max: Epley run
     * backwards, rounded down to the 1.25 kg a pair of the smallest plates adds. It
     * is the table Strong and Hevy print, and the honest caveat is the same — past
     * ten reps it is a guess.
     */
    fun repMaxTable(e1rm: Double, reps: IntRange = 1..12): List<Pair<Int, Double>> {
        if (e1rm <= 0) return emptyList()
        return reps.map { r ->
            val w = if (r == 1) e1rm else e1rm / (1 + r / 30.0)
            r to floorTo(w, 1.25)
        }
    }

    /** 100% down to 50% of the estimated max, for working out warm-ups and back-off sets. */
    fun percentTable(e1rm: Double): List<Pair<Int, Double>> {
        if (e1rm <= 0) return emptyList()
        return (100 downTo 50 step 10).map { it to floorTo(e1rm * it / 100.0, 1.25) }
    }

    /**
     * The end-of-workout card: how long, how much, and what was beaten. [before] is
     * everything logged on these lifts *before* this session, so a record means
     * better than any earlier session, not better than an earlier set tonight.
     */
    data class Report(
        val minutes: Int,
        val workingSets: Int,
        val volumeKg: Double,
        val lifts: List<String>,
        val records: List<SetRow>
    )

    fun report(startedAt: Long, endedAt: Long, sets: List<SetRow>, before: List<SetRow>): Report {
        val working = sets.filter { !it.isWarmup }
        val records = working.groupBy { it.exerciseId }.mapNotNull { (_, onLift) ->
            // The session's single best set on each lift is the candidate; two records
            // on one lift in one evening read as noise on a summary card.
            val candidate = if (onLift.first().isAssisted) {
                onLift.minWithOrNull(compareBy<SetRow>({ it.weightKg }, { -it.reps }))
            } else {
                onLift.maxByOrNull { Training.estimatedOneRepMax(it.weightKg, it.reps) }
            } ?: return@mapNotNull null
            val earlier = before.filter { it.exerciseId == candidate.exerciseId && it.sessionId != candidate.sessionId }
            if (earlier.none { !it.isWarmup }) return@mapNotNull null // a first time is not a record
            candidate.takeIf { Training.isPersonalRecord(it, earlier + it) }
        }
        return Report(
            minutes = max(0L, (endedAt - startedAt) / 60_000).toInt(),
            workingSets = working.size,
            volumeKg = Training.volume(sets),
            lifts = sets.map { it.exerciseName }.distinct(),
            records = records
        )
    }

    private fun floorTo(v: Double, step: Double): Double {
        val n = kotlin.math.floor(v / step + 1e-9)
        return n * step
    }
}
