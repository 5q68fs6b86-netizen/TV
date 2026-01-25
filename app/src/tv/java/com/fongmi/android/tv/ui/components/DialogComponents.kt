package com.fongmi.android.tv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 统一的对话框按钮样式
 */
@Composable
fun DialogButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false
) {
    FocusableItem(
        onClick = onClick,
        modifier = modifier
    ) { isFocused ->
        Box(
            modifier = Modifier
                .background(
                    color = when {
                        isFocused -> MaterialTheme.colorScheme.primary
                        isPrimary -> MaterialTheme.colorScheme.primaryContainer
                        else -> Color.Transparent
                    },
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = if (!isFocused && !isPrimary) 1.dp else 0.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 20.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = when {
                    isFocused -> MaterialTheme.colorScheme.onPrimary
                    isPrimary -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

/**
 * 选项卡切换按钮
 */
@Composable
fun TabChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FocusableItem(
        onClick = onClick,
        modifier = modifier
    ) { isFocused ->
        Box(
            modifier = Modifier
                .background(
                    color = when {
                        isFocused -> MaterialTheme.colorScheme.primary
                        isSelected -> MaterialTheme.colorScheme.primaryContainer
                        else -> Color.Transparent
                    },
                    shape = RoundedCornerShape(20.dp)
                )
                .border(
                    width = if (!isFocused && !isSelected) 1.dp else 0.dp,
                    color = MaterialTheme.colorScheme.outline,
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
