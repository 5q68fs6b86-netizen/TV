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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fongmi.android.tv.R
import com.fongmi.android.tv.ui.theme.JetStreamTheme
import com.fongmi.android.tv.ui.theme.JetStreamAnimations
import com.fongmi.android.tv.ui.theme.JetStreamShapes
import com.fongmi.android.tv.ui.theme.JetStreamBorders

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
    private var currentSelectedKey by mutableStateOf("")

    init {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
    }

    fun setItems(newItems: List<NavItem>) {
        items.clear()
        items.addAll(newItems)
    }

    fun setSelectedKey(key: String) {
        currentSelectedKey = key
    }

    @Composable
    override fun Content() {
        JetStreamTheme {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items.forEach { item ->
                    NavButton(
                        item = item,
                        selected = item.key == currentSelectedKey,
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
        val scale by animateFloatAsState(
            if (focused) JetStreamAnimations.FocusScaleMedium else 1.0f,
            animationSpec = JetStreamAnimations.ScaleSpring,
            label = "navScale"
        )
        val background by animateColorAsState(
            targetValue = when {
                focused -> MaterialTheme.colorScheme.primaryContainer
                selected -> MaterialTheme.colorScheme.secondaryContainer
                else -> Color.Transparent
            },
            animationSpec = JetStreamAnimations.ColorTween,
            label = "navBackground"
        )
        val border by animateColorAsState(
            targetValue = if (focused) MaterialTheme.colorScheme.primary else Color.Transparent,
            animationSpec = JetStreamAnimations.ColorTween,
            label = "navBorder"
        )
        val contentColor by animateColorAsState(
            targetValue = when {
                focused -> MaterialTheme.colorScheme.onPrimaryContainer
                selected -> MaterialTheme.colorScheme.onSecondaryContainer
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            animationSpec = JetStreamAnimations.ColorTween,
            label = "navContent"
        )

        Row(
            modifier = Modifier
                .height(34.dp)
                .widthIn(min = 54.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .clip(JetStreamShapes.Button)
                .background(background)
                .then(
                    if (focused) Modifier.border(
                        JetStreamBorders.Thin,
                        border,
                        JetStreamShapes.Button
                    )
                    else Modifier
                )
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .padding(horizontal = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = remember(item.drawableRes) { iconFor(item.drawableRes) },
                contentDescription = item.text,
                modifier = Modifier.size(19.dp),
                tint = contentColor
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = item.text,
                color = contentColor,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    companion object {
        private fun iconFor(resId: Int): ImageVector {
            return when (resId) {
                R.drawable.ic_home_live -> Icons.Rounded.LiveTv
                R.drawable.ic_home_search -> Icons.Rounded.Search
                R.drawable.ic_home_keep -> Icons.Rounded.Favorite
                R.drawable.ic_home_push -> Icons.Rounded.CloudUpload
                R.drawable.ic_home_setting -> Icons.Rounded.Settings
                else -> Icons.Rounded.Movie
            }
        }
    }
}
