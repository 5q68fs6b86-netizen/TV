package com.fongmi.android.tv.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.fongmi.android.tv.R
import com.fongmi.android.tv.ui.components.jetStreamHorizontalScrimBrush
import com.fongmi.android.tv.ui.theme.JetStreamBorders
import com.fongmi.android.tv.ui.theme.JetStreamShapes
import com.fongmi.android.tv.ui.theme.JetStreamSpacing
import com.fongmi.android.tv.ui.theme.JetStreamTheme

class JetStreamEmptyStateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AbstractComposeView(context, attrs, defStyleAttr) {

    private var message by mutableStateOf(context.getString(R.string.error_empty))

    init {
        val typedArray = context.obtainStyledAttributes(attrs, intArrayOf(android.R.attr.text), defStyleAttr, 0)
        message = typedArray.getText(0)?.toString()?.takeIf { it.isNotBlank() } ?: context.getString(R.string.error_empty)
        typedArray.recycle()
        isFocusable = false
        isFocusableInTouchMode = false
        descendantFocusability = android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
    }

    fun setText(text: CharSequence?) {
        message = text?.toString()?.takeIf { it.isNotBlank() } ?: context.getString(R.string.error_empty)
    }

    fun setText(resId: Int) {
        message = if (resId == 0) context.getString(R.string.error_empty) else context.getString(resId)
    }

    @Composable
    override fun Content() {
        JetStreamTheme {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 320.dp)
                    .clip(JetStreamShapes.Card)
                    .background(
                        jetStreamHorizontalScrimBrush(
                            startColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
                            middleColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.68f),
                            endColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
                        )
                    )
                    .border(
                        JetStreamBorders.Thin,
                        MaterialTheme.colorScheme.outlineVariant,
                        JetStreamShapes.Card
                    )
                    .padding(horizontal = JetStreamSpacing.ExtraExtraLarge, vertical = JetStreamSpacing.ExtraLarge),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AndroidView(
                        factory = { viewContext ->
                            LottieAnimationView(viewContext).apply {
                                setAnimation(R.raw.empty)
                                repeatCount = LottieDrawable.INFINITE
                                scaleType = ImageView.ScaleType.CENTER_INSIDE
                                playAnimation()
                            }
                        },
                        modifier = Modifier.size(width = 180.dp, height = 190.dp)
                    )
                    Spacer(Modifier.height(JetStreamSpacing.Large))
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 17.sp,
                        lineHeight = 23.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
