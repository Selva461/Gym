package dev.avles.gym.ui.components

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

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
