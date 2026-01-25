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
import com.fongmi.android.tv.Setting
import com.fongmi.android.tv.ui.components.FocusableItem
import com.github.catvod.utils.Util

/**
 * User-Agent 设置对话框
 */
@Composable
fun UaDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val initialUa = remember { Setting.getUa() ?: "" }
    var ua by remember { mutableStateOf(initialUa) }
    val focusRequester = remember { FocusRequester() }

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
                Text(
                    text = "User-Agent 设置",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "输入 c 自动填充 Chrome UA，输入 o 自动填充 OkHttp UA",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                UaTextField(
                    value = ua,
                    onValueChange = { newValue ->
                        ua = autoCompleteUa(ua, newValue)
                    },
                    placeholder = "输入 User-Agent",
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 快捷按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UaChip(
                        text = "Chrome",
                        onClick = { ua = Util.CHROME },
                        modifier = Modifier.weight(1f)
                    )
                    UaChip(
                        text = "OkHttp",
                        onClick = { ua = Util.OKHTTP },
                        modifier = Modifier.weight(1f)
                    )
                    UaChip(
                        text = "清空",
                        onClick = { ua = "" },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    UaButton(text = "取消", onClick = onDismiss)
                    Spacer(modifier = Modifier.width(12.dp))
                    UaButton(
                        text = "确定",
                        isPrimary = true,
                        onClick = { onConfirm(ua.trim()) }
                    )
                }
            }
        }
    }
}

private fun autoCompleteUa(oldValue: String, newValue: String): String {
    if (oldValue.isEmpty() && newValue.length == 1) {
        return when (newValue.lowercase()) {
            "c" -> Util.CHROME
            "o" -> Util.OKHTTP
            else -> newValue
        }
    }
    return newValue
}

@Composable
private fun UaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = MaterialTheme.typography.bodyMedium.fontSize
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
            .height(80.dp),
        decorationBox = { innerTextField ->
            Box {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun UaChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FocusableItem(onClick = onClick, modifier = modifier) { isFocused ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = if (isFocused) MaterialTheme.colorScheme.primaryContainer
                           else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = if (isFocused) MaterialTheme.colorScheme.onPrimaryContainer
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun UaButton(
    text: String,
    onClick: () -> Unit,
    isPrimary: Boolean = false
) {
    FocusableItem(onClick = onClick) { isFocused ->
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
