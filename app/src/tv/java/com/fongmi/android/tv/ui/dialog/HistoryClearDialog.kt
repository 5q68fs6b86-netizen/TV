package com.fongmi.android.tv.ui.dialog

import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.fongmi.android.tv.ui.components.FocusableItem

/**
 * 历史记录清理选项
 */
enum class HistoryClearOption {
    TODAY,      // 今天
    WEEK,       // 一周内
    MONTH,      // 一个月内
    ALL         // 全部
}

/**
 * 历史记录清理对话框
 */
@Composable
fun HistoryClearDialog(
    totalCount: Int,
    onDismiss: () -> Unit,
    onClear: (HistoryClearOption) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .onKeyEvent { event ->
                    if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN &&
                        event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_BACK) {
                        onDismiss()
                        true
                    } else false
                }
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = "清理历史记录",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "共 $totalCount 条记录",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                ClearOptionItem(
                    title = "清理今天的记录",
                    onClick = { onClear(HistoryClearOption.TODAY); onDismiss() }
                )
                Spacer(modifier = Modifier.height(8.dp))
                ClearOptionItem(
                    title = "清理一周内的记录",
                    onClick = { onClear(HistoryClearOption.WEEK); onDismiss() }
                )
                Spacer(modifier = Modifier.height(8.dp))
                ClearOptionItem(
                    title = "清理一个月内的记录",
                    onClick = { onClear(HistoryClearOption.MONTH); onDismiss() }
                )
                Spacer(modifier = Modifier.height(8.dp))
                ClearOptionItem(
                    title = "清理全部记录",
                    isDestructive = true,
                    onClick = { onClear(HistoryClearOption.ALL); onDismiss() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    FocusableItem(onClick = onDismiss) { isFocused ->
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (isFocused) MaterialTheme.colorScheme.primary
                                           else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "取消",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isFocused) MaterialTheme.colorScheme.onPrimary
                                       else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClearOptionItem(
    title: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    FocusableItem(onClick = onClick) { isFocused ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = when {
                        isFocused && isDestructive -> MaterialTheme.colorScheme.error
                        isFocused -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isDestructive) Icons.Default.Delete else Icons.Default.History,
                contentDescription = null,
                tint = when {
                    isFocused && isDestructive -> MaterialTheme.colorScheme.onError
                    isFocused -> MaterialTheme.colorScheme.onPrimaryContainer
                    isDestructive -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    isFocused && isDestructive -> MaterialTheme.colorScheme.onError
                    isFocused -> MaterialTheme.colorScheme.onPrimaryContainer
                    isDestructive -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}
