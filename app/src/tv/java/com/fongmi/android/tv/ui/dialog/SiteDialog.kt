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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.fongmi.android.tv.api.config.VodConfig
import com.fongmi.android.tv.bean.Site
import com.fongmi.android.tv.ui.components.FocusableItem

/**
 * 站点模式
 */
enum class SiteMode {
    NORMAL,   // 普通模式 - 选择首页站点
    SEARCH,   // 搜索模式 - 选择可搜索站点
    CHANGE    // 换源模式 - 选择可换源站点
}

/**
 * 站点选择对话框
 */
@Composable
fun SiteDialog(
    mode: SiteMode = SiteMode.NORMAL,
    onDismiss: () -> Unit,
    onSiteSelected: (Site) -> Unit
) {
    val sites = remember {
        try {
            VodConfig.get()?.getSites() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    val homeIndex = remember {
        try {
            VodConfig.getHomeIndex()
        } catch (e: Exception) {
            0
        }
    }
    val listState = rememberLazyListState()

    var currentMode by remember { mutableStateOf(mode) }

    // 滚动到当前选中的站点
    LaunchedEffect(Unit) {
        if (homeIndex >= 0 && homeIndex < sites.size) {
            listState.scrollToItem(homeIndex)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.7f)
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
                    text = "选择站点",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 模式切换
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ModeChip(
                        text = "首页",
                        isSelected = currentMode == SiteMode.NORMAL,
                        onClick = { currentMode = SiteMode.NORMAL }
                    )
                    ModeChip(
                        text = "搜索",
                        isSelected = currentMode == SiteMode.SEARCH,
                        onClick = { currentMode = SiteMode.SEARCH }
                    )
                    ModeChip(
                        text = "换源",
                        isSelected = currentMode == SiteMode.CHANGE,
                        onClick = { currentMode = SiteMode.CHANGE }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 站点列表
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                ) {
                    itemsIndexed(sites) { index, site ->
                        val isEnabled = when (currentMode) {
                            SiteMode.NORMAL -> true
                            SiteMode.SEARCH -> site.isSearchable
                            SiteMode.CHANGE -> site.isChangeable
                        }

                        SiteItem(
                            site = site,
                            isHome = index == homeIndex,
                            isEnabled = isEnabled,
                            currentMode = currentMode,
                            onClick = {
                                if (isEnabled || currentMode == SiteMode.NORMAL) {
                                    onSiteSelected(site)
                                    onDismiss()
                                }
                            },
                            onToggle = {
                                // 切换搜索/换源状态
                                when (currentMode) {
                                    SiteMode.SEARCH -> site.setSearchable(!site.isSearchable)
                                    SiteMode.CHANGE -> site.setChangeable(!site.isChangeable)
                                    else -> {}
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 底部按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (currentMode != SiteMode.NORMAL) {
                        SiteActionButton(
                            text = "全选",
                            onClick = {
                                sites.forEach { site ->
                                    when (currentMode) {
                                        SiteMode.SEARCH -> site.setSearchable(true)
                                        SiteMode.CHANGE -> site.setChangeable(true)
                                        else -> {}
                                    }
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        SiteActionButton(
                            text = "取消全选",
                            onClick = {
                                sites.forEach { site ->
                                    when (currentMode) {
                                        SiteMode.SEARCH -> site.setSearchable(false)
                                        SiteMode.CHANGE -> site.setChangeable(false)
                                        else -> {}
                                    }
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    SiteActionButton(
                        text = "关闭",
                        isPrimary = true,
                        onClick = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeChip(
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
                        else -> MaterialTheme.colorScheme.surfaceVariant
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
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
private fun SiteItem(
    site: Site,
    isHome: Boolean,
    isEnabled: Boolean,
    currentMode: SiteMode,
    onClick: () -> Unit,
    onToggle: () -> Unit
) {
    FocusableItem(
        onClick = if (currentMode == SiteMode.NORMAL) onClick else onToggle
    ) { isFocused ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = when {
                        isFocused -> MaterialTheme.colorScheme.primaryContainer
                        isHome -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = site.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isEnabled || currentMode == SiteMode.NORMAL)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                if (isHome) {
                    Text(
                        text = "当前首页",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 显示状态标记
            if (currentMode != SiteMode.NORMAL) {
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isEnabled) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isEnabled) "✓" else "✗",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isEnabled) MaterialTheme.colorScheme.onPrimary
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SiteActionButton(
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = when {
                    isFocused -> MaterialTheme.colorScheme.onPrimary
                    isPrimary -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}
