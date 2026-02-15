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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.fongmi.android.tv.data.repository.DanmuSearchAnime
import com.fongmi.android.tv.data.repository.DanmuSearchEpisode
import com.fongmi.android.tv.ui.components.FocusableItem

/**
 * Danmu search dialog - search anime and select episode for danmu
 */
@Composable
fun DanmuSearchDialog(
    initialKeyword: String = "",
    searchResults: List<DanmuSearchAnime>,
    episodeResults: List<DanmuSearchEpisode>,
    isSearching: Boolean,
    isLoadingEpisodes: Boolean,
    selectedAnime: DanmuSearchAnime?,
    onSearch: (String) -> Unit,
    onSelectAnime: (DanmuSearchAnime) -> Unit,
    onSelectEpisode: (DanmuSearchEpisode) -> Unit,
    onBack: () -> Unit,
    onDismiss: () -> Unit
) {
    var searchText by remember { mutableStateOf(initialKeyword) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        if (initialKeyword.isNotEmpty()) {
            onSearch(initialKeyword)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .onKeyEvent { event ->
                    if (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_BACK) {
                        if (selectedAnime != null) {
                            onBack()
                            true
                        } else {
                            onDismiss()
                            true
                        }
                    } else false
                },
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (selectedAnime != null) {
                        FocusableItem(
                            onClick = { onBack() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedAnime.animeTitle,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Text(
                            text = "搜索弹幕",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search input (only show in anime list mode)
                if (selectedAnime == null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        BasicTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            textStyle = TextStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = { onSearch(searchText) }
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (searchText.isEmpty()) {
                                        Text(
                                            text = "输入影片名称",
                                            style = TextStyle(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 14.sp
                                            )
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FocusableItem(
                            onClick = { onSearch(searchText) }
                        ) {
                            Text(
                                text = "搜索",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Content area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    when {
                        isSearching || isLoadingEpisodes -> {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(32.dp)
                                    .align(Alignment.Center)
                            )
                        }
                        selectedAnime != null -> {
                            // Episode list
                            if (episodeResults.isEmpty()) {
                                Text(
                                    text = "暂无剧集信息",
                                    modifier = Modifier.align(Alignment.Center),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items(episodeResults) { episode ->
                                        FocusableItem(
                                            onClick = { onSelectEpisode(episode) },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = episode.episodeTitle,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(
                                                        horizontal = 12.dp,
                                                        vertical = 8.dp
                                                    ),
                                                fontSize = 14.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        searchResults.isEmpty() -> {
                            Text(
                                text = "输入关键词搜索弹幕",
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        else -> {
                            // Anime list
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(searchResults) { anime ->
                                    FocusableItem(
                                        onClick = { onSelectAnime(anime) },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(
                                                    horizontal = 12.dp,
                                                    vertical = 8.dp
                                                )
                                        ) {
                                            Text(
                                                text = anime.animeTitle,
                                                fontSize = 14.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (anime.type.isNotEmpty()) {
                                                Text(
                                                    text = anime.type,
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
