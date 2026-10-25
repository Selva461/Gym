package dev.avles.gym.ui.components

/**
 * Whether the canvas can paint a batch of coloured vertices. Android's hardware
 * canvas only learned to in Android 10; before that the call silently draws nothing,
 * so older phones get the demonstrator one flat triangle at a time instead.
 */
internal expect val canDrawVertices: Boolean
