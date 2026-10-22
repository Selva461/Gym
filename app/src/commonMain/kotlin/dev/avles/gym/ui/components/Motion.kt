package dev.avles.gym.ui.components

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * The app's motion, in one place.
 *
 * Everything moves on the same few curves so the app feels like one object rather
 * than a set of screens that each animate their own way. Durations are short on
 * purpose: this is used between sets with a racing heart, and an animation that
 * makes you wait for it is worse than none.
 */
object Motion {
    /** Small things changing state: a chip lighting up, a colour, a dot. */
    const val QUICK = 160

    /** Content arriving: a screen, a panel, a number. */
    const val STANDARD = 260

    fun <T> quick() = tween<T>(QUICK, easing = FastOutSlowInEasing)
    fun <T> standard() = tween<T>(STANDARD, easing = FastOutSlowInEasing)
    fun <T> enter() = tween<T>(STANDARD, easing = LinearOutSlowInEasing)

    /** Under-damped just enough to feel physical when a finger lifts, never wobbly. */
    fun <T> press() = spring<T>(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow * 2)

    /**
     * A number rolling to its new value: up when it grows, down when it shrinks, so
     * the direction of the change is visible before the digits are read.
     */
    fun AnimatedContentTransitionScope<String>.rollNumber(): ContentTransform {
        val grew = (targetState.toDoubleOrNull() ?: 0.0) >= (initialState.toDoubleOrNull() ?: 0.0)
        val sign = if (grew) 1 else -1
        return (slideInVertically(enter()) { sign * it / 2 } + fadeIn(enter()))
            .togetherWith(slideOutVertically(quick()) { -sign * it / 2 } + fadeOut(quick()))
            .using(SizeTransform(clip = false))
    }

    /** A panel swapping for another: the new one settles in from slightly smaller. */
    fun <S> AnimatedContentTransitionScope<S>.fadeThrough(): ContentTransform =
        (fadeIn(enter()) + scaleIn(enter(), initialScale = 0.97f))
            .togetherWith(fadeOut(quick()))
}

/** Which buzz a tap earns. Selection is a tick, committing something is a confirm. */
enum class Haptic { NONE, TICK, CONFIRM }

/**
 * A tap target with physical feedback: it gives a little under the finger and
 * springs back on release, with the theme's ripple and an optional haptic.
 *
 * Put it before `background` in the chain, so the whole surface scales rather
 * than only its contents, and pass the shape so the ripple and the scale are
 * clipped to the rounded corners instead of a hard rectangle.
 */
fun Modifier.pressable(
    shape: Shape? = null,
    enabled: Boolean = true,
    haptic: Haptic = Haptic.TICK,
    pressedScale: Float = 0.97f,
    onClick: () -> Unit
): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) pressedScale else 1f,
        animationSpec = Motion.press(),
        label = "press"
    )
    val haptics = LocalHapticFeedback.current
    graphicsLayer {
        scaleX = scale
        scaleY = scale
        if (shape != null) {
            this.shape = shape
            clip = true
        }
    }.clickable(
        interactionSource = interaction,
        indication = LocalIndication.current,
        enabled = enabled
    ) {
        when (haptic) {
            Haptic.NONE -> Unit
            Haptic.TICK -> haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
            Haptic.CONFIRM -> haptics.performHapticFeedback(HapticFeedbackType.Confirm)
        }
        onClick()
    }
}

/**
 * Press feedback for a Material button, which owns its own click handling. Pass
 * the same [MutableInteractionSource] to the button's `interactionSource`.
 */
fun Modifier.pressScale(interaction: MutableInteractionSource, pressedScale: Float = 0.97f): Modifier =
    composed {
        val pressed by interaction.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (pressed) pressedScale else 1f,
            animationSpec = Motion.press(),
            label = "press"
        )
        graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    }
