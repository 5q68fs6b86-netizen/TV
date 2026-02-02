package com.fongmi.android.tv.ui.dialog

import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Source
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.fongmi.android.tv.api.config.VodConfig
import com.fongmi.android.tv.bean.Site
import com.fongmi.android.tv.ui.components.DialogButton
import com.fongmi.android.tv.ui.components.FocusableItem
import com.fongmi.android.tv.ui.components.TabChip
import com.fongmi.android.tv.ui.state.HomeMode
import com.fongmi.android.tv.ui.theme.TvTypography

/**
 * Dialog for switching between home modes (TMDB / Site) and selecting sites
 */
@Composable
fun HomeModeDialog(
    currentMode: HomeMode,
    currentSite: Site?,
    sites: List<Site>,
    onModeChange: (HomeMode) -> Unit,
    onSiteChange: (Site) -> Unit,
    onDismiss: () -> Unit
) {
    val homeIndex = try {
        VodConfig.getHomeIndex()
    } catch (e: Exception) {
        0
    }
    val listState = rememberLazyListState()

    // Scroll to current site
    LaunchedEffect(Unit) {
        if (homeIndex >= 0 && homeIndex < sites.size) {
            listState.scrollToItem(homeIndex)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .onKeyEvent { event ->
                    if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN &&
                        event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_BACK) {
                        onDismiss()
                        true
                    } else {
                        false
                    }
                }
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Title
                Text(
                    text = "首页设置",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Mode selection tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HomeModeTab(
                        mode = HomeMode.TMDB,
                        isSelected = currentMode == HomeMode.TMDB,
                        onClick = { onModeChange(HomeMode.TMDB) },
                        modifier = Modifier.weight(1f)
                    )
                    HomeModeTab(
                        mode = HomeMode.SITE,
                        isSelected = currentMode == HomeMode.SITE,
                        onClick = { onModeChange(HomeMode.SITE) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Site list header
                Text(
                    text = "站源选择",
                    style = TvTypography.TitleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Site list
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    itemsIndexed(sites) { index, site ->
                        HomeSiteItem(
                            site = site,
                            isHome = index == homeIndex,
                            onClick = { onSiteChange(site) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    DialogButton(
                        text = "关闭",
                        isPrimary = true,
                        onClick = onDismiss
                    )
                }
            }
        }
    }
}

/**
 * Tab for selecting home mode
 */
@Composable
private fun HomeModeTab(
    mode: HomeMode,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FocusableItem(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) { isFocused ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = when {
                        isFocused -> MaterialTheme.colorScheme.primary
                        isSelected -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    width = if (isSelected && !isFocused) 2.dp else 0.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (mode == HomeMode.TMDB) Icons.Default.Movie else Icons.Default.Source,
                contentDescription = null,
                tint = when {
                    isFocused -> MaterialTheme.colorScheme.onPrimary
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (mode == HomeMode.TMDB) "TMDB 热门" else "站源内容",
                style = TvTypography.LabelLarge,
                color = when {
                    isFocused -> MaterialTheme.colorScheme.onPrimary
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

/**
 * Site item in the list
 */
@Composable
private fun HomeSiteItem(
    site: Site,
    isHome: Boolean,
    onClick: () -> Unit
) {
    FocusableItem(
        onClick = onClick
    ) { isFocused ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = when {
                        isFocused -> MaterialTheme.colorScheme.primaryContainer
                        isHome -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = if (isFocused) 2.dp else 0.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = site.name ?: "未知站点",
                    style = TvTypography.BodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isHome) {
                    Text(
                        text = "当前首页",
                        style = TvTypography.BodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (isHome) {
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "✓",
                        style = TvTypography.LabelSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
