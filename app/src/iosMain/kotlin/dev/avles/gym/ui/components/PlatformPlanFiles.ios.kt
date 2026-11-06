package dev.avles.gym.ui.components

import androidx.compose.runtime.Composable

/** Not on iPhone yet: it needs a UIDocumentPickerViewController. The plan still shows if one exists. */
@Composable
internal actual fun rememberPlanFileActions(
    templateName: () -> String,
    templateBytes: suspend () -> ByteArray,
    onPicked: (bytes: ByteArray?, error: String?) -> Unit,
    onMessage: (String) -> Unit
): PlanFileActions? = null
