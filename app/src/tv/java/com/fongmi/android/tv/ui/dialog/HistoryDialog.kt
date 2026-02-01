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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.fongmi.android.tv.bean.Config
import com.fongmi.android.tv.ui.components.DialogButton
import com.fongmi.android.tv.ui.components.FocusableItem

/**
 * 配置历史对话框
 */
@Composable
fun HistoryDialog(
    configs: List<Config>,
    onDismiss: () -> Unit,
    onSelect: (Config) -> Unit,
    onDelete: (Config) -> Unit
) {
    val configList = remember(configs) {
        mutableStateListOf<Config>().apply {
            addAll(configs)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.5f)
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
                    text = "配置历史",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (configList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无历史记录",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        items(configList.toList(), key = { it.url ?: it.hashCode() }) { config ->
                            HistoryItem(
                                config = config,
                                onSelect = {
                                    onSelect(config)
                                    onDismiss()
                                },
                                onDelete = {
                                    onDelete(config)
                                    configList.remove(config)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    DialogButton(text = "关闭", onClick = onDismiss)
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(
    config: Config,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FocusableItem(
            onClick = onSelect,
            modifier = Modifier.weight(1f)
        ) { isFocused ->
            Column(
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
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = config.name.ifEmpty { config.url ?: "" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                if (config.name.isNotEmpty() && !config.url.isNullOrEmpty()) {
                    Text(
                        text = config.url,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }

        FocusableItem(onClick = onDelete) { isFocused ->
            Box(
                modifier = Modifier
                    .background(
                        color = if (isFocused) MaterialTheme.colorScheme.error
                               else MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = if (isFocused) MaterialTheme.colorScheme.onError
                          else MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}
