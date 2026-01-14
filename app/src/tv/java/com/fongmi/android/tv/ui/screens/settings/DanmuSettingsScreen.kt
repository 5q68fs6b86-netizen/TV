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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.ViewStream
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
import com.fongmi.android.tv.ui.components.FocusableItem
import com.fongmi.android.tv.ui.theme.TvColors
import com.fongmi.android.tv.ui.theme.TvDimens
import com.fongmi.android.tv.ui.theme.TvTypography
import com.fongmi.android.tv.ui.viewmodel.DanmuSettingsViewModel

/**
 * Danmu (Bullet Comments) Settings Screen
 */
@Composable
fun DanmuSettingsScreen(
    viewModel: DanmuSettingsViewModel = hiltViewModel(),
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
        // Header
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
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = if (isFocused) TvColors.OnPrimary else TvColors.TextPrimary
                    )
                }
            }

            Text(
                text = "弹幕设置",
                style = TvTypography.HeadlineLarge,
                color = TvColors.TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Size settings
            item {
                DanmuSettingsSection(
                    icon = Icons.Default.FormatSize,
                    title = "弹幕大小"
                ) {
                    DanmuOptionRow(
                        title = "字体大小",
                        options = listOf("小", "中", "大", "特大"),
                        selectedIndex = uiState.fontSize,
                        onSelect = { viewModel.setFontSize(it) }
                    )
                }
            }

            // Speed settings
            item {
                DanmuSettingsSection(
                    icon = Icons.Default.Speed,
                    title = "滚动速度"
                ) {
                    DanmuOptionRow(
                        title = "弹幕速度",
                        options = listOf("慢", "中", "快", "极快"),
                        selectedIndex = uiState.speed,
                        onSelect = { viewModel.setSpeed(it) }
                    )
                }
            }

            // Display settings
            item {
                DanmuSettingsSection(
                    icon = Icons.Default.ViewStream,
                    title = "显示设置"
                ) {
                    DanmuOptionRow(
                        title = "最大行数",
                        options = listOf("3行", "5行", "8行", "不限制"),
                        selectedIndex = uiState.maxLines,
                        onSelect = { viewModel.setMaxLines(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DanmuOptionRow(
                        title = "透明度",
                        options = listOf("25%", "50%", "75%", "100%"),
                        selectedIndex = uiState.alpha,
                        onSelect = { viewModel.setAlpha(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DanmuOptionRow(
                        title = "弹幕区域",
                        options = listOf("1/4屏", "1/2屏", "3/4屏", "全屏"),
                        selectedIndex = uiState.area,
                        onSelect = { viewModel.setArea(it) }
                    )
                }
            }

            // Filter settings
            item {
                DanmuSettingsSection(
                    icon = Icons.Default.Translate,
                    title = "过滤设置"
                ) {
                    DanmuOptionRow(
                        title = "滚动弹幕",
                        options = listOf("显示", "隐藏"),
                        selectedIndex = if (uiState.showScrolling) 0 else 1,
                        onSelect = { viewModel.setShowScrolling(it == 0) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DanmuOptionRow(
                        title = "顶部弹幕",
                        options = listOf("显示", "隐藏"),
                        selectedIndex = if (uiState.showTop) 0 else 1,
                        onSelect = { viewModel.setShowTop(it == 0) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DanmuOptionRow(
                        title = "底部弹幕",
                        options = listOf("显示", "隐藏"),
                        selectedIndex = if (uiState.showBottom) 0 else 1,
                        onSelect = { viewModel.setShowBottom(it == 0) }
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
private fun DanmuSettingsSection(
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
private fun DanmuOptionRow(
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
                DanmuOptionChip(
                    text = options[index],
                    isSelected = index == selectedIndex,
                    onClick = { onSelect(index) }
                )
            }
        }
    }
}

@Composable
private fun DanmuOptionChip(
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
