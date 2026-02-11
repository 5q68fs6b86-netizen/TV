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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fongmi.android.tv.ui.components.FocusableItem
import com.fongmi.android.tv.ui.viewmodel.CustomSettingsViewModel

/**
 * 自定义设置页面
 */
@Composable
fun CustomSettingsScreen(
    viewModel: CustomSettingsViewModel = hiltViewModel(),
    onBackClick: () -> Unit
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
            CustomIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                onClick = onBackClick,
                modifier = Modifier.focusRequester(focusRequester)
            )
            Text(
                text = "自定义设置",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ========== 显示设置 ==========
            item {
                SettingsSectionTitle("显示设置")
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CustomValueButton(
                        title = "图片质量",
                        value = uiState.qualityText,
                        onClick = { viewModel.toggleQuality() },
                        modifier = Modifier.weight(1f)
                    )
                    CustomValueButton(
                        title = "卡片尺寸",
                        value = uiState.sizeText,
                        onClick = { viewModel.toggleSize() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CustomValueButton(
                        title = "剧集排序",
                        value = uiState.episodeText,
                        onClick = { viewModel.toggleEpisode() },
                        modifier = Modifier.weight(1f)
                    )
                    CustomValueButton(
                        title = "首页UI",
                        value = uiState.homeUIText,
                        onClick = { viewModel.toggleHomeUI() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ========== 播放设置 ==========
            item {
                SettingsSectionTitle("播放设置")
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CustomValueButton(
                        title = "播放速度",
                        value = uiState.speedText,
                        onClick = { viewModel.toggleSpeed() },
                        onLongClick = { viewModel.resetSpeed() },
                        modifier = Modifier.weight(1f)
                    )
                    CustomValueButton(
                        title = "去广告",
                        value = uiState.removeAdText,
                        onClick = { viewModel.toggleRemoveAd() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ========== 按键设置 ==========
            item {
                SettingsSectionTitle("按键设置")
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CustomValueButton(
                        title = "全屏菜单键",
                        value = uiState.fullscreenMenuKeyText,
                        onClick = { viewModel.toggleFullscreenMenuKey() },
                        modifier = Modifier.weight(1f)
                    )
                    CustomValueButton(
                        title = "小窗返回键",
                        value = uiState.smallWindowBackKeyText,
                        onClick = { viewModel.toggleSmallWindowBackKey() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                CustomValueButton(
                    title = "首页菜单键",
                    value = uiState.homeMenuKeyText,
                    onClick = { viewModel.toggleHomeMenuKey() },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ========== 首页设置 ==========
            item {
                SettingsSectionTitle("首页设置")
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CustomValueButton(
                        title = "站点锁定",
                        value = uiState.homeSiteLockText,
                        onClick = { viewModel.toggleHomeSiteLock() },
                        modifier = Modifier.weight(1f)
                    )
                    CustomValueButton(
                        title = "首页历史",
                        value = uiState.homeHistoryText,
                        onClick = { viewModel.toggleHomeHistory() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CustomValueButton(
                        title = "聚合搜索",
                        value = uiState.aggregatedSearchText,
                        onClick = { viewModel.toggleAggregatedSearch() },
                        modifier = Modifier.weight(1f)
                    )
                    CustomValueButton(
                        title = "站源结果数",
                        value = uiState.searchResultLimitText,
                        onClick = { viewModel.toggleSearchResultLimit() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CustomValueButton(
                        title = "无痕模式",
                        value = uiState.incognitoText,
                        onClick = { viewModel.toggleIncognito() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ========== 高级设置 ==========
            item {
                SettingsSectionTitle("高级设置")
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CustomValueButton(
                        title = "解析WebView",
                        value = uiState.parseWebViewText,
                        onClick = { viewModel.toggleParseWebView() },
                        modifier = Modifier.weight(1f)
                    )
                    CustomValueButton(
                        title = "配置缓存",
                        value = uiState.configCacheText,
                        onClick = { viewModel.toggleConfigCache() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                CustomValueButton(
                    title = "语言",
                    value = uiState.languageText,
                    onClick = { viewModel.toggleLanguage() },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ========== 危险操作 ==========
            item {
                SettingsSectionTitle("危险操作")
            }

            item {
                CustomButton(
                    title = "重置应用",
                    onClick = { viewModel.showResetDialog() },
                    isDanger = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun CustomValueButton(
    title: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null
) {
    FocusableItem(
        onClick = {
            onClick()
        },
        modifier = modifier,
        shape = RoundedCornerShape(8.dp)
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CustomButton(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDanger: Boolean = false
) {
    FocusableItem(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp)
    ) { isFocused ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = when {
                        isFocused && isDanger -> MaterialTheme.colorScheme.error
                        isFocused -> MaterialTheme.colorScheme.primaryContainer
                        isDanger -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = if (isFocused) 2.dp else 0.dp,
                    color = if (isDanger) MaterialTheme.colorScheme.error
                           else MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = when {
                    isFocused && isDanger -> MaterialTheme.colorScheme.onError
                    isDanger -> MaterialTheme.colorScheme.onErrorContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

@Composable
private fun CustomIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FocusableItem(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp)
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
