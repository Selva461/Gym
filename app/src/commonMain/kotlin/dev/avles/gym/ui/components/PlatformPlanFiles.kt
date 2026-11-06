package dev.avles.gym.ui.components

import androidx.compose.runtime.Composable

/** Saving the plan template to a file the person picks, and picking a filled plan to import. */
class PlanFileActions(val saveTemplate: () -> Unit, val pickPlan: () -> Unit)

/**
 * The platform's file pickers for the week plan, or null where they are not built
 * yet (iOS). [templateBytes] builds the template when a place to save it has been
 * chosen; [onPicked] gets the chosen file's bytes, or null and a sentence saying why
 * it could not be read; [onMessage] gets a sentence to show after saving.
 * Files larger than [dev.avles.gym.core.xlsx.Xlsx.MAX_FILE_BYTES] are refused
 * before they are read into memory.
 */
@Composable
internal expect fun rememberPlanFileActions(
    templateName: () -> String,
    templateBytes: suspend () -> ByteArray,
    onPicked: (bytes: ByteArray?, error: String?) -> Unit,
    onMessage: (String) -> Unit
): PlanFileActions?
