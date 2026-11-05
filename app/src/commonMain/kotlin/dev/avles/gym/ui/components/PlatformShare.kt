package dev.avles.gym.ui.components

import androidx.compose.runtime.Composable

/**
 * The platform's share sheet for a block of text, such as a CSV export. Returns the
 * function that opens it with a subject and the text. Sharing hands the text to an
 * app the person picks; this app itself still never touches the network.
 */
@Composable
internal expect fun rememberTextSharer(): (subject: String, text: String) -> Unit

/**
 * The share sheet for a whole file, for exports too large to pass as text (Android
 * caps an intent's extras at about 1 MB, which a year of logs passes). Returns the
 * function that writes [text] to a file called fileName and offers it. Where a
 * platform has no file route it shares the text instead.
 */
@Composable
internal expect fun rememberFileSharer(): (fileName: String, text: String) -> Unit
