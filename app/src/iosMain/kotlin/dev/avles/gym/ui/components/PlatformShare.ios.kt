package dev.avles.gym.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIActivityViewController

@Composable
internal actual fun rememberTextSharer(): (subject: String, text: String) -> Unit = remember {
    { _, text ->
        val top = topViewController()
        val sheet = UIActivityViewController(activityItems = listOf(text), applicationActivities = null)
        // On an iPad the sheet is a popover and needs an anchor, or UIKit throws.
        sheet.popoverPresentationController?.sourceView = top?.view
        top?.presentViewController(sheet, animated = true, completion = null)
    }
}
