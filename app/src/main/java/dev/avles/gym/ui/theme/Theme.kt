package dev.avles.gym.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val GymColors = darkColorScheme(
    primary = Brass,
    onPrimary = Ink,
    primaryContainer = Raised,
    onPrimaryContainer = Brass,
    secondary = Sage,
    onSecondary = Ink,
    secondaryContainer = Raised,
    onSecondaryContainer = Sage,
    error = Coral,
    onError = Ink,
    background = Ink,
    onBackground = Bone,
    surface = Slate,
    onSurface = Bone,
    surfaceVariant = Raised,
    onSurfaceVariant = Mute,
    outline = Line,
    outlineVariant = Line
)

@Suppress("UNUSED_PARAMETER")
@Composable
fun GymTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Deliberately dark in both system modes. See the note in Color.kt.
    MaterialTheme(
        colorScheme = GymColors,
        typography = GymType,
        content = content
    )
}
