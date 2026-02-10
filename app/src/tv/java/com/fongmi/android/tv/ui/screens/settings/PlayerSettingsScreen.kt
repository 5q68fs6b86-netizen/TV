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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp
import com.fongmi.android.tv.Setting
import com.fongmi.android.tv.ui.components.FocusableItem

/**
 * Player Settings Screen with all player options from original XML
 */
@Composable
fun PlayerSettingsScreen(
    onBackClick: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    
    // Player settings state
    var player by remember { mutableIntStateOf(Setting.getPlayer()) }
    var decode by remember { mutableIntStateOf(Setting.getDecode(player)) }
    var render by remember { mutableIntStateOf(Setting.getRender()) }
    var scale by remember { mutableIntStateOf(Setting.getScale()) }
    var flag by remember { mutableIntStateOf(Setting.getFlag()) }
    var tunnel by remember { mutableIntStateOf(if (Setting.isTunnel()) 1 else 0) }
    var http by remember { mutableIntStateOf(Setting.getHttp()) }
    var buffer by remember { mutableIntStateOf(Setting.getBuffer()) }
    var rtsp by remember { mutableIntStateOf(Setting.getRtsp()) }

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
                text = "播放设置",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 播放器选择
            item {
                SettingSelector(
                    title = "播放器",
                    options = listOf("系统", "ExoPlayer", "IJKPlayer"),
                    selectedIndex = player,
                    onSelect = { index ->
                        player = index
                        Setting.putPlayer(index)
                    }
                )
            }

            // 解码方式
            item {
                SettingSelector(
                    title = "解码",
                    options = listOf("软解", "硬解"),
                    selectedIndex = decode,
                    onSelect = { index ->
                        decode = index
                        Setting.putDecode(player, index)
                    }
                )
            }

            // 渲染
            item {
                SettingSelector(
                    title = "渲染",
                    options = listOf("SurfaceView", "TextureView"),
                    selectedIndex = render,
                    onSelect = { index ->
                        render = index
                        Setting.putRender(index)
                    }
                )
            }

            // 画面比例
            item {
                SettingSelector(
                    title = "画面比例",
                    options = listOf("默认", "16:9", "4:3", "填充", "原始", "裁剪"),
                    selectedIndex = scale,
                    onSelect = { index ->
                        scale = index
                        Setting.putScale(index)
                    }
                )
            }

            // 线路选择
            item {
                SettingSelector(
                    title = "线路",
                    options = listOf("自动", "手动"),
                    selectedIndex = flag,
                    onSelect = { index ->
                        flag = index
                        Setting.putFlag(index)
                    }
                )
            }

            // Tunnel 模式
            item {
                SettingSelector(
                    title = "隧道播放",
                    options = listOf("关闭", "开启"),
                    selectedIndex = tunnel,
                    onSelect = { index ->
                        tunnel = index
                        Setting.putTunnel(index == 1)
                    }
                )
            }

            // HTTP 库
            item {
                SettingSelector(
                    title = "HTTP",
                    options = listOf("OkHttp", "Cronet", "默认"),
                    selectedIndex = http,
                    onSelect = { index ->
                        http = index
                        Setting.putHttp(index)
                    }
                )
            }

            // 缓冲倍数
            item {
                SettingSelector(
                    title = "缓冲",
                    options = listOf("0.5x", "1x", "1.5x", "2x", "3x", "5x"),
                    selectedIndex = buffer.coerceIn(0, 5),
                    onSelect = { index ->
                        buffer = index
                        Setting.putBuffer(index)
                    }
                )
            }

            // RTSP
            item {
                SettingSelector(
                    title = "RTSP",
                    options = listOf("UDP", "TCP", "自动"),
                    selectedIndex = rtsp,
                    onSelect = { index ->
                        rtsp = index
                        Setting.putRtsp(index)
                    }
                )
            }

            // UA 设置
            item {
                SettingTextItem(
                    title = "User-Agent",
                    value = Setting.getUa().ifEmpty { "默认" },
                    onClick = { /* Show UA input dialog */ }
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SettingSelector(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
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
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEachIndexed { index, option ->
                OptionChip(
                    text = option,
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
    FocusableItem(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp)
    ) { isFocused ->
        Box(
            modifier = Modifier
                .background(
                    color = when {
                        isFocused -> MaterialTheme.colorScheme.primary
                        isSelected -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surface
                    },
                    shape = RoundedCornerShape(20.dp)
                )
                .border(
                    width = if (isSelected && !isFocused) 1.dp else 0.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(20.dp)
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

@Composable
private fun SettingTextItem(
    title: String,
    value: String,
    onClick: () -> Unit
) {
    FocusableItem(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp)
    ) { isFocused ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = if (isFocused) MaterialTheme.colorScheme.primaryContainer
                           else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    width = if (isFocused) 2.dp else 0.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SettingsIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
                .background(
                    color = if (isFocused) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(12.dp),
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
