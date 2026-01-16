package com.fongmi.android.tv.ui.screens.settings

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Source
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import android.view.KeyEvent
import androidx.hilt.navigation.compose.hiltViewModel
import com.fongmi.android.tv.bean.Site
import com.fongmi.android.tv.ui.components.FocusableItem
import com.fongmi.android.tv.ui.theme.TvColors
import com.fongmi.android.tv.ui.theme.TvDimens
import com.fongmi.android.tv.ui.theme.TvTypography
import com.fongmi.android.tv.ui.viewmodel.SettingsViewModel

/**
 * Settings Screen for app configuration
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onDanmuSettingsClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TvColors.Background)
            .padding(horizontal = TvDimens.ScreenPaddingHorizontal)
            .padding(top = TvDimens.ScreenPaddingVertical)
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
        // Header with back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FocusableItem(
                onClick = onBackClick,
                modifier = Modifier.focusRequester(focusRequester)
            ) { isFocused ->
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isFocused) TvColors.Primary else TvColors.Surface,
                            shape = CircleShape
                        )
                        .padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = if (isFocused) TvColors.OnPrimary else TvColors.TextPrimary
                    )
                }
            }

            Text(
                text = "设置",
                style = TvTypography.HeadlineLarge,
                color = TvColors.TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Source settings
            item {
                SettingsSection(
                    icon = Icons.Default.Source,
                    title = "数据源",
                    modifier = Modifier.focusRequester(focusRequester)
                ) {
                    // Current source
                    SettingsItem(
                        title = "当前源",
                        value = uiState.currentSite?.name ?: "未设置",
                        onClick = { /* Show source picker */ }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Source list
                    if (uiState.sites.isNotEmpty()) {
                        Text(
                            text = "可用源",
                            style = TvTypography.LabelMedium,
                            color = TvColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.sites.take(10)) { site ->
                                SiteChip(
                                    site = site,
                                    isSelected = site.key == uiState.currentSite?.key,
                                    onClick = { viewModel.setHomeSite(site) }
                                )
                            }
                        }
                    }
                }
            }

            // Player settings
            item {
                SettingsSection(
                    icon = Icons.Default.PlayCircle,
                    title = "播放器"
                ) {
                    // Player selection
                    val playerNames = viewModel.getPlayerNames()
                    SettingsOptionRow(
                        title = "默认播放器",
                        options = playerNames,
                        selectedIndex = uiState.playerSettings.defaultPlayer,
                        onSelect = { viewModel.setPlayer(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Decode type
                    val decodeNames = viewModel.getDecodeTypeNames()
                    SettingsOptionRow(
                        title = "解码方式",
                        options = decodeNames,
                        selectedIndex = uiState.playerSettings.decodeType,
                        onSelect = { viewModel.setDecodeType(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buffer size
                    SettingsSlider(
                        title = "缓冲大小",
                        value = uiState.playerSettings.bufferSize,
                        range = 1..5,
                        valueLabel = { "${it}MB" },
                        onValueChange = { viewModel.setBufferSize(it) }
                    )
                }
            }

            // Danmu settings
            item {
                SettingsSection(
                    icon = Icons.AutoMirrored.Filled.Comment,
                    title = "弹幕"
                ) {
                    SettingsItem(
                        title = "弹幕设置",
                        value = "字体、速度、显示区域等",
                        onClick = onDanmuSettingsClick
                    )
                }
            }

            // Theme settings
            item {
                SettingsSection(
                    icon = Icons.Default.Palette,
                    title = "主题"
                ) {
                    // Theme selection
                    val themeConfig = com.fongmi.android.tv.ui.theme.ThemeState.config
                    val themeNames = listOf("Aurora (紫蓝)", "Sakura (粉蓝)")
                    val currentThemeIndex = themeConfig.theme.ordinal
                    
                    SettingsOptionRow(
                        title = "主题风格",
                        options = themeNames,
                        selectedIndex = currentThemeIndex,
                        onSelect = { index ->
                            val newTheme = com.fongmi.android.tv.ui.theme.AppTheme.entries[index]
                            com.fongmi.android.tv.ui.theme.ThemeState.setTheme(newTheme)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mode selection
                    val modeNames = listOf("深色", "浅色", "跟随系统")
                    val currentModeIndex = themeConfig.mode.ordinal
                    
                    SettingsOptionRow(
                        title = "显示模式",
                        options = modeNames,
                        selectedIndex = currentModeIndex,
                        onSelect = { index ->
                            val newMode = com.fongmi.android.tv.ui.theme.ThemeMode.entries[index]
                            com.fongmi.android.tv.ui.theme.ThemeState.setMode(newMode)
                        }
                    )
                }
            }

            // Display settings
            item {
                SettingsSection(
                    icon = Icons.Default.Palette,
                    title = "显示"
                ) {
                    // Display size
                    SettingsSlider(
                        title = "海报大小",
                        value = uiState.displaySettings.size,
                        range = 1..7,
                        valueLabel = { "级别 $it" },
                        onValueChange = { viewModel.setDisplaySize(it) }
                    )
                }
            }

            // Language settings
            item {
                SettingsSection(
                    icon = Icons.Default.Language,
                    title = "语言"
                ) {
                    val languageNames = viewModel.getLanguageNames()
                    SettingsOptionRow(
                        title = "界面语言",
                        options = languageNames,
                        selectedIndex = uiState.displaySettings.language,
                        onSelect = { viewModel.setLanguage(it) }
                    )
                }
            }

            // About
            item {
                SettingsSection(
                    icon = Icons.Default.Info,
                    title = "关于"
                ) {
                    SettingsItem(
                        title = "版本",
                        value = "TV Compose v1.0.0",
                        onClick = { }
                    )
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SettingsSection(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(TvColors.Surface, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TvColors.Primary
            )
            Text(
                text = title,
                style = TvTypography.TitleMedium,
                color = TvColors.TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        content()
    }
}

@Composable
private fun SettingsItem(
    title: String,
    value: String,
    onClick: () -> Unit
) {
    FocusableItem(onClick = onClick) { isFocused ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = if (isFocused) TvColors.Primary.copy(alpha = 0.1f) else TvColors.Background,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = TvTypography.BodyMedium,
                color = TvColors.TextPrimary
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = value,
                    style = TvTypography.BodyMedium,
                    color = TvColors.TextSecondary
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TvColors.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun SettingsOptionRow(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Column {
        Text(
            text = title,
            style = TvTypography.BodyMedium,
            color = TvColors.TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(options.size) { index ->
                OptionChip(
                    text = options[index],
                    isSelected = index == selectedIndex,
                    onClick = { onSelect(index) }
                )
            }
        }
    }
}

@Composable
private fun OptionChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FocusableItem(onClick = onClick) { isFocused ->
        Box(
            modifier = Modifier
                .background(
                    color = when {
                        isFocused -> TvColors.Primary
                        isSelected -> TvColors.Primary.copy(alpha = 0.2f)
                        else -> TvColors.Background
                    },
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = if (isSelected && !isFocused) 1.dp else 0.dp,
                    color = TvColors.Primary,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = if (isFocused) TvColors.OnPrimary else TvColors.Primary
                    )
                }
                Text(
                    text = text,
                    style = TvTypography.LabelMedium,
                    color = when {
                        isFocused -> TvColors.OnPrimary
                        isSelected -> TvColors.Primary
                        else -> TvColors.TextPrimary
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsSlider(
    title: String,
    value: Int,
    range: IntRange,
    valueLabel: (Int) -> String,
    onValueChange: (Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = TvTypography.BodyMedium,
                color = TvColors.TextPrimary
            )
            Text(
                text = valueLabel(value),
                style = TvTypography.BodyMedium,
                color = TvColors.Primary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(range.count()) { index ->
                val itemValue = range.first + index
                OptionChip(
                    text = valueLabel(itemValue),
                    isSelected = itemValue == value,
                    onClick = { onValueChange(itemValue) }
                )
            }
        }
    }
}

@Composable
private fun SiteChip(
    site: Site,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FocusableItem(onClick = onClick) { isFocused ->
        Box(
            modifier = Modifier
                .background(
                    color = when {
                        isFocused -> TvColors.Primary
                        isSelected -> TvColors.Primary.copy(alpha = 0.2f)
                        else -> TvColors.Background
                    },
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = if (isSelected && !isFocused) 1.dp else 0.dp,
                    color = TvColors.Primary,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = if (isFocused) TvColors.OnPrimary else TvColors.Primary
                    )
                }
                Text(
                    text = site.name ?: site.key ?: "",
                    style = TvTypography.LabelMedium,
                    color = when {
                        isFocused -> TvColors.OnPrimary
                        isSelected -> TvColors.Primary
                        else -> TvColors.TextPrimary
                    }
                )
            }
        }
    }
}
