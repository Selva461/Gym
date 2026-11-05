package dev.avles.gym.ui.components

import android.content.ClipData
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import dev.avles.gym.domain.AiExport
import java.io.File

@Composable
internal actual fun rememberTextSharer(): (subject: String, text: String) -> Unit {
    val context = LocalContext.current
    return remember(context) {
        { subject, text ->
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(Intent.createChooser(send, subject).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}

/**
 * Writes the file into the app's own cache, under the one folder the FileProvider
 * exposes (res/xml/file_paths.xml), and hands the chosen app a read grant on that
 * single URI for as long as it needs it. Nothing else in the app becomes readable,
 * the provider is not exported, and earlier exports are cleared first so the cache
 * holds at most the latest copy of the log.
 */
@Composable
internal actual fun rememberFileSharer(): (fileName: String, text: String) -> Unit {
    val context = LocalContext.current
    return remember(context) {
        { fileName, text ->
            runCatching {
                val dir = File(context.cacheDir, EXPORT_DIR).apply { mkdirs() }
                dir.listFiles()?.forEach { it.delete() }
                // Only the name's last segment is used, so a name can never point outside the folder.
                val file = File(dir, File(fileName).name).apply { writeText(text) }
                val uri = FileProvider.getUriForFile(context, "${context.packageName}$AUTHORITY_SUFFIX", file)
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = AiExport.MIME
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, fileName)
                    clipData = ClipData.newRawUri(fileName, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(send, fileName).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }.onFailure {
                Toast.makeText(context, "Could not export: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

/** Must match the cache-path in res/xml/file_paths.xml and the authority in the manifest. */
private const val EXPORT_DIR = "exports"
private const val AUTHORITY_SUFFIX = ".files"
