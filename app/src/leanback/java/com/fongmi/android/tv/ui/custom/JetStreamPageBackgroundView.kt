package com.fongmi.android.tv.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.fongmi.android.tv.ui.theme.JetStreamColors
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                JetStreamColors.Background,
                                JetStreamColors.Surface,
                                Color(0xFF090A10)
                            )
                        )
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    JetStreamColors.Primary.copy(alpha = 0.22f),
                                    Color.Transparent
                                ),
                                center = Offset(180f, 40f),
                                radius = 920f
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    JetStreamColors.Tertiary.copy(alpha = 0.16f),
                                    Color.Transparent
                                ),
                                center = Offset(1600f, 760f),
                                radius = 980f
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.04f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.30f)
                                )
                            )
                        )
                )
            }
        }
    }
}
