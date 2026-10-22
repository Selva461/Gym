package dev.avles.gym.data

import androidx.room.execSQL
import androidx.room.useWriterConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

/**
 * The whole log as one file: a consistent copy of the database, made with SQLite's
 * own `VACUUM INTO` so it is safe while the app is running, and checked before it
 * is ever put back. Photos and settings are not in it; the CSV export and Android's
 * own backup cover those.
 */
object Backup {

    const val MIME = "application/octet-stream"

    fun fileName(isoDate: String) = "gym-backup-$isoDate.db"

    /** Writes a consistent copy of [db] to [path], which must not exist yet. */
    suspend fun snapshot(db: GymDatabase, path: String) {
        db.useWriterConnection { it.execSQL("VACUUM INTO '${path.replace("'", "''")}'") }
    }

    /**
     * Null when [path] is a backup this app can open (its schema version is one the
     * migrations know, and the core tables are there), or why it is not.
     */
    fun problemWith(path: String): String? {
        val newest = GymDatabase.MIGRATIONS.last().endVersion
        val conn = try {
            BundledSQLiteDriver().open(path)
        } catch (e: Exception) {
            return "That file could not be opened."
        }
        return try {
            val version = conn.prepare("PRAGMA user_version").let { st ->
                try { if (st.step()) st.getLong(0) else 0L } finally { st.close() }
            }
            val tables = conn.prepare("SELECT name FROM sqlite_master WHERE type = 'table'").let { st ->
                try { buildSet { while (st.step()) add(st.getText(0)) } } finally { st.close() }
            }
            when {
                !tables.containsAll(CORE_TABLES) -> "That is not a backup from this app."
                version !in 1..newest -> "That backup is from a newer version of the app. Update first."
                else -> null
            }
        } catch (e: Exception) {
            "That is not a backup from this app."
        } finally {
            conn.close()
        }
    }

    private val CORE_TABLES = listOf("exercise", "set_entry", "food_item", "food_log", "body_weight")
}
