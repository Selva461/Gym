package dev.avles.gym.ui.components

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import dev.avles.gym.GymApp
import dev.avles.gym.core.Dates
import dev.avles.gym.data.Backup
import dev.avles.gym.data.GymDatabase
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal actual fun rememberBackupActions(onResult: (String) -> Unit): BackupActions? {
    val app = LocalContext.current.applicationContext as GymApp
    val scope = rememberCoroutineScope()
    val create = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(Backup.MIME)) { uri ->
        if (uri != null) scope.launch {
            onResult(withContext(Dispatchers.IO) { runCatching { write(app, uri) }.fold({ "Backed up." }, { "Backup failed: ${it.message}" }) })
        }
    }
    val open = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            val problem = withContext(Dispatchers.IO) { runCatching { restore(app, uri) }.getOrElse { "Restore failed: ${it.message}" } }
            if (problem != null) onResult(problem)
        }
    }
    return remember(create, open) {
        BackupActions(
            backUp = { create.launch(Backup.fileName(Dates.today().toString())) },
            restore = { open.launch(arrayOf("*/*")) }
        )
    }
}

private suspend fun write(app: GymApp, uri: Uri) {
    val tmp = File(app.cacheDir, "backup.db").also { it.delete() }
    try {
        Backup.snapshot(app.container.database, tmp.absolutePath)
        val out = app.contentResolver.openOutputStream(uri, "wt") ?: error("could not write there")
        out.use { o -> tmp.inputStream().use { it.copyTo(o) } }
    } finally {
        tmp.delete()
    }
}

/**
 * Checks the picked file, keeps the current log beside it as `gym-before-restore.db`,
 * swaps the file in and restarts so everything reopens on it (migrating it if it is
 * older). Returns why it refused, or never returns.
 */
private suspend fun restore(app: GymApp, uri: Uri): String? {
    val tmp = File(app.cacheDir, "restore.db").also { it.delete() }
    val input = app.contentResolver.openInputStream(uri) ?: return "That file could not be read."
    input.use { i -> tmp.outputStream().use { i.copyTo(it) } }
    Backup.problemWith(tmp.absolutePath)?.let { tmp.delete(); return it }

    val target = app.getDatabasePath(GymDatabase.FILE_NAME)
    val safety = File(target.parentFile, "gym-before-restore.db").also { it.delete() }
    Backup.snapshot(app.container.database, safety.absolutePath)
    app.container.database.close()
    listOf("-wal", "-shm", "-journal").forEach { File(target.path + it).delete() }
    tmp.copyTo(target, overwrite = true)
    tmp.delete()

    val relaunch = app.packageManager.getLaunchIntentForPackage(app.packageName)
        ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    if (relaunch != null) app.startActivity(relaunch)
    Runtime.getRuntime().exit(0)
    return null
}
