package com.fongmi.android.tv.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.fongmi.android.tv.ui.components.JetStreamRadialScrim
import com.fongmi.android.tv.ui.components.jetStreamVerticalScrimBrush
import com.fongmi.android.tv.ui.theme.JetStreamTheme

class JetStreamPageBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AbstractComposeView(context, attrs, defStyleAttr) {

    init {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
        isFocusable = false
        isFocusableInTouchMode = false
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    @Composable
    override fun Content() {
        JetStreamTheme {
            val colorScheme = MaterialTheme.colorScheme
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        jetStreamVerticalScrimBrush(
                            startColor = colorScheme.background,
                            middleColor = colorScheme.surface,
                            endColor = colorScheme.background.copy(alpha = 0.92f)
                        )
                    )
            ) {
                JetStreamRadialScrim(
                    modifier = Modifier.fillMaxSize(),
                    centerColor = colorScheme.primary.copy(alpha = 0.22f),
                    edgeColor = Color.Transparent,
                    center = Offset(180f, 40f),
                    radius = 920f
                )
                JetStreamRadialScrim(
                    modifier = Modifier.fillMaxSize(),
                    centerColor = colorScheme.tertiary.copy(alpha = 0.16f),
                    edgeColor = Color.Transparent,
                    center = Offset(1600f, 760f),
                    radius = 980f
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            jetStreamVerticalScrimBrush(
                                startColor = colorScheme.background.copy(alpha = 0.04f),
                                middleColor = Color.Transparent,
                                endColor = colorScheme.background.copy(alpha = 0.30f)
                            )
                        )
                )
            }
        }
    }
}
