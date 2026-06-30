package com.fongmi.android.tv.ui.custom

import android.content.Context
import android.util.AttributeSet
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat

class JetStreamHomeNavView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AbstractComposeView(context, attrs, defStyleAttr) {

    interface Listener {
        fun onNavClick(key: String)
        fun onNavLongClick(key: String)
    }

    data class NavItem(
        val key: String,
        val text: String,
        val drawableRes: Int
    )

    var listener: Listener? = null
    private val items = mutableStateListOf<NavItem>()
    private var selectedKey by mutableStateOf("")

    init {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
    }

    fun setItems(newItems: List<NavItem>) {
        items.clear()
        items.addAll(newItems)
    }

    fun setSelectedKey(key: String) {
        selectedKey = key
    }

    @Composable
    override fun Content() {
        MaterialTheme(colorScheme = darkColorScheme(primary = Color.White, onSurface = Color.White, surface = Color.Transparent)) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEach { item ->
                    NavButton(
                        item = item,
                        selected = item.key == selectedKey,
                        onClick = { listener?.onNavClick(item.key) },
                        onLongClick = { listener?.onNavLongClick(item.key) }
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun NavButton(item: NavItem, selected: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
        val interactionSource = remember { MutableInteractionSource() }
        val focused by interactionSource.collectIsFocusedAsState()
        val scale by animateFloatAsState(if (focused) 1.05f else 1.0f, label = "navScale")
        val background by animateColorAsState(
            targetValue = when {
                focused -> Color.White.copy(alpha = 0.22f)
                selected -> Color.White.copy(alpha = 0.16f)
                else -> Color.White.copy(alpha = 0.08f)
            },
            label = "navBackground"
        )
        val borderAlpha by animateFloatAsState(if (focused) 0.25f else 0f, label = "navBorder")

        Row(
            modifier = Modifier
                .height(44.dp)
                .widthIn(min = 92.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .clip(RoundedCornerShape(12.dp))
                .background(background)
                .then(
                    if (focused) Modifier.border(1.5.dp, Color.White.copy(alpha = borderAlpha), RoundedCornerShape(12.dp))
                    else Modifier
                )
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val painter = rememberDrawablePainter(item.drawableRes)
            if (painter != null) {
                Icon(
                    painter = painter,
                    contentDescription = item.text,
                    modifier = Modifier.size(21.dp),
                    tint = Color.White.copy(alpha = if (focused || selected) 1f else 0.78f)
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = item.text,
                color = Color.White.copy(alpha = if (focused || selected) 1f else 0.78f),
                fontSize = 16.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    companion object {
        @Composable
        private fun rememberDrawablePainter(resId: Int): BitmapPainter? {
            val resources = androidx.compose.ui.platform.LocalContext.current.resources
            return remember(resId) {
                val drawable = ResourcesCompat.getDrawable(resources, resId, null) ?: return@remember null
                val bitmap = android.graphics.Bitmap.createBitmap(
                    drawable.intrinsicWidth.coerceAtLeast(1),
                    drawable.intrinsicHeight.coerceAtLeast(1),
                    android.graphics.Bitmap.Config.ARGB_8888
                )
                val canvas = android.graphics.Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                BitmapPainter(bitmap.asImageBitmap())
            }
        }
    }
}
