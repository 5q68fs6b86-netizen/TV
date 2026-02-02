package com.fongmi.android.tv.ui.dialog

import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.fongmi.android.tv.api.config.LiveConfig
import com.fongmi.android.tv.api.config.VodConfig
import com.fongmi.android.tv.api.config.WallConfig
import com.fongmi.android.tv.bean.Config
import com.fongmi.android.tv.server.Server
import com.fongmi.android.tv.ui.components.FocusableItem
import com.fongmi.android.tv.utils.UrlUtil

/**
 * 配置类型
 */
enum class ConfigType(val value: Int) {
    VOD(0),
    LIVE(1),
    WALL(2)
}

/**
 * 配置对话框
 */
@Composable
fun ConfigDialog(
    type: ConfigType,
    isEdit: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (Config) -> Unit
) {
    val initialUrl = remember {
        try {
            when (type) {
                ConfigType.VOD -> VodConfig.getUrl()
                ConfigType.LIVE -> LiveConfig.getUrl()
                ConfigType.WALL -> WallConfig.getUrl()
            } ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    var url by remember { mutableStateOf(initialUrl) }
    var name by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    val title = remember {
        when (type) {
            ConfigType.VOD -> "点播配置"
            ConfigType.LIVE -> "直播配置"
            ConfigType.WALL -> "壁纸配置"
        }
    }

    val serverAddress = remember {
        try {
            Server.get().getAddress() ?: "未启动"
        } catch (e: Exception) {
            "未启动"
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.6f)
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
                // 标题
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 推送地址提示
                Text(
                    text = "推送地址: $serverAddress",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 名称输入框
                Text(
                    text = "名称 (可选)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                ConfigTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "输入配置名称",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // URL 输入框
                Text(
                    text = "配置地址",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                ConfigTextField(
                    value = url,
                    onValueChange = { newValue ->
                        url = autoComplete(url, newValue)
                    },
                    placeholder = "http:// 或 file://",
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    DialogButton(
                        text = "取消",
                        onClick = onDismiss
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    DialogButton(
                        text = if (isEdit) "编辑" else "确定",
                        isPrimary = true,
                        onClick = {
                            val fixedUrl = UrlUtil.fixUrl(url.trim())
                            if (isEdit) {
                                Config.find(initialUrl, type.value).url(fixedUrl).update()
                            }
                            if (fixedUrl.isEmpty()) {
                                Config.delete(initialUrl, type.value)
                            }
                            val config = if (name.trim().isEmpty()) {
                                Config.find(fixedUrl, type.value)
                            } else {
                                Config.find(fixedUrl, name.trim(), type.value)
                            }
                            onConfirm(config)
                        }
                    )
                }
            }
        }
    }
}

/**
 * 自动补全 URL
 */
private fun autoComplete(oldValue: String, newValue: String): String {
    if (oldValue.isEmpty() && newValue.length == 1) {
        return when (newValue.lowercase()) {
            "h" -> "http://"
            "f" -> "file://"
            "a" -> "assets://"
            else -> newValue
        }
    }
    return newValue
}

@Composable
private fun ConfigTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = MaterialTheme.typography.bodyLarge.fontSize
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        decorationBox = { innerTextField ->
            Box {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun DialogButton(
    text: String,
    onClick: () -> Unit,
    isPrimary: Boolean = false
) {
    FocusableItem(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        focusElevation = 0.dp
    ) { isFocused ->
        Box(
            modifier = Modifier
                .background(
                    color = when {
                        isFocused -> MaterialTheme.colorScheme.primary
                        isPrimary -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = if (isFocused) 2.dp else 0.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 24.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = when {
                    isFocused -> MaterialTheme.colorScheme.onPrimary
                    isPrimary -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}
