package dev.avles.gym.ui.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

/** The language chosen in Settings, provided once at the root. */
val LocalLanguage = compositionLocalOf { Strings.ENGLISH }

/** [english] in the chosen language. */
@Composable
fun tr(english: String): String = Strings.of(LocalLanguage.current, english)
