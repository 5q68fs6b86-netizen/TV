package com.fongmi.android.tv.ui.custom

import android.content.Context
import android.util.AttributeSet
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.fongmi.android.tv.R
import com.fongmi.android.tv.ui.components.jetStreamHorizontalScrimBrush
import com.fongmi.android.tv.ui.theme.JetStreamBorders
import com.fongmi.android.tv.ui.theme.JetStreamShapes
import com.fongmi.android.tv.ui.theme.JetStreamSpacing
import com.fongmi.android.tv.ui.theme.JetStreamTheme

class JetStreamSheetHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AbstractComposeView(context, attrs, defStyleAttr) {

    private var title by mutableStateOf("")
    private var subtitle by mutableStateOf("")
    private var eyebrow by mutableStateOf("")

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.JetStreamSheetHeaderView, defStyleAttr, 0)
        eyebrow = typedArray.getString(R.styleable.JetStreamSheetHeaderView_jetStreamEyebrow).orEmpty()
        title = typedArray.getString(R.styleable.JetStreamSheetHeaderView_jetStreamTitle).orEmpty()
        subtitle = typedArray.getString(R.styleable.JetStreamSheetHeaderView_jetStreamSubtitle).orEmpty()
        typedArray.recycle()
        isFocusable = false
        isFocusableInTouchMode = false
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
    }

    fun setText(text: CharSequence?) {
        title = text?.toString().orEmpty()
    }

    fun setText(resId: Int) {
        title = if (resId == 0) "" else context.getString(resId)
    }

    fun setTitle(text: CharSequence?) {
        title = text?.toString().orEmpty()
    }

    fun setSubtitle(text: CharSequence?) {
        subtitle = text?.toString().orEmpty()
    }

    fun setEyebrow(text: CharSequence?) {
        eyebrow = text?.toString().orEmpty()
    }

    @Composable
    override fun Content() {
        JetStreamTheme {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(JetStreamShapes.Large)
                    .background(
                        jetStreamHorizontalScrimBrush(
                            startColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.74f),
                            middleColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.20f),
                            endColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.46f)
                        )
                    )
                    .border(
                        JetStreamBorders.Thin,
                        MaterialTheme.colorScheme.outlineVariant,
                        JetStreamShapes.Large
                    )
                    .padding(horizontal = JetStreamSpacing.Large, vertical = JetStreamSpacing.Medium),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(36.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(Modifier.width(JetStreamSpacing.Medium))
                    Column(modifier = Modifier.weight(1f)) {
                        if (eyebrow.isNotBlank()) {
                            Text(
                                text = eyebrow.uppercase(),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.1.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(2.dp))
                        }
                        Text(
                            text = title,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 20.sp,
                            lineHeight = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (subtitle.isNotBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = subtitle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                lineHeight = 17.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
