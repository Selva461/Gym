package dev.avles.gym.ui.components

import androidx.compose.runtime.Composable

/** Saving the whole log to a file the user picks, and putting one back. */
class BackupActions(val backUp: () -> Unit, val restore: () -> Unit)

/**
 * The platform's file pickers wired to [dev.avles.gym.data.Backup], or null where
 * that is not built yet (iOS). [onResult] gets a sentence to show; a successful
 * restore restarts the app instead.
 */
@Composable
internal expect fun rememberBackupActions(onResult: (String) -> Unit): BackupActions?
