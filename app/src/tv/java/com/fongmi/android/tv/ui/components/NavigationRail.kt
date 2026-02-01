package com.fongmi.android.tv.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.fongmi.android.tv.ui.theme.CommonColors
import com.fongmi.android.tv.ui.theme.TvTheme

/**
 * Navigation item data
 */
data class NavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

/**
 * Default navigation items for TV app
 */
val defaultNavItems = listOf(
    NavItem("home", Icons.Filled.Home, "首页"),
    NavItem("search", Icons.Filled.Search, "搜索"),
    NavItem("live", Icons.Filled.LiveTv, "直播"),
    NavItem("favorites", Icons.Filled.Favorite, "收藏"),
    NavItem("history", Icons.Filled.History, "历史"),
    NavItem("settings", Icons.Filled.Settings, "设置")
)

/**
 * TV Navigation Rail with glassmorphism effect
 *
 * @param items Navigation items to display
 * @param selectedIndex Currently selected index
 * @param onItemSelected Called when an item is selected
 * @param modifier Modifier for the rail
 */
@Composable
fun TvNavigationRail(
    items: List<NavItem> = defaultNavItems,
    selectedIndex: Int = 0,
    onItemSelected: (Int, NavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequesters = remember { items.map { FocusRequester() } }
    var focusedIndex by remember { mutableIntStateOf(-1) }

    // Glassmorphism background with left padding
    Box(
        modifier = modifier
            .padding(start = 24.dp, top = 16.dp, bottom = 16.dp)
            .width(72.dp)
            .fillMaxHeight()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Logo/Brand area at top
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "TV",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation items
            items.forEachIndexed { index, item ->
                NavRailItem(
                    icon = item.icon,
                    label = item.label,
                    isSelected = selectedIndex == index,
                    isFocused = focusedIndex == index,
                    focusRequester = focusRequesters[index],
                    onFocusChanged = { focused ->
                        if (focused) focusedIndex = index
                    },
                    onClick = { onItemSelected(index, item) }
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

/**
 * Single navigation rail item
 */
@Composable
private fun NavRailItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    isFocused: Boolean,
    focusRequester: FocusRequester,
    onFocusChanged: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isFocused -> MaterialTheme.colorScheme.primary
            isSelected -> MaterialTheme.colorScheme.primaryContainer
            else -> Color.Transparent
        },
        animationSpec = tween(200),
        label = "navItemBg"
    )

    val iconColor by animateColorAsState(
        targetValue = when {
            isFocused -> MaterialTheme.colorScheme.onPrimary
            isSelected -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(200),
        label = "navItemIcon"
    )

    FocusableItem(
        onClick = onClick,
        modifier = Modifier
            .focusRequester(focusRequester)
            .onFocusChanged { onFocusChanged(it.isFocused) },
        shape = RoundedCornerShape(12.dp),
        focusBorderWidth = 0.dp,
        focusScale = 1.0f
    ) { focused ->
        Box(
            modifier = Modifier
                .width(56.dp)
                .height(48.dp)
                .background(backgroundColor, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
