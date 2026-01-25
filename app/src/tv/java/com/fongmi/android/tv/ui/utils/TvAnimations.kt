package com.fongmi.android.tv.ui.utils

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically

/**
 * TV-optimized animation utilities
 * Provides smooth, performant animations suitable for TV displays
 */
object TvAnimations {

    // Duration constants (TV needs slightly longer animations for visibility)
    const val DURATION_FAST = 150
    const val DURATION_NORMAL = 250
    const val DURATION_SLOW = 400

    // ========== Fade Animations ==========

    fun fadeInFast(): EnterTransition = fadeIn(
        animationSpec = tween(DURATION_FAST, easing = LinearEasing)
    )

    fun fadeInNormal(): EnterTransition = fadeIn(
        animationSpec = tween(DURATION_NORMAL, easing = FastOutSlowInEasing)
    )

    fun fadeOutFast(): ExitTransition = fadeOut(
        animationSpec = tween(DURATION_FAST, easing = LinearEasing)
    )

    fun fadeOutNormal(): ExitTransition = fadeOut(
        animationSpec = tween(DURATION_NORMAL, easing = FastOutSlowInEasing)
    )

    // ========== Scale Animations ==========

    fun scaleInFocus(): EnterTransition = scaleIn(
        initialScale = 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    fun scaleOutFocus(): ExitTransition = scaleOut(
        targetScale = 0.95f,
        animationSpec = tween(DURATION_FAST)
    )

    fun scaleInDialog(): EnterTransition = scaleIn(
        initialScale = 0.9f,
        animationSpec = tween(DURATION_NORMAL, easing = FastOutSlowInEasing)
    ) + fadeIn(animationSpec = tween(DURATION_NORMAL))

    fun scaleOutDialog(): ExitTransition = scaleOut(
        targetScale = 0.9f,
        animationSpec = tween(DURATION_FAST)
    ) + fadeOut(animationSpec = tween(DURATION_FAST))

    // ========== Slide Animations ==========

    fun slideInFromRight(): EnterTransition = slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(DURATION_NORMAL, easing = FastOutSlowInEasing)
    )

    fun slideOutToRight(): ExitTransition = slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(DURATION_NORMAL, easing = FastOutSlowInEasing)
    )

    fun slideInFromLeft(): EnterTransition = slideInHorizontally(
        initialOffsetX = { -it },
        animationSpec = tween(DURATION_NORMAL, easing = FastOutSlowInEasing)
    )

    fun slideOutToLeft(): ExitTransition = slideOutHorizontally(
        targetOffsetX = { -it },
        animationSpec = tween(DURATION_NORMAL, easing = FastOutSlowInEasing)
    )

    fun slideInFromBottom(): EnterTransition = slideInVertically(
        initialOffsetY = { it },
        animationSpec = tween(DURATION_NORMAL, easing = FastOutSlowInEasing)
    )

    fun slideOutToBottom(): ExitTransition = slideOutVertically(
        targetOffsetY = { it },
        animationSpec = tween(DURATION_NORMAL, easing = FastOutSlowInEasing)
    )

    fun slideInFromTop(): EnterTransition = slideInVertically(
        initialOffsetY = { -it },
        animationSpec = tween(DURATION_NORMAL, easing = FastOutSlowInEasing)
    )

    fun slideOutToTop(): ExitTransition = slideOutVertically(
        targetOffsetY = { -it },
        animationSpec = tween(DURATION_NORMAL, easing = FastOutSlowInEasing)
    )

    // ========== Combined Animations ==========

    fun slideInWithFade(fromRight: Boolean = true): EnterTransition {
        val slide = if (fromRight) slideInFromRight() else slideInFromLeft()
        return slide + fadeInNormal()
    }

    fun slideOutWithFade(toRight: Boolean = true): ExitTransition {
        val slide = if (toRight) slideOutToRight() else slideOutToLeft()
        return slide + fadeOutNormal()
    }

    // ========== Sidebar Animations ==========

    fun sidebarEnter(): EnterTransition = slideInFromRight() + fadeIn(
        animationSpec = tween(DURATION_NORMAL)
    )

    fun sidebarExit(): ExitTransition = slideOutToRight() + fadeOut(
        animationSpec = tween(DURATION_FAST)
    )

    // ========== Overlay Animations ==========

    fun overlayEnter(): EnterTransition = fadeIn(
        animationSpec = tween(DURATION_NORMAL)
    )

    fun overlayExit(): ExitTransition = fadeOut(
        animationSpec = tween(DURATION_FAST)
    )

    // ========== Card/Item Animations ==========

    fun cardEnter(index: Int, staggerDelay: Int = 50): EnterTransition = fadeIn(
        animationSpec = tween(
            durationMillis = DURATION_NORMAL,
            delayMillis = index * staggerDelay,
            easing = FastOutSlowInEasing
        )
    ) + scaleIn(
        initialScale = 0.92f,
        animationSpec = tween(
            durationMillis = DURATION_NORMAL,
            delayMillis = index * staggerDelay,
            easing = FastOutSlowInEasing
        )
    )

    // ========== Player Control Animations ==========

    fun controlsEnter(): EnterTransition = fadeIn(
        animationSpec = tween(DURATION_FAST)
    )

    fun controlsExit(): ExitTransition = fadeOut(
        animationSpec = tween(DURATION_SLOW)
    )
}

/**
 * Spring animation specs for TV
 */
object TvSpringSpecs {
    val Bouncy = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val Smooth = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    val Quick = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessHigh
    )
}
