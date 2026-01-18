package com.fongmi.android.tv.ui.screens.settings

import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fongmi.android.tv.ui.components.FocusableItem
import com.fongmi.android.tv.ui.viewmodel.SettingsViewModel

/**
 * Complete Settings Screen with all options from original XML layout
 * Matches Material3 design with all configuration options
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onVodConfigClick: () -> Unit = {},
    onLiveConfigClick: () -> Unit = {},
    onWallConfigClick: () -> Unit = {},
    onPlayerSettingsClick: () -> Unit = {},
    onDanmuSettingsClick: () -> Unit = {},
    onCustomSettingsClick: () -> Unit = {},
    onAboutClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .onKeyEvent { event ->
                if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN &&
                    event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_BACK) {
                    onBackClick()
                    true
                } else {
                    false
                }
            }
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                onClick = onBackClick,
                modifier = Modifier.focusRequester(focusRequester)
            )
            Text(
                text = "设置",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ========== VOD 配置 ==========
            item {
                ConfigRow(
                    title = "点播配置",
                    value = uiState.currentSite?.name ?: "未配置",
                    onClick = onVodConfigClick,
                    onHomeClick = { uiState.currentSite?.let { viewModel.setHomeSite(it) } },
                    onHistoryClick = { /* Show VOD history dialog */ }
                )
            }

            // ========== Live 配置 ==========
            item {
                ConfigRow(
                    title = "直播配置",
                    value = "未配置",
                    onClick = onLiveConfigClick,
                    onHomeClick = { /* Set live home */ },
                    onHistoryClick = { /* Show live history */ }
                )
            }

            // ========== Wall 配置 ==========
            item {
                ConfigRow(
                    title = "壁纸配置",
                    value = "默认",
                    onClick = onWallConfigClick,
                    onHomeClick = { /* Set default wall */ },
                    onHistoryClick = null,
                    historyIcon = Icons.Default.Refresh,
                    historyLabel = "刷新"
                )
            }

            // ========== 播放器 & 弹幕 ==========
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SettingsButton(
                        title = "播放设置",
                        onClick = onPlayerSettingsClick,
                        modifier = Modifier.weight(1f)
                    )
                    SettingsButton(
                        title = "弹幕设置",
                        onClick = onDanmuSettingsClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ========== 自定义 & 代理 ==========
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SettingsButton(
                        title = "自定义",
                        onClick = onCustomSettingsClick,
                        modifier = Modifier.weight(1f)
                    )
                    SettingsValueButton(
                        title = "代理",
                        value = viewModel.getProxyText(),
                        onClick = { viewModel.showProxyDialog() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ========== 备份 & 恢复 ==========
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SettingsValueButton(
                        title = "备份",
                        value = viewModel.getBackupModeText(),
                        onClick = { viewModel.toggleBackupMode() },
                        modifier = Modifier.weight(1f)
                    )
                    SettingsButton(
                        title = "恢复",
                        onClick = { viewModel.restore() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ========== 缓存 & DOH ==========
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SettingsValueButton(
                        title = "缓存",
                        value = viewModel.getCacheText(),
                        onClick = { viewModel.clearCache() },
                        modifier = Modifier.weight(1f)
                    )
                    SettingsValueButton(
                        title = "DOH",
                        value = viewModel.getDohText(),
                        onClick = { viewModel.showDohDialog() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ========== 主题设置 ==========
            item {
                SettingsSection(title = "主题") {
                    val themeConfig = com.fongmi.android.tv.ui.theme.ThemeState.config
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Theme style
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "主题风格",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ThemeChip(
                                    text = "Aurora",
                                    isSelected = themeConfig.theme == com.fongmi.android.tv.ui.theme.AppTheme.AURORA,
                                    onClick = { 
                                        com.fongmi.android.tv.ui.theme.ThemeState.setTheme(
                                            com.fongmi.android.tv.ui.theme.AppTheme.AURORA
                                        )
                                    }
                                )
                                ThemeChip(
                                    text = "Sakura",
                                    isSelected = themeConfig.theme == com.fongmi.android.tv.ui.theme.AppTheme.SAKURA,
                                    onClick = { 
                                        com.fongmi.android.tv.ui.theme.ThemeState.setTheme(
                                            com.fongmi.android.tv.ui.theme.AppTheme.SAKURA
                                        )
                                    }
                                )
                            }
                        }
                        
                        // Theme mode
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "显示模式",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ThemeChip(
                                    text = "深色",
                                    isSelected = themeConfig.mode == com.fongmi.android.tv.ui.theme.ThemeMode.DARK,
                                    onClick = { 
                                        com.fongmi.android.tv.ui.theme.ThemeState.setMode(
                                            com.fongmi.android.tv.ui.theme.ThemeMode.DARK
                                        )
                                    }
                                )
                                ThemeChip(
                                    text = "浅色",
                                    isSelected = themeConfig.mode == com.fongmi.android.tv.ui.theme.ThemeMode.LIGHT,
                                    onClick = { 
                                        com.fongmi.android.tv.ui.theme.ThemeState.setMode(
                                            com.fongmi.android.tv.ui.theme.ThemeMode.LIGHT
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // ========== 版本 & 关于 ==========
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SettingsValueButton(
                        title = "版本",
                        value = viewModel.getVersionText(),
                        onClick = { viewModel.checkUpdate() },
                        modifier = Modifier.weight(1f)
                    )
                    SettingsValueButton(
                        title = "关于",
                        value = "",
                        onClick = onAboutClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun ConfigRow(
    title: String,
    value: String,
    onClick: () -> Unit,
    onHomeClick: () -> Unit,
    onHistoryClick: (() -> Unit)?,
    historyIcon: ImageVector = Icons.Default.History,
    historyLabel: String = "历史"
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Main config button
        FocusableItem(
            onClick = onClick,
            modifier = Modifier.weight(1f)
        ) { isFocused ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (isFocused) MaterialTheme.colorScheme.primaryContainer
                               else MaterialTheme.colorScheme.surfaceVariant,
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
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false).padding(start = 16.dp)
                )
            }
        }

        // Home button
        SettingsIconButton(
            icon = Icons.Default.Home,
            onClick = onHomeClick
        )

        // History/Refresh button
        if (onHistoryClick != null) {
            SettingsIconButton(
                icon = historyIcon,
                onClick = onHistoryClick
            )
        }
    }
}

@Composable
private fun SettingsButton(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FocusableItem(
        onClick = onClick,
        modifier = modifier
    ) { isFocused ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = if (isFocused) MaterialTheme.colorScheme.primaryContainer
                           else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = if (isFocused) 2.dp else 0.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SettingsValueButton(
    title: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FocusableItem(
        onClick = onClick,
        modifier = modifier
    ) { isFocused ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = if (isFocused) MaterialTheme.colorScheme.primaryContainer
                           else MaterialTheme.colorScheme.surfaceVariant,
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
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (value.isNotEmpty()) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SettingsIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FocusableItem(
        onClick = onClick,
        modifier = modifier
    ) { isFocused ->
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = if (isFocused) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = if (isFocused) 2.dp else 0.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isFocused) MaterialTheme.colorScheme.onPrimary
                      else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun ThemeChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FocusableItem(onClick = onClick) { isFocused ->
        Box(
            modifier = Modifier
                .background(
                    color = when {
                        isFocused -> MaterialTheme.colorScheme.primary
                        isSelected -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surface
                    },
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = if (isSelected && !isFocused) 1.dp else 0.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = when {
                    isFocused -> MaterialTheme.colorScheme.onPrimary
                    isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}
