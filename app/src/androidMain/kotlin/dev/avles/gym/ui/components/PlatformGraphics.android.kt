package dev.avles.gym.ui.components

import android.os.Build

internal actual val canDrawVertices: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
