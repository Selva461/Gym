package dev.avles.gym.ui.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import dev.avles.gym.core.xlsx.Xlsx
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal actual fun rememberPlanFileActions(
    templateName: () -> String,
    templateBytes: suspend () -> ByteArray,
    onPicked: (bytes: ByteArray?, error: String?) -> Unit,
    onMessage: (String) -> Unit
): PlanFileActions? {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    val create = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(Xlsx.MIME)) { uri ->
        if (uri != null) scope.launch {
            val note = runCatching {
                val bytes = templateBytes()
                withContext(Dispatchers.IO) { write(context, uri, bytes) }
            }.fold(
                { "Template saved. Give it to Claude to fill in, then import it here." },
                { "Could not save the template: ${it.message}" }
            )
            onMessage(note)
        }
    }
    val open = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            val read = withContext(Dispatchers.IO) { runCatching { readCapped(context, uri) } }
            read.fold(
                { bytes -> if (bytes == null) onPicked(null, "That file is larger than a plan should be (10 MB).") else onPicked(bytes, null) },
                { onPicked(null, "That file could not be read: ${it.message}") }
            )
        }
    }
    return remember(create, open) {
        PlanFileActions(
            saveTemplate = { create.launch(templateName()) },
            // Any type: Drive and mail apps label .xlsx files inconsistently. The import
            // checks the content itself and refuses anything that is not a plan workbook.
            pickPlan = { open.launch(arrayOf("*/*")) }
        )
    }
}

private fun write(context: Context, uri: Uri, bytes: ByteArray) {
    val out = context.contentResolver.openOutputStream(uri, "wt") ?: error("could not write there")
    out.use { it.write(bytes) }
}

/** The file's bytes, or null if it is over the size cap; never reads more than cap + 1 bytes. */
private fun readCapped(context: Context, uri: Uri): ByteArray? {
    val input = context.contentResolver.openInputStream(uri) ?: error("it could not be opened")
    input.use { stream ->
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(64 * 1024)
        var total = 0
        while (true) {
            val n = stream.read(buffer)
            if (n < 0) break
            total += n
            if (total > Xlsx.MAX_FILE_BYTES) return null
            out.write(buffer, 0, n)
        }
        return out.toByteArray()
    }
}
