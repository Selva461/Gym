package dev.avles.gym.share

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import dev.avles.gym.GymApp
import kotlinx.coroutines.runBlocking

/**
 * The training log, read-only, for SelfUp — and for nothing that the user has not
 * said yes to.
 *
 * The only way in is the `READ_TRAINING` permission this app defines in its
 * manifest. It is `dangerous`, so holding it is not enough: Android asks the user
 * on the other app's behalf, and the answer can be taken back in system settings.
 * A `normal` permission would have been granted silently to any app that asked,
 * which is the wrong default for a health log.
 *
 * Three tables leave, and only what SelfUp needs of them: working sets (no
 * warm-ups), cardio and weigh-ins. Food, photos, measurements and notes do not.
 * Every insert, update and delete refuses: another app can read the log, never
 * write to it.
 *
 * The paths and column names are a contract with another app that ships on its
 * own schedule, which is why they are versioned (`v1/`) and spelled out in
 * [TrainingContract] rather than borrowed from the Room entities.
 */
class TrainingProvider : ContentProvider() {

    private val matcher = UriMatcher(UriMatcher.NO_MATCH)

    override fun onCreate(): Boolean {
        val authority = "${context!!.packageName}${TrainingContract.AUTHORITY_SUFFIX}"
        matcher.addURI(authority, TrainingContract.PATH_SETS, SETS)
        matcher.addURI(authority, TrainingContract.PATH_CARDIO, CARDIO)
        matcher.addURI(authority, TrainingContract.PATH_BODY_WEIGHT, BODY_WEIGHT)
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val table = matcher.match(uri)
        if (table == UriMatcher.NO_MATCH) return null
        // A provider answers on a binder thread, so blocking here blocks no screen.
        // The container is lazy, so a query that arrives while the app is still
        // starting builds it rather than finding it missing.
        val shared = runBlocking { (context!!.applicationContext as GymApp).container.repository.sharedTraining() }
        return when (table) {
            SETS -> MatrixCursor(TrainingContract.SET_COLUMNS, shared.sets.size).apply {
                shared.sets.forEach {
                    addRow(arrayOf<Any>(it.setId, it.sessionId, it.exerciseName, it.weightKg, it.reps, it.variation, if (it.isAssisted) 1 else 0, it.loggedAt))
                }
            }
            CARDIO -> MatrixCursor(TrainingContract.CARDIO_COLUMNS, shared.cardio.size).apply {
                shared.cardio.forEach { addRow(arrayOf<Any>(it.id, it.activity, it.minutes, it.distanceKm, it.loggedAt)) }
            }
            else -> MatrixCursor(TrainingContract.BODY_WEIGHT_COLUMNS, shared.weights.size).apply {
                shared.weights.forEach { addRow(arrayOf<Any>(it.dateEpochDay, it.weightKg)) }
            }
        }
    }

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = throw UnsupportedOperationException(READ_ONLY)

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int =
        throw UnsupportedOperationException(READ_ONLY)

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int =
        throw UnsupportedOperationException(READ_ONLY)

    private companion object {
        const val SETS = 1
        const val CARDIO = 2
        const val BODY_WEIGHT = 3
        const val READ_ONLY = "The training log is read-only to other apps"
    }
}

/** The shape SelfUp reads. Change it by adding a `v2/`, never by editing `v1/`. */
object TrainingContract {
    /** Appended to the package name, so the debug and release builds never clash. */
    const val AUTHORITY_SUFFIX = ".training"

    const val PATH_SETS = "v1/sets"
    const val PATH_CARDIO = "v1/cardio"
    const val PATH_BODY_WEIGHT = "v1/body_weight"

    val SET_COLUMNS = arrayOf("set_id", "session_id", "exercise", "weight_kg", "reps", "variation", "assisted", "logged_at")
    val CARDIO_COLUMNS = arrayOf("id", "activity", "minutes", "distance_km", "logged_at")
    val BODY_WEIGHT_COLUMNS = arrayOf("day", "weight_kg")
}
