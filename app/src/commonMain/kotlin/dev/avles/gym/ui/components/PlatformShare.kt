package dev.avles.gym.ui.components

import androidx.compose.runtime.Composable

/**
 * The platform's share sheet for a block of text, such as a CSV export. Returns the
 * function that opens it with a subject and the text. Sharing hands the text to an
 * app the person picks; this app itself still never touches the network.
 */
@Composable
internal expect fun rememberTextSharer(): (subject: String, text: String) -> Unit
